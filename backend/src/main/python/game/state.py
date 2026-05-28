"""
Game State — complete snapshot of an Imperium game
"""
import random
import uuid
from dataclasses import dataclass, field
from typing import List, Optional, Dict
from .cards import (Card, GainResourceAction, AcquireCardAction, AppropriateCardAction,
                    ChoiceAction, ChoiceOption, PlayFromDiscardAction,
                    DrawFromDeckOptionalAction, GainPerLabelAction, GainPerCategoryAction,
                    StealResourceAction, ReturnExploitTokenOptionalAction,
                    build_base_deck_classics, get_nation_deck)
from .enums import (Period, Nation, GamePhase, TurnAction, EndCondition,
                    Difficulty, CardCategory, CardLabel, CardSubtype, ResourceType)


def _card_info(card: Card) -> dict:
    cats = getattr(card, 'categories', [])
    sub = getattr(card, 'subtype', None)

    # Derive display fields from on_play_actions
    gives_resource = sum(
        a.amount for a in card.on_play_actions
        if isinstance(a, GainResourceAction) and a.resource_type == ResourceType.MATERIAL
    )
    gives_population = sum(
        a.amount for a in card.on_play_actions
        if isinstance(a, GainResourceAction) and a.resource_type == ResourceType.POPULATION
    )
    gives_progress = sum(
        a.amount for a in card.on_play_actions
        if isinstance(a, GainResourceAction) and a.resource_type == ResourceType.PROGRESS
    )

    # Serialize on_play_actions for frontend
    serialized_actions = []
    for a in card.on_play_actions:
        if isinstance(a, GainResourceAction):
            serialized_actions.append({
                "type": "gain_resource",
                "resource_type": a.resource_type.value,
                "amount": a.amount,
            })
        elif isinstance(a, AcquireCardAction):
            serialized_actions.append({
                "type": "acquire_from_market",
                "categories": [c.value for c in a.allowed_categories],
                "count": a.count,
            })
        elif isinstance(a, AppropriateCardAction):
            serialized_actions.append({
                "type": "appropriate",
                "categories": [c.value for c in a.allowed_categories],
                "source_decks": a.allowed_source_decks,
                "include_main_deck": a.include_main_deck,
                "count": a.count,
            })
        elif isinstance(a, StealResourceAction):
            serialized_actions.append({
                "type": "steal_resource",
                "resource_type": a.resource_type.name,
                "amount": a.amount,
            })
        elif isinstance(a, ReturnExploitTokenOptionalAction):
            serialized_actions.append({"type": "return_exploit_token_optional"})
        elif isinstance(a, DrawFromDeckOptionalAction):
            serialized_actions.append({"type": "draw_from_deck_optional"})
        elif isinstance(a, ChoiceAction):
            serialized_opts = []
            for opt in a.options:
                inner = opt.action
                if isinstance(inner, AcquireCardAction):
                    inner_data = {
                        "type": "acquire_from_market",
                        "categories": [c.value for c in inner.allowed_categories],
                        "count": inner.count,
                    }
                elif isinstance(inner, AppropriateCardAction):
                    inner_data = {
                        "type": "appropriate",
                        "categories": [c.value for c in inner.allowed_categories],
                        "source_decks": inner.allowed_source_decks,
                        "include_main_deck": inner.include_main_deck,
                        "count": inner.count,
                    }
                elif isinstance(inner, GainPerLabelAction):
                    inner_data = {
                        "type": "gain_per_label",
                        "label": inner.label,
                        "resource_type": inner.resource_type.name,
                    }
                elif isinstance(inner, GainPerCategoryAction):
                    inner_data = {
                        "type": "gain_per_category",
                        "category": inner.category.value,
                        "resource_type": inner.resource_type.name,
                    }
                elif isinstance(inner, GainResourceAction):
                    inner_data = {
                        "type": "gain_resource",
                        "resource_type": inner.resource_type.name,
                        "amount": inner.amount,
                    }
                else:
                    continue
                serialized_opts.append({
                    "label": opt.label,
                    "cost_population": opt.cost_population,
                    "cost_resource": opt.cost_resource,
                    "action": inner_data,
                })
            serialized_actions.append({"type": "choice", "options": serialized_opts})

    # Serialize exploit_actions
    serialized_exploit_actions = []
    for a in card.exploit_actions:
        if isinstance(a, PlayFromDiscardAction):
            serialized_exploit_actions.append({
                "type": "play_from_discard",
                "categories": [c.value for c in a.allowed_categories],
                "count": a.count,
                "cost_action": a.cost_action,
            })

    return {
        "id": card.id,
        "name": card.name,
        "categories": [c.value for c in cats],
        "subtype": sub.value if sub else None,
        "period": card.period.value if card.period else None,
        "vp_fixed": card.vp_fixed,
        "vp_condition": card.vp_condition,
        "vp_per_condition": card.vp_per_condition,
        "vp_penalty": card.vp_penalty,
        "requires_action": card.requires_action_token,
        "is_exploit": card.is_exploit,
        "passive_effect": card.passive_effect,
        "solstice_effect": card.solstice_effect,
        "card_type": card.card_type.value,

        "progress_cost_resource": card.progress_cost_resource,
        "progress_cost_population": card.progress_cost_population,
        "progress_cost_upgrade": card.progress_cost_upgrade,
        "gives_resource": gives_resource,
        "gives_population": gives_population,
        "gives_progress": gives_progress,
        "on_play_actions": serialized_actions,
        "goes_to_chronicle": card.goes_to_chronicle,
        "can_be_chronicled": card.can_be_chronicled,
        "exploit_actions": serialized_exploit_actions,
        "labels": [lb.value for lb in getattr(card, 'labels', [])],
        "exploit_passive": getattr(card, 'exploit_passive', "") or None,
    }


