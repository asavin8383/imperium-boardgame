"""
Card model and all card definitions for Imperium: Classics
"""
from dataclasses import dataclass, field
from typing import Optional, List
from .enums import CardCategory, CardLabel, CardSubtype, CardType, Period, Nation, ResourceType
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
class SpendResourceAction:
    """Действие карты: игрок тратит N ресурсов указанного типа (проверяется до розыгрыша)."""
    resource_type: ResourceType
    amount: int


@dataclass
class BotGainsDisorderAction:
    """Все остальные игроки (бот) берут N карт беспорядков из общей колоды."""
    count: int = 1


@dataclass
class AcquireCardAction:
    """Действие карты: приобрести карту с рынка по категории."""
    allowed_categories: List[CardCategory]
    count: int = 1


@dataclass
class AppropriateCardAction:
    """Присвоить карту с рынка (беспорядки → стопка беспорядков)
    или взять верхнюю карту из заданной колоды,
    или найти карту нужного типа в основной колоде."""
    allowed_categories: List[CardCategory]
    allowed_source_decks: List[str] = field(default_factory=list)
    include_main_deck: bool = False
    count: int = 1


@dataclass
class ChoiceOption:
    """Одна опция в ChoiceAction."""
    label: str
    cost_population: int = 0
    cost_resource: int = 0
    action: object = None  # AcquireCardAction | AppropriateCardAction
    opponent_gains_progress: int = 0  # бот получает N жетонов прогресса при выборе этого варианта
    opponent_recalls_region: int = 0  # бот отзывает N карт регионов из игровой области при выборе этого варианта


@dataclass
class ChoiceAction:
    """Действие карты: выбор игрока из нескольких опций."""
    options: List  # List[ChoiceOption]


@dataclass
class PlayFromDiscardAction:
    """Действие эксплуатации: сыграть карту из личного сброса в игровую область."""
    allowed_categories: List[CardCategory]
    count: int = 1
    cost_action: int = 0


@dataclass
class StealResourceAction:
    """Украсть ресурс у оппонента (бота)."""
    resource_type: ResourceType
    amount: int


@dataclass
class ReturnExploitTokenOptionalAction:
    """Игрок МОЖЕТ вернуть жетон эксплуатации с карты игровой области в запас."""
    pass


@dataclass
class DrawFromDeckOptionalAction:
    """Все игроки МОГУТ взять 1 карту из личной колоды в руку."""
    pass


@dataclass
class ChronicleFromDiscardAction:
    """Занести 1 карту из личного сброса в летопись."""
    optional: bool = False  # если True — игрок МОЖЕТ пропустить


@dataclass
class ExileFromMarketAction:
    """Изгнать карту с рынка: навсегда убирает из игры, перезаполняет слот."""
    pass


@dataclass
class DestroyFromPlayAreaAction:
    """Разрушить N карт указанной категории из игровой области (→ личный сброс)."""
    category: CardCategory
    count: int


@dataclass
class LookAtGloryDeckAction:
    """Посмотреть верхние look_count карт колоды славы, взять take_count в руку."""
    look_count: int = 2
    take_count: int = 1


@dataclass
class SacredPathExploitAction:
    """Эксплуатация 1REG14: показать верхнюю карту колоды усиления,
    МОЖНО разрушить 1REG14 и обменять карту из руки на верхнюю карту усиления."""
    pass


@dataclass
class AllPlayersGainResourceAction:
    """При эксплуатации все игроки получают ресурсы."""
    resource_type: ResourceType
    amount: int


@dataclass
class SolsticeOptionalGainProgressThenFateAction:
    """Солнцестояние: МОЖНО взять N жетонов прогресса, затем обязательно разрушить или занести карту в летопись."""
    amount: int = 2


@dataclass
class ExploitSpendResourceDrawCardAction:
    """Потратить N ресурсов чтобы взять M карт из личной колоды."""
    resource_type: ResourceType
    resource_cost: int = 1
    draw_count: int = 1


