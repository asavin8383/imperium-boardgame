"""
Bot Logic — resolves bot card effects using nation-specific action tables.
Based on solo rules pages 13-26.
"""
import random
from typing import Optional
from .state import GameState
from .cards import Card
from .enums import CardCategory, Period, Nation


class BotLogic:
    """
    Реализует таблицы народов для бота (стр. 13-26 правил одиночной игры).
    Каждый метод соответствует конкретному народу.
    """

    @staticmethod
    def resolve_card(state: GameState, card: Card, slot_index: int) -> GameState:
        """Dispatch to the correct nation table"""
        # If disorder card — return to disorder deck
        if CardCategory.DISORDER in card.categories:
            state.shared.disorder_deck.append(card)
            random.shuffle(state.shared.disorder_deck)
            state.add_log("Бот: карта беспорядков возвращена в колоду")
            return state

        nation = state.bot.nation
        dispatch = {
            Nation.ROMANS: BotLogic._romans,
            Nation.GREEKS: BotLogic._greeks,
            Nation.CARTHAGINIANS: BotLogic._carthaginians,
            Nation.CELTS: BotLogic._celts,
            Nation.MACEDONIANS: BotLogic._macedonians,
            Nation.PERSIANS: BotLogic._persians,
            Nation.SCYTHIANS: BotLogic._scythians,
            Nation.VIKINGS: BotLogic._vikings,
        }
        handler = dispatch.get(nation, BotLogic._default)
        return handler(state, card)

    # ── ROMANS ────────────────────────────────────────────────────────────────
    @staticmethod
    def _romans(state: GameState, card: Card) -> GameState:
        period = state.bot.period
        if period == Period.BARBARISM:
            return BotLogic._romans_barbarism(state, card)
        else:
            return BotLogic._romans_civilization(state, card)

    @staticmethod
    def _romans_barbarism(state: GameState, card: Card) -> GameState:
        cats = card.categories
        name = card.name.lower()

        if CardCategory.RAID in cats:
            # Player recalls 1 region; player takes 1 disorder card
            _bot_player_recall_region(state)
            _bot_player_take_disorder(state)
            state.add_log("Бот(Рим): Набег! Игрок отзывает регион и берёт беспорядки")

        elif "величие" in name:
            # Destroy 3 regions to move top glory to bot deck
            destroyed = _bot_destroy_regions(state, 3)
            if destroyed:
                if state.shared.glory_deck:
                    top_glory = state.shared.glory_deck.pop(0)
                    state.bot.bot_deck.insert(0, top_glory)
                    state.add_log(f"Бот(Рим): Величие — взята слава {top_glory.name}")
            else:
                _bot_assign(state, CardCategory.REGION)

        elif CardCategory.REGION in cats:
            _bot_place_in_play_area(state, card)
            _bot_exile_market_card(state)
            state.add_log("Бот(Рим): Регион — завоёван, изгнана карта рынка")

        elif card.card_type is not None and "attack" in str(card.card_type):
            _bot_spend_all_population_for_upgrade(state)
            _bot_chronicle(state, card)

        else:
            _bot_assign(state, CardCategory.REGION)
            state.add_log("Бот(Рим): Иное — присвоен регион")

        return state

    @staticmethod
    def _romans_civilization(state: GameState, card: Card) -> GameState:
        name = card.name.lower()
        cats = card.categories

        if CardCategory.RAID in cats:
            # Player discards 1 card; player destroys 1 region
            _bot_player_discard(state, 1)
            _bot_player_destroy_region(state)

        elif "величие" in name:
            destroyed = _bot_destroy_regions(state, 3)
            if destroyed:
                if state.shared.glory_deck:
                    top_glory = state.shared.glory_deck.pop(0)
                    state.bot.bot_deck.insert(0, top_glory)
            else:
                _bot_player_discard(state, 2)

        elif CardCategory.CIVILIZATION in cats:
            _bot_assign(state, CardCategory.CIVILIZATION)
            _bot_chronicle(state, card)

        elif CardCategory.REGION in cats:
            _bot_place_in_play_area(state, card)
            _bot_exile_market_card(state)

        else:
            # Gain 2 resource
            state.bot.resource += 2
            _bot_chronicle(state, card)

        return state

    # ── GREEKS ────────────────────────────────────────────────────────────────
    @staticmethod
    def _greeks(state: GameState, card: Card) -> GameState:
        period = state.bot.period
        if period == Period.BARBARISM:
            return BotLogic._greeks_barbarism(state, card)
        else:
            return BotLogic._greeks_civilization(state, card)

    @staticmethod
    def _greeks_barbarism(state: GameState, card: Card) -> GameState:
        cats = card.categories
        name = card.name.lower()

        if "реформы" in name:
            if state.bot.population >= 1:
                state.bot.population -= 1
                _bot_assign(state, CardCategory.CIVILIZATION)
            else:
                _bot_acquire_best(state, CardCategory.ORIGINS)

        elif "величие" in name:
            destroyed = _bot_destroy_regions(state, 3)
            if destroyed:
                if state.shared.glory_deck:
                    top = state.shared.glory_deck.pop(0)
                    state.bot.bot_deck.insert(0, top)
            else:
                _bot_assign(state, CardCategory.REGION)

        elif CardCategory.RAID in cats:
            _bot_discard_top(state, 2)
            _bot_steal(state, "resource", 2)
            state.player.resources.population += 1

        elif CardCategory.REGION in cats:
            _bot_place_in_play_area(state, card)
            _bot_exile_market_card(state)

        else:
            # Try spend 2 population to assign region
            if state.bot.population >= 2:
                state.bot.population -= 2
                _bot_assign(state, CardCategory.REGION)
            elif state.bot.resource >= 2:
                state.bot.resource -= 2
                _bot_assign(state, CardCategory.ORIGINS)
            else:
                _bot_discard_top(state, 2)
                _bot_chronicle(state, card)

        return state

    @staticmethod
    def _greeks_civilization(state: GameState, card: Card) -> GameState:
        cats = card.categories

        if CardCategory.RAID in cats:
            _bot_acquire_best(state, CardCategory.CIVILIZATION)
            _bot_player_recall_region(state)

        elif "величие" in name if (name := card.name.lower()) else False:
            destroyed = _bot_destroy_regions(state, 3)
            if destroyed:
                if state.shared.glory_deck:
                    top = state.shared.glory_deck.pop(0)
                    state.bot.bot_deck.insert(0, top)
            else:
                state.bot.upgrade += 2

        elif CardCategory.CIVILIZATION in cats:
            _bot_acquire_best_or_assign(state, CardCategory.CIVILIZATION, CardCategory.ORIGINS)

        else:
            if state.bot.population >= 3:
                state.bot.population -= 3
                _bot_acquire_best(state, CardCategory.RAID)
            elif state.bot.resource >= 2:
                state.bot.resource -= 2
                _bot_acquire_best(state, CardCategory.CIVILIZATION)
            else:
                state.bot.upgrade += 1
                _bot_chronicle(state, card)

        return state

    # ── CARTHAGINIANS ─────────────────────────────────────────────────────────
    @staticmethod
    def _carthaginians(state: GameState, card: Card) -> GameState:
        period = state.bot.period
        cats = card.categories
        name = card.name.lower()

        if period == Period.BARBARISM:
            if CardCategory.REGION in cats:
                _bot_place_in_play_area(state, card)
                _bot_exile_market_card(state)
                state.add_log("Бот(Карфаген): Захват региона")
            elif "процветание" in name:
                _bot_discard_top(state, 1)
                regions_count = len([c for c in state.bot.play_area
                                     if CardCategory.REGION in c.categories])
                state.bot.population += 1
                state.bot.resource += regions_count
                state.add_log(f"Бот(Карфаген): Процветание — {regions_count} ресурсов")
            elif "величие" in name:
                destroyed = _bot_destroy_regions(state, 3)
                if destroyed:
                    if state.shared.glory_deck:
                        top = state.shared.glory_deck.pop(0)
                        state.bot.bot_deck.insert(0, top)
                else:
                    _bot_assign(state, CardCategory.REGION)
            else:
                # Gain 1 resource + 1 population, chronicle
                state.bot.resource += 1
                state.bot.population += 1
                _bot_chronicle(state, card)

        else:  # civilization
            if CardCategory.CIVILIZATION in cats:
                state.bot.resource += 2
                _bot_chronicle(state, card)
            elif CardCategory.RAID in cats:
                _bot_assign(state, CardCategory.RAID)
                _bot_player_destroy_region(state)
            else:
                _bot_chronicle(state, card)
                if state.bot.resource >= 2 or state.bot.population >= 2:
                    _bot_spend_and_gain_upgrade(state)
            state.add_log("Бот(Карфаген): действие в цивилизации")

        return state

    # ── CELTS ─────────────────────────────────────────────────────────────────
    @staticmethod
    def _celts(state: GameState, card: Card) -> GameState:
        period = state.bot.period
        cats = card.categories
        name = card.name.lower()

        if period == Period.BARBARISM:
            if CardCategory.RAID in cats:
                _bot_steal(state, "resource", 1)
                _bot_steal(state, "upgrade", 1)
                state.add_log("Бот(Кельты): Набег — укражена ресурс и модернизация")
            elif CardCategory.ORIGINS in cats:
                _bot_assign(state, CardCategory.ORIGINS)
                _bot_chronicle(state, card)
            elif "величие" in name:
                destroyed = _bot_destroy_regions(state, 3)
                if destroyed:
                    if state.shared.glory_deck:
                        top = state.shared.glory_deck.pop(0)
                        state.bot.bot_deck.insert(0, top)
                else:
                    if state.bot.dynasty_deck:
                        dynasty = state.bot.dynasty_deck.pop(0)
                        state.bot.bot_discard.append(dynasty)
                    _bot_player_take_disorder(state)
            else:
                _bot_try_return_disorder(state)
                state.bot.upgrade += 1
                _bot_discard_top(state, 1)
        else:
            if CardCategory.RAID in cats:
                _bot_chronicle(state, card)
                _bot_player_take_disorder(state)
            elif CardCategory.REGION in cats:
                _bot_place_in_play_area(state, card)
                _bot_exile_market_card(state)
            else:
                _bot_acquire_best_or_assign(state, CardCategory.CIVILIZATION, CardCategory.ORIGINS)
                _bot_player_take_disorder(state)

        return state

    # ── MACEDONIANS ───────────────────────────────────────────────────────────
    @staticmethod
    def _macedonians(state: GameState, card: Card) -> GameState:
        period = state.bot.period
        cats = card.categories

        if period == Period.BARBARISM:
            if CardCategory.ORIGINS in cats:
                _bot_chronicle(state, card)
            elif "величие" in card.name.lower():
                destroyed = _bot_destroy_regions(state, 3)
                if destroyed:
                    if state.shared.glory_deck:
                        top = state.shared.glory_deck.pop(0)
                        state.bot.bot_deck.insert(0, top)
                else:
                    _bot_assign(state, CardCategory.REGION)
            elif CardCategory.REGION in cats:
                _bot_place_in_play_area(state, card)
                _bot_exile_market_card(state)
                state.bot.resource += 1
            else:
                regions_count = len([c for c in state.bot.play_area
                                     if CardCategory.REGION in c.categories])
                state.bot.upgrade += regions_count
                _bot_discard_top(state, 1)
                _bot_chronicle(state, card)
        else:
            if CardCategory.REGION in cats:
                regions_count = len([c for c in state.bot.play_area
                                     if CardCategory.REGION in c.categories])
                state.bot.upgrade += regions_count
                _bot_chronicle(state, card)
            elif CardCategory.CIVILIZATION in cats:
                if state.bot.resource >= 2:
                    state.bot.resource -= 2
                    _bot_acquire_best(state, CardCategory.REGION)
                else:
                    _bot_discard_top_dynasty(state)
                    _bot_chronicle(state, card)

        return state

    # ── PERSIANS ──────────────────────────────────────────────────────────────
    @staticmethod
    def _persians(state: GameState, card: Card) -> GameState:
        period = state.bot.period
        cats = card.categories

        if period == Period.BARBARISM:
            if CardCategory.ORIGINS in cats:
                state.bot.resource += 2
                state.bot.population += 2
                _bot_discard_top(state, 1)
                _bot_chronicle(state, card)
            elif "величие" in card.name.lower():
                destroyed = _bot_destroy_regions(state, 3)
                if destroyed:
                    if state.shared.glory_deck:
                        top = state.shared.glory_deck.pop(0)
                        state.bot.bot_deck.insert(0, top)
                else:
                    _bot_assign(state, CardCategory.REGION)
            elif CardCategory.REGION in cats:
                _bot_place_in_play_area(state, card)
                _bot_exile_market_card(state)
            else:
                if state.bot.population >= 3:
                    state.bot.population -= 3
                    _bot_assign(state, CardCategory.RAID)
                    _bot_chronicle(state, card)
                elif state.bot.resource >= 2:
                    state.bot.resource -= 2
                    _bot_acquire_best(state, CardCategory.CIVILIZATION)
                else:
                    state.bot.resource += 1
                    state.bot.population += 1
                    state.bot.bot_deck.insert(0, card)  # return to top
        else:
            if CardCategory.ORIGINS in cats:
                _bot_spend_all_population_for_upgrade(state)
                _bot_chronicle(state, card)
            elif CardCategory.RAID in cats:
                _bot_assign(state, CardCategory.RAID)
                state.player.resources.resource += 2
                _bot_player_take_disorder(state)
            else:
                if state.bot.resource >= 1:
                    _bot_acquire_best(state, CardCategory.REGION)
                else:
                    state.bot.upgrade += 1

        return state

    # ── SCYTHIANS ─────────────────────────────────────────────────────────────
    @staticmethod
    def _scythians(state: GameState, card: Card) -> GameState:
        period = state.bot.period
        cats = card.categories

        if period == Period.BARBARISM:
            if CardCategory.RAID in cats:
                _bot_discard_top(state, 2)
                _bot_player_recall_region(state)
                if state.bot.period == Period.CIVILIZATION:
                    _bot_steal(state, "population", 1)
            elif "завоевание" in card.name.lower():
                if state.bot.population >= 4:
                    state.bot.population -= 4
                    _bot_assign(state, CardCategory.RAID)
                elif state.bot.resource >= 0:
                    _bot_acquire_best(state, CardCategory.REGION)
                else:
                    state.bot.resource += 1
            elif CardCategory.REGION in cats:
                _bot_place_in_play_area(state, card)
                _bot_exile_market_card(state)
                state.bot.population += 1
            else:
                _bot_try_return_disorder(state)
                _bot_assign(state, CardCategory.REGION)
                _bot_chronicle(state, card)
        else:
            regions_count = len([c for c in state.bot.play_area
                                  if CardCategory.REGION in c.categories])
            if CardCategory.ORIGINS in cats:
                state.bot.upgrade += regions_count
                _bot_chronicle(state, card)
            elif CardCategory.REGION in cats:
                _bot_place_in_play_area(state, card)
                _bot_exile_market_card(state)
                state.bot.population += 1
            else:
                state.bot.population += 2
                _bot_player_discard(state, 1)

        return state

    # ── VIKINGS ───────────────────────────────────────────────────────────────
    @staticmethod
    def _vikings(state: GameState, card: Card) -> GameState:
        cats = card.categories
        name = card.name.lower()

        if CardCategory.RAID in cats:
            _bot_try_return_disorder(state)
            _bot_acquire_best(state, CardCategory.CIVILIZATION)
            _bot_chronicle(state, card)
        elif "величие" in name:
            destroyed = _bot_destroy_regions(state, 3)
            if destroyed:
                if state.shared.glory_deck:
                    top = state.shared.glory_deck.pop(0)
                    state.bot.bot_deck.insert(0, top)
            else:
                _bot_discard_top(state, 1)
                _bot_steal(state, "resource", 2)
        elif CardCategory.REGION in cats:
            if state.bot.resource >= 3:
                state.bot.resource -= 3
                _bot_acquire_best(state, CardCategory.ORIGINS)
            _bot_place_in_play_area(state, card)
            _bot_exile_market_card(state)
        elif CardCategory.ORIGINS in cats:
            _bot_discard_top_dynasty(state)
            move_region = next((c for c in state.bot.bot_discard
                                 if CardCategory.REGION in c.categories), None)
            if move_region:
                state.bot.bot_discard.remove(move_region)
                _bot_place_in_play_area(state, move_region)
            _bot_chronicle(state, card)
        else:
            _bot_try_return_disorder(state)
            _bot_acquire_best(state, CardCategory.REGION)
            _bot_chronicle(state, card)

        return state

    # ── DEFAULT ───────────────────────────────────────────────────────────────
    @staticmethod
    def _default(state: GameState, card: Card) -> GameState:
        """Fallback: acquire best card, chronicle played card"""
        _bot_acquire_best(state, CardCategory.REGION)
        _bot_chronicle(state, card)
        return state


