"""
Card model and all card definitions for Imperium: Classics
"""
from dataclasses import dataclass, field
from typing import Optional, List
from .enums import CardCategory, CardSubtype, CardType, RegionType, Period, Nation


@dataclass
class Card:
    id: str                          # уникальный буквенно-числовой номер
    name: str
    nation: Optional[Nation] = None  # None = базовая колода
    card_type: CardType = CardType.NORMAL
    period: Optional[Period] = None  # ограничение по периоду
    region_types: List[RegionType] = field(default_factory=list)
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
        if self.region_types:
            self.card_type = CardType.PERMANENT


# ──────────────────────────────────────────────────
# BASE DECK — shared cards (Классика)
# ──────────────────────────────────────────────────
def build_base_deck_classics() -> List[Card]:
    cards: List[Card] = []

    # ── REGIONS (REG) ──────────────────────────────
    reg_cards = [
        BaseCard("1REG1", "Плодородные земли", categories=[CardCategory.REGION],
             region_types=[RegionType.LAND], vp_fixed=1, requires_action_token=True),
        BaseCard("1REG2", "Речная долина", categories=[CardCategory.REGION],
             region_types=[RegionType.LAND], vp_fixed=1, is_exploit=True, solstice_effect=False),
        BaseCard("1REG3", "Горный перевал", categories=[CardCategory.REGION],
             region_types=[RegionType.MOUNTAIN], vp_fixed=1),
        BaseCard("1REG4", "Прибрежные земли", categories=[CardCategory.REGION],
             region_types=[RegionType.SEA], vp_fixed=1),
        BaseCard("1REG5", "Пойма реки", categories=[CardCategory.REGION],
             region_types=[RegionType.LAND], vp_fixed=2, is_exploit=True),
        BaseCard("1REG6", "Степи", categories=[CardCategory.REGION],
             region_types=[RegionType.LAND], vp_fixed=1),
        BaseCard("1REG7", "Оазис", categories=[CardCategory.REGION],
             region_types=[RegionType.LAND], vp_fixed=2, is_exploit=True),
        BaseCard("1REG8", "Острова", categories=[CardCategory.REGION],
             region_types=[RegionType.SEA], vp_fixed=2),
        BaseCard("1REG9", "Горная крепость", categories=[CardCategory.REGION],
             region_types=[RegionType.MOUNTAIN], vp_fixed=2),
        BaseCard("1REG10", "Морской путь", categories=[CardCategory.REGION],
             region_types=[RegionType.SEA], vp_fixed=1),
        BaseCard("1REG11", "Лесные угодья", categories=[CardCategory.REGION],
             region_types=[RegionType.LAND], vp_fixed=1),
        BaseCard("1REG12", "Болота", categories=[CardCategory.REGION],
             region_types=[RegionType.LAND], vp_fixed=0),
        BaseCard("1REG13", "Побережье", categories=[CardCategory.REGION],
             region_types=[RegionType.SEA], vp_fixed=1),
        BaseCard("1REG14", "Священный путь", categories=[CardCategory.REGION],
             region_types=[RegionType.LAND], vp_fixed=2),
    ]
    cards.extend(reg_cards)

    # ── ORIGINS (IST) ──────────────────────────────
    ist_cards = [
        BaseCard("1IST1", "Регулярная армия", categories=[CardCategory.ORIGINS],
             passive_effect=True, vp_fixed=1),
        BaseCard("1IST2", "Земледелие", categories=[CardCategory.ORIGINS], vp_fixed=1),
        BaseCard("1IST3", "Скотоводство", categories=[CardCategory.ORIGINS], vp_fixed=1),
        BaseCard("1IST4", "Металлургия", categories=[CardCategory.ORIGINS], vp_fixed=1),
        BaseCard("1IST5", "Торговля", categories=[CardCategory.ORIGINS], vp_fixed=1),
        BaseCard("1IST6", "Мореплавание", categories=[CardCategory.ORIGINS], vp_fixed=1),
        BaseCard("1IST7", "Письменность", categories=[CardCategory.ORIGINS], vp_fixed=2),
        BaseCard("1IST8", "Строительство", categories=[CardCategory.ORIGINS], vp_fixed=1),
        BaseCard("1IST9", "Лодки", categories=[CardCategory.ORIGINS], vp_fixed=1),
        BaseCard("1IST10", "Порт", categories=[CardCategory.ORIGINS], vp_fixed=1),
        BaseCard("1IST11", "Процветание",
             categories=[CardCategory.ORIGINS, CardCategory.CIVILIZATION],
             vp_fixed=2, is_exploit=True),
        BaseCard("1IST12", "Колодец-журавль", categories=[CardCategory.ORIGINS],
             vp_fixed=2, is_exploit=True, passive_effect=True),
        BaseCard("1IST13", "Гончарное дело", categories=[CardCategory.ORIGINS], vp_fixed=1),
        BaseCard("1IST14", "Одомашнивание", categories=[CardCategory.ORIGINS],
             vp_fixed=1, is_exploit=True, passive_effect=True),
        BaseCard("1IST15", "Медицина", categories=[CardCategory.ORIGINS], vp_fixed=2),
        BaseCard("1IST16", "Астрономия", categories=[CardCategory.ORIGINS], vp_fixed=2),
        BaseCard("1IST17", "Математика", categories=[CardCategory.ORIGINS], vp_fixed=2),
        BaseCard("1IST18", "Философия", categories=[CardCategory.ORIGINS], vp_fixed=2),
        BaseCard("1IST19", "Музыка", categories=[CardCategory.ORIGINS], vp_fixed=1),
        BaseCard("1IST20", "Охота", categories=[CardCategory.ORIGINS], vp_fixed=1),
        BaseCard("1IST21", "Рыболовство", categories=[CardCategory.ORIGINS], vp_fixed=1),
        BaseCard("1IST22", "Поселенцы", categories=[CardCategory.ORIGINS], vp_fixed=1),
    ]
    cards.extend(ist_cards)

    # ── CIVILIZATION (CIV) ─────────────────────────
    civ_cards = [
        BaseCard("1CIV1", "Величие", categories=[CardCategory.CIVILIZATION],
             period=Period.CIVILIZATION, vp_fixed=3),
        BaseCard("1CIV2", "Образование", categories=[CardCategory.CIVILIZATION],
             period=Period.CIVILIZATION, vp_fixed=2, requires_action_token=False),
        BaseCard("1CIV3", "Реформы", categories=[CardCategory.CIVILIZATION],
             period=Period.CIVILIZATION, vp_fixed=2),
        BaseCard("1CIV4", "Завоевание", categories=[CardCategory.CIVILIZATION],
             period=Period.CIVILIZATION, vp_fixed=2),
        BaseCard("1CIV5", "Архитектура", categories=[CardCategory.CIVILIZATION],
             period=Period.CIVILIZATION, vp_fixed=3),
        BaseCard("1CIV6", "Метрополия", categories=[CardCategory.CIVILIZATION],
             period=Period.CIVILIZATION, vp_fixed=2, solstice_effect=True, is_exploit=False),
        BaseCard("1CIV7", "Законы", categories=[CardCategory.CIVILIZATION],
             period=Period.CIVILIZATION, vp_fixed=2),
        BaseCard("1CIV8", "Торговый путь", categories=[CardCategory.CIVILIZATION],
             period=Period.CIVILIZATION, vp_fixed=2),
        BaseCard("1CIV9", "Дипломатия", categories=[CardCategory.CIVILIZATION],
             period=Period.CIVILIZATION, vp_fixed=2),
        BaseCard("1CIV10", "Флот", categories=[CardCategory.CIVILIZATION],
             period=Period.CIVILIZATION, vp_fixed=2),
        BaseCard("1CIV11", "Легион", categories=[CardCategory.CIVILIZATION],
             period=Period.CIVILIZATION, vp_fixed=2, card_type=CardType.ATTACK),
        BaseCard("1CIV12", "Наёмники", categories=[CardCategory.CIVILIZATION],
             period=Period.CIVILIZATION, vp_fixed=1),
        BaseCard("1CIV13", "Таран", categories=[CardCategory.CIVILIZATION],
             period=Period.CIVILIZATION, vp_fixed=1, card_type=CardType.ATTACK),
        BaseCard("1CIV14", "Рынок", categories=[CardCategory.CIVILIZATION],
             period=Period.CIVILIZATION, vp_fixed=2),
        BaseCard("1CIV15", "Акрополь", categories=[CardCategory.CIVILIZATION],
             period=Period.CIVILIZATION, vp_fixed=3, solstice_effect=True),
    ]
    cards.extend(civ_cards)

    # ── RAIDS (NAB) ────────────────────────────────
    # Раздел набегов — карты народов, которых можно покорить
    raid_cards = [
        BaseCard("1NAB1", "Египтяне", categories=[CardCategory.RAID], vp_per_condition="region"),
        BaseCard("1NAB2", "Персы", categories=[CardCategory.RAID], vp_per_condition="population"),
        BaseCard("1NAB3", "Греки", categories=[CardCategory.RAID], vp_per_condition="civilization"),
        BaseCard("1NAB4", "Кельты", categories=[CardCategory.RAID], vp_per_condition="origins"),
        BaseCard("1NAB5", "Империя Цинь", categories=[CardCategory.RAID], passive_effect=True,
             vp_per_condition="region"),
        BaseCard("1NAB6", "Карфагеняне", categories=[CardCategory.RAID], vp_per_condition="resource"),
        BaseCard("1NAB7", "Шумеры", categories=[CardCategory.RAID], vp_fixed=3),
        BaseCard("1NAB8", "Ассирийцы", categories=[CardCategory.RAID], vp_per_condition="attack"),
        BaseCard("1NAB9", "Хетты", categories=[CardCategory.RAID], vp_fixed=2),
        BaseCard("1NAB10", "Финикийцы", categories=[CardCategory.RAID], vp_per_condition="sea"),
        BaseCard("1NAB11", "Скифы", categories=[CardCategory.RAID], vp_per_condition="region"),
    ]
    cards.extend(raid_cards)

    # ── GLORY (SLV) ────────────────────────────────
    glory_cards = [
        BaseCard("1SLV1", "Триумф", categories=[CardCategory.GLORY], vp_fixed=3),
        BaseCard("1SLV2", "Великий поход", categories=[CardCategory.GLORY], vp_per_condition="region"),
        BaseCard("1SLV3", "Золотой век", categories=[CardCategory.GLORY], vp_per_condition="civilization"),
        BaseCard("1SLV4", "Мировое господство", categories=[CardCategory.GLORY], vp_per_condition="region"),
        BaseCard("1SLV5", "Наследие", categories=[CardCategory.GLORY], vp_per_condition="origins"),
        BaseCard("1SLV6", "Эпоха процветания", categories=[CardCategory.GLORY], vp_per_condition="resource"),
        BaseCard("1SLV7", "Слава народа", categories=[CardCategory.GLORY], vp_per_condition="population"),
        BaseCard("1SLV8", "Легенда", categories=[CardCategory.GLORY], vp_per_condition="glory"),
        BaseCard("1SLV9A", "Царь царей", categories=[CardCategory.GLORY],
             solstice_effect=True, vp_condition="period_based"),
    ]
    cards.extend(glory_cards)

    # ── DISORDER (BES) ─────────────────────────────
    disorder_cards = []
    for i in range(1, 13):
        disorder_cards.append(
            BaseCard(f"1BES{i}", "Беспорядки",
                 card_type=CardType.DISORDER,
                 start_location="reserve",
                 vp_penalty=1)
        )
    cards.extend(disorder_cards)

    return cards