@dataclass
class Resources:
    """Запасы жетонов игрока"""
    resource: int = 0      # ресурс (монеты/зерно)
    population: int = 0    # население
    upgrade: int = 0       # модернизация
    action: int = 3        # жетоны действия на карте периода
    exploit: int = 5       # жетоны эксплуатации на карте периода

    def to_dict(self):
        return {
            "resource": self.resource,
            "population": self.population,
            "upgrade": self.upgrade,
            "action": self.action,
            "exploit": self.exploit,
        }


@dataclass
class PlayerArea:
    """Личная игровая область игрока"""
    nation: Nation
    period: Period = Period.BARBARISM

    # Карты
    deck: List[Card] = field(default_factory=list)          # личная колода
    hand: List[Card] = field(default_factory=list)          # рука
    discard: List[Card] = field(default_factory=list)       # сброс
    play_area: List[Card] = field(default_factory=list)     # игровая область (разыгранные ∞)
    reinforcements: Dict[str, Card] = field(default_factory=dict)  # укрепления: parent_card_id → reinforcing_card
    chronicle: List[Card] = field(default_factory=list)     # летопись (под картой способности)
    progress_area: List[Card] = field(default_factory=list) # область прогресса

    # Специальные колоды
    boost_deck: List[Card] = field(default_factory=list)    # колода усиления
    boost_top_token: bool = False                            # жетон X на верхней карте усиления

    # Карты способности
    ability_card: Optional[Card] = None
    ability_side: str = "B"  # "A" или "B"
    ability_exploit_used: bool = False  # использована ли эксплуатация способности

    # Жетоны
    resources: Resources = field(default_factory=Resources)

    # Предел руки
    hand_limit: int = 5

    # Флаг — был ли ход сыгран
    turn_action_chosen: Optional[TurnAction] = None
    cards_played_this_turn: List[Card] = field(default_factory=list)
    exploits_used_this_turn: List[Card] = field(default_factory=list)

    def to_dict(self):
        return {
            "nation": self.nation.value,
            "period": self.period.value,
            "deck_count": len(self.deck),
            "hand": [_card_info(c) for c in self.hand],
            "discard_count": len(self.discard),
            "discard_top": _card_info(self.discard[-1]) if self.discard else None,
            "play_area": [
                {**_card_info(c), "reinforcement": _card_info(self.reinforcements[c.id]) if c.id in self.reinforcements else None}
                for c in self.play_area
            ],
            "chronicle_count": len(self.chronicle),
            "chronicle": [_card_info(c) for c in self.chronicle],
            "progress_area": [_card_info(c) for c in self.progress_area],
            "boost_deck_count": len(self.boost_deck),
            "boost_top_token": self.boost_top_token,
            "boost_bottom_card": _card_info(self.boost_deck[-1])
                if self.boost_deck and getattr(self.boost_deck[-1], 'subtype', None) is not None
                   and self.boost_deck[-1].subtype.value == 'transformation'
                else None,
            "ability_card": {**_card_info(self.ability_card),
                              "side": self.ability_side} if self.ability_card else None,
            "resources": self.resources.to_dict(),
            "hand_limit": self.hand_limit,
            "turn_action_chosen": self.turn_action_chosen.value if self.turn_action_chosen else None,
            "exploits_used_ids": [c.id for c in self.exploits_used_this_turn],
            "play_area_labels": self._count_play_area_labels(),
        }

    def _count_play_area_labels(self) -> dict:
        counts: dict = {}
        for card in self.play_area:
            for lb in getattr(card, 'labels', []):
                key = lb.value
                counts[key] = counts.get(key, 0) + 1
        # Passive exploit: Порт — если эксплуатирована, 1 GRAIN = 3 SACK
        if any(getattr(c, 'exploit_passive', '') == 'grain_to_sack_3'
               for c in self.exploits_used_this_turn):
            grain = counts.get('grain', 0)
            if grain:
                counts['sack'] = counts.get('sack', 0) + grain * 3
        return counts


