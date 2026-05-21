"""
Game Setup — prepares a new solo game of Imperium: Classics
"""
import random
from typing import List
from .cards import (Card, build_base_deck_classics, get_nation_deck)
from .enums import Nation, Period, Difficulty, GamePhase, CardCategory, CardSubtype, CardType
from .state import (GameState, PlayerArea, BotArea, SharedArea,
                    MarketSlot, Resources)


# How many cards per category deck by player count
# Solo = 2 players equivalent → 6 cards each
SOLO_CARDS_PER_CATEGORY = 6


def setup_solo_game(
    player_nation: Nation,
    bot_nation: Nation,
    difficulty: Difficulty = Difficulty.EMPEROR,
    ability_side: str = "B"
) -> GameState:
    """
    Подготовка одиночной игры против бота.
    Следует правилам «Книги правил одиночной игры».
    """
    state = GameState(difficulty=difficulty)

    # ── 1. Build all card pools ────────────────────
    base_cards = build_base_deck_classics()
    player_nation_cards = get_nation_deck(player_nation)
    bot_nation_cards = get_nation_deck(bot_nation)

    # ── 2. Setup shared area ───────────────────────
    shared = _setup_shared_area(base_cards, player_nation, bot_nation)
    state.shared = shared

    # ── 3. Setup player personal area ─────────────
    player = _setup_player_area(player_nation, player_nation_cards,
                                 shared, ability_side)
    state.player = player

    # ── 4. Setup bot area ──────────────────────────
    bot = _setup_bot_area(bot_nation, bot_nation_cards, difficulty)
    state.bot = bot

    # ── 5. Difficulty adjustments ─────────────────
    _apply_difficulty_setup(state, difficulty)

    # ── 6. Initial hand ───────────────────────────
    _draw_to_hand(player, 5)

    state.phase = GamePhase.PLAYER_TURN
    state.add_log(f"Игра начата! Вы играете за {player_nation.value}, бот — за {bot_nation.value}")
    state.add_log(f"Уровень сложности: {difficulty.value}")

    return state


def _setup_shared_area(base_cards: List[Card],
                         player_nation: Nation,
                        bot_nation: Nation) -> SharedArea:
    shared = SharedArea()

    # Separate disorder cards
    disorder_cards = [c for c in base_cards if c.card_type == CardType.DISORDER]
    non_disorder = [c for c in base_cards if c.card_type != CardType.DISORDER]

    # Remove player_count-restricted cards (≥3 and 4-player cards)
    # In solo mode remove all multi-player restricted cards
    # (cards marked with player symbols — we flag them in card data as solo-compatible)
    eligible = [c for c in non_disorder]

    # Categorise
    regions = [c for c in eligible if CardCategory.REGION in c.categories
               and CardCategory.ORIGINS not in c.categories]
    origins = [c for c in eligible if CardCategory.ORIGINS in c.categories
               and CardCategory.CIVILIZATION not in c.categories]
    civs = [c for c in eligible if CardCategory.CIVILIZATION in c.categories
            and CardCategory.ORIGINS not in c.categories
            and CardCategory.REGION not in c.categories]
    raids = [c for c in eligible if CardCategory.RAID in c.categories]
    glorys = [c for c in eligible if CardCategory.GLORY in c.categories]

    # Remove raid cards that match played nations
    played = {player_nation.value.lower(), bot_nation.value.lower()}
    raids = [c for c in raids if c.name.lower() not in played]

    # Build decks for solo (6 cards each for reg/ist/civ per rulebook)
    random.shuffle(regions)
    random.shuffle(origins)
    random.shuffle(civs)
    shared.region_deck = regions[:SOLO_CARDS_PER_CATEGORY]
    shared.origins_deck = origins[:SOLO_CARDS_PER_CATEGORY]
    shared.civilization_deck = civs[:SOLO_CARDS_PER_CATEGORY]

    # Glory deck — remove 2 top cards for solo (per rulebook step 9)
    random.shuffle(glorys)
    king_of_kings = next((c for c in glorys if "царь царей" in c.name.lower()), None)
    glory_without_kok = [c for c in glorys if c is not king_of_kings]
    random.shuffle(glory_without_kok)
    # Remove 2 top cards face-down (just skip them)
    shared.glory_deck = glory_without_kok[2:]
    shared.king_of_kings = king_of_kings

    # Main deck = remaining reg/ist/civ + raids
    remaining_reg = regions[SOLO_CARDS_PER_CATEGORY:]
    remaining_ist = origins[SOLO_CARDS_PER_CATEGORY:]
    remaining_civ = civs[SOLO_CARDS_PER_CATEGORY:]
    main = remaining_reg + remaining_ist + remaining_civ + raids
    random.shuffle(main)
    shared.main_deck = main

    # Disorder deck
    random.shuffle(disorder_cards)
    shared.disorder_deck = disorder_cards

    # Build market (5 slots: 1 reg, 1 ist, 1 civ, 2 from main)
    # source_deck: "region" | "origins" | "civilization" | "main"
    slot_specs = []
    if shared.region_deck:
        slot_specs.append(("region", shared.region_deck.pop(0)))
    if shared.origins_deck:
        slot_specs.append(("origins", shared.origins_deck.pop(0)))
    if shared.civilization_deck:
        slot_specs.append(("civilization", shared.civilization_deck.pop(0)))
    for _ in range(2):
        if shared.main_deck:
            slot_specs.append(("main", shared.main_deck.pop(0)))

    market_slots = []
    for i, (source_deck, card) in enumerate(slot_specs):
        cats = card.categories
        # Беспорядки подкладываются под карты истоков, цивилизаций и набегов
        needs_disorder = (CardCategory.ORIGINS in cats or
                          CardCategory.CIVILIZATION in cats or
                          CardCategory.RAID in cats)
        disorder_under = shared.disorder_deck.pop(0) if needs_disorder and shared.disorder_deck else None

        needs_upgrade = (CardCategory.CIVILIZATION in cats and CardCategory.ORIGINS not in cats)
        upgrade_tokens = 1 if needs_upgrade else 0

        market_slots.append(MarketSlot(
            card=card,
            upgrade_tokens=upgrade_tokens,
            disorder_under=disorder_under,
            market_marker=i + 1,
            source_deck=source_deck,
        ))

    shared.market = market_slots
    return shared