# ──────────────────────────────────────────────────
# NATION DECKS — Классика
# ──────────────────────────────────────────────────

def build_vikings_deck() -> List[Card]:
    cards = []
    # Ability card
    cards.append(NationCard("1VIK1A", "Викинги (A)", nation=Nation.VIKINGS,
                       subtype=CardSubtype.ABILITY,
                       passive_effect=True, vp_fixed=1, start_location="ability",
                       vp_condition="per_region"))
    cards.append(NationCard("1VIK1B", "Викинги (B)", nation=Nation.VIKINGS,
                       subtype=CardSubtype.ABILITY,
                       passive_effect=True, vp_fixed=2, start_location="ability",
                       vp_condition="per_region"))
    # Transformation (зенит — для викингов)
    cards.append(NationCard("1VIK_ZENITH", "Харальд III Суровый", nation=Nation.VIKINGS,
                       subtype=CardSubtype.TRANSFORMATION,
                       start_location="transformation", vp_fixed=4))
    # Boost cards
    for i in range(1, 8):
        cards.append(NationCard(f"1VIK_B{i}", f"Сага {i}", nation=Nation.VIKINGS,
                           subtype=CardSubtype.BOOST,
                           start_location="boost", vp_fixed=1))
    # Progress cards
    for i in range(1, 4):
        cards.append(NationCard(f"1VIK_P{i}", f"Завоевание {i}", nation=Nation.VIKINGS,
                           card_type=CardType.PERMANENT,
                           period=Period.BARBARISM,
                           start_location="progress",
                           progress_cost_resource=2 + i,
                           vp_fixed=i))
    # Main deck cards
    main_cards_data = [
        ("1VIK2", "Драккар", Period.BARBARISM, [CardCategory.ORIGINS], False, 1),
        ("1VIK3", "Набег", Period.BARBARISM, [CardCategory.RAID], True, 1),
        ("1VIK4", "Готия", Period.BARBARISM, [CardCategory.REGION], False, 2),
        ("1VIK5", "Рунический камень", Period.BARBARISM, [CardCategory.GLORY], False, 1),
        ("1VIK6", "Ярл", Period.BARBARISM, [CardCategory.ORIGINS], False, 1),
        ("1VIK7", "Валькирия", Period.BARBARISM, [CardCategory.GLORY], False, 2),
        ("1VIK8", "Берсерк", Period.BARBARISM, [CardCategory.ORIGINS], True, 0),
        ("1VIK9", "Норвегия", Period.BARBARISM, [CardCategory.REGION], False, 2),
        ("1VIK10", "Альтинг", Period.BARBARISM, [CardCategory.ORIGINS], False, 1),
        ("1VIK11", "Торговый путь", Period.BARBARISM, [CardCategory.ORIGINS], False, 1),
        ("1VIK12", "Лейф Эрикссон", Period.BARBARISM, [CardCategory.GLORY], False, 3),
        ("1VIK13", "Шведская гвардия", Period.BARBARISM, [CardCategory.RAID], True, 1),
        ("1VIK14", "Олаф Харальдссон", Period.BARBARISM, [CardCategory.GLORY], False, 3),
    ]
    for cid, cname, cperiod, _, cattack, cvp in main_cards_data:
        ct = CardType.ATTACK if cattack else CardType.NORMAL
        cards.append(NationCard(cid, cname, nation=Nation.VIKINGS,
                           period=cperiod,
                           card_type=ct, vp_fixed=cvp))
    return cards