@dataclass
class BotArea:
    """Игровая область бота (соло-режим)"""
    nation: Nation
    period: Period = Period.BARBARISM

    # Колоды бота
    bot_deck: List[Card] = field(default_factory=list)
    bot_discard: List[Card] = field(default_factory=list)
    dynasty_deck: List[Card] = field(default_factory=list)
    chronicle: List[Card] = field(default_factory=list)
    play_area: List[Card] = field(default_factory=list)  # карты ∞ в области бота

    # Карты ниже маркеров рынка (рука бота — 5 слотов)
    hand_slots: List[Optional[Card]] = field(default_factory=lambda: [None] * 5)

    # Жетоны бота
    resource: int = 0
    population: int = 0
    upgrade: int = 0

    # Карта способности бота (игнорируется в механике)
    ability_card: Optional[Card] = None

    def to_dict(self):
        return {
            "nation": self.nation.value,
            "period": self.period.value,
            "bot_deck_count": len(self.bot_deck),
            "dynasty_deck_count": len(self.dynasty_deck),
            "chronicle_count": len(self.chronicle),
            "play_area_count": len(self.play_area),
            "hand_slots": [{"id": c.id, "name": c.name} if c else None
                           for c in self.hand_slots],
            "resource": self.resource,
            "population": self.population,
            "upgrade": self.upgrade,
        }


@dataclass
class MarketSlot:
    """Слот на текущем рынке"""
    card: Optional[Card]
    upgrade_tokens: int = 0      # жетоны модернизации на карте
    disorder_under: Optional[Card] = None  # карта беспорядков под картой
    market_marker: int = 0       # номер маркера рынка (1-5, для бота)
    source_deck: str = "main"    # "region" | "origins" | "civilization" | "main"

    def to_dict(self):
        return {
            "card": _card_info(self.card) if self.card else None,
            "upgrade_tokens": self.upgrade_tokens,
            "has_disorder_under": self.disorder_under is not None,
            "market_marker": self.market_marker,
            "source_deck": self.source_deck,
        }


