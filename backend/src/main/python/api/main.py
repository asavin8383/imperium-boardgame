"""
FastAPI application — Imperium REST API
"""
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))

from game import session as game_session
from game.enums import Nation, Difficulty
from game.cards import get_all_available_nations

app = FastAPI(title="Imperium: Classics API", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://localhost:5173"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


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
    category: str  # "region" | "origins" | "civilization" | "raid"


class RevolutionRequest(BaseModel):
    card_ids: List[str]


class EndTurnRequest(BaseModel):
    discard_ids: Optional[List[str]] = None


class AcquireCardRequest(BaseModel):
    slot_index: int


class AccelerateProgressRequest(BaseModel):
    progress_card_id: str


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
def create_game(req: CreateGameRequest):
    try:
        state = game_session.create_game(
            req.player_nation, req.bot_nation,
            req.difficulty, req.ability_side
        )
        return {"game_id": state.game_id, "state": state.to_dict()}
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.get("/api/games/{game_id}")
def get_game(game_id: str):
    state = game_session.get_game(game_id)
    if state is None:
        raise HTTPException(status_code=404, detail="Game not found")
    return {"state": state.to_dict()}


@app.post("/api/games/{game_id}/play-card")
def play_card(game_id: str, req: PlayCardRequest):
    try:
        state = game_session.play_card(game_id, req.card_id)
        return {"state": state.to_dict()}
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.post("/api/games/{game_id}/exploit-card")
def exploit_card(game_id: str, req: ExploitCardRequest):
    try:
        state = game_session.exploit_card(game_id, req.card_id)
        return {"state": state.to_dict()}
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.post("/api/games/{game_id}/innovation")
def do_innovation(game_id: str, req: InnovationRequest):
    try:
        state = game_session.do_innovation(game_id, req.category)
        return {"state": state.to_dict()}
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.post("/api/games/{game_id}/revolution")
def do_revolution(game_id: str, req: RevolutionRequest):
    try:
        state = game_session.do_revolution(game_id, req.card_ids)
        return {"state": state.to_dict()}
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.post("/api/games/{game_id}/end-turn")
def end_turn(game_id: str, req: EndTurnRequest):
    try:
        state = game_session.end_player_turn(
            game_id, req.discard_ids or []
        )
        return {"state": state.to_dict()}
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.post("/api/games/{game_id}/acquire-card")
def acquire_card(game_id: str, req: AcquireCardRequest):
    try:
        state = game_session.acquire_card(game_id, req.slot_index)
        return {"state": state.to_dict()}
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.post("/api/games/{game_id}/accelerate-progress")
def accelerate_progress(game_id: str, req: AccelerateProgressRequest):
    try:
        state = game_session.accelerate_progress(game_id, req.progress_card_id)
        return {"state": state.to_dict()}
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.post("/api/games/{game_id}/undo")
def undo_action(game_id: str):
    try:
        state = game_session.undo_last_action(game_id)
        return {"state": state.to_dict()}
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))


@app.delete("/api/games/{game_id}")
def delete_game(game_id: str):
    game_session.delete_game(game_id)
    return {"message": "Game deleted"}


@app.get("/api/health")
def health():
    return {"status": "ok", "version": "1.0.0"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000, reload=True)