def build_greeks_deck() -> List[Card]:
    cards = []
    cards.append(NationCard("1GRE1A", "Греки (A)", nation=Nation.GREEKS,
                       subtype=CardSubtype.ABILITY, passive_effect=True,
                       vp_fixed=1, start_location="ability"))
    cards.append(NationCard("1GRE1B", "Греки (B)", nation=Nation.GREEKS,
                       subtype=CardSubtype.ABILITY, passive_effect=True,
                       vp_fixed=1, start_location="ability"))
    cards.append(NationCard("1GRE_TR", "Трансформация греков", nation=Nation.GREEKS,
                       subtype=CardSubtype.TRANSFORMATION, start_location="transformation"))
    for i in range(1, 6):
        cards.append(NationCard(f"1GRE_B{i}", f"Греческое усиление {i}", nation=Nation.GREEKS,
                           subtype=CardSubtype.BOOST, start_location="boost"))
    for i in range(1, 4):
        cards.append(NationCard(f"1GRE_P{i}", f"Греческий прогресс {i}", nation=Nation.GREEKS,
                           period=Period.CIVILIZATION, start_location="progress",
                           progress_cost_resource=i + 2, progress_cost_upgrade=i,
                           vp_fixed=i + 1))
    greek_mains = [
        ("1GRE2", "Поселенцы", Period.BARBARISM, CardCategory.ORIGINS, 1),
        ("1GRE3", "Греческие наёмники", Period.BARBARISM, CardCategory.ORIGINS, 1),
        ("1GRE4", "Спарта", Period.BARBARISM, CardCategory.REGION, 2),
        ("1GRE5", "Афины", Period.BARBARISM, CardCategory.REGION, 3),
        ("1GRE6", "Олимпийские игры", Period.BARBARISM, CardCategory.GLORY, 2),
        ("1GRE7", "Александрийский маяк", Period.CIVILIZATION, CardCategory.GLORY, 3),
        ("1GRE8", "Акрополь", Period.CIVILIZATION, CardCategory.CIVILIZATION, 3),
        ("1GRE9", "Философская школа", Period.CIVILIZATION, CardCategory.CIVILIZATION, 2),
        ("1GRE10", "Изобретения", Period.CIVILIZATION, CardCategory.CIVILIZATION, 2),
        ("1GRE11", "Македонская фаланга", Period.BARBARISM, CardCategory.RAID, 1),
        ("1GRE12", "Морская битва", Period.BARBARISM, CardCategory.GLORY, 2),
        ("1GRE13", "Дельфийский оракул", Period.BARBARISM, CardCategory.GLORY, 2),
    ]
    for cid, cname, cp, _, cvp in greek_mains:
        cards.append(NationCard(cid, cname, nation=Nation.GREEKS,
                           period=cp, vp_fixed=cvp))
    return cards