# ── BOT HELPER FUNCTIONS ──────────────────────────────────────────────────────

def _bot_chronicle(state: GameState, card: Card):
    """Bot files a card in its chronicle"""
    if card in state.bot.bot_discard:
        state.bot.bot_discard.remove(card)
    state.bot.chronicle.append(card)
    state.add_log(f"Бот: {card.name} занесена в летопись")


def _bot_place_in_play_area(state: GameState, card: Card):
    """Bot plays a permanent card to its play area"""
    state.bot.play_area.append(card)
    state.add_log(f"Бот: {card.name} выложена в игровую область")


def _bot_acquire_best(state: GameState, category: CardCategory):
    """Bot acquires the highest-VP card of given category from market"""
    best_idx = None
    best_vp = -1
    for i, slot in enumerate(state.shared.market):
        if slot.card and category in slot.card.categories:
            vp = slot.card.vp_fixed + slot.upgrade_tokens
            if vp > best_vp:
                best_vp = vp
                best_idx = i

    if best_idx is not None:
        slot = state.shared.market[best_idx]
        card = slot.card
        state.bot.upgrade += slot.upgrade_tokens
        if slot.disorder_under:
            state.bot.bot_deck.insert(0, slot.disorder_under)
        state.bot.bot_deck.insert(0, card)
        # Refill
        from .engine import GameEngine
        eng = GameEngine()
        state = eng._refill_market_slot(state, best_idx)
        state.shared.market[best_idx].card = None
        # Actually refill
        state = eng._refill_market_slot(state, best_idx)
        state.add_log(f"Бот: приобретена {card.name} (ПО={card.vp_fixed})")
    else:
        # Draw from top of category deck
        deck = _get_category_deck(state, category)
        if deck:
            card = deck.pop(0)
            state.bot.bot_deck.insert(0, card)
            state.add_log(f"Бот: взял {card.name} из колоды")