def _setup_player_area(nation: Nation, nation_cards: List[Card],
                        shared: SharedArea, ability_side: str) -> PlayerArea:
    player = PlayerArea(nation=nation)

    # Find ability card
    ability_cards = [c for c in nation_cards if getattr(c, 'subtype', None) == CardSubtype.ABILITY]
    ability = next((c for c in ability_cards if c.id.endswith(ability_side)), None)
    if ability is None and ability_cards:
        ability = ability_cards[0]
    player.ability_card = ability
    player.ability_side = ability_side

    # Determine period
    # Atlanteans start in civilisation period — but we only have Classics here
    player.period = Period.BARBARISM

    # Progress area (+) — карты с subtype PROGRESS, лежат открыто рядом с картой способности
    progress_cards = [c for c in nation_cards
                      if getattr(c, 'subtype', None) == CardSubtype.PROGRESS]
    random.shuffle(progress_cards)
    player.progress_area = progress_cards

    # Boost deck (⌒) — справа от зоны прогресса:
    #   · карты BOOST перемешиваются и кладутся сверху закрытыми
    #   · карта TRANSFORMATION лежит снизу открытой
    transformation = next((c for c in nation_cards
                            if getattr(c, 'subtype', None) == CardSubtype.TRANSFORMATION), None)
    boost_cards = [c for c in nation_cards if getattr(c, 'subtype', None) == CardSubtype.BOOST]
    random.shuffle(boost_cards)
    if transformation:
        player.boost_deck = boost_cards + [transformation]
    else:
        player.boost_deck = boost_cards

    # Reserve cards (→) go to disorder deck
    reserve_cards = [c for c in nation_cards if c.start_location == "reserve"]
    shared.disorder_deck.extend(reserve_cards)
    random.shuffle(shared.disorder_deck)

    # Personal deck = только стартовые карты (subtype START) + карты без специального подтипа
    used_ids = set()
    if ability:
        used_ids.add(ability.id)
    for c in player.boost_deck:
        used_ids.add(c.id)
    for c in player.progress_area:
        used_ids.add(c.id)
    for c in reserve_cards:
        used_ids.add(c.id)

    personal = [c for c in nation_cards if c.id not in used_ids
                and getattr(c, 'subtype', None) not in (
                    CardSubtype.ABILITY, CardSubtype.BOOST,
                    CardSubtype.TRANSFORMATION, CardSubtype.PROGRESS,
                )]
    random.shuffle(personal)
    player.deck = personal

    # Starting resources
    player.resources = Resources(resource=3, population=2, upgrade=1,
                                  action=3, exploit=5)
    return player


