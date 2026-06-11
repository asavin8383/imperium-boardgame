"""
FastAPI application — Imperium REST API
"""
from fastapi import FastAPI, HTTPException, Depends, Request
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
from contextlib import contextmanager
import sys
import os
import uuid
import time
from datetime import datetime, timezone

sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))

from game import session as game_session
from game.enums import Nation, Difficulty
from game.cards import get_all_available_nations
from game.database import engine, get_db, Base
from game.db_models import GameSave, CURRENT_FORMAT_VERSION
from game.serialization import serialize_state, deserialize_state
from game.logger import log
from sqlalchemy.orm import Session

# Создаём таблицы, если ещё не созданы (Alembic управляет схемой в проде)
Base.metadata.create_all(bind=engine)

app = FastAPI(title="Imperium: Classics API", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://localhost:5173"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ── LOGGING HELPERS ────────────────────────────────────────────────────────────

@app.middleware("http")
async def _log_requests(request: Request, call_next):
    """Логирует все входящие запросы и ответы (кроме GET)."""
    start = time.monotonic()
    response = await call_next(request)
    ms = int((time.monotonic() - start) * 1000)

    method = request.method
    path = request.url.path
    status = response.status_code

    # Извлекаем game_id/save_id из пути (/api/games/{id}/... или /api/saves/{id}/...)
    parts = path.strip("/").split("/")
    prefix = ""
    if len(parts) >= 3 and parts[1] in ("games", "saves") and parts[2]:
        prefix = f"[{parts[2]}] "

    msg = f"{prefix}{method} {path} → {status} ({ms}ms)"
    if status >= 500:
        log.error(msg)
    elif status >= 400:
        log.warning(msg)
    elif method != "GET":
        log.info(msg)

    return response


@contextmanager
def _game_action(game_id: str, action: str):
    """Контекстный менеджер: перехватывает ValueError, логирует ошибку и возвращает HTTP 400."""
    try:
        yield
    except ValueError as e:
        log.error("[%s] %s: %s", game_id, action, e)
        raise HTTPException(status_code=400, detail=str(e))


def _respond(state, db: Session) -> dict:
    """Возвращает state dict; если наступил ход игрока — пишет автосейв."""
    from game.enums import GamePhase
    if state.phase == GamePhase.PLAYER_TURN:
        try:
            _do_autosave(state, db)
        except Exception as e:
            log.error("Autosave failed: %s", e)
    return {"state": state.to_dict()}


# ── REQUEST MODELS ─────────────────────────────────────────────────────────────

class CreateGameRequest(BaseModel):
    player_nation: str
    bot_nation: str
    difficulty: str = "emperor"
    ability_side: str = "B"


class PlayCardRequest(BaseModel):
    card_id: str


class ExploitCardRequest(BaseModel):
    card_id: str


class InnovationRequest(BaseModel):
    pass  # no body needed


class RevolutionRequest(BaseModel):
    card_ids: List[str]


class EndTurnRequest(BaseModel):
    discard_ids: Optional[List[str]] = None


class AcquireCardRequest(BaseModel):
    slot_index: int


class AccelerateProgressRequest(BaseModel):
    progress_card_id: str


class ChooseOptionRequest(BaseModel):
    option_index: int


class AppropriateFromDeckRequest(BaseModel):
    deck_name: str


class AcquireFromExileRequest(BaseModel):
    card_id: str


class SelectAppropriateCategoryRequest(BaseModel):
    category: str


class ChronicleChoiceRequest(BaseModel):
    send_to_chronicle: bool


class ReturnExploitTokenRequest(BaseModel):
    card_id: Optional[str] = None


class DrawFromDeckOptionalRequest(BaseModel):
    draw: bool


class AppropriateOptionalRequest(BaseModel):
    proceed: bool


class RecallFromChronicleRequest(BaseModel):
    card_id: Optional[str] = None  # None — пропустить


class ReinforceChoiceRequest(BaseModel):
    reinforce: bool


class ReinforceWithCardRequest(BaseModel):
    hand_card_id: str


class PlayFromDiscardRequest(BaseModel):
    card_id: str


class PlaceUpgradeTokenRequest(BaseModel):
    slot_index: int


class RecallToAvoidAttackRequest(BaseModel):
    recall: bool


class ChronicleFromDiscardRequest(BaseModel):
    card_id: Optional[str] = None  # None = пропустить (только если optional)


class ChronicleFromHandRequest(BaseModel):
    card_id: Optional[str] = None  # None = пропустить (только если optional)


class ExileFromMarketRequest(BaseModel):
    slot_index: int


class DestroyCardsRequest(BaseModel):
    card_ids: List[str]


class GloryDeckTakeRequest(BaseModel):
    card_id: str


class MoveDiscardToDeckRequest(BaseModel):
    card_id: Optional[str] = None  # None = пропустить (только если optional)


class SacredPathExploitRequest(BaseModel):
    destroy: bool


class ReturnCardToDeckTopRequest(BaseModel):
    card_id: str


class SacredPathExchangeRequest(BaseModel):
    hand_card_id: str


class SolsticeGainProgressRequest(BaseModel):
    take: bool


class SolsticeFateRequest(BaseModel):
    choice: str  # "destroy" or "chronicle"


class SolsticeSelectCardRequest(BaseModel):
    card_id: str


class SolsticeDiscardHandReturnDisorderRequest(BaseModel):
    hand_card_id: Optional[str] = None   # None = skip
    disorder_card_id: Optional[str] = None


class SolsticeChoiceRequest(BaseModel):
    option_index: int
    card_ids: Optional[List[str]] = None  # ID карт для сброса (если требуется опцией)


class SolsticeDiscardForRewardRequest(BaseModel):
    hand_card_id: Optional[str] = None  # None — пропустить эффект


class SolsticeDiscardRewardChoiceRequest(BaseModel):
    option_index: int


class DrawDiscardChoiceRequest(BaseModel):
    card_id: str  # ID карты из взятых для сброса


class ExploitRecallChoiceRequest(BaseModel):
    option_index: int
    card_id: str  # ID карты в игровой области для отзыва


class GuessDeckCategoryRequest(BaseModel):
    category: str  # "region" | "origins" | "civilization" | "raid"


class GiveCardToBotRequest(BaseModel):
    card_id: str


class ReinforceRegionOptionalRequest(BaseModel):
    region_card_id: Optional[str] = None  # None = пропустить


class ReturnDisordersRequest(BaseModel):
    card_ids: List[str]  # пустой список = пропустить


class SelfDispositionRequest(BaseModel):
    choice: str  # "chronicle" | "reinforce_region" | "skip"
    region_card_id: Optional[str] = None  # нужен при choice="reinforce_region"


class LookDeckTopRequest(BaseModel):
    choice: str  # "discard" | "return" | "chronicle"


class TakeFromDiscardRequest(BaseModel):
    card_id: str


class SaveGameRequest(BaseModel):
    name: str


# ── ENDPOINTS ──────────────────────────────────────────────────────────────────

@app.get("/api/nations")
def list_nations():
    """Get all available nations with descriptions"""
    nation_info = {
        "vikings": {
            "name": "Викинги",
            "difficulty": 2,
            "description": "Не могут вступить в период цивилизации и не заносят карты в летопись. "
                           "Быстрое прокручивание колоды усиления.",
            "complexity": "★★☆☆☆"
        },
        "greeks": {
            "name": "Греки",
            "difficulty": 4,
            "description": "Развитые города и передовые технологии.",
            "complexity": "★★★★☆"
        },
        "carthaginians": {
            "name": "Карфагеняне",
            "difficulty": 1,
            "description": "Цель — торговать и накопить как можно больше ресурсов.",
            "complexity": "★☆☆☆☆"
        },
        "celts": {
            "name": "Кельты",
            "difficulty": 2,
            "description": "Агрессивная игра: распространять карты беспорядков.",
            "complexity": "★★☆☆☆"
        },
        "macedonians": {
            "name": "Македоняне",
            "difficulty": 1,
            "description": "Захват новых территорий — главная цель.",
            "complexity": "★☆☆☆☆"
        },
        "persians": {
            "name": "Персы",
            "difficulty": 1,
            "description": "Покорение народов и использование набегов.",
            "complexity": "★☆☆☆☆"
        },
        "romans": {
            "name": "Римляне",
            "difficulty": 1,
            "description": "Быстрая и агрессивная экспансия.",
            "complexity": "★☆☆☆☆"
        },
        "scythians": {
            "name": "Скифы",
            "difficulty": 2,
            "description": "Распространение влияния на многие регионы.",
            "complexity": "★★☆☆☆"
        },
    }
    return {"nations": nation_info}


@app.get("/api/difficulties")
def list_difficulties():
    return {
        "difficulties": [
            {"id": "chieftain", "name": "Вождь", "description": "Бот играет 3-4 карты за ход"},
            {"id": "commander", "name": "Полководец", "description": "Бот играет 3-4 карты, +сброс"},
            {"id": "emperor", "name": "Император", "description": "Стандарт (4-5 карт)"},
            {"id": "overlord", "name": "Повелитель", "description": "Бот получает стартовые ресурсы"},
            {"id": "sovereign", "name": "Властелин", "description": "Бот играет 5 карт за ход"},
        ]
    }


@app.post("/api/games")
def create_game(req: CreateGameRequest, db: Session = Depends(get_db)):
    try:
        state = game_session.create_game(
            req.player_nation, req.bot_nation,
            req.difficulty, req.ability_side
        )
        log.info(
            "[%s] create_game: player=%s bot=%s difficulty=%s",
            state.game_id, req.player_nation, req.bot_nation, req.difficulty,
        )
        _do_autosave(state, db)
        return {"game_id": state.game_id, "state": state.to_dict()}
    except ValueError as e:
        log.error("create_game error: %s", e)
        raise HTTPException(status_code=400, detail=str(e))


@app.get("/api/games/{game_id}")
def get_game(game_id: str):
    state = game_session.get_game(game_id)
    if state is None:
        raise HTTPException(status_code=404, detail="Game not found")
    return {"state": state.to_dict()}


@app.post("/api/games/{game_id}/play-card")
def play_card(game_id: str, req: PlayCardRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"play_card({req.card_id})"):
        state = game_session.play_card(game_id, req.card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/exploit-card")
def exploit_card(game_id: str, req: ExploitCardRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"exploit_card({req.card_id})"):
        state = game_session.exploit_card(game_id, req.card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/innovation")
def do_innovation(game_id: str, db: Session = Depends(get_db)):
    with _game_action(game_id, "innovation"):
        state = game_session.do_innovation(game_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/revolution")
def do_revolution(game_id: str, req: RevolutionRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"revolution(cards={req.card_ids})"):
        state = game_session.do_revolution(game_id, req.card_ids)
        return _respond(state, db)


@app.post("/api/games/{game_id}/end-turn")
def end_turn(game_id: str, req: EndTurnRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"end_turn(discard={req.discard_ids})"):
        state = game_session.end_player_turn(game_id, req.discard_ids or [])
        return _respond(state, db)


@app.post("/api/games/{game_id}/acquire-card")
def acquire_card(game_id: str, req: AcquireCardRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"acquire_card(slot={req.slot_index})"):
        state = game_session.acquire_card(game_id, req.slot_index)
        return _respond(state, db)


@app.post("/api/games/{game_id}/accelerate-progress")
def accelerate_progress(game_id: str, req: AccelerateProgressRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"accelerate_progress({req.progress_card_id})"):
        state = game_session.accelerate_progress(game_id, req.progress_card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/choose-option")
def choose_option(game_id: str, req: ChooseOptionRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"choose_option(index={req.option_index})"):
        state = game_session.choose_option(game_id, req.option_index)
        return _respond(state, db)


@app.post("/api/games/{game_id}/select-appropriate-category")
def select_appropriate_category(game_id: str, req: SelectAppropriateCategoryRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"select_appropriate_category({req.category})"):
        state = game_session.select_appropriate_category(game_id, req.category)
        return _respond(state, db)


@app.post("/api/games/{game_id}/appropriate-from-deck")
def appropriate_from_deck(game_id: str, req: AppropriateFromDeckRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"appropriate_from_deck({req.deck_name})"):
        state = game_session.appropriate_from_deck(game_id, req.deck_name)
        return _respond(state, db)


@app.post("/api/games/{game_id}/return-exploit-token")
def return_exploit_token(game_id: str, req: ReturnExploitTokenRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"return_exploit_token({req.card_id})"):
        state = game_session.return_exploit_token(game_id, req.card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/draw-from-deck-optional")
def draw_from_deck_optional(game_id: str, req: DrawFromDeckOptionalRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"draw_from_deck_optional(draw={req.draw})"):
        state = game_session.resolve_draw_from_deck_optional(game_id, req.draw)
        return _respond(state, db)


@app.post("/api/games/{game_id}/acquire-from-exile")
def acquire_from_exile(game_id: str, req: AcquireFromExileRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"acquire_from_exile({req.card_id})"):
        state = game_session.acquire_from_exile(game_id, req.card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/accelerate-progress-from-card")
def accelerate_progress_from_card(game_id: str, req: AccelerateProgressRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"accelerate_progress_from_card({req.progress_card_id})"):
        state = game_session.accelerate_progress_from_card(game_id, req.progress_card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/take-from-discard")
def take_from_discard(game_id: str, req: TakeFromDiscardRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"take_from_discard({req.card_id})"):
        state = game_session.take_from_discard(game_id, req.card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/appropriate-optional")
def appropriate_optional(game_id: str, req: AppropriateOptionalRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"appropriate_optional(proceed={req.proceed})"):
        state = game_session.resolve_appropriate_optional(game_id, req.proceed)
        return _respond(state, db)


@app.post("/api/games/{game_id}/reinforce-choice")
def reinforce_choice(game_id: str, req: ReinforceChoiceRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"reinforce_choice(reinforce={req.reinforce})"):
        state = game_session.resolve_reinforce_choice(game_id, req.reinforce)
        return _respond(state, db)


@app.post("/api/games/{game_id}/reinforce-with-card")
def reinforce_with_card(game_id: str, req: ReinforceWithCardRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"reinforce_with_card({req.hand_card_id})"):
        state = game_session.reinforce_with_card(game_id, req.hand_card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/play-from-discard")
def play_from_discard(game_id: str, req: PlayFromDiscardRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"play_from_discard({req.card_id})"):
        state = game_session.play_from_discard(game_id, req.card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/place-upgrade-token")
def place_upgrade_token(game_id: str, req: PlaceUpgradeTokenRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"place_upgrade_token(slot={req.slot_index})"):
        state = game_session.place_upgrade_token(game_id, req.slot_index)
        return _respond(state, db)


@app.post("/api/games/{game_id}/recall-to-avoid-attack")
def recall_to_avoid_attack(game_id: str, req: RecallToAvoidAttackRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"recall_to_avoid_attack(recall={req.recall})"):
        state = game_session.resolve_recall_to_avoid_attack(game_id, req.recall)
        return _respond(state, db)


@app.post("/api/games/{game_id}/chronicle-from-hand")
def chronicle_from_hand(game_id: str, req: ChronicleFromHandRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"chronicle_from_hand({req.card_id})"):
        state = game_session.chronicle_card_from_hand(game_id, req.card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/chronicle-from-discard")
def chronicle_from_discard(game_id: str, req: ChronicleFromDiscardRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"chronicle_from_discard({req.card_id})"):
        state = game_session.chronicle_card_from_discard(game_id, req.card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/recall-from-chronicle")
def recall_from_chronicle(game_id: str, req: RecallFromChronicleRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"recall_from_chronicle({req.card_id})"):
        state = game_session.resolve_recall_from_chronicle(game_id, req.card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/exile-from-market")
def exile_from_market(game_id: str, req: ExileFromMarketRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"exile_from_market(slot={req.slot_index})"):
        state = game_session.exile_card_from_market(game_id, req.slot_index)
        return _respond(state, db)


@app.post("/api/games/{game_id}/destroy-cards")
def destroy_cards(game_id: str, req: DestroyCardsRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"destroy_cards({req.card_ids})"):
        state = game_session.select_destroy_cards(game_id, req.card_ids)
        return _respond(state, db)


@app.post("/api/games/{game_id}/glory-deck-take")
def glory_deck_take(game_id: str, req: GloryDeckTakeRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"glory_deck_take({req.card_id})"):
        state = game_session.select_glory_deck_card(game_id, req.card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/sacred-path-exploit")
def sacred_path_exploit(game_id: str, req: SacredPathExploitRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"sacred_path_exploit(destroy={req.destroy})"):
        state = game_session.resolve_sacred_path_exploit(game_id, req.destroy)
        return _respond(state, db)


@app.post("/api/games/{game_id}/sacred-path-exchange")
def sacred_path_exchange(game_id: str, req: SacredPathExchangeRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"sacred_path_exchange({req.hand_card_id})"):
        state = game_session.resolve_sacred_path_exchange(game_id, req.hand_card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/move-discard-to-deck")
def move_discard_to_deck(game_id: str, req: MoveDiscardToDeckRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"move_discard_to_deck({req.card_id})"):
        state = game_session.move_discard_to_deck_card(game_id, req.card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/return-card-to-deck-top")
def return_card_to_deck_top(game_id: str, req: ReturnCardToDeckTopRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"return_card_to_deck_top({req.card_id})"):
        state = game_session.resolve_return_card_to_deck_top(game_id, req.card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/chronicle-choice")
def chronicle_choice(game_id: str, req: ChronicleChoiceRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"chronicle_choice(send={req.send_to_chronicle})"):
        state = game_session.resolve_chronicle_choice(game_id, req.send_to_chronicle)
        return _respond(state, db)


@app.post("/api/games/{game_id}/solstice-skip")
def solstice_skip(game_id: str, db: Session = Depends(get_db)):
    with _game_action(game_id, "solstice_skip"):
        state = game_session.skip_solstice(game_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/solstice-gain-progress")
def solstice_gain_progress(game_id: str, req: SolsticeGainProgressRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"solstice_gain_progress(take={req.take})"):
        state = game_session.resolve_solstice_gain_progress(game_id, req.take)
        return _respond(state, db)


@app.post("/api/games/{game_id}/solstice-fate")
def solstice_fate(game_id: str, req: SolsticeFateRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"solstice_fate({req.choice})"):
        state = game_session.resolve_solstice_fate(game_id, req.choice)
        return _respond(state, db)


@app.post("/api/games/{game_id}/solstice-select-card")
def solstice_select_card(game_id: str, req: SolsticeSelectCardRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"solstice_select_card({req.card_id})"):
        state = game_session.resolve_solstice_select_card(game_id, req.card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/solstice-discard-hand-return-disorder")
def solstice_discard_hand_return_disorder(game_id: str, req: SolsticeDiscardHandReturnDisorderRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"solstice_discard_hand_return_disorder(hand={req.hand_card_id} disorder={req.disorder_card_id})"):
        state = game_session.resolve_solstice_discard_hand_return_disorder(game_id, req.hand_card_id, req.disorder_card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/solstice-choice")
def solstice_choice(game_id: str, req: SolsticeChoiceRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"solstice_choice(index={req.option_index})"):
        state = game_session.resolve_solstice_choice(game_id, req.option_index, req.card_ids)
        return _respond(state, db)


@app.post("/api/games/{game_id}/solstice-discard-for-reward")
def solstice_discard_for_reward(game_id: str, req: SolsticeDiscardForRewardRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"solstice_discard_for_reward({req.hand_card_id})"):
        state = game_session.resolve_solstice_discard_for_reward(game_id, req.hand_card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/solstice-discard-reward-choice")
def solstice_discard_reward_choice(game_id: str, req: SolsticeDiscardRewardChoiceRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"solstice_discard_reward_choice(index={req.option_index})"):
        state = game_session.resolve_solstice_discard_reward_choice(game_id, req.option_index)
        return _respond(state, db)


@app.post("/api/games/{game_id}/draw-discard-choice")
def draw_discard_choice(game_id: str, req: DrawDiscardChoiceRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"draw_discard_choice({req.card_id})"):
        state = game_session.resolve_draw_discard_choice(game_id, req.card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/exploit-recall-choice")
def exploit_recall_choice(game_id: str, req: ExploitRecallChoiceRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"exploit_recall_choice(index={req.option_index} card={req.card_id})"):
        state = game_session.resolve_exploit_recall_choice(game_id, req.option_index, req.card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/exploit-recall-label-for-resource-token-card")
def exploit_recall_label_for_resource_token_card(game_id: str, req: PlayFromDiscardRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"exploit_recall_label_for_resource_token_card({req.card_id})"):
        state = game_session.resolve_exploit_recall_label_for_resource_token_card(game_id, req.card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/exploit-discard-for-resource-token-card")
def exploit_discard_for_resource_token_card(game_id: str, req: PlayFromDiscardRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"exploit_discard_for_resource_token_card({req.card_id})"):
        state = game_session.resolve_exploit_discard_for_resource_token_card(game_id, req.card_id)
        return _respond(state, db)


class AcquireAndPlayRegionRequest(BaseModel):
    slot_index: Optional[int] = None  # None = пропустить


@app.post("/api/games/{game_id}/acquire-and-play-region")
def acquire_and_play_region(game_id: str, req: AcquireAndPlayRegionRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"acquire_and_play_region({req.slot_index})"):
        state = game_session.resolve_acquire_and_play_region(game_id, req.slot_index)
        return _respond(state, db)


class PlaceResourceOnMarketRequest(BaseModel):
    slot_index: int


@app.post("/api/games/{game_id}/place-resource-on-market")
def place_resource_on_market(game_id: str, req: PlaceResourceOnMarketRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"place_resource_on_market({req.slot_index})"):
        state = game_session.resolve_place_resource_on_market(game_id, req.slot_index)
        return _respond(state, db)


class ChronicleFromHandOrDiscardRequest(BaseModel):
    card_id: Optional[str] = None  # None = пропустить


@app.post("/api/games/{game_id}/chronicle-from-hand-or-discard")
def chronicle_from_hand_or_discard(game_id: str, req: ChronicleFromHandOrDiscardRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"chronicle_from_hand_or_discard({req.card_id})"):
        state = game_session.resolve_chronicle_from_hand_or_discard(game_id, req.card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/bot-turn")
def bot_turn(game_id: str, db: Session = Depends(get_db)):
    with _game_action(game_id, "bot_turn"):
        state = game_session.run_bot_turn(game_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/bot-play-card")
def bot_play_card(game_id: str, db: Session = Depends(get_db)):
    with _game_action(game_id, "bot_play_card"):
        state = game_session.play_bot_card_step(game_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/guess-deck-category")
def guess_deck_category(game_id: str, req: GuessDeckCategoryRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"guess_deck_category({req.category})"):
        state = game_session.resolve_guess_deck_category(game_id, req.category)
        return _respond(state, db)


@app.post("/api/games/{game_id}/exploit-discard-hand")
def exploit_discard_hand(game_id: str, req: PlayFromDiscardRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"exploit_discard_hand({req.card_id})"):
        state = game_session.resolve_exploit_discard_hand(game_id, req.card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/solstice-return-disorder")
def solstice_return_disorder(game_id: str, req: PlayFromDiscardRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"solstice_return_disorder({req.card_id})"):
        state = game_session.resolve_solstice_return_disorder(game_id, req.card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/reinforce-region-optional")
def reinforce_region_optional(game_id: str, req: ReinforceRegionOptionalRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"reinforce_region_optional({req.region_card_id})"):
        state = game_session.resolve_reinforce_region_optional(game_id, req.region_card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/look-deck-top")
def look_deck_top(game_id: str, req: LookDeckTopRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"look_deck_top({req.choice})"):
        state = game_session.resolve_look_deck_top(game_id, req.choice)
        return _respond(state, db)


@app.post("/api/games/{game_id}/self-disposition")
def self_disposition(game_id: str, req: SelfDispositionRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"self_disposition({req.choice})"):
        state = game_session.resolve_self_disposition(game_id, req.choice, req.region_card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/pre-scoring-return-disorders")
def pre_scoring_return_disorders(game_id: str, req: ReturnDisordersRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"pre_scoring_return_disorders({req.card_ids})"):
        state = game_session.resolve_pre_scoring_return_disorders(game_id, req.card_ids)
        return _respond(state, db)


@app.post("/api/games/{game_id}/return-disorders")
def return_disorders(game_id: str, req: ReturnDisordersRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"return_disorders({req.card_ids})"):
        state = game_session.resolve_return_disorders(game_id, req.card_ids)
        return _respond(state, db)


@app.post("/api/games/{game_id}/give-card-to-bot")
def give_card_to_bot(game_id: str, req: GiveCardToBotRequest, db: Session = Depends(get_db)):
    with _game_action(game_id, f"give_card_to_bot({req.card_id})"):
        state = game_session.resolve_give_card_to_bot(game_id, req.card_id)
        return _respond(state, db)


@app.post("/api/games/{game_id}/undo")
def undo_action(game_id: str):
    with _game_action(game_id, "undo"):
        state = game_session.undo_last_action(game_id)
        return {"state": state.to_dict()}


@app.delete("/api/games/{game_id}")
def delete_game(game_id: str):
    log.info("[%s] delete_game", game_id)
    game_session.delete_game(game_id)
    return {"message": "Game deleted"}


@app.get("/api/health")
def health():
    return {"status": "ok", "version": "1.0.0"}


# ── AUTOSAVE ──────────────────────────────────────────────────────────────────

AUTOSAVE_NAME = "autosave"


def _do_autosave(state, db: Session) -> None:
    """Перезаписывает единственный автосейв (upsert по имени 'autosave')."""
    now = datetime.now(timezone.utc)
    data = serialize_state(state)
    existing = db.query(GameSave).filter(GameSave.name == AUTOSAVE_NAME).first()
    if existing:
        existing.player_nation = state.player.nation.value
        existing.bot_nation = state.bot.nation.value
        existing.difficulty = state.difficulty.value
        existing.game_phase = state.phase.value
        existing.round_number = state.round_number
        existing.player_period = state.player.period.value
        existing.updated_at = now
        existing.state_data = data
        existing.format_version = CURRENT_FORMAT_VERSION
    else:
        db.add(GameSave(
            id=str(uuid.uuid4()),
            name=AUTOSAVE_NAME,
            player_nation=state.player.nation.value,
            bot_nation=state.bot.nation.value,
            difficulty=state.difficulty.value,
            game_phase=state.phase.value,
            round_number=state.round_number,
            player_period=state.player.period.value,
            created_at=now,
            updated_at=now,
            state_data=data,
            format_version=CURRENT_FORMAT_VERSION,
        ))
    db.commit()
    log.info("[%s] autosave written (round=%s phase=%s)", state.game_id, state.round_number, state.phase.value)


# ── SAVE / LOAD ────────────────────────────────────────────────────────────────

@app.post("/api/games/{game_id}/save")
def save_game_to_db(game_id: str, req: SaveGameRequest, db: Session = Depends(get_db)):
    """Сохранить текущее состояние партии в базу данных."""
    state = game_session.get_game(game_id)
    if state is None:
        raise HTTPException(status_code=404, detail="Партия не найдена")
    if not req.name.strip():
        raise HTTPException(status_code=400, detail="Укажите название сохранения")

    now = datetime.now(timezone.utc)
    save = GameSave(
        id=str(uuid.uuid4()),
        name=req.name.strip(),
        player_nation=state.player.nation.value,
        bot_nation=state.bot.nation.value,
        difficulty=state.difficulty.value,
        game_phase=state.phase.value,
        round_number=state.round_number,
        player_period=state.player.period.value,
        created_at=now,
        updated_at=now,
        state_data=serialize_state(state),
        format_version=CURRENT_FORMAT_VERSION,
    )
    db.add(save)
    db.commit()
    db.refresh(save)
    log.info("[%s] manual save: name='%s' id=%s", game_id, req.name.strip(), save.id)
    return {"save_id": save.id, "save": save.to_dict()}


@app.get("/api/saves")
def list_saves(db: Session = Depends(get_db)):
    """Список всех сохранённых партий (без данных состояния)."""
    saves = db.query(GameSave).order_by(GameSave.updated_at.desc()).all()
    return {"saves": [s.to_dict() for s in saves]}


@app.post("/api/saves/{save_id}/load")
def load_save(save_id: str, db: Session = Depends(get_db)):
    """Загрузить сохранение в активную сессию и вернуть состояние."""
    save = db.query(GameSave).filter(GameSave.id == save_id).first()
    if save is None:
        raise HTTPException(status_code=404, detail="Сохранение не найдено")

    state = deserialize_state(save.state_data)
    game_session.save_game(state)
    log.info("[%s] load_save: name='%s' id=%s", state.game_id, save.name, save_id)
    if save.name != AUTOSAVE_NAME:
        _do_autosave(state, db)
    return {"game_id": state.game_id, "state": state.to_dict()}


@app.delete("/api/saves/{save_id}")
def delete_save(save_id: str, db: Session = Depends(get_db)):
    """Удалить сохранение из базы данных."""
    save = db.query(GameSave).filter(GameSave.id == save_id).first()
    if save is None:
        raise HTTPException(status_code=404, detail="Сохранение не найдено")
    log.info("delete_save: name='%s' id=%s", save.name, save_id)
    db.delete(save)
    db.commit()
    return {"message": "Удалено"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000, reload=True)