@dataclass
class SolsticeOptionalDiscardHandReturnDisorderAction:
    """Солнцестояние: МОЖНО сбросить карту из руки, чтобы вернуть карту беспорядков из сброса в колоду беспорядков."""
    pass


@dataclass
class MoveDiscardToDeckAction:
    """Переместить 1 карту из личного сброса на верх личной колоды (опционально)."""
    optional: bool = True


@dataclass
class DrawUpToNFromDeckAction:
    """Взять до N карт с верха личной колоды в руку (берётся столько, сколько есть)."""
    count: int = 3


@dataclass
class ReturnCardToDeckTopAction:
    """Вернуть 1 карту из руки на верх личной колоды (обязательно)."""
    pass


@dataclass
class SolsticeChoiceAction:
    """Солнцестояние: выбор из нескольких опций (аналог ChoiceAction для on_play_actions)."""
    options: List[dict] = field(default_factory=list)


@dataclass
class SolsticeOptionalDiscardForChoiceAction:
    """Солнцестояние: МОЖНО сбросить карту из руки → выбрать 1 награду из опций."""
    options: List[dict] = field(default_factory=list)


@dataclass
class DrawThenDiscardChoiceAction:
    """Взять N карт из личной колоды в руку, затем обязательно сбросить M из них."""
    draw_count: int = 2
    discard_count: int = 1


@dataclass
class ChronicleFromHandAction:
    """Занести карту из руки в летопись."""
    optional: bool = False


@dataclass
class GuessDeckCategoryAction:
    """Назвать категорию → вскрыть верхнюю карту основной колоды → в руку если совпало, иначе изгнать."""
    allowed_categories: List[str] = field(default_factory=lambda: ["region", "origins", "civilization", "raid"])


@dataclass
class ExploitRecallLabelChoiceAction:
    """Эксплуатация: выбор из опций 'отозвать карту с меткой X → получить ресурсы'."""
    options: List[dict] = field(default_factory=list)
    # options: [{"label": "water", "gains": [{"resource_type": "MATERIAL", "amount": 2}, ...]}, ...]


@dataclass
class GainPerLabelAction:
    """Получить 1 ресурс за каждую метку указанного типа в игровой области игрока."""
    label: str           # "sack", "grain", "water"
    resource_type: ResourceType


@dataclass
class GainPerCategoryAction:
    """Получить 1 ресурс за каждую карту указанной категории в игровой области игрока."""
    category: CardCategory
    resource_type: ResourceType


# Словарь имя → член enum, не зависящий от поведения __getitem__ в Python 3.11
_RESOURCE_TYPE_BY_NAME: dict = {rt.name: rt for rt in ResourceType}


def _parse_choice_action_inner(a: dict):
    """Разбирает одно вложенное действие внутри опции выбора."""
    at = a.get("type")
    if at == "acquire_from_market":
        return AcquireCardAction(
            allowed_categories=[CardCategory(c) for c in a.get("categories", [])],
            count=a.get("count", 1),
        )
    if at == "appropriate":
        return AppropriateCardAction(
            allowed_categories=[CardCategory(c) for c in a.get("categories", [])],
            allowed_source_decks=a.get("source_decks", []),
            include_main_deck=a.get("include_main_deck", False),
            count=a.get("count", 1),
        )
    if at == "gain_per_label":
        return GainPerLabelAction(
            label=a["label"],
            resource_type=_RESOURCE_TYPE_BY_NAME[a["resource_type"]],
        )
    if at == "gain_per_category":
        return GainPerCategoryAction(
            category=CardCategory(a["category"]),
            resource_type=_RESOURCE_TYPE_BY_NAME[a["resource_type"]],
        )
    if at == "gain_resource":
        return GainResourceAction(
            resource_type=_RESOURCE_TYPE_BY_NAME[a["resource_type"]],
            amount=a.get("amount", 0),
        )
    if at == "draw_up_to_n_from_deck":
        return DrawUpToNFromDeckAction(count=a.get("count", 1))
    if at == "chronicle_from_discard":
        return ChronicleFromDiscardAction(optional=a.get("optional", False))
    if at == "chronicle_from_hand":
        return ChronicleFromHandAction(optional=a.get("optional", False))
    return None


