"""
Card model and all card definitions for Imperium: Classics
"""
from dataclasses import dataclass, field
from typing import Optional, List
from .enums import CardCategory, CardSubtype, CardType, Period, Nation, ResourceType
from .data import vikings as _d_vikings
from .data import greeks as _d_greeks
from .data import carthaginians as _d_carthaginians
from .data import celts as _d_celts
from .data import macedonians as _d_macedonians
from .data import persians as _d_persians
from .data import romans as _d_romans
from .data import scythians as _d_scythians
from .data import base_regions as _d_base_regions
from .data import base_origins as _d_base_origins
from .data import base_civilization as _d_base_civilization
from .data import base_raids as _d_base_raids
from .data import base_glory as _d_base_glory
from .data import base_disorder as _d_base_disorder


@dataclass
class GainResourceAction:
    """Действие карты: добавить игроку ресурсы указанного типа."""
    resource_type: ResourceType
    amount: int


@dataclass
class AcquireCardAction:
    """Действие карты: приобрести карту с рынка по категории."""
    allowed_categories: List[CardCategory]
    count: int = 1


# Словарь имя → член enum, не зависящий от поведения __getitem__ в Python 3.11
_RESOURCE_TYPE_BY_NAME: dict = {rt.name: rt for rt in ResourceType}


def _parse_on_play_actions(data: dict) -> List:
    actions = []
    for a in data.get("on_play_actions", []):
        action_type = a.get("type")
        if action_type == "acquire_from_market":
            actions.append(AcquireCardAction(
                allowed_categories=[CardCategory(c) for c in a.get("categories", [])],
                count=a.get("count", 1),
            ))
        else:
            # Без type — считаем gain_resource (обратная совместимость)
            rt_name = a["resource_type"]
            if rt_name not in _RESOURCE_TYPE_BY_NAME:
                raise ValueError(f"Неизвестный ResourceType: '{rt_name}'. "
                                 f"Допустимые: {list(_RESOURCE_TYPE_BY_NAME)}")
            actions.append(GainResourceAction(
                resource_type=_RESOURCE_TYPE_BY_NAME[rt_name],
                amount=a["amount"],
            ))
    return actions


@dataclass
class Card:
    id: str                          # уникальный буквенно-числовой номер
    name: str
    nation: Optional[Nation] = None  # None = базовая колода
    card_type: CardType = CardType.NORMAL
    period: Optional[Period] = None  # ограничение по периоду
    # VP
    vp_fixed: int = 0               # фиксированные ПО (X)
    vp_condition: Optional[str] = None   # условные ПО (?)
    vp_per_condition: Optional[str] = None  # ПО за каждое выполнение (*) max 10
    vp_penalty: int = 0             # штрафные ПО (-)
    # Cost for progress cards
    progress_cost_resource: int = 0
    progress_cost_population: int = 0
    progress_cost_upgrade: int = 0
    # Gameplay flags
    requires_action_token: bool = True
    is_exploit: bool = False
    solstice_effect: bool = False
    passive_effect: bool = False
    # Starting location symbol
    start_location: str = ""  # "ability", "boost", "transformation", "progress", "reserve"
    # Resource generation on play
    gives_resource: int = 0       # даёт жетоны ресурсов при розыгрыше
    gives_population: int = 0     # даёт жетоны населения при розыгрыше
    gives_progress: int = 0       # даёт жетоны прогресса при розыгрыше
    draws_cards: int = 0          # тянет карты из колоды при розыгрыше
    # Opponent effects
    steal_progress: int = 0       # забирает жетоны прогресса у соперника
    steal_population: int = 0     # забирает жетоны населения у соперника
    discard_opponent_card: bool = False  # соперник сбрасывает карту из игровой зоны
    gives_disorder: int = 0       # даёт сопернику карты беспорядков
    # Reinforcement
    can_be_reinforced: bool = False  # можно укрепить (добавить карту из руки поверх)
    # Chronicle
    sends_to_chronicle: int = 0   # отправляет N карт соперника/своих в летопись
    goes_to_chronicle: bool = False  # сама идёт в летопись после розыгрыша (не в сброс)
    # Actions executed on play
    on_play_actions: List[GainResourceAction] = field(default_factory=list)

    def __hash__(self):
        return hash(self.id)

    def __eq__(self, other):
        return isinstance(other, Card) and self.id == other.id

    def __repr__(self):
        return f"Card({self.id}: {self.name})"


@dataclass
class BaseCard(Card):
    """Карта базовой колоды (регионы, истоки, цивилизации, набеги, слава, беспорядки)."""
    categories: List[CardCategory] = field(default_factory=list)

    def __post_init__(self):
        if CardCategory.REGION in self.categories:
            self.card_type = CardType.PERMANENT


@dataclass
class NationCard(Card):
    """Карта колоды нации."""
    subtype: Optional[CardSubtype] = None

    def __post_init__(self):
        pass


# ──────────────────────────────────────────────────
# BASE DECK FACTORY
# ──────────────────────────────────────────────────

