"""
Explicit JSON serialization for GameState.

Cards are stored as IDs only; on load they are reconstructed from the card
registry (base deck + the two nation decks). This guarantees that every zone
is saved in its exact order, every token value is stored explicitly, and the
format stays human-readable and version-independent.
"""
import json
from typing import Optional, List, Dict

from .state import GameState, PlayerArea, BotArea, SharedArea, MarketSlot, Resources
from .cards import build_base_deck_classics, get_nation_deck, Card
from .enums import (
    Nation, Period, GamePhase, TurnAction, Difficulty, EndCondition,
)


# ── Card registry ──────────────────────────────────────────────────────────────

def _build_registry(player_nation: Nation, bot_nation: Nation) -> Dict[str, Card]:
    """Map card_id → Card for every card that can appear in this game."""
    registry: Dict[str, Card] = {}
    for card in build_base_deck_classics():
        registry[card.id] = card
    for card in get_nation_deck(player_nation):
        registry[card.id] = card
    for card in get_nation_deck(bot_nation):
        registry[card.id] = card
    return registry


# ── Helpers ────────────────────────────────────────────────────────────────────

def _ids(cards: List[Optional[Card]]) -> List[Optional[str]]:
    """Convert a list of Card (or None) to a list of id (or None)."""
    return [c.id if c is not None else None for c in cards]


def _strict_ids(cards: List[Card]) -> List[str]:
    """Convert a list of Card (no Nones) to a list of ids."""
    return [c.id for c in cards]


# ── Serialization ──────────────────────────────────────────────────────────────

def serialize_state(state: GameState) -> str:
    """Serialize GameState to a JSON string."""
    p = state.player
    b = state.bot
    s = state.shared

    data = {
        # ── Game-level fields ──────────────────────────────────────────────────
        "game_id": state.game_id,
        "phase": state.phase.value,
        "round_number": state.round_number,
        "is_final_round": state.is_final_round,
        "end_condition": state.end_condition.value if state.end_condition else None,
        "difficulty": state.difficulty.value,
        "log": state.log,
        # Pending-state dicts — already JSON-safe (no Card objects)
        "pending_choice": state.pending_choice,
        "pending_chronicle_card_id": state.pending_chronicle_card_id,
        "pending_reinforce_card_id": state.pending_reinforce_card_id,
        "pending_card_play_actions": state.pending_card_play_actions,
        "pending_bot_attacks": state.pending_bot_attacks,
        "pending_bot_turn_continuation": state.pending_bot_turn_continuation,
        "pending_solstice_card_ids": state.pending_solstice_card_ids,

        # ── Player ────────────────────────────────────────────────────────────
        "player": {
            "nation": p.nation.value,
            "period": p.period.value,
            # Card zones — stored as ordered lists of IDs
            "deck": _strict_ids(p.deck),
            "hand": _strict_ids(p.hand),
            "discard": _strict_ids(p.discard),
            "play_area": _strict_ids(p.play_area),
            # Reinforcements: {parent_card_id: reinforcing_card_id}
            "reinforcements": {k: v.id for k, v in p.reinforcements.items()},
            "chronicle": _strict_ids(p.chronicle),
            "progress_area": _strict_ids(p.progress_area),
            "boost_deck": _strict_ids(p.boost_deck),
            "boost_top_token": p.boost_top_token,
            "ability_card_id": p.ability_card.id if p.ability_card else None,
            "ability_side": p.ability_side,
            "ability_exploit_used": p.ability_exploit_used,
            # All token reserves (action, exploit, resource, population, upgrade)
            "resources": {
                "resource":   p.resources.resource,
                "population": p.resources.population,
                "upgrade":    p.resources.upgrade,
                "action":     p.resources.action,
                "exploit":    p.resources.exploit,
            },
            "hand_limit": p.hand_limit,
            "turn_action_chosen": p.turn_action_chosen.value if p.turn_action_chosen else None,
            # Track cards played/exploited this turn for proper UI state
            "cards_played_this_turn": _strict_ids(p.cards_played_this_turn),
            "exploits_used_this_turn": _strict_ids(p.exploits_used_this_turn),
        },

        # ── Bot ───────────────────────────────────────────────────────────────
        "bot": {
            "nation": b.nation.value,
            "period": b.period.value,
            # Card zones — stored as ordered lists of IDs
            "bot_deck": _strict_ids(b.bot_deck),
            "bot_discard": _strict_ids(b.bot_discard),
            "dynasty_deck": _strict_ids(b.dynasty_deck),
            "chronicle": _strict_ids(b.chronicle),
            "play_area": _strict_ids(b.play_area),
            # Hand slots may contain None (empty slot)
            "hand_slots": _ids(b.hand_slots),
            # Bot token reserves
            "resource":   b.resource,
            "population": b.population,
            "upgrade":    b.upgrade,
            "ability_card_id": b.ability_card.id if b.ability_card else None,
        },

        # ── Shared area ───────────────────────────────────────────────────────
        "shared": {
            # Common decks — stored as ordered lists of IDs
            "region_deck": _strict_ids(s.region_deck),
            "origins_deck": _strict_ids(s.origins_deck),
            "civilization_deck": _strict_ids(s.civilization_deck),
            "main_deck": _strict_ids(s.main_deck),
            "disorder_deck": _strict_ids(s.disorder_deck),
            "glory_deck": _strict_ids(s.glory_deck),
            "exile_pile": _strict_ids(s.exile_pile),
            # King of Kings
            "king_of_kings_id": s.king_of_kings.id if s.king_of_kings else None,
            "king_of_kings_side_b": s.king_of_kings_side_b,
            "solstice_card_owner": s.solstice_card_owner,
            # Market: each slot stores card ID + all token/marker state on that slot
            "market": [
                {
                    "card_id":         slot.card.id if slot.card else None,
                    "upgrade_tokens":  slot.upgrade_tokens,
                    "disorder_under_id": slot.disorder_under.id if slot.disorder_under else None,
                    "market_marker":   slot.market_marker,
                    "source_deck":     slot.source_deck,
                }
                for slot in s.market
            ],
        },
    }
    return json.dumps(data, ensure_ascii=False)