def _bot_assign(state: GameState, category: CardCategory):
    """Bot assigns a card — same as acquire best"""
    _bot_acquire_best(state, category)


def _bot_acquire_best_or_assign(state: GameState,
                                 cat1: CardCategory, cat2: CardCategory):
    """Try to acquire cat1, fallback to cat2"""
    found = any(cat1 in s.card.categories for s in state.shared.market if s.card)
    if found:
        _bot_acquire_best(state, cat1)
    else:
        _bot_acquire_best(state, cat2)


def _bot_exile_market_card(state: GameState):
    """Bot exiles the lowest-value card from market"""
    best_idx = None
    for i, slot in enumerate(state.shared.market):
        if slot.card and slot.upgrade_tokens == 0:
            best_idx = i
            break
    if best_idx is None and state.shared.market:
        best_idx = 0

    if best_idx is not None:
        slot = state.shared.market[best_idx]
        if slot.card:
            state.shared.exile_pile.append(slot.card)
            if slot.disorder_under:
                state.shared.disorder_deck.append(slot.disorder_under)
            slot.card = None
            slot.disorder_under = None
            slot.upgrade_tokens = 0
            state.add_log("Бот: карта отправлена в изгнание")
            # Refill
            from .engine import GameEngine
            eng = GameEngine()
            state = eng._refill_market_slot(state, best_idx)


