"""
Game Engine — processes player and bot actions according to Imperium rules
"""
import random
from typing import List, Optional, Tuple
from .state import GameState, PlayerArea, MarketSlot, Resources
from .enums import (Period, GamePhase, TurnAction, EndCondition,
                    CardCategory, Difficulty)
from .cards import Card
from .setup import _draw_to_hand


class GameEngine:
    """
    Движок игры Империи (соло-режим).
    Все методы принимают GameState и возвращают изменённый GameState.
    """

    # ── PLAYER ACTIONS ─────────────────────────────────────────────────────────

    def start_activation(self, state: GameState) -> GameState:
        """Игрок выбирает «Активацию»"""
        if state.phase != GamePhase.PLAYER_TURN:
            raise ValueError("Не ваш ход")
        state.player.turn_action_chosen = TurnAction.ACTIVATION
        state.add_log("Вы выбрали Активацию")
        return state

    def play_card(self, state: GameState, card_id: str) -> GameState:
        """
        Розыгрыш карты с руки.
        Правила: убрать жетон действия с карты периода → выложить карту → применить эффект.
        """
        if state.player.turn_action_chosen != TurnAction.ACTIVATION:
            raise ValueError("Сначала выберите Активацию")

        card = self._find_in_hand(state, card_id)
        if card is None:
            raise ValueError(f"Карта {card_id} не найдена на руке")

        # Check period restriction
        if card.period is not None and card.period != state.player.period:
            raise ValueError(f"Нельзя разыграть карту периода {card.period.value} "
                             f"в периоде {state.player.period.value}")

        # Requires action token check
        if card.requires_action_token:
            if state.player.resources.action <= 0:
                raise ValueError("Нет жетонов действия")
            state.player.resources.action -= 1

        # Move card from hand to play area (or discard if not ∞)
        state.player.hand.remove(card)
        state.player.cards_played_this_turn.append(card)

        # Apply card effect
        state = self._apply_player_card_effect(state, card)

        # If not permanent — move to discard
        from .enums import CardType
        if card.card_type != CardType.PERMANENT:
            if card not in state.player.discard and card not in state.player.play_area:
                state.player.discard.append(card)

        state.add_log(f"Разыграна карта: {card.name}")
        state = self._check_end_conditions(state)
        return state

    def exploit_card(self, state: GameState, card_id: str) -> GameState:
        """
        Эксплуатация карты в личной игровой области.
        Правила: переместить жетон X с карты периода на эксплуатируемую карту.
        """
        if state.player.turn_action_chosen != TurnAction.ACTIVATION:
            raise ValueError("Сначала выберите Активацию")
        if state.player.resources.exploit <= 0:
            raise ValueError("Нет жетонов эксплуатации")

        # Find in play_area or ability card
        card = None
        if (state.player.ability_card and
                state.player.ability_card.id == card_id and
                not state.player.ability_exploit_used):
            card = state.player.ability_card
            state.player.ability_exploit_used = True
        else:
            card = next((c for c in state.player.play_area if c.id == card_id), None)

        if card is None:
            raise ValueError(f"Карта {card_id} не найдена для эксплуатации")

        if card in state.player.exploits_used_this_turn:
            raise ValueError("Карта уже эксплуатировалась в этом ходу")

        state.player.resources.exploit -= 1
        state.player.exploits_used_this_turn.append(card)

        state = self._apply_exploit_effect(state, card)
        state.add_log(f"Эксплуатирована карта: {card.name}")
        return state

    def do_innovation(self, state: GameState, category: str) -> GameState:
        """
        Инновация: сбросить все карты с руки → присвоить карту указанной категории.
        """
        if state.phase != GamePhase.PLAYER_TURN:
            raise ValueError("Не ваш ход")
        state.player.turn_action_chosen = TurnAction.INNOVATION

        # Discard hand
        state.player.discard.extend(state.player.hand)
        state.player.hand = []

        # Assign a card from market
        state = self._assign_card(state, category)
        state.add_log(f"Инновация: присвоена карта категории {category}")
        state = self._end_player_turn(state)
        return state

    def do_revolution(self, state: GameState,
                      card_ids: List[str]) -> GameState:
        """
        Революция: вернуть карты беспорядков с руки в колоду беспорядков.
        """
        if state.phase != GamePhase.PLAYER_TURN:
            raise ValueError("Не ваш ход")
        state.player.turn_action_chosen = TurnAction.REVOLUTION

        for cid in card_ids:
            card = self._find_in_hand(state, cid)
            if card and CardCategory.DISORDER in card.categories:
                state.player.hand.remove(card)
                state.shared.disorder_deck.append(card)
                random.shuffle(state.shared.disorder_deck)

        state.add_log(f"Революция: возвращено {len(card_ids)} карт беспорядков")
        state = self._end_player_turn(state)
        return state

    def end_turn(self, state: GameState) -> GameState:
        """Игрок завершает ход (фаза обновления)"""
        if state.phase != GamePhase.PLAYER_TURN:
            raise ValueError("Не ваш ход")
        if state.player.turn_action_chosen is None:
            # Default to ending without action
            state.player.turn_action_chosen = TurnAction.ACTIVATION
        state = self._end_player_turn(state)
        return state

    def discard_from_hand(self, state: GameState, card_ids: List[str]) -> GameState:
        """Сбросить карты с руки (во время фазы обновления)"""
        for cid in card_ids:
            card = self._find_in_hand(state, cid)
            if card:
                state.player.hand.remove(card)
                state.player.discard.append(card)
        return state

    def acquire_card(self, state: GameState, slot_index: int) -> GameState:
        """
        Приобрести карту с текущего рынка (эффект некоторых карт / инновация).
        """
        if slot_index < 0 or slot_index >= len(state.shared.market):
            raise ValueError("Неверный индекс слота рынка")
        slot = state.shared.market[slot_index]
        if slot.card is None:
            raise ValueError("Слот пуст")

        card = slot.card
        # Take upgrade tokens
        state.player.resources.upgrade += slot.upgrade_tokens

        # Take disorder card if present
        if slot.disorder_under:
            state.player.hand.append(slot.disorder_under)
            slot.disorder_under = None

        # Card goes to player hand
        state.player.hand.append(card)

        # Refill slot
        state = self._refill_market_slot(state, slot_index)
        state.add_log(f"Приобретена карта: {card.name}")
        state = self._check_end_conditions(state)
        return state

    def accelerate_progress(self, state: GameState,
                             progress_card_id: str) -> GameState:
        """
        Ускорить прогресс — оплатить стоимость карты прогресса и переместить в сброс.
        """
        card = next((c for c in state.player.progress_area
                     if c.id == progress_card_id), None)
        if card is None:
            raise ValueError("Карта прогресса не найдена")

        # Check cost
        if (state.player.resources.resource < card.progress_cost_resource or
                state.player.resources.population < card.progress_cost_population or
                state.player.resources.upgrade < card.progress_cost_upgrade):
            raise ValueError("Недостаточно ресурсов для ускорения прогресса")

        state.player.resources.resource -= card.progress_cost_resource
        state.player.resources.population -= card.progress_cost_population
        state.player.resources.upgrade -= card.progress_cost_upgrade

        state.player.progress_area.remove(card)
        state.player.discard.append(card)
        state.add_log(f"Ускорен прогресс: {card.name}")
        state = self._check_end_conditions(state)
        return state

    # ── BOT TURN ───────────────────────────────────────────────────────────────

    def run_bot_turn(self, state: GameState) -> GameState:
        """
        Полный ход бота согласно правилам одиночного режима.
        """
        if state.phase != GamePhase.BOT_TURN:
            raise ValueError("Не ход бота")

        state.add_log("─── Ход бота ───")

        # Step 1: Roll die and set aside one card
        die_roll = random.randint(1, 6)
        state.add_log(f"Бот бросает кубик: {die_roll}")

        set_aside_slot = None
        num_slots = len(state.bot.hand_slots)
        # Check if die value matches any marker
        if 1 <= die_roll <= num_slots:
            slot_idx = die_roll - 1  # 0-indexed
            if state.bot.hand_slots[slot_idx] is not None:
                set_aside_slot = slot_idx
                state.add_log(f"Карта под маркером {die_roll} отложена в сторону")

        # Step 2: Play remaining cards (ascending marker order)
        for i in range(num_slots):
            if i == set_aside_slot:
                continue
            card = state.bot.hand_slots[i]
            if card is None:
                continue
            state = self._bot_play_card(state, card, i)

        # Step 3: Update phase — place upgrade token on market
        state = self._bot_update_phase(state, die_roll, set_aside_slot)

        # Step 4: Refill bot hand
        state = self._bot_refill_hand(state)

        state.add_log("─── Конец хода бота ───")

        # Check end conditions
        state = self._check_end_conditions(state)
        if state.phase != GamePhase.GAME_OVER:
            # Move to solstice (end of round)
            state.phase = GamePhase.SOLSTICE
            state = self._apply_solstice(state)

        return state

    def _bot_play_card(self, state: GameState, card: Card,
                        slot_index: int) -> GameState:
        """Play one bot card using the bot action table"""
        from .bot_logic import BotLogic
        state = BotLogic.resolve_card(state, card, slot_index)
        state.bot.hand_slots[slot_index] = None
        state.bot.bot_discard.append(card)
        return state

    def _bot_update_phase(self, state: GameState,
                           die_roll: int, set_aside_slot: Optional[int]) -> GameState:
        """Bot update phase: place token, return set-aside card"""
        if 1 <= die_roll <= len(state.shared.market):
            slot = state.shared.market[die_roll - 1]
            # Carthaginians bot: +2 resource; Qin: +1 population
            if state.bot.nation.value == "carthaginians":
                slot.upgrade_tokens += 2
            elif state.bot.nation.value == "scythians":
                slot.upgrade_tokens += 1
            else:
                slot.upgrade_tokens += 1
            state.add_log(f"Жетон добавлен на карту рынка {die_roll}")

        # Return set-aside card to slot 1 (marker 1)
        if set_aside_slot is not None:
            card = state.bot.hand_slots[set_aside_slot]
            if card:
                # Put it face-down at slot index 0 (marker 1)
                state.bot.hand_slots[0] = card
                state.bot.hand_slots[set_aside_slot] = None
        return state

    def _bot_refill_hand(self, state: GameState) -> GameState:
        """Refill bot hand slots from bot deck"""
        for i in range(len(state.bot.hand_slots)):
            if state.bot.hand_slots[i] is None:
                if not state.bot.bot_deck:
                    # Shuffle dynasty card into discard, form new deck
                    if state.bot.dynasty_deck:
                        dynasty_card = state.bot.dynasty_deck.pop(0)
                        state.bot.bot_discard.append(dynasty_card)
                        # Check if dynasty ended
                        if not state.bot.dynasty_deck:
                            state.add_log("Колода династии бота закончилась!")
                            state = self._check_end_conditions(state)
                            if state.phase == GamePhase.GAME_OVER:
                                return state
                    state.bot.bot_deck = state.bot.bot_discard[:]
                    state.bot.bot_discard = []
                    random.shuffle(state.bot.bot_deck)
                    # Check transformation card
                    if dynasty_card and CardCategory.TRANSFORMATION in dynasty_card.categories:
                        state.bot.period = Period.CIVILIZATION
                        state.add_log("Бот вступает в период цивилизации!")

                if state.bot.bot_deck:
                    state.bot.hand_slots[i] = state.bot.bot_deck.pop(0)
        return state

    # ── SOLSTICE ───────────────────────────────────────────────────────────────

    def _apply_solstice(self, state: GameState) -> GameState:
        """
        Эффекты солнцестояния в конце раунда.
        Игрок применяет их; бот никогда не применяет солнцестояние.
        """
        state.add_log("─── Солнцестояние ───")

        # Apply solstice effects on player's permanent cards
        for card in state.player.play_area:
            if card.solstice_effect:
                state = self._apply_solstice_card_effect(state, card)

        # Apply solstice on ability card if applicable
        if state.player.ability_card and state.player.ability_card.solstice_effect:
            state = self._apply_solstice_card_effect(state, state.player.ability_card)

        state.round_number += 1
        state.phase = GamePhase.PLAYER_TURN
        # Reset turn state
        state.player.turn_action_chosen = None
        state.player.cards_played_this_turn = []
        state.player.exploits_used_this_turn = []
        state.player.ability_exploit_used = False

        state.add_log(f"Начинается раунд {state.round_number}")
        return state

    def _apply_solstice_card_effect(self, state: GameState, card: Card) -> GameState:
        """Generic solstice — gain 1 resource (simplified; specific cards override)"""
        # Metropolis effect: choose resource, population, or draw card
        if "метрополия" in card.name.lower():
            # Default: gain resource
            state.player.resources.resource += 1
        elif "акрополь" in card.name.lower():
            state.player.resources.population += 1
        return state

    # ── PLAYER UPDATE PHASE ─────────────────────────────────────────────────────

    def _end_player_turn(self, state: GameState) -> GameState:
        """
        Фаза обновления для игрока:
        1. Положить жетон > на карту рынка
        2. Убрать жетоны ● и X с карты периода
        3. Сбросить любые карты с руки (handled by UI)
        4. Добрать до 5 карт
        Also trigger deck shuffle if needed.
        """
        # Place 1 upgrade token on any market card (player chooses; default: first slot)
        if state.shared.market:
            state.shared.market[0].upgrade_tokens += 1

        # Reset tokens
        state.player.resources.action = 3
        state.player.resources.exploit = 5

        # Draw to hand limit
        _draw_to_hand(state.player, state.player.hand_limit)

        # Bot turn follows
        state.phase = GamePhase.BOT_TURN
        return state

    # ── CARD SHUFFLING ─────────────────────────────────────────────────────────

    def _reshuffle_player_deck(self, state: GameState) -> GameState:
        """
        Перетасовка личной колоды.
        В период варварства: переместить верхнюю карту усиления в сброс,
        перетасовать сброс, перевернуть карту периода если нужно.
        """
        player = state.player

        if player.period == Period.BARBARISM:
            # Move top boost card to discard
            if player.boost_deck and not player.boost_top_token:
                top_boost = player.boost_deck.pop(0)
                player.discard.append(top_boost)
                state.add_log(f"Карта усиления «{top_boost.name}» перемещена в сброс")
                # If it was the transformation card, flip period
                if CardCategory.TRANSFORMATION in top_boost.categories:
                    player.period = Period.CIVILIZATION
                    state.add_log("Период изменён на Цивилизацию!")
                else:
                    # Place exploit token on new top boost card
                    if player.boost_deck:
                        player.boost_top_token = True
            elif player.boost_top_token:
                # Token already on top — skip step 1
                pass

        elif player.period == Period.CIVILIZATION:
            # Можно ускорить прогресс при перетасовке
            pass

        # Reshuffle discard into new deck
        if player.discard:
            player.deck = player.discard[:]
            player.discard = []
            random.shuffle(player.deck)

        return state

    # ── END CONDITION CHECK ────────────────────────────────────────────────────

    def _check_end_conditions(self, state: GameState) -> GameState:
        """Check all end conditions after each action"""
        if state.phase == GamePhase.GAME_OVER:
            return state

        # Decline: disorder deck empty
        if not state.shared.disorder_deck:
            state.end_condition = EndCondition.DECLINE
            state.phase = GamePhase.GAME_OVER
            state.add_log("УПАДОК! Колода беспорядков закончилась.")
            return state

        # Main deck empty
        if not state.shared.main_deck:
            if not state.is_final_round:
                state.is_final_round = True
                state.add_log("Основная колода закончилась — начинается финальный раунд!")
                state.end_condition = EndCondition.MAIN_DECK_EMPTY

        # King of Kings flipped to B
        if state.shared.king_of_kings_side_b:
            if not state.is_final_round:
                state.is_final_round = True
                state.end_condition = EndCondition.KING_OF_KINGS_B
                state.add_log("Царь царей перевёрнут на сторону Б — начинается финальный раунд!")

        # Player progress area empty
        if not state.player.progress_area and state.player.period == Period.CIVILIZATION:
            if not state.is_final_round:
                state.is_final_round = True
                state.end_condition = EndCondition.PROGRESS_EMPTY
                state.add_log("Область прогресса игрока пуста — начинается финальный раунд!")

        # Bot dynasty deck empty
        if not state.bot.dynasty_deck:
            if not state.is_final_round:
                state.is_final_round = True
                state.add_log("Колода династии бота закончилась — начинается финальный раунд!")

        # If final round is complete → scoring
        if state.is_final_round and state.phase == GamePhase.SOLSTICE:
            state.phase = GamePhase.SCORING
            state = self.calculate_scores(state)

        return state

    # ── SCORING ────────────────────────────────────────────────────────────────

    def calculate_scores(self, state: GameState) -> GameState:
        """Подсчёт ПО в конце игры"""
        player_vp = self._calculate_player_vp(state)
        bot_vp = self._calculate_bot_vp(state)

        state.add_log(f"=== ПОДСЧЁТ ПО ===")
        state.add_log(f"Игрок: {player_vp} ПО")
        state.add_log(f"Бот: {bot_vp} ПО")

        if player_vp > bot_vp:
            state.add_log("🏆 ПОБЕДА ИГРОКА!")
        elif bot_vp >= player_vp:
            state.add_log("Победил бот. Попробуйте снова!")

        state.phase = GamePhase.GAME_OVER
        state.pending_choice = {
            "type": "game_result",
            "player_vp": player_vp,
            "bot_vp": bot_vp,
            "winner": "player" if player_vp > bot_vp else "bot"
        }
        return state

    def _calculate_player_vp(self, state: GameState) -> int:
        vp = 0
        player = state.player

        # Each upgrade token = 1 VP
        vp += player.resources.upgrade

        # Count all cards
        all_cards = (player.hand + player.deck + player.discard +
                     player.play_area + player.chronicle)
        if player.ability_card:
            all_cards.append(player.ability_card)

        for card in all_cards:
            vp += card.vp_fixed
            vp -= card.vp_penalty
            if card.vp_per_condition:
                vp += self._evaluate_condition_vp(state, card, is_player=True)

        return max(0, vp)

    def _calculate_bot_vp(self, state: GameState) -> int:
        vp = 0
        bot = state.bot

        # Each upgrade token = 1 VP
        vp += bot.upgrade

        # Resource + population: every 10 = 1 VP
        vp += (bot.resource + bot.population) // 10

        # Count all cards (not dynasty deck)
        all_cards = (bot.bot_deck + bot.bot_discard +
                     bot.chronicle + bot.play_area)
        for slot in bot.hand_slots:
            if slot:
                all_cards.append(slot)

        for card in all_cards:
            vp += card.vp_fixed
            vp -= card.vp_penalty
            if card.vp_per_condition:
                # Bot gets max possible VP from condition cards
                vp += min(10, 5)  # simplified

        return max(0, vp)

    def _evaluate_condition_vp(self, state: GameState,
                                card: Card, is_player: bool) -> int:
        """Evaluate conditional VP cards"""
        # Simplified evaluation
        player = state.player
        all_cards = (player.hand + player.deck + player.discard +
                     player.play_area + player.chronicle)

        cond = card.vp_condition or card.vp_per_condition or ""
        if "region" in cond:
            count = sum(1 for c in all_cards if CardCategory.REGION in c.categories)
            return min(count, 10)
        elif "civilization" in cond:
            count = sum(1 for c in all_cards if CardCategory.CIVILIZATION in c.categories)
            return min(count, 10)
        elif "origins" in cond:
            count = sum(1 for c in all_cards if CardCategory.ORIGINS in c.categories)
            return min(count, 10)
        elif "population" in cond:
            return min(player.resources.population // 2, 10)
        elif "resource" in cond:
            return min(player.resources.resource // 3, 10)
        return 0

    # ── MARKET HELPERS ─────────────────────────────────────────────────────────

    def _refill_market_slot(self, state: GameState,
                             slot_index: int) -> GameState:
        """Refill a market slot after a card is acquired"""
        slot = state.shared.market[slot_index]
        if slot.card is None:
            # Determine which deck to draw from
            card = None
            if slot_index == 0 and state.shared.region_deck:
                card = state.shared.region_deck.pop(0)
            elif slot_index == 1 and state.shared.origins_deck:
                card = state.shared.origins_deck.pop(0)
            elif slot_index == 2 and state.shared.civilization_deck:
                card = state.shared.civilization_deck.pop(0)
            elif state.shared.main_deck:
                card = state.shared.main_deck.pop(0)

            if card:
                slot.card = card
                needs_disorder = (CardCategory.REGION in card.categories or
                                  CardCategory.ORIGINS in card.categories or
                                  CardCategory.CIVILIZATION in card.categories)
                if needs_disorder and state.shared.disorder_deck:
                    slot.disorder_under = state.shared.disorder_deck.pop(0)
                needs_upgrade = (CardCategory.CIVILIZATION in card.categories and
                                 CardCategory.ORIGINS not in card.categories)
                if needs_upgrade:
                    slot.upgrade_tokens = 1
        return state

    # ── ASSIGN (ПРИСВОИТЬ) ─────────────────────────────────────────────────────

    def _assign_card(self, state: GameState, category: str) -> GameState:
        """
        Присвоить карту — взять лучшую карту категории с текущего рынка.
        Если нет — с верха соответствующей колоды.
        """
        from .enums import CardCategory as CC
        cat_map = {
            "region": CC.REGION,
            "origins": CC.ORIGINS,
            "civilization": CC.CIVILIZATION,
            "raid": CC.RAID,
        }
        target_cat = cat_map.get(category.lower())
        if target_cat is None:
            return state

        # Find best card on market
        best_slot_idx = None
        best_vp = -1
        for i, slot in enumerate(state.shared.market):
            if slot.card and target_cat in slot.card.categories:
                total_vp = slot.card.vp_fixed + slot.upgrade_tokens
                if total_vp > best_vp:
                    best_vp = total_vp
                    best_slot_idx = i

        if best_slot_idx is not None:
            state = self.acquire_card(state, best_slot_idx)
        else:
            # Draw from top of category deck
            deck_map = {
                CC.REGION: state.shared.region_deck,
                CC.ORIGINS: state.shared.origins_deck,
                CC.CIVILIZATION: state.shared.civilization_deck,
            }
            deck = deck_map.get(target_cat, state.shared.main_deck)
            if deck:
                card = deck.pop(0)
                state.player.hand.append(card)
                state.add_log(f"Присвоена карта из колоды: {card.name}")

        return state

    # ── CARD EFFECT RESOLUTION ─────────────────────────────────────────────────

    def _apply_player_card_effect(self, state: GameState, card: Card) -> GameState:
        """Apply the effect of a played card (simplified for now)"""
        from .enums import CardType

        # Move to play area if permanent
        if card.card_type == CardType.PERMANENT:
            if card not in state.player.play_area:
                state.player.play_area.append(card)

        # Generic effects based on category
        if CardCategory.REGION in card.categories:
            # Regions give resources
            state.player.resources.resource += 1

        if CardCategory.ORIGINS in card.categories:
            # Origins give population or allow progress
            state.player.resources.population += 1

        if CardCategory.CIVILIZATION in card.categories:
            # Civilisation cards give upgrade tokens
            state.player.resources.upgrade += 1

        if CardCategory.RAID in card.categories:
            # Raid: attack bot — bot loses a resource
            if card.card_type == CardType.ATTACK:
                state.bot.resource = max(0, state.bot.resource - 1)
                state.add_log("Набег! Бот теряет 1 ресурс")

        if CardCategory.GLORY in card.categories:
            # Glory cards go to chronicle (летопись)
            if card in state.player.play_area:
                state.player.play_area.remove(card)
            state.player.chronicle.append(card)

        return state

    def _apply_exploit_effect(self, state: GameState, card: Card) -> GameState:
        """Apply exploitation effect of a card"""
        # Generic exploit: gain resources based on card type
        if CardCategory.REGION in card.categories:
            state.player.resources.resource += 2
        elif CardCategory.ORIGINS in card.categories:
            state.player.resources.population += 1
            state.player.resources.resource += 1
        elif CardCategory.CIVILIZATION in card.categories:
            state.player.resources.upgrade += 1
        return state

    # ── UTILS ──────────────────────────────────────────────────────────────────

    def _find_in_hand(self, state: GameState,
                       card_id: str) -> Optional[Card]:
        return next((c for c in state.player.hand if c.id == card_id), None)