def build_carthaginians_deck() -> List[Card]:
    cards = []
    cards.append(NationCard("1KAR1A", "Карфагеняне (A)", nation=Nation.CARTHAGINIANS,
                       subtype=CardSubtype.ABILITY, passive_effect=True,
                       vp_fixed=1, start_location="ability"))
    cards.append(NationCard("1KAR1B", "Карфагеняне (B)", nation=Nation.CARTHAGINIANS,
                       subtype=CardSubtype.ABILITY, passive_effect=True,
                       vp_fixed=1, start_location="ability"))
    cards.append(NationCard("1KAR_TR", "Трансформация карфагенян", nation=Nation.CARTHAGINIANS,
                       subtype=CardSubtype.TRANSFORMATION, start_location="transformation"))
    for i in range(1, 6):
        cards.append(NationCard(f"1KAR_B{i}", f"Карфагенское усиление {i}", nation=Nation.CARTHAGINIANS,
                           subtype=CardSubtype.BOOST, start_location="boost"))
    for i in range(1, 4):
        cards.append(NationCard(f"1KAR_P{i}", f"Карфагенский прогресс {i}",
                           nation=Nation.CARTHAGINIANS,
                           period=Period.CIVILIZATION,
                           start_location="progress", progress_cost_resource=i + 2,
                           vp_fixed=i + 1))
    carth_mains = [
        ("1KAR2", "Торговый каравана", Period.BARBARISM, CardCategory.ORIGINS, 1),
        ("1KAR3", "Торговые суда", Period.BARBARISM, CardCategory.ORIGINS, 1),
        ("1KAR4", "Карфаген", Period.BARBARISM, CardCategory.REGION, 2),
        ("1KAR5", "Монополия на торговлю", Period.CIVILIZATION, CardCategory.CIVILIZATION, 3),
        ("1KAR6", "Ганнибал", Period.CIVILIZATION, CardCategory.GLORY, 4),
        ("1KAR7", "Боевые слоны", Period.CIVILIZATION, CardCategory.CIVILIZATION, 2),
        ("1KAR8", "Пунические войны", Period.CIVILIZATION, CardCategory.RAID, 2),
        ("1KAR9", "Сицилия", Period.BARBARISM, CardCategory.REGION, 2),
        ("1KAR10", "Иберия", Period.BARBARISM, CardCategory.REGION, 2),
        ("1KAR11", "Северная Африка", Period.BARBARISM, CardCategory.REGION, 2),
        ("1KAR12", "Карфагенский флот", Period.CIVILIZATION, CardCategory.CIVILIZATION, 2),
    ]
    for cid, cname, cp, _, cvp in carth_mains:
        cards.append(NationCard(cid, cname, nation=Nation.CARTHAGINIANS,
                           period=cp, vp_fixed=cvp))
    return cards