# ── Deserialization ────────────────────────────────────────────────────────────

def deserialize_state(encoded: str) -> GameState:
    """Reconstruct a GameState from a JSON string produced by serialize_state."""
    data = json.loads(encoded)

    player_nation = Nation(data["player"]["nation"])
    bot_nation = Nation(data["bot"]["nation"])
    reg = _build_registry(player_nation, bot_nation)

    def lookup(card_id: Optional[str]) -> Optional[Card]:
        if card_id is None:
            return None
        card = reg.get(card_id)
        if card is None:
            raise ValueError(
                f"Card ID '{card_id}' not found in registry. "
                f"Nations in game: {player_nation.value}, {bot_nation.value}"
            )
        return card

    def lookup_list(ids: List[Optional[str]]) -> List[Card]:
        return [lookup(i) for i in ids]  # type: ignore[return-value]

    def strict_lookup_list(ids: List[str]) -> List[Card]:
        result = []
        for i in ids:
            c = lookup(i)
            if c is None:
                raise ValueError(f"Unexpected None in strict lookup for id '{i}'")
            result.append(c)
        return result

    # ── Player ────────────────────────────────────────────────────────────────
    pd = data["player"]
    player = PlayerArea(nation=player_nation)
    player.period = Period(pd["period"])
    player.deck = strict_lookup_list(pd["deck"])
    player.hand = strict_lookup_list(pd["hand"])
    player.discard = strict_lookup_list(pd["discard"])
    player.play_area = strict_lookup_list(pd["play_area"])
    player.reinforcements = {
        parent_id: lookup(child_id)  # type: ignore[assignment]
        for parent_id, child_id in pd["reinforcements"].items()
    }
    player.chronicle = strict_lookup_list(pd["chronicle"])
    player.progress_area = strict_lookup_list(pd["progress_area"])
    player.boost_deck = strict_lookup_list(pd["boost_deck"])
    player.boost_top_token = pd["boost_top_token"]
    player.ability_card = lookup(pd["ability_card_id"])
    player.ability_side = pd["ability_side"]
    player.ability_exploit_used = pd["ability_exploit_used"]
    r = pd["resources"]
    player.resources = Resources(
        resource=r["resource"],
        population=r["population"],
        upgrade=r["upgrade"],
        action=r["action"],
        exploit=r["exploit"],
    )
    player.hand_limit = pd["hand_limit"]
    player.turn_action_chosen = (
        TurnAction(pd["turn_action_chosen"]) if pd["turn_action_chosen"] else None
    )
    player.cards_played_this_turn = strict_lookup_list(pd["cards_played_this_turn"])
    player.exploits_used_this_turn = strict_lookup_list(pd["exploits_used_this_turn"])

    # ── Bot ───────────────────────────────────────────────────────────────────
    bd = data["bot"]
    bot = BotArea(nation=bot_nation)
    bot.period = Period(bd["period"])
    bot.bot_deck = strict_lookup_list(bd["bot_deck"])
    bot.bot_discard = strict_lookup_list(bd["bot_discard"])
    bot.dynasty_deck = strict_lookup_list(bd["dynasty_deck"])
    bot.chronicle = strict_lookup_list(bd["chronicle"])
    bot.play_area = strict_lookup_list(bd["play_area"])
    bot.hand_slots = lookup_list(bd["hand_slots"])
    bot.resource = bd["resource"]
    bot.population = bd["population"]
    bot.upgrade = bd["upgrade"]
    bot.ability_card = lookup(bd["ability_card_id"])

    # ── Shared ────────────────────────────────────────────────────────────────
    sd = data["shared"]
    shared = SharedArea()
    shared.region_deck = strict_lookup_list(sd["region_deck"])
    shared.origins_deck = strict_lookup_list(sd["origins_deck"])
    shared.civilization_deck = strict_lookup_list(sd["civilization_deck"])
    shared.main_deck = strict_lookup_list(sd["main_deck"])
    shared.disorder_deck = strict_lookup_list(sd["disorder_deck"])
    shared.glory_deck = strict_lookup_list(sd["glory_deck"])
    shared.exile_pile = strict_lookup_list(sd["exile_pile"])
    shared.king_of_kings = lookup(sd["king_of_kings_id"])
    shared.king_of_kings_side_b = sd["king_of_kings_side_b"]
    shared.solstice_card_owner = sd["solstice_card_owner"]
    shared.market = [
        MarketSlot(
            card=lookup(slot["card_id"]),
            upgrade_tokens=slot["upgrade_tokens"],
            disorder_under=lookup(slot["disorder_under_id"]),
            market_marker=slot["market_marker"],
            source_deck=slot["source_deck"],
        )
        for slot in sd["market"]
    ]

    # ── GameState ─────────────────────────────────────────────────────────────
    state = GameState(game_id=data["game_id"])
    state.phase = GamePhase(data["phase"])
    state.round_number = data["round_number"]
    state.is_final_round = data["is_final_round"]
    state.end_condition = (
        EndCondition(data["end_condition"]) if data["end_condition"] else None
    )
    state.difficulty = Difficulty(data["difficulty"])
    state.log = data["log"]
    state.pending_choice = data["pending_choice"]
    state.pending_chronicle_card_id = data["pending_chronicle_card_id"]
    state.pending_reinforce_card_id = data["pending_reinforce_card_id"]
    state.pending_card_play_actions = data["pending_card_play_actions"]
    state.pending_bot_attacks = data["pending_bot_attacks"]
    state.pending_bot_turn_continuation = data["pending_bot_turn_continuation"]
    state.pending_solstice_card_ids = data["pending_solstice_card_ids"]
    state.player = player
    state.bot = bot
    state.shared = shared

    return state