def _setup_bot_area(nation: Nation, nation_cards: List[Card],
                    difficulty: Difficulty) -> BotArea:
    bot = BotArea(nation=nation)

    # Ability card (ignored mechanically but stored)
    ability_cards = [c for c in nation_cards if getattr(c, 'subtype', None) == CardSubtype.ABILITY]
    bot.ability_card = ability_cards[0] if ability_cards else None

    # Dynasty deck: boost cards (shuffled) on top, transformation at bottom (face-up),
    # then progress cards sorted by VP ascending below transformation
    progress_cards = [c for c in nation_cards
                      if getattr(c, 'subtype', None) == CardSubtype.PROGRESS]
    progress_cards.sort(key=lambda c: c.vp_fixed)

    transformation = next((c for c in nation_cards
                            if getattr(c, 'subtype', None) == CardSubtype.TRANSFORMATION), None)
    boost_cards = [c for c in nation_cards if getattr(c, 'subtype', None) == CardSubtype.BOOST]
    random.shuffle(boost_cards)
    if transformation:
        bot.dynasty_deck = boost_cards + [transformation] + progress_cards
    else:
        bot.dynasty_deck = progress_cards

    # Bot deck = стартовые карты (subtype START) + карты без специального подтипа
    used_ids = set()
    if bot.ability_card:
        used_ids.add(bot.ability_card.id)
    for c in bot.dynasty_deck:
        used_ids.add(c.id)

    reserve_cards = [c for c in nation_cards if c.start_location == "reserve"]
    for c in reserve_cards:
        used_ids.add(c.id)

    bot_main = [c for c in nation_cards if c.id not in used_ids
                and getattr(c, 'subtype', None) not in (
                    CardSubtype.ABILITY, CardSubtype.BOOST,
                    CardSubtype.TRANSFORMATION, CardSubtype.PROGRESS,
                )]
    random.shuffle(bot_main)
    bot.bot_deck = bot_main

    # Fill hand slots (5 slots, one card each face-down)
    num_slots = 5
    bot.hand_slots = []
    for i in range(num_slots):
        if bot.bot_deck:
            bot.hand_slots.append(bot.bot_deck.pop(0))
        else:
            bot.hand_slots.append(None)

    return bot


def _apply_difficulty_setup(state: GameState, difficulty: Difficulty):
    """Apply difficulty-specific modifications per rulebook"""
    from .enums import Difficulty

    if difficulty == Difficulty.CHIEFTAIN:
        # Replace slot 5 with an exploit token (bot plays 3-4 cards)
        if len(state.bot.hand_slots) >= 5:
            displaced = state.bot.hand_slots[4]
            if displaced and state.bot.bot_deck is not None:
                state.bot.bot_deck.insert(0, displaced)
            state.bot.hand_slots[4] = None  # signal: blocked slot

    elif difficulty == Difficulty.OVERLORD:
        # Bot gets extra starting resources
        state.bot.resource += 3
        state.bot.population += 2
        state.bot.upgrade += 1

    elif difficulty == Difficulty.SOVEREIGN:
        # Same as Overlord + 6th slot
        state.bot.resource += 3
        state.bot.population += 2
        state.bot.upgrade += 1
        # Add 6th slot
        if state.bot.bot_deck:
            state.bot.hand_slots.append(state.bot.bot_deck.pop(0))
        else:
            state.bot.hand_slots.append(None)


def _draw_to_hand(player: PlayerArea, count: int):
    """Draw cards from personal deck to hand.

    При перемешивании: если колода усиления не пуста и у верхней карты нет жетона эксплуатации,
    верхняя карта усиления добавляется в сброс перед перемешиванием.
    """
    while len(player.hand) < count:
        if not player.deck:
            if not player.discard:
                break
            # Добавить верхнюю карту усиления в сброс (если доступна)
            if player.boost_deck and not player.boost_top_token:
                top_boost = player.boost_deck.pop(0)
                player.discard.append(top_boost)
                if getattr(top_boost, 'subtype', None) == CardSubtype.TRANSFORMATION:
                    player.period = Period.CIVILIZATION
                elif player.boost_deck:
                    player.boost_top_token = True
                    player.resources.exploit -= 1
            # Shuffle discard into new deck
            player.deck = player.discard[:]
            player.discard = []
            random.shuffle(player.deck)
        if player.deck:
            player.hand.append(player.deck.pop(0))