def build_celts_deck() -> List[Card]:
    cards = []
    cards.append(NationCard("1KEL1A", "Кельты (A)", nation=Nation.CELTS,
                       subtype=CardSubtype.ABILITY, is_exploit=True,
                       vp_fixed=1, start_location="ability"))
    cards.append(NationCard("1KEL1B", "Кельты (B)", nation=Nation.CELTS,
                       subtype=CardSubtype.ABILITY, is_exploit=True,
                       vp_fixed=1, start_location="ability"))
    cards.append(NationCard("1KEL_TR", "Трансформация кельтов", nation=Nation.CELTS,
                       subtype=CardSubtype.TRANSFORMATION, start_location="transformation"))
    for i in range(1, 6):
        cards.append(NationCard(f"1KEL_B{i}", f"Кельтское усиление {i}", nation=Nation.CELTS,
                           subtype=CardSubtype.BOOST, start_location="boost"))
    for i in range(1, 4):
        cards.append(NationCard(f"1KEL_P{i}", f"Кельтский прогресс {i}", nation=Nation.CELTS,
                           start_location="progress",
                           progress_cost_resource=i + 2, progress_cost_population=i,
                           vp_fixed=i))
    celt_mains = [
        ("1KEL2", "Друиды", Period.BARBARISM, CardCategory.ORIGINS, 1),
        ("1KEL3", "Угон скота", Period.BARBARISM, CardCategory.ORIGINS, 0),
        ("1KEL4", "Керидвен", Period.BARBARISM, CardCategory.GLORY, 2),
        ("1KEL5", "Галлия", Period.BARBARISM, CardCategory.REGION, 2),
        ("1KEL6", "Британия", Period.BARBARISM, CardCategory.REGION, 2),
        ("1KEL7", "Кельтский вождь", Period.BARBARISM, CardCategory.ORIGINS, 1),
        ("1KEL8", "Военная хитрость", Period.BARBARISM, CardCategory.ORIGINS, 0),
        ("1KEL9", "Железный меч", Period.BARBARISM, CardCategory.ORIGINS, 1),
        ("1KEL10", "Кельтские колесницы", Period.BARBARISM, CardCategory.RAID, 1),
        ("1KEL11", "Верцингеториг", Period.BARBARISM, CardCategory.GLORY, 3),
        ("1KEL12", "Оппидум", Period.CIVILIZATION, CardCategory.CIVILIZATION, 2),
        ("1KEL13", "Боудика", Period.CIVILIZATION, CardCategory.GLORY, 3),
    ]
    for cid, cname, cp, _, cvp in celt_mains:
        cards.append(NationCard(cid, cname, nation=Nation.CELTS,
                           period=cp, vp_fixed=cvp))
    # Beспорядки specific to celts (reserve cards that go to disorder deck)
    for i in range(1, 4):
        cards.append(NationCard(f"1KEL_DIS{i}", "Беспорядки кельтов", nation=Nation.CELTS,
                           start_location="reserve",
                           vp_penalty=1))
    return cards