def _parse_exploit_actions(data: dict) -> List:
    """Разбирает список exploit_actions из словаря данных карты."""
    actions = []
    for a in data.get("exploit_actions", []):
        action_type = a.get("type")
        if action_type == "play_from_discard":
            actions.append(PlayFromDiscardAction(
                allowed_categories=[CardCategory(c) for c in a.get("categories", [])],
                count=a.get("count", 1),
                cost_action=a.get("cost_action", 0),
            ))
        elif action_type == "sacred_path_exploit":
            actions.append(SacredPathExploitAction())
        elif action_type == "all_players_gain_resource":
            rt_name = a["resource_type"]
            if rt_name not in _RESOURCE_TYPE_BY_NAME:
                raise ValueError(f"Неизвестный ResourceType: '{rt_name}'")
            actions.append(AllPlayersGainResourceAction(
                resource_type=_RESOURCE_TYPE_BY_NAME[rt_name],
                amount=a.get("amount", 1),
            ))
        elif action_type == "spend_resource_draw_card":
            rt_name = a["resource_type"]
            if rt_name not in _RESOURCE_TYPE_BY_NAME:
                raise ValueError(f"Неизвестный ResourceType: '{rt_name}'")
            actions.append(ExploitSpendResourceDrawCardAction(
                resource_type=_RESOURCE_TYPE_BY_NAME[rt_name],
                resource_cost=a.get("resource_cost", 1),
                draw_count=a.get("draw_count", 1),
            ))
        elif action_type == "gain_resource":
            rt_name = a["resource_type"]
            if rt_name not in _RESOURCE_TYPE_BY_NAME:
                raise ValueError(f"Неизвестный ResourceType: '{rt_name}'")
            actions.append(GainResourceAction(
                resource_type=_RESOURCE_TYPE_BY_NAME[rt_name],
                amount=a.get("amount", 1),
            ))
        elif action_type == "recall_label_choice":
            actions.append(ExploitRecallLabelChoiceAction(options=a.get("options", [])))
    return actions


def _parse_solstice_actions(data: dict) -> List:
    """Разбирает список solstice_actions из словаря данных карты."""
    actions = []
    for a in data.get("solstice_actions", []):
        action_type = a.get("type")
        if action_type == "optional_gain_progress_then_fate":
            actions.append(SolsticeOptionalGainProgressThenFateAction(
                amount=a.get("amount", 2),
            ))
        elif action_type == "optional_discard_hand_return_disorder":
            actions.append(SolsticeOptionalDiscardHandReturnDisorderAction())
        elif action_type == "choice":
            actions.append(SolsticeChoiceAction(options=a.get("options", [])))
        elif action_type == "optional_discard_hand_for_choice":
            actions.append(SolsticeOptionalDiscardForChoiceAction(options=a.get("options", [])))
    return actions