def _base_card_from_dict(card_id: str, data: dict) -> BaseCard:
    """Создаёт BaseCard из словаря параметров карты."""
    categories = [CardCategory(c) for c in data.get("categories", [])]
    card_type = CardType(data["card_type"]) if "card_type" in data else CardType.NORMAL
    period = Period(data["period"]) if "period" in data else None

    return BaseCard(
        id=card_id,
        name=data["name"],
        categories=categories,
        card_type=card_type,
        period=period,
        vp_fixed=data.get("vp_fixed", 0),
        vp_condition=data.get("vp_condition"),
        vp_per_condition=data.get("vp_per_condition"),
        vp_penalty=data.get("vp_penalty", 0),
        requires_action_token=data.get("requires_action_token", True),
        is_exploit=data.get("is_exploit", False),
        solstice_effect=data.get("solstice_effect", False),
        passive_effect=data.get("passive_effect", False),
        start_location=data.get("start_location", ""),
        gives_resource=data.get("gives_resource", 0),
        gives_population=data.get("gives_population", 0),
        gives_progress=data.get("gives_progress", 0),
        draws_cards=data.get("draws_cards", 0),
        steal_progress=data.get("steal_progress", 0),
        steal_population=data.get("steal_population", 0),
        discard_opponent_card=data.get("discard_opponent_card", False),
        gives_disorder=data.get("gives_disorder", 0),
        can_be_reinforced=data.get("can_be_reinforced", False),
        sends_to_chronicle=data.get("sends_to_chronicle", 0),
        goes_to_chronicle=data.get("goes_to_chronicle", False),
        on_play_actions=_parse_on_play_actions(data),
    )


def build_base_deck_from_dict(deck_data: dict) -> List[Card]:
    """Строит список BaseCard из словаря данных карт."""
    return [_base_card_from_dict(card_id, card_data)
            for card_id, card_data in deck_data.items()]


# ──────────────────────────────────────────────────
# BASE DECK — shared cards (Классика)
# ──────────────────────────────────────────────────

def build_base_deck_classics() -> List[Card]:
    cards: List[Card] = []

    cards.extend(build_base_deck_from_dict(_d_base_regions.DECK))
    cards.extend(build_base_deck_from_dict(_d_base_origins.DECK))
    cards.extend(build_base_deck_from_dict(_d_base_civilization.DECK))
    cards.extend(build_base_deck_from_dict(_d_base_raids.DECK))
    cards.extend(build_base_deck_from_dict(_d_base_glory.DECK))
    cards.extend(build_base_deck_from_dict(_d_base_disorder.DECK))

    return cards


# ──────────────────────────────────────────────────
# NATION DECK FACTORY
# ──────────────────────────────────────────────────

def _card_from_dict(card_id: str, data: dict, nation: Nation) -> NationCard:
    """Создаёт NationCard из словаря параметров карты."""
    subtype = CardSubtype(data["subtype"]) if "subtype" in data else None
    card_type = CardType(data.get("card_type", CardType.NORMAL))
    period = Period(data["period"]) if "period" in data else None

    return NationCard(
        id=card_id,
        name=data["name"],
        nation=nation,
        subtype=subtype,
        card_type=card_type,
        period=period,
        vp_fixed=data.get("vp_fixed", 0),
        vp_condition=data.get("vp_condition"),
        vp_per_condition=data.get("vp_per_condition"),
        vp_penalty=data.get("vp_penalty", 0),
        progress_cost_resource=data.get("progress_cost_resource", 0),
        progress_cost_population=data.get("progress_cost_population", 0),
        progress_cost_upgrade=data.get("progress_cost_upgrade", 0),
        requires_action_token=data.get("requires_action_token", True),
        is_exploit=data.get("is_exploit", False),
        solstice_effect=data.get("solstice_effect", False),
        passive_effect=data.get("passive_effect", False),
        start_location=data.get("start_location", ""),
        gives_resource=data.get("gives_resource", 0),
        gives_population=data.get("gives_population", 0),
        gives_progress=data.get("gives_progress", 0),
        draws_cards=data.get("draws_cards", 0),
        steal_progress=data.get("steal_progress", 0),
        steal_population=data.get("steal_population", 0),
        discard_opponent_card=data.get("discard_opponent_card", False),
        gives_disorder=data.get("gives_disorder", 0),
        can_be_reinforced=data.get("can_be_reinforced", False),
        sends_to_chronicle=data.get("sends_to_chronicle", 0),
        goes_to_chronicle=data.get("goes_to_chronicle", False),
        on_play_actions=_parse_on_play_actions(data),
    )


def build_nation_deck_from_dict(deck_data: dict, nation: Nation) -> List[Card]:
    """Строит колоду нации из словаря данных карт."""
    return [_card_from_dict(card_id, card_data, nation)
            for card_id, card_data in deck_data.items()]


# ──────────────────────────────────────────────────
# CARD REGISTRY
# ──────────────────────────────────────────────────

_NATION_DATA: dict = {
    Nation.VIKINGS:       (_d_vikings.DECK,       Nation.VIKINGS),
    Nation.GREEKS:        (_d_greeks.DECK,        Nation.GREEKS),
    Nation.CARTHAGINIANS: (_d_carthaginians.DECK, Nation.CARTHAGINIANS),
    Nation.CELTS:         (_d_celts.DECK,         Nation.CELTS),
    Nation.MACEDONIANS:   (_d_macedonians.DECK,   Nation.MACEDONIANS),
    Nation.PERSIANS:      (_d_persians.DECK,      Nation.PERSIANS),
    Nation.ROMANS:        (_d_romans.DECK,        Nation.ROMANS),
    Nation.SCYTHIANS:     (_d_scythians.DECK,     Nation.SCYTHIANS),
}


def get_nation_deck(nation: Nation) -> List[Card]:
    entry = _NATION_DATA.get(nation)
    if not entry:
        raise ValueError(f"Unknown nation: {nation}")
    deck_data, nation_enum = entry
    return build_nation_deck_from_dict(deck_data, nation_enum)


def get_all_available_nations():
    return list(Nation)