def build_macedonians_deck() -> List[Card]:
    cards = []
    cards.append(NationCard("1MAK1A", "Македоняне (A)", nation=Nation.MACEDONIANS,
                       subtype=CardSubtype.ABILITY, passive_effect=True,
                       vp_fixed=1, start_location="ability"))
    cards.append(NationCard("1MAK1B", "Македоняне (B)", nation=Nation.MACEDONIANS,
                       subtype=CardSubtype.ABILITY, passive_effect=True,
                       vp_fixed=2, start_location="ability"))

    cards.append(NationCard("1MAK2", "Царевич Александр", nation=Nation.MACEDONIANS,
                            subtype=CardSubtype.TRANSFORMATION))
    cards.append(NationCard("1MAK3", "Развитие", nation=Nation.MACEDONIANS,
                            subtype=CardSubtype.PROGRESS, period=Period.CIVILIZATION, vp_fixed=3))
    cards.append(NationCard("1MAK4", "Парменион", nation=Nation.MACEDONIANS,
                            subtype=CardSubtype.PROGRESS, period=Period.CIVILIZATION, vp_fixed=2))
    cards.append(NationCard("1MAK5", "Гейтары", nation=Nation.MACEDONIANS,
                            subtype=CardSubtype.PROGRESS, period=Period.CIVILIZATION, vp_fixed=3))
    cards.append(NationCard("1MAK6", "Александр Македонский", nation=Nation.MACEDONIANS,
                            subtype=CardSubtype.PROGRESS, period=Period.CIVILIZATION, vp_fixed=4))
    cards.append(NationCard("1MAK7", "Мозаики", nation=Nation.MACEDONIANS,
                            subtype=CardSubtype.PROGRESS, period=Period.CIVILIZATION, vp_fixed=3))
    cards.append(NationCard("1MAK8", "Александрия Египетская", nation=Nation.MACEDONIANS,
                            subtype=CardSubtype.PROGRESS, period=Period.CIVILIZATION, vp_fixed=2))
    cards.append(NationCard("1MAK9", "Александрия в Ариане", nation=Nation.MACEDONIANS,
                            subtype=CardSubtype.PROGRESS, period=Period.CIVILIZATION))
    cards.append(NationCard("1MAK10", "Процветание", nation=Nation.MACEDONIANS,
                            subtype=CardSubtype.BOOST))
    cards.append(NationCard("1MAK11", "Уксии", nation=Nation.MACEDONIANS,
                            subtype=CardSubtype.BOOST, card_type=CardType.ATTACK))
    cards.append(NationCard("1MAK12", "Величие", nation=Nation.MACEDONIANS,
                            subtype=CardSubtype.BOOST))
    cards.append(NationCard("1MAK13", "Реформы", nation=Nation.MACEDONIANS,
                            subtype=CardSubtype.START, period=Period.BARBARISM))
    cards.append(NationCard("1MAK14", "Завоевание", nation=Nation.MACEDONIANS,
                            subtype=CardSubtype.START, period=Period.BARBARISM))
    cards.append(NationCard("1MAK15", "Завоевание", nation=Nation.MACEDONIANS,
                            subtype=CardSubtype.START, period=Period.BARBARISM))
    cards.append(NationCard("1MAK16", "Филипп II Македонский", nation=Nation.MACEDONIANS,
                            subtype=CardSubtype.START, period=Period.BARBARISM))
    cards.append(NationCard("1MAK17", "Македонские фаланги", nation=Nation.MACEDONIANS,
                            subtype=CardSubtype.START, period=Period.BARBARISM))
    cards.append(NationCard("1MAK18", "Долина реки Альякмон", nation=Nation.MACEDONIANS,
                            subtype=CardSubtype.START, card_type=CardType.PERMANENT))
    cards.append(NationCard("1MAK19", "Орестида", nation=Nation.MACEDONIANS,
                            subtype=CardSubtype.START, card_type=CardType.PERMANENT))
    cards.append(NationCard("1MAK20", "Пелагония", nation=Nation.MACEDONIANS,
                            subtype=CardSubtype.START, card_type=CardType.PERMANENT))
    cards.append(NationCard("1MAK21", "Беспорядки", nation=Nation.MACEDONIANS,
                            subtype=CardSubtype.START, card_type=CardType.DISORDER))
    cards.append(NationCard("1MAK22", "Беспорядки", nation=Nation.MACEDONIANS,
                            subtype=CardSubtype.START, card_type=CardType.DISORDER))
    cards.append(NationCard("1MAK23", "Беспорядки", nation=Nation.MACEDONIANS,
                            subtype=CardSubtype.START, card_type=CardType.DISORDER))

    return cards