def _bot_discard_top(state: GameState, n: int):
    """Bot moves top n cards of its deck to discard"""
    for _ in range(n):
        if state.bot.bot_deck:
            card = state.bot.bot_deck.pop(0)
            state.bot.bot_discard.append(card)
            state.add_log(f"Бот: {card.name} перемещена в сброс")


def _bot_discard_top_dynasty(state: GameState):
    """Bot moves top dynasty card to discard"""
    if state.bot.dynasty_deck:
        card = state.bot.dynasty_deck.pop(0)
        state.bot.bot_discard.append(card)
        state.add_log(f"Бот: {card.name} (династия) в сброс")


def _bot_steal(state: GameState, resource_type: str, amount: int):
    """Bot steals resources from player"""
    if resource_type == "resource":
        actual = min(amount, state.player.resources.resource)
        state.player.resources.resource -= actual
        state.bot.resource += actual
        state.add_log(f"Бот: украдено {actual} ресурсов у игрока")
    elif resource_type == "population":
        actual = min(amount, state.player.resources.population)
        state.player.resources.population -= actual
        state.bot.population += actual
    elif resource_type == "upgrade":
        actual = min(amount, state.player.resources.upgrade)
        state.player.resources.upgrade -= actual
        state.bot.upgrade += actual


def _bot_try_return_disorder(state: GameState) -> bool:
    """Bot tries to return disorder from its discard to disorder deck"""
    disorder = next((c for c in state.bot.bot_discard
                      if CardCategory.DISORDER in c.categories), None)
    if disorder:
        state.bot.bot_discard.remove(disorder)
        state.shared.disorder_deck.append(disorder)
        random.shuffle(state.shared.disorder_deck)
        state.add_log("Бот: карта беспорядков возвращена в колоду")
        return True
    return False


