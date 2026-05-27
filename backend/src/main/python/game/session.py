"""
Session manager — holds active games in memory
"""
from typing import Dict, Optional
from .state import GameState
from .setup import setup_solo_game
from .engine import GameEngine
from .enums import Nation, Difficulty

_sessions: Dict[str, GameState] = {}
_snapshots: Dict[str, GameState] = {}  # снапшоты для отмены хода
_engine = GameEngine()


def _snapshot(game_id: str, state: GameState):
    import copy
    _snapshots[game_id] = copy.deepcopy(state)


def create_game(player_nation: str, bot_nation: str,
                difficulty: str = "emperor",
                ability_side: str = "B") -> GameState:
    pn = Nation(player_nation)
    bn = Nation(bot_nation)
    diff = Difficulty(difficulty)
    state = setup_solo_game(pn, bn, diff, ability_side)
    _sessions[state.game_id] = state
    return state


def get_game(game_id: str) -> Optional[GameState]:
    return _sessions.get(game_id)


def save_game(state: GameState):
    _sessions[state.game_id] = state


def delete_game(game_id: str):
    _sessions.pop(game_id, None)


def play_card(game_id: str, card_id: str) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.play_card(state, card_id)
    save_game(state)
    return state


def exploit_card(game_id: str, card_id: str) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.exploit_card(state, card_id)
    save_game(state)
    return state


def do_innovation(game_id: str) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.do_innovation(state)
    save_game(state)
    return state


def do_revolution(game_id: str, card_ids: list) -> GameState:
    from .enums import GamePhase
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.do_revolution(state, card_ids)
    # Революция завершает ход — переходим в фазу сброса
    state = _engine.end_turn(state)  # PLAYER_TURN → PLAYER_DISCARD
    # Если лимит руки не превышен — пропускаем фазу сброса
    if state.phase == GamePhase.PLAYER_DISCARD:
        if len(state.player.hand) <= state.player.hand_limit:
            state = _engine.end_turn(state)
            if state.phase == GamePhase.BOT_TURN:
                state = _engine.run_bot_turn(state)
    save_game(state)
    return state


def end_player_turn(game_id: str, discard_ids: list = None) -> GameState:
    from .enums import GamePhase
    state = _require(game_id)

    if state.phase == GamePhase.PLAYER_TURN:
        _snapshot(game_id, state)
        state = _engine.end_turn(state)
    elif state.phase == GamePhase.PLAYER_DISCARD:
        if discard_ids:
            state = _engine.discard_from_hand(state, discard_ids)
        state = _engine.end_turn(state)
        if state.phase == GamePhase.BOT_TURN:
            state = _engine.run_bot_turn(state)

    save_game(state)
    return state


def acquire_card(game_id: str, slot_index: int) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.acquire_card(state, slot_index)
    save_game(state)
    return state


def accelerate_progress(game_id: str, progress_card_id: str) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.accelerate_progress(state, progress_card_id)
    save_game(state)
    return state


def choose_option(game_id: str, option_index: int) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.choose_option(state, option_index)
    save_game(state)
    return state


def select_appropriate_category(game_id: str, category: str) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.select_appropriate_category(state, category)
    save_game(state)
    return state


def appropriate_from_deck(game_id: str, deck_name: str) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.appropriate_from_deck(state, deck_name)
    save_game(state)
    return state


def resolve_reinforce_choice(game_id: str, reinforce: bool) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.resolve_reinforce_choice(state, reinforce)
    save_game(state)
    return state


def reinforce_with_card(game_id: str, hand_card_id: str) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.reinforce_with_card(state, hand_card_id)
    save_game(state)
    return state


def place_upgrade_token(game_id: str, slot_index: int) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.place_upgrade_token(state, slot_index)
    save_game(state)
    return state


def play_from_discard(game_id: str, card_id: str) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.select_play_from_discard(state, card_id)
    save_game(state)
    return state


def return_exploit_token(game_id: str, card_id: str | None) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.return_exploit_token(state, card_id)
    save_game(state)
    return state


def resolve_draw_from_deck_optional(game_id: str, draw: bool) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.resolve_draw_from_deck_optional(state, draw)
    save_game(state)
    return state


def resolve_chronicle_choice(game_id: str, send_to_chronicle: bool) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.resolve_chronicle_choice(state, send_to_chronicle)
    save_game(state)
    return state


def resolve_recall_to_avoid_attack(game_id: str, recall: bool) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.resolve_recall_to_avoid_attack(state, recall)
    save_game(state)
    return state


def chronicle_card_from_discard(game_id: str, card_id: str) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.chronicle_card_from_discard(state, card_id)
    save_game(state)
    return state


def exile_card_from_market(game_id: str, slot_index: int) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.exile_card_from_market(state, slot_index)
    save_game(state)
    return state


def select_destroy_cards(game_id: str, card_ids: list) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.select_destroy_cards(state, card_ids)
    save_game(state)
    return state


def resolve_sacred_path_exploit(game_id: str, destroy: bool) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.resolve_sacred_path_exploit(state, destroy)
    save_game(state)
    return state


def resolve_sacred_path_exchange(game_id: str, hand_card_id: str) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.resolve_sacred_path_exchange(state, hand_card_id)
    save_game(state)
    return state


def move_discard_to_deck_card(game_id: str, card_id) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.move_discard_to_deck_card(state, card_id)
    save_game(state)
    return state


def select_glory_deck_card(game_id: str, card_id: str) -> GameState:
    state = _require(game_id)
    _snapshot(game_id, state)
    state = _engine.select_glory_deck_card(state, card_id)
    save_game(state)
    return state


def undo_last_action(game_id: str) -> GameState:
    prev = _snapshots.get(game_id)
    if prev is None:
        raise ValueError("Нет действий для отмены")
    _sessions[game_id] = prev
    del _snapshots[game_id]
    return prev


def _require(game_id: str) -> GameState:
    state = get_game(game_id)
    if state is None:
        raise ValueError(f"Game {game_id} not found")
    return state