def build_persians_deck() -> List[Card]:
    cards = []
    cards.append(NationCard("1PER1A", "Персы (A)", nation=Nation.PERSIANS,
                       subtype=CardSubtype.ABILITY, passive_effect=True,
                       vp_fixed=1, start_location="ability"))
    cards.append(NationCard("1PER1B", "Персы (B)", nation=Nation.PERSIANS,
                       subtype=CardSubtype.ABILITY, passive_effect=True,
                       vp_fixed=1, start_location="ability"))
    cards.append(NationCard("1PER_TR", "Трансформация персов", nation=Nation.PERSIANS,
                       subtype=CardSubtype.TRANSFORMATION, start_location="transformation"))
    for i in range(1, 5):
        cards.append(NationCard(f"1PER_B{i}", f"Персидское усиление {i}", nation=Nation.PERSIANS,
                           subtype=CardSubtype.BOOST, start_location="boost"))
    for i in range(1, 4):
        cards.append(NationCard(f"1PER_P{i}", f"Персидский прогресс {i}", nation=Nation.PERSIANS,
                           period=Period.CIVILIZATION,
                           start_location="progress", progress_cost_resource=i + 2,
                           vp_fixed=i))
    per_mains = [
        ("1PER2", "Ахеменидская империя", Period.BARBARISM, CardCategory.REGION, 2),
        ("1PER3", "Золото персов", Period.CIVILIZATION, CardCategory.CIVILIZATION, 2),
        ("1PER4", "Таран", Period.CIVILIZATION, CardCategory.CIVILIZATION, 1),
        ("1PER5", "Персидские бессмертные", Period.BARBARISM, CardCategory.ORIGINS, 2),
        ("1PER6", "Царь царей (Персы)", Period.BARBARISM, CardCategory.GLORY, 4),
        ("1PER7", "Месопотамия", Period.BARBARISM, CardCategory.REGION, 2),
        ("1PER8", "Вавилон", Period.BARBARISM, CardCategory.REGION, 3),
        ("1PER9", "Персеполь", Period.CIVILIZATION, CardCategory.GLORY, 3),
        ("1PER10", "Дарий I", Period.CIVILIZATION, CardCategory.GLORY, 3),
        ("1PER11", "Ксеркс", Period.CIVILIZATION, CardCategory.GLORY, 3),
    ]
    for cid, cname, cp, _, cvp in per_mains:
        cards.append(NationCard(cid, cname, nation=Nation.PERSIANS,
                           period=cp, vp_fixed=cvp))
    return cards


def build_romans_deck() -> List[Card]:
    cards = []
    cards.append(NationCard("1RIM1A", "Римляне (A)", nation=Nation.ROMANS,
                       subtype=CardSubtype.ABILITY, passive_effect=True,
                       vp_fixed=1, start_location="ability", vp_condition="per_region"))
    cards.append(NationCard("1RIM1B", "Римляне (B)", nation=Nation.ROMANS,
                       subtype=CardSubtype.ABILITY, passive_effect=True,
                       vp_fixed=4, start_location="ability", vp_condition="per_region"))
    cards.append(NationCard("1RIM_TR", "Трансформация римлян", nation=Nation.ROMANS,
                       subtype=CardSubtype.TRANSFORMATION, start_location="transformation"))
    for i in range(1, 5):
        cards.append(NationCard(f"1RIM_B{i}", f"Римское усиление {i}", nation=Nation.ROMANS,
                           subtype=CardSubtype.BOOST, start_location="boost"))
    for i in range(1, 4):
        cards.append(NationCard(f"1RIM_P{i}", f"Римский прогресс {i}", nation=Nation.ROMANS,
                           period=Period.CIVILIZATION,
                           start_location="progress",
                           progress_cost_resource=i * 2,
                           progress_cost_upgrade=1,
                           vp_fixed=i + 1))
    roman_mains = [
        ("1RIM2", "Гай Юлий Цезарь", Period.BARBARISM, CardCategory.GLORY, False, 3),
        ("1RIM3", "Военная инженерия", Period.CIVILIZATION, CardCategory.CIVILIZATION, True, 2),
        ("1RIM4", "Легионы", Period.BARBARISM, CardCategory.ORIGINS, False, 1),
        ("1RIM5", "Рим", Period.BARBARISM, CardCategory.REGION, False, 3),
        ("1RIM6", "Галлия", Period.BARBARISM, CardCategory.REGION, False, 2),
        ("1RIM7", "Карфаген (набег)", Period.CIVILIZATION, CardCategory.RAID, True, 2),
        ("1RIM8", "Римская экспансия", Period.CIVILIZATION, CardCategory.RAID, True, 2),
        ("1RIM9", "Сенат", Period.CIVILIZATION, CardCategory.CIVILIZATION, False, 2),
        ("1RIM10", "Колизей", Period.CIVILIZATION, CardCategory.GLORY, False, 4),
        ("1RIM11", "Октавиан Август", Period.CIVILIZATION, CardCategory.GLORY, False, 4),
        ("1RIM12", "Пакс Романа", Period.CIVILIZATION, CardCategory.GLORY, False, 3),
        ("1RIM13", "Триумф (Рим)", Period.CIVILIZATION, CardCategory.GLORY, False, 2),
        ("1RIM14", "Преторианцы", Period.CIVILIZATION, CardCategory.CIVILIZATION, True, 1),
        ("1RIM15", "Цирк Максимус", Period.CIVILIZATION, CardCategory.CIVILIZATION, False, 2),
        ("1RIM16", "Аппиева дорога", Period.CIVILIZATION, CardCategory.ORIGINS, False, 2),
        ("1RIM17", "Веспасиан", Period.CIVILIZATION, CardCategory.GLORY, False, 2),
    ]
    for item in roman_mains:
        cid, cname, cp, _, cattack, cvp = item
        ct = CardType.ATTACK if cattack else CardType.NORMAL
        cards.append(NationCard(cid, cname, nation=Nation.ROMANS,
                           period=cp,
                           card_type=ct, vp_fixed=cvp))
    return cards