def _bot_destroy_regions(state: GameState, n: int) -> bool:
    """Bot destroys n region cards from its play area"""
    regions = [c for c in state.bot.play_area if CardCategory.REGION in c.categories]
    if len(regions) < n:
        return False
    for c in regions[:n]:
        state.bot.play_area.remove(c)
        state.bot.bot_discard.append(c)
    state.add_log(f"Бот: уничтожено {n} регионов")
    return True


def _bot_player_recall_region(state: GameState):
    """Force player to recall a region card from play area"""
    region = next((c for c in state.player.play_area
                    if CardCategory.REGION in c.categories), None)
    if region:
        state.player.play_area.remove(region)
        state.player.hand.append(region)
        state.add_log(f"Игрок: отозван регион {region.name}")


def _bot_player_take_disorder(state: GameState):
    """Player takes a disorder card to hand"""
    if state.shared.disorder_deck:
        dis = state.shared.disorder_deck.pop(0)
        state.player.hand.append(dis)
        state.add_log(f"Игрок: взята карта беспорядков!")


def _bot_player_discard(state: GameState, n: int):
    """Force player to discard n cards (they choose; here: last in hand)"""
    for _ in range(n):
        if state.player.hand:
            card = state.player.hand.pop(-1)
            state.player.discard.append(card)
            state.add_log(f"Игрок: сброшена карта {card.name}")


def _bot_player_destroy_region(state: GameState):
    """Force player to destroy a region"""
    region = next((c for c in state.player.play_area
                    if CardCategory.REGION in c.categories), None)
    if region:
        state.player.play_area.remove(region)
        state.player.discard.append(region)
        state.add_log(f"Игрок: уничтожен регион {region.name}")


def _bot_spend_all_population_for_upgrade(state: GameState):
    """Bot spends all population to gain same amount of upgrade"""
    pop = state.bot.population
    if pop > 0:
        state.bot.upgrade += pop // 2
        state.bot.population = 0
        state.add_log(f"Бот: {pop} населения → {pop // 2} модернизаций")


def _bot_spend_and_gain_upgrade(state: GameState):
    if state.bot.resource >= 2:
        state.bot.resource -= 2
        state.bot.upgrade += 1
    elif state.bot.population >= 2:
        state.bot.population -= 2
        state.bot.upgrade += 1


def _get_category_deck(state: GameState, category: CardCategory):
    if category == CardCategory.REGION:
        return state.shared.region_deck
    elif category == CardCategory.ORIGINS:
        return state.shared.origins_deck
    elif category == CardCategory.CIVILIZATION:
        return state.shared.civilization_deck
    return state.shared.main_deck
