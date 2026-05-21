"""
Game Engine — processes player and bot actions according to Imperium rules
"""
import random
from typing import List, Optional, Tuple
from .state import GameState, PlayerArea, MarketSlot, Resources
from .enums import (Period, GamePhase, TurnAction, EndCondition,
                    CardCategory, CardSubtype, Difficulty, ResourceType)
from .cards import Card, GainResourceAction, AcquireCardAction, AppropriateCardAction, ChoiceAction
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
        # Автоматически выбираем активацию если действие ещё не выбрано
        if state.player.turn_action_chosen is None:
            state.player.turn_action_chosen = TurnAction.ACTIVATION
        elif state.player.turn_action_chosen != TurnAction.ACTIVATION:
            raise ValueError("Действие уже выбрано — розыгрыш карт недоступен")

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
        if state.player.turn_action_chosen is None:
            state.player.turn_action_chosen = TurnAction.ACTIVATION
        elif state.player.turn_action_chosen != TurnAction.ACTIVATION:
            raise ValueError("Эксплуатация доступна только при Активации")
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

    def do_innovation(self, state: GameState) -> GameState:
        """
        Инновация: присвоить одну карту с рынка (регион, исток, цивилизация, набег).
        Карта беспорядков из-под неё в руку не идёт.
        """
        if state.phase != GamePhase.PLAYER_TURN:
            raise ValueError("Не ваш ход")
        if state.player.turn_action_chosen is not None:
            raise ValueError("Действие уже выбрано")
        state.player.turn_action_chosen = TurnAction.INNOVATION
        state.pending_choice = {
            "type": "innovate_from_market",
            "allowed_categories": ["region", "origins", "civilization", "raid"],
        }
        state.add_log("Инновация: выберите карту с рынка (регион, исток, цивилизация, набег)")
        return state

    def do_revolution(self, state: GameState,
                      card_ids: List[str]) -> GameState:
        """
        Революция: вернуть карты беспорядков с руки в колоду беспорядков.
        """
        if state.phase != GamePhase.PLAYER_TURN:
            raise ValueError("Не ваш ход")
        if state.player.turn_action_chosen is not None:
            raise ValueError("Действие уже выбрано")
        state.player.turn_action_chosen = TurnAction.REVOLUTION

        returned = 0
        for cid in card_ids:
            card = self._find_in_hand(state, cid)
            if card and CardCategory.DISORDER in getattr(card, 'categories', []):
                state.player.hand.remove(card)
                state.shared.disorder_deck.append(card)
                returned += 1
        if returned > 0:
            random.shuffle(state.shared.disorder_deck)

        state.add_log(f"Революция: возвращено {returned} карт беспорядков")
        return state

    def end_turn(self, state: GameState) -> GameState:
        """Игрок завершает ход — переходит к фазе сброса карт"""
        if state.phase == GamePhase.PLAYER_TURN:
            pending_type = state.pending_choice.get("type") if state.pending_choice else None
            if pending_type == "innovate_from_market":
                raise ValueError("Сначала выберите карту с рынка для инновации")
            if pending_type in ("player_choice", "acquire_from_market", "appropriate", "appropriate_select_category"):
                raise ValueError("Сначала завершите действие карты")
            if state.player.turn_action_chosen is None:
                state.player.turn_action_chosen = TurnAction.ACTIVATION
            state.phase = GamePhase.PLAYER_DISCARD
            state.pending_choice = {"type": "discard"}
            state.add_log("Выберите карты для сброса (или пропустите)")
            return state
        elif state.phase == GamePhase.PLAYER_DISCARD:
            state.pending_choice = None
            return self._end_player_turn(state)
        else:
            raise ValueError("Не ваш ход")

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

        # Если есть pending выбор с рынка — проверяем категорию
        pending = state.pending_choice
        pending_type = pending.get("type") if pending else None
        if pending_type in ("acquire_from_market", "innovate_from_market", "appropriate"):
            allowed = pending["allowed_categories"]
            card_cats = [c.value for c in getattr(slot.card, 'categories', [])]
            if not any(c in allowed for c in card_cats):
                raise ValueError(
                    f"Эта карта не подходит. Разрешены: {', '.join(allowed)}"
                )

        card = slot.card
        slot.card = None  # clear slot before refill

        # Take upgrade tokens
        state.player.resources.upgrade += slot.upgrade_tokens
        slot.upgrade_tokens = 0

        # Обработка карты беспорядков под картой
        if pending_type == "appropriate" and slot.disorder_under:
            # Присвоение: беспорядки возвращаются в стопку беспорядков
            state.shared.disorder_deck.append(slot.disorder_under)
            state.add_log("Карта беспорядков возвращена в стопку беспорядков")
        elif pending_type != "innovate_from_market" and slot.disorder_under:
            state.player.hand.append(slot.disorder_under)
            state.add_log("Карта беспорядков из-под купленной карты добавлена в руку")
        slot.disorder_under = None

        # Card goes to player hand
        state.player.hand.append(card)

        # Refill slot
        state = self._refill_market_slot(state, slot_index)
        state.add_log(f"Приобретена карта: {card.name}")

        # Обновляем pending_choice
        if pending_type in ("acquire_from_market", "appropriate"):
            remaining = pending["remaining"] - 1
            if remaining <= 0:
                state.pending_choice = None
            else:
                pending["remaining"] = remaining
                state.add_log(f"Осталось выборов с рынка: {remaining}")
        elif pending_type == "innovate_from_market":
            state.pending_choice = None

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
                    if dynasty_card and getattr(dynasty_card, 'subtype', None) == CardSubtype.TRANSFORMATION:
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
                if getattr(top_boost, 'subtype', None) == CardSubtype.TRANSFORMATION:
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
            count = sum(1 for c in all_cards if CardCategory.REGION in getattr(c, 'categories', []))
            return min(count, 10)
        elif "civilization" in cond:
            count = sum(1 for c in all_cards if CardCategory.CIVILIZATION in getattr(c, 'categories', []))
            return min(count, 10)
        elif "origins" in cond:
            count = sum(1 for c in all_cards if CardCategory.ORIGINS in getattr(c, 'categories', []))
            return min(count, 10)
        elif "population" in cond:
            return min(player.resources.population // 2, 10)
        elif "resource" in cond:
            return min(player.resources.resource // 3, 10)
        return 0

    # ── MARKET HELPERS ─────────────────────────────────────────────────────────

    def _refill_market_slot(self, state: GameState,
                             slot_index: int) -> GameState:
        """Refill a market slot after a card is acquired — draws from slot's source_deck."""
        slot = state.shared.market[slot_index]
        if slot.card is not None:
            return state

        source_map = {
            "region":       state.shared.region_deck,
            "origins":      state.shared.origins_deck,
            "civilization": state.shared.civilization_deck,
            "main":         state.shared.main_deck,
        }
        deck = source_map.get(slot.source_deck, state.shared.main_deck)
        card = deck.pop(0) if deck else None

        if card:
            slot.card = card
            slot.upgrade_tokens = 0
            slot.disorder_under = None
            cats = getattr(card, 'categories', [])
            # Беспорядки под картами истоков, цивилизаций и набегов
            if (CardCategory.ORIGINS in cats or
                    CardCategory.CIVILIZATION in cats or
                    CardCategory.RAID in cats):
                if state.shared.disorder_deck:
                    slot.disorder_under = state.shared.disorder_deck.pop(0)
            # Жетон модернизации на картах цивилизации
            if CardCategory.CIVILIZATION in cats and CardCategory.ORIGINS not in cats:
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
            if slot.card and target_cat in getattr(slot.card, 'categories', []):
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
        if CardCategory.REGION in getattr(card, 'categories', []):
            # Regions give resources
            state.player.resources.resource += 1

        if CardCategory.ORIGINS in getattr(card, 'categories', []):
            # Origins give population or allow progress
            state.player.resources.population += 1

        if CardCategory.CIVILIZATION in getattr(card, 'categories', []):
            # Civilisation cards give upgrade tokens
            state.player.resources.upgrade += 1

        if CardCategory.RAID in getattr(card, 'categories', []):
            # Raid: attack bot — bot loses a resource
            if card.card_type == CardType.ATTACK:
                state.bot.resource = max(0, state.bot.resource - 1)
                state.add_log("Набег! Бот теряет 1 ресурс")

        if CardCategory.GLORY in getattr(card, 'categories', []):
            # Glory cards go to chronicle (летопись)
            if card in state.player.play_area:
                state.player.play_area.remove(card)
            state.player.chronicle.append(card)

        # Apply typed on_play_actions
        for action in card.on_play_actions:
            if isinstance(action, GainResourceAction):
                state = self._apply_gain_resource_action(state, action)
            elif isinstance(action, AcquireCardAction):
                state = self._apply_acquire_card_action(state, action)
            elif isinstance(action, AppropriateCardAction):
                state = self._apply_appropriate_card_action(state, action)
            elif isinstance(action, ChoiceAction):
                state = self._apply_choice_action(state, action)

        return state

    def _apply_exploit_effect(self, state: GameState, card: Card) -> GameState:
        """Apply exploitation effect of a card"""
        # Generic exploit: gain resources based on card type
        if CardCategory.REGION in getattr(card, 'categories', []):
            state.player.resources.resource += 2
        elif CardCategory.ORIGINS in getattr(card, 'categories', []):
            state.player.resources.population += 1
            state.player.resources.resource += 1
        elif CardCategory.CIVILIZATION in getattr(card, 'categories', []):
            state.player.resources.upgrade += 1
        return state

    def _apply_acquire_card_action(self, state: GameState, action: AcquireCardAction) -> GameState:
        """Устанавливает pending_choice для выбора карты с рынка."""
        allowed = [c.value for c in action.allowed_categories]
        state.pending_choice = {
            "type": "acquire_from_market",
            "allowed_categories": allowed,
            "remaining": action.count,
        }
        state.add_log(f"Выберите карту с рынка ({', '.join(allowed)})")
        return state

    def _apply_appropriate_card_action(self, state: GameState, action: AppropriateCardAction) -> GameState:
        """Устанавливает pending_choice для присвоения карты с рынка или из колоды."""
        allowed = [c.value for c in action.allowed_categories]
        self._set_appropriate_pending(state, allowed, action.allowed_source_decks, action.count)
        return state

    def _set_appropriate_pending(self, state: GameState, categories: list, source_decks: list, count: int) -> None:
        """Устанавливает pending_choice для присвоения.
        Если категорий несколько — сначала запрашивает выбор категории.
        Основная колода всегда доступна.
        """
        if len(categories) > 1:
            state.pending_choice = {
                "type": "appropriate_select_category",
                "categories": categories,
                "source_decks": source_decks,
                "count": count,
            }
            state.add_log(f"Присвоение: выберите тип карты ({', '.join(categories)})")
        else:
            state.pending_choice = {
                "type": "appropriate",
                "allowed_categories": categories,
                "source_decks": source_decks,
                "include_main_deck": True,
                "remaining": count,
            }
            state.add_log(f"Присвоение: выберите карту ({', '.join(categories)})")

    def _apply_choice_action(self, state: GameState, action: ChoiceAction) -> GameState:
        """Устанавливает pending_choice для выбора игрока из нескольких опций."""
        options_data = []
        for opt in action.options:
            a = opt.action
            if isinstance(a, AcquireCardAction):
                action_data = {
                    "type": "acquire_from_market",
                    "categories": [c.value for c in a.allowed_categories],
                    "count": a.count,
                }
            elif isinstance(a, AppropriateCardAction):
                action_data = {
                    "type": "appropriate",
                    "categories": [c.value for c in a.allowed_categories],
                    "source_decks": a.allowed_source_decks,
                    "include_main_deck": a.include_main_deck,
                    "count": a.count,
                }
            else:
                continue
            options_data.append({
                "label": opt.label,
                "cost_population": opt.cost_population,
                "cost_resource": opt.cost_resource,
                "action": action_data,
            })
        state.pending_choice = {"type": "player_choice", "options": options_data}
        state.add_log("Выберите вариант действия")
        return state

    def choose_option(self, state: GameState, option_index: int) -> GameState:
        """Игрок выбирает вариант из player_choice: списывает стоимость, активирует действие."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "player_choice":
            raise ValueError("Нет активного выбора")
        options = pending.get("options", [])
        if option_index < 0 or option_index >= len(options):
            raise ValueError("Неверный индекс варианта")
        option = options[option_index]

        cost_pop = option.get("cost_population", 0)
        cost_res = option.get("cost_resource", 0)

        # Универсальное правило: нехватку населения покрывают жетоны прогресса 1:1,
        # нехватку ресурсов — жетоны прогресса 2:1 (1 жетон = 2 ресурса).
        pop_avail = state.player.resources.population
        res_avail = state.player.resources.resource
        upgrade_avail = state.player.resources.upgrade

        pop_pay = min(pop_avail, cost_pop)
        upgrade_for_pop = cost_pop - pop_pay

        res_pay = min(res_avail, cost_res)
        res_deficit = cost_res - res_pay
        upgrade_for_res = (res_deficit + 1) // 2  # каждые 2 недостающих ресурса = 1 жетон

        upgrade_pay = upgrade_for_pop + upgrade_for_res
        if upgrade_avail < upgrade_pay:
            raise ValueError(
                f"Недостаточно ресурсов: нужно {cost_pop} нас. и {cost_res} рес. "
                f"(есть {pop_avail} нас., {res_avail} рес., {upgrade_avail} жет. прогресса)"
            )

        state.player.resources.population -= pop_pay
        state.player.resources.resource -= res_pay
        state.player.resources.upgrade -= upgrade_pay

        parts = []
        if pop_pay > 0:
            parts.append(f"{pop_pay} нас.")
        if res_pay > 0:
            parts.append(f"{res_pay} рес.")
        if upgrade_pay > 0:
            parts.append(f"{upgrade_pay} жет. прогресса")
        if parts:
            state.add_log(f"Потрачено {', '.join(parts)}")

        action_data = option.get("action", {})
        action_type = action_data.get("type")

        if action_type == "acquire_from_market":
            state.pending_choice = {
                "type": "acquire_from_market",
                "allowed_categories": action_data.get("categories", []),
                "remaining": action_data.get("count", 1),
            }
            state.add_log(f"Выберите карту с рынка ({', '.join(action_data.get('categories', []))})")
        elif action_type == "appropriate":
            self._set_appropriate_pending(
                state,
                action_data.get("categories", []),
                action_data.get("source_decks", []),
                action_data.get("count", 1),
            )
        else:
            state.pending_choice = None

        return state

    def select_appropriate_category(self, state: GameState, category: str) -> GameState:
        """Игрок выбирает категорию при присвоении нескольких возможных типов карт."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "appropriate_select_category":
            raise ValueError("Нет активного выбора категории присвоения")
        allowed = pending.get("categories", [])
        if category not in allowed:
            raise ValueError(f"Недопустимая категория: {category}. Доступно: {allowed}")
        source_decks = pending.get("source_decks", [])
        count = pending.get("count", 1)
        self._set_appropriate_pending(state, [category], source_decks, count)
        return state

    def appropriate_from_deck(self, state: GameState, deck_name: str) -> GameState:
        """Присвоить верхнюю карту из заданной колоды или найти нужный тип в основной колоде."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "appropriate":
            raise ValueError("Нет активного присвоения")

        allowed_cats = pending.get("allowed_categories", [])
        source_decks = pending.get("source_decks", [])
        include_main_deck = pending.get("include_main_deck", False)

        deck_map = {
            "region": state.shared.region_deck,
            "origins": state.shared.origins_deck,
            "civilization": state.shared.civilization_deck,
        }

        if deck_name == "main":
            if not include_main_deck:
                raise ValueError("Основная колода недоступна для этого действия")
            main_deck = state.shared.main_deck
            found_idx = None
            for i, card in enumerate(main_deck):
                card_cats = [c.value for c in getattr(card, 'categories', [])]
                if any(c in allowed_cats for c in card_cats):
                    found_idx = i
                    break
            if found_idx is None:
                raise ValueError(f"В основной колоде нет карт типов: {', '.join(allowed_cats)}")
            found_card = main_deck.pop(found_idx)
            state.player.hand.append(found_card)
            state.add_log(f"Присвоена карта из основной колоды: {found_card.name}")
        else:
            if deck_name not in source_decks:
                raise ValueError(f"Колода '{deck_name}' недоступна для этого действия")
            deck = deck_map.get(deck_name)
            if deck is None:
                raise ValueError(f"Неизвестная колода: {deck_name}")
            if not deck:
                raise ValueError(f"Колода '{deck_name}' пуста")
            card = deck.pop(0)
            state.player.hand.append(card)
            state.add_log(f"Присвоена верхняя карта из колоды '{deck_name}': {card.name}")

        remaining = pending.get("remaining", 1) - 1
        if remaining <= 0:
            state.pending_choice = None
        else:
            pending["remaining"] = remaining

        state = self._check_end_conditions(state)
        return state

    def _apply_gain_resource_action(self, state: GameState, action: GainResourceAction) -> GameState:
        """Применяет действие GainResourceAction: добавляет ресурс игроку."""
        _RESOURCE_ATTR: dict = {
            ResourceType.MATERIAL:   "resource",
            ResourceType.POPULATION: "population",
            ResourceType.PROGRESS:   "upgrade",
            ResourceType.ACTION:     "action",
            ResourceType.EXPLOIT:    "exploit",
        }
        attr = _RESOURCE_ATTR[action.resource_type]
        setattr(state.player.resources, attr,
                getattr(state.player.resources, attr) + action.amount)
        state.add_log(f"+{action.amount} {action.resource_type.value}")
        return state

    # ── UTILS ──────────────────────────────────────────────────────────────────

    def _find_in_hand(self, state: GameState,
                       card_id: str) -> Optional[Card]:
        return next((c for c in state.player.hand if c.id == card_id), None)