def build_scythians_deck() -> List[Card]:
    cards = []
    cards.append(NationCard("1SKF1A", "Скифы (A)", nation=Nation.SCYTHIANS,
                       subtype=CardSubtype.ABILITY, passive_effect=True,
                       vp_fixed=1, start_location="ability"))
    cards.append(NationCard("1SKF1B", "Скифы (B)", nation=Nation.SCYTHIANS,
                       subtype=CardSubtype.ABILITY, passive_effect=True,
                       vp_fixed=2, start_location="ability"))
    cards.append(NationCard("1SKF_TR", "Трансформация скифов", nation=Nation.SCYTHIANS,
                       subtype=CardSubtype.TRANSFORMATION, start_location="transformation"))
    for i in range(1, 5):
        cards.append(NationCard(f"1SKF_B{i}", f"Скифское усиление {i}", nation=Nation.SCYTHIANS,
                           subtype=CardSubtype.BOOST, start_location="boost"))
    for i in range(1, 4):
        cards.append(NationCard(f"1SKF_P{i}", f"Скифский прогресс {i}", nation=Nation.SCYTHIANS,
                           period=Period.CIVILIZATION,
                           start_location="progress", progress_cost_resource=i + 2,
                           vp_fixed=i + 1))
    scy_mains = [
        ("1SKF2", "Шатры", Period.BARBARISM, CardCategory.ORIGINS, 0),
        ("1SKF3", "Конные лучники", Period.BARBARISM, CardCategory.ORIGINS, 1),
        ("1SKF4", "Понтийская степь", Period.BARBARISM, CardCategory.REGION, 2),
        ("1SKF5", "Кочевники 1", Period.BARBARISM, CardCategory.ORIGINS, 1),
        ("1SKF6", "Великое переселение", Period.CIVILIZATION, CardCategory.GLORY, 3),
        ("1SKF7", "Степной путь", Period.BARBARISM, CardCategory.ORIGINS, 1),
        ("1SKF8", "Скифское золото", Period.CIVILIZATION, CardCategory.GLORY, 3),
        ("1SKF9", "Сарматы", Period.CIVILIZATION, CardCategory.RAID, 1),
        ("1SKF10", "Кочевой образ жизни", Period.BARBARISM, CardCategory.ORIGINS, 1),
        ("1SKF11", "Курган", Period.BARBARISM, CardCategory.GLORY, 2),
        ("1SKF12", "Торговля мехом", Period.BARBARISM, CardCategory.ORIGINS, 1),
    ]
    for cid, cname, cp, _, cvp in scy_mains:
        cards.append(NationCard(cid, cname, nation=Nation.SCYTHIANS,
                           period=cp, vp_fixed=cvp))
    # Кочевники — дублирующие карты (одна с символом атаки)
    cards.append(NationCard("1SKF14", "Кочевники", nation=Nation.SCYTHIANS,
                       period=Period.BARBARISM, vp_fixed=1))
    cards.append(NationCard("1SKF15", "Кочевники (атака)", nation=Nation.SCYTHIANS,
                       period=Period.BARBARISM,
                       card_type=CardType.ATTACK, vp_fixed=1))
    return cards


# ──────────────────────────────────────────────────
# CARD REGISTRY
# ──────────────────────────────────────────────────
_NATION_BUILDERS = {
    Nation.VIKINGS: build_vikings_deck,
    Nation.GREEKS: build_greeks_deck,
    Nation.CARTHAGINIANS: build_carthaginians_deck,
    Nation.CELTS: build_celts_deck,
    Nation.MACEDONIANS: build_macedonians_deck,
    Nation.PERSIANS: build_persians_deck,
    Nation.ROMANS: build_romans_deck,
    Nation.SCYTHIANS: build_scythians_deck,
}


def get_nation_deck(nation: Nation) -> List[Card]:
    builder = _NATION_BUILDERS.get(nation)
    if not builder:
        raise ValueError(f"Unknown nation: {nation}")
    return builder()


def get_all_available_nations():
    return list(Nation)