def _parse_on_play_actions(data: dict) -> List:
    actions = []
    for a in data.get("on_play_actions", []):
        action_type = a.get("type")
        if action_type == "acquire_from_market":
            actions.append(AcquireCardAction(
                allowed_categories=[CardCategory(c) for c in a.get("categories", [])],
                count=a.get("count", 1),
            ))
        elif action_type == "appropriate":
            actions.append(AppropriateCardAction(
                allowed_categories=[CardCategory(c) for c in a.get("categories", [])],
                allowed_source_decks=a.get("source_decks", []),
                include_main_deck=a.get("include_main_deck", False),
                count=a.get("count", 1),
            ))
        elif action_type == "choice":
            options = []
            for opt in a.get("options", []):
                inner = _parse_choice_action_inner(opt.get("action", {}))
                if inner is not None:
                    options.append(ChoiceOption(
                        label=opt.get("label", ""),
                        cost_population=opt.get("cost_population", 0),
                        cost_resource=opt.get("cost_resource", 0),
                        action=inner,
                        opponent_gains_progress=opt.get("opponent_gains_progress", 0),
                        opponent_recalls_region=opt.get("opponent_recalls_region", 0),
                    ))
            actions.append(ChoiceAction(options=options))
        elif action_type == "steal_resource":
            rt_name = a["resource_type"]
            if rt_name not in _RESOURCE_TYPE_BY_NAME:
                raise ValueError(f"Неизвестный ResourceType: '{rt_name}'")
            actions.append(StealResourceAction(
                resource_type=_RESOURCE_TYPE_BY_NAME[rt_name],
                amount=a.get("amount", 1),
            ))
        elif action_type == "return_exploit_token_optional":
            actions.append(ReturnExploitTokenOptionalAction())
        elif action_type == "draw_from_deck_optional":
            actions.append(DrawFromDeckOptionalAction())
        elif action_type == "chronicle_from_discard":
            actions.append(ChronicleFromDiscardAction(
                optional=a.get("optional", False),
            ))
        elif action_type == "exile_from_market":
            actions.append(ExileFromMarketAction())
        elif action_type == "destroy_from_play_area":
            actions.append(DestroyFromPlayAreaAction(
                category=CardCategory(a["category"]),
                count=a.get("count", 1),
            ))
        elif action_type == "look_at_glory_deck":
            actions.append(LookAtGloryDeckAction(
                look_count=a.get("look_count", 2),
                take_count=a.get("take_count", 1),
            ))
        elif action_type == "move_discard_to_deck":
            actions.append(MoveDiscardToDeckAction(
                optional=a.get("optional", True),
            ))
        elif action_type == "draw_up_to_n_from_deck":
            actions.append(DrawUpToNFromDeckAction(count=a.get("count", 3)))
        elif action_type == "return_card_to_deck_top":
            actions.append(ReturnCardToDeckTopAction())
        elif action_type == "spend_resource":
            rt_name = a["resource_type"]
            if rt_name not in _RESOURCE_TYPE_BY_NAME:
                raise ValueError(f"Неизвестный ResourceType: '{rt_name}'")
            actions.append(SpendResourceAction(
                resource_type=_RESOURCE_TYPE_BY_NAME[rt_name],
                amount=a["amount"],
            ))
        elif action_type == "bot_gains_disorder":
            actions.append(BotGainsDisorderAction(count=a.get("count", 1)))
        elif action_type == "draw_then_discard_choice":
            actions.append(DrawThenDiscardChoiceAction(
                draw_count=a.get("draw_count", 2),
                discard_count=a.get("discard_count", 1),
            ))
        elif action_type == "guess_main_deck_category":
            actions.append(GuessDeckCategoryAction(
                allowed_categories=a.get("allowed_categories",
                                         ["region", "origins", "civilization", "raid"]),
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
    categories: List[CardCategory] = field(default_factory=list)  # категории карты (пусто для нац. карт)
    period: Optional[Period] = None  # ограничение по периоду
    # VP
    vp_fixed: int = 0               # фиксированные ПО (X)
    vp_condition: Optional[str] = None   # условные ПО (?)
    vp_per_condition: Optional[str] = None  # ПО за каждое выполнение (*) max 10
    vp_per_condition_value: int = 1         # множитель ПО за одно выполнение условия
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
    # Labels (метки на карте: зерно, вода, деньги)
    labels: List[CardLabel] = field(default_factory=list)
    # Reinforcement
    can_be_reinforced: bool = False  # можно укрепить (добавить карту из руки поверх)
    cannot_be_reinforcement: bool = False  # карта НЕ МОЖЕТ быть картой укрепления
    # Chronicle
    sends_to_chronicle: int = 0   # отправляет N карт соперника/своих в летопись
    goes_to_chronicle: bool = False  # сама идёт в летопись после розыгрыша (обязательно, не в сброс)
    can_be_chronicled: bool = False  # игрок МОЖЕТ занести карту в летопись (на выбор)
    vp_in_chronicle: Optional[int] = None  # ПО если карта находится в летописи
    vp_out_of_chronicle: int = 0           # ПО если карта НЕ находится в летописи (только при vp_in_chronicle)
    # Actions executed on play
    on_play_actions: List[GainResourceAction] = field(default_factory=list)
    # Actions executed on exploitation
    exploit_actions: List = field(default_factory=list)
    # Passive effect triggered when exploit token is placed on this card
    exploit_passive: str = ""  # e.g. "grain_to_sack_3"
    # Actions executed on solstice
    solstice_actions: List = field(default_factory=list)

    def __hash__(self):
        return hash(self.id)

    def __eq__(self, other):
        return isinstance(other, Card) and self.id == other.id

    def __repr__(self):
        return f"Card({self.id}: {self.name})"


@dataclass
class BaseCard(Card):
    """Карта базовой колоды (регионы, истоки, цивилизации, набеги, слава, беспорядки)."""

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
    labels = [CardLabel(lb) for lb in data.get("labels", [])]

    return BaseCard(
        id=card_id,
        name=data["name"],
        categories=categories,
        card_type=card_type,
        period=period,
        vp_fixed=data.get("vp_fixed", 0),
        vp_condition=data.get("vp_condition"),
        vp_per_condition=data.get("vp_per_condition"),
        vp_per_condition_value=data.get("vp_per_condition_value", 1),
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
        labels=labels,
        can_be_reinforced=data.get("can_be_reinforced", False),
        cannot_be_reinforcement=data.get("cannot_be_reinforcement", False),
        sends_to_chronicle=data.get("sends_to_chronicle", 0),
        goes_to_chronicle=data.get("goes_to_chronicle", False),
        can_be_chronicled=data.get("can_be_chronicled", False),
        vp_in_chronicle=data.get("vp_in_chronicle"),
        vp_out_of_chronicle=data.get("vp_out_of_chronicle", 0),
        on_play_actions=_parse_on_play_actions(data),
        exploit_actions=_parse_exploit_actions(data),
        solstice_actions=_parse_solstice_actions(data),
        exploit_passive=data.get("exploit_passive", ""),
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
    categories = [CardCategory(c) for c in data.get("categories", [])]
    labels = [CardLabel(lb) for lb in data.get("labels", [])]

    return NationCard(
        id=card_id,
        name=data["name"],
        nation=nation,
        subtype=subtype,
        card_type=card_type,
        period=period,
        categories=categories,
        vp_fixed=data.get("vp_fixed", 0),
        vp_condition=data.get("vp_condition"),
        vp_per_condition=data.get("vp_per_condition"),
        vp_per_condition_value=data.get("vp_per_condition_value", 1),
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
        labels=labels,
        can_be_reinforced=data.get("can_be_reinforced", False),
        cannot_be_reinforcement=data.get("cannot_be_reinforcement", False),
        sends_to_chronicle=data.get("sends_to_chronicle", 0),
        goes_to_chronicle=data.get("goes_to_chronicle", False),
        can_be_chronicled=data.get("can_be_chronicled", False),
        vp_in_chronicle=data.get("vp_in_chronicle"),
        vp_out_of_chronicle=data.get("vp_out_of_chronicle", 0),
        on_play_actions=_parse_on_play_actions(data),
        exploit_actions=_parse_exploit_actions(data),
        solstice_actions=_parse_solstice_actions(data),
        exploit_passive=data.get("exploit_passive", ""),
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