@dataclass
class SharedArea:
    """Общая игровая область"""
    # Колоды
    region_deck: List[Card] = field(default_factory=list)
    origins_deck: List[Card] = field(default_factory=list)
    civilization_deck: List[Card] = field(default_factory=list)
    main_deck: List[Card] = field(default_factory=list)      # основная колода
    disorder_deck: List[Card] = field(default_factory=list)  # колода беспорядков
    glory_deck: List[Card] = field(default_factory=list)     # колода славы
    exile_pile: List[Card] = field(default_factory=list)     # колода изгнания

    # Карта «Царь царей»
    king_of_kings: Optional[Card] = None
    king_of_kings_side_b: bool = False

    # Текущий рынок (5 слотов + 2 из основной колоды)
    market: List[MarketSlot] = field(default_factory=list)

    # Солнцестояние
    solstice_card_owner: str = "player"  # кто сидит справа от карты

    def to_dict(self):
        return {
            "region_deck_count": len(self.region_deck),
            "origins_deck_count": len(self.origins_deck),
            "civilization_deck_count": len(self.civilization_deck),
            "main_deck_count": len(self.main_deck),
            "disorder_deck_count": len(self.disorder_deck),
            "glory_deck_count": len(self.glory_deck),
            "exile_pile_count": len(self.exile_pile),
            "king_of_kings_side_b": self.king_of_kings_side_b,
            "market": [slot.to_dict() for slot in self.market],
        }


@dataclass
class GameState:
    """Полное состояние партии"""
    game_id: str = field(default_factory=lambda: str(uuid.uuid4()))
    phase: GamePhase = GamePhase.SETUP
    round_number: int = 1
    is_final_round: bool = False
    end_condition: Optional[EndCondition] = None

    player: Optional[PlayerArea] = None
    bot: Optional[BotArea] = None
    shared: SharedArea = field(default_factory=SharedArea)

    difficulty: Difficulty = Difficulty.EMPEROR

    # История ходов для UI
    log: List[str] = field(default_factory=list)

    # Pending action — для multi-step interactions
    pending_choice: Optional[Dict] = None

    # Отложенный запрос «занести в летопись» (ставится, если pending_choice уже занят)
    pending_chronicle_card_id: Optional[str] = None

    # Отложенный запрос «укрепить карту» (ставится, если pending_choice уже занят)
    pending_reinforce_card_id: Optional[str] = None

    # Очередь действий карты, ожидающих выполнения после разрешения pending_choice
    pending_card_play_actions: List[dict] = field(default_factory=list)

    # Отложенные атаки бота (ID карт), ожидающие решения игрока о отзыве 1REG12
    pending_bot_attacks: List[str] = field(default_factory=list)

    # Данные продолжения хода бота после разрешения отложенных атак
    pending_bot_turn_continuation: Optional[dict] = None

    # Очередь карт с эффектами солнцестояния, ожидающих обработки
    pending_solstice_card_ids: List[str] = field(default_factory=list)

    def add_log(self, message: str):
        self.log.append(message)
        if len(self.log) > 100:
            self.log = self.log[-100:]

    def to_dict(self):
        return {
            "game_id": self.game_id,
            "phase": self.phase.value,
            "round_number": self.round_number,
            "is_final_round": self.is_final_round,
            "end_condition": self.end_condition.value if self.end_condition else None,
            "player": self.player.to_dict() if self.player else None,
            "bot": self.bot.to_dict() if self.bot else None,
            "shared": self.shared.to_dict(),
            "difficulty": self.difficulty.value,
            "log": self.log[-20:],  # last 20 messages
            "pending_choice": self.pending_choice,
            "pending_chronicle_card_id": self.pending_chronicle_card_id,
            "pending_reinforce_card_id": self.pending_reinforce_card_id,
        }
