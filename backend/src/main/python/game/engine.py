"""
Game Engine — processes player and bot actions according to Imperium rules
"""
import random
from typing import List, Optional, Tuple
from .state import GameState, PlayerArea, MarketSlot, Resources
from .enums import (Period, GamePhase, TurnAction, EndCondition,
                    CardCategory, CardSubtype, Difficulty, ResourceType, CardLabel)
from .cards import Card, GainResourceAction, AcquireCardAction, AppropriateCardAction, ChoiceAction, PlayFromDiscardAction, DrawFromDeckOptionalAction, GainPerLabelAction, GainPerCategoryAction, StealResourceAction, ReturnExploitTokenOptionalAction, DestroyFromPlayAreaAction, LookAtGloryDeckAction, ExileFromMarketAction, ChronicleFromDiscardAction, MoveDiscardToDeckAction, SacredPathExploitAction, DrawUpToNFromDeckAction, ReturnCardToDeckTopAction, AllPlayersGainResourceAction, SolsticeOptionalGainProgressThenFateAction, ExploitSpendResourceDrawCardAction, SolsticeOptionalDiscardHandReturnDisorderAction, SpendResourceAction, BotGainsDisorderAction, SolsticeChoiceAction, SolsticeOptionalDiscardForChoiceAction, DrawThenDiscardChoiceAction, ExploitRecallLabelChoiceAction, GuessDeckCategoryAction, ChronicleFromHandAction
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

        # Pre-validate SpendResourceAction costs
        _SPEND_ATTR = {"MATERIAL": "resource", "POPULATION": "population", "PROGRESS": "upgrade",
                       "ACTION": "action", "EXPLOIT": "exploit"}
        for action in card.on_play_actions:
            if isinstance(action, SpendResourceAction):
                attr = _SPEND_ATTR.get(action.resource_type.name, "resource")
                available = getattr(state.player.resources, attr)
                if available < action.amount:
                    raise ValueError(
                        f"Недостаточно {action.resource_type.value}: нужно {action.amount}, есть {available}"
                    )

        # Move card from hand to play area (or discard if not ∞)
        state.player.hand.remove(card)
        state.player.cards_played_this_turn.append(card)

        # Apply card effect
        _mat_before_play = state.player.resources.resource
        state = self._apply_player_card_effect(state, card)
        # Trigger: Колодец-журавль fires when MATERIAL gained via WATER card
        if (state.player.resources.resource > _mat_before_play
                and CardLabel.WATER in getattr(card, 'labels', [])):
            state = self._check_well_crane_trigger(state, card.id)

        # Determine destination after play
        from .enums import CardType
        if card.card_type != CardType.PERMANENT:
            if card not in state.player.discard and card not in state.player.play_area \
                    and card not in state.player.chronicle:
                if card.goes_to_chronicle:
                    # Обязательно в летопись
                    state.player.chronicle.append(card)
                    state.add_log(f"Карта «{card.name}» занесена в летопись")
                else:
                    state.player.discard.append(card)
                    if card.can_be_chronicled:
                        # Запрос «занести в летопись?» — отложим если pending_choice занят
                        state.pending_chronicle_card_id = card.id
        else:
            # Permanent card: check if reinforcement is available
            if card.can_be_reinforced and card in state.player.play_area:
                state.pending_reinforce_card_id = card.id

        # Activate deferred choices (reinforce > chronicle, priority order)
        state = self._check_deferred_choices(state)

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

        if getattr(card, 'exploit_passive', '') == 'well_crane':
            raise ValueError(f"«{card.name}» активируется только автоматически по триггеру")

        # Проверяем дополнительные стоимости эксплуатации
        for action in card.exploit_actions:
            if isinstance(action, PlayFromDiscardAction) and action.cost_action > 0:
                if state.player.resources.action < action.cost_action:
                    raise ValueError(f"Нет жетонов действия (нужно {action.cost_action})")

        state.player.resources.exploit -= 1
        state.player.exploits_used_this_turn.append(card)

        _mat_before_exploit = state.player.resources.resource
        if card.exploit_actions:
            state = self._apply_exploit_card_actions(state, card)
        elif card.exploit_passive:
            # Purely passive exploit — no immediate action, effect is computed elsewhere
            state.add_log(f"Жетон эксплуатации размещён на «{card.name}» (пассивный эффект активирован)")
        else:
            state = self._apply_exploit_effect(state, card)
        # Trigger: Колодец-журавль fires when MATERIAL gained via WATER card
        if (state.player.resources.resource > _mat_before_exploit
                and CardLabel.WATER in getattr(card, 'labels', [])):
            state = self._check_well_crane_trigger(state, card.id)
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
            if card and self._is_disorder(card):
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
            if pending_type in ("player_choice", "acquire_from_market", "appropriate", "appropriate_select_category", "play_from_discard"):
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
                state = self._check_deferred_choices(state)
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

        # Check for deferred attacks (1REG12 passive)
        if state.pending_bot_attacks:
            state.pending_bot_turn_continuation = {
                "die_roll": die_roll,
                "set_aside_slot": set_aside_slot,
            }
            state = self._setup_recall_attack_choice(state)
            return state

        # Step 3: Update phase — place upgrade token on market
        state = self._bot_update_phase(state, die_roll, set_aside_slot)

        # Step 4: Refill bot hand
        state = self._bot_refill_hand(state)

        state = self._finish_bot_turn(state)
        return state

    def _finish_bot_turn(self, state: GameState) -> GameState:
        """Завершает ход бота: лог, проверка окончания, солнцестояние."""
        state.add_log("─── Конец хода бота ───")
        state = self._check_end_conditions(state)
        if state.phase != GamePhase.GAME_OVER:
            state.phase = GamePhase.SOLSTICE
            state = self._apply_solstice(state)
        return state

    def _setup_recall_attack_choice(self, state: GameState) -> GameState:
        """Устанавливает pending_choice для выбора: отозвать 1REG12 или нет."""
        attack_card_id = state.pending_bot_attacks[0]
        card = next((c for c in state.bot.bot_discard if c.id == attack_card_id), None)
        card_name = card.name if card else attack_card_id
        state.pending_choice = {
            "type": "recall_to_avoid_attack",
            "attack_card_id": attack_card_id,
            "attack_card_name": card_name,
            "recall_card_id": "1REG12",
        }
        state.add_log(f"Атака «{card_name}»: отозвать «Мыс» (1REG12), чтобы избежать?")
        return state

    def resolve_recall_to_avoid_attack(self, state: GameState, recall: bool) -> GameState:
        """Игрок решает: отозвать 1REG12 или принять атаку."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "recall_to_avoid_attack":
            raise ValueError("Нет активного выбора отзыва карты")

        attack_card_id = pending["attack_card_id"]
        state.pending_choice = None

        if recall:
            card_1reg12 = next((c for c in state.player.play_area if c.id == '1REG12'), None)
            if card_1reg12:
                state.player.play_area.remove(card_1reg12)
                state.player.hand.append(card_1reg12)
                state.add_log("«Мыс» (1REG12) отозван — атака предотвращена")
        else:
            attack_card = next((c for c in state.bot.bot_discard if c.id == attack_card_id), None)
            if attack_card:
                from .bot_logic import BotLogic
                state = BotLogic.resolve_card(state, attack_card, -1)

        state.pending_bot_attacks.pop(0)

        # If more attacks remain
        if state.pending_bot_attacks:
            has_1reg12 = any(c.id == '1REG12' for c in state.player.play_area)
            if has_1reg12:
                state = self._setup_recall_attack_choice(state)
                return state
            else:
                # Apply remaining attacks automatically — 1REG12 already recalled
                for cid in state.pending_bot_attacks:
                    attack_card = next((c for c in state.bot.bot_discard if c.id == cid), None)
                    if attack_card:
                        from .bot_logic import BotLogic
                        state = BotLogic.resolve_card(state, attack_card, -1)
                state.pending_bot_attacks = []

        # Resume bot turn
        cont = state.pending_bot_turn_continuation or {}
        state.pending_bot_turn_continuation = None
        state = self._bot_update_phase(state, cont.get("die_roll", 0), cont.get("set_aside_slot"))
        state = self._bot_refill_hand(state)
        state = self._finish_bot_turn(state)
        return state

    def _bot_play_card(self, state: GameState, card: Card,
                        slot_index: int) -> GameState:
        """Play one bot card using the bot action table."""
        from .bot_logic import BotLogic
        from .enums import CardType as CT

        # 1REG12 passive: defer attack if Мыс is in player's play_area
        if (card.card_type == CT.ATTACK and
                any(c.id == '1REG12' for c in state.player.play_area)):
            state.pending_bot_attacks.append(card.id)
            state.add_log(f"Бот играет атаку «{card.name}» — ожидает решения игрока")
        else:
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
        cards = [c for c in state.player.play_area if c.solstice_effect]
        if state.player.ability_card and state.player.ability_card.solstice_effect:
            cards.append(state.player.ability_card)
        state.pending_solstice_card_ids = [c.id for c in cards]
        return self._prompt_solstice_selection(state)

    def _prompt_solstice_selection(self, state: GameState) -> GameState:
        """Show available solstice cards; if none left, finish solstice."""
        if not state.pending_solstice_card_ids:
            return self._finish_solstice(state)
        from .state import _card_info
        available = []
        for cid in state.pending_solstice_card_ids:
            card = next((c for c in state.player.play_area if c.id == cid), None)
            if card is None and state.player.ability_card and state.player.ability_card.id == cid:
                card = state.player.ability_card
            if card:
                available.append(_card_info(card))
        if not available:
            return self._finish_solstice(state)
        state.pending_choice = {
            "type": "solstice_select_card",
            "available_cards": available,
        }
        state.add_log("Солнцестояние: выберите карту для активации")
        return state

    def resolve_solstice_select_card(self, state: GameState, card_id: str) -> GameState:
        """Player picks which solstice card to activate next."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "solstice_select_card":
            raise ValueError("Нет активного выбора карты солнцестояния")
        if card_id not in state.pending_solstice_card_ids:
            raise ValueError(f"Карта {card_id} не доступна для солнцестояния")
        state.pending_solstice_card_ids.remove(card_id)
        state.pending_choice = None
        card = next((c for c in state.player.play_area if c.id == card_id), None)
        if card is None and state.player.ability_card and state.player.ability_card.id == card_id:
            card = state.player.ability_card
        if card:
            state = self._apply_solstice_card_effect(state, card)
        if state.pending_choice is None:
            return self._prompt_solstice_selection(state)
        return state

    def _continue_solstice_effects(self, state: GameState) -> GameState:
        """Called after a solstice card's pending_choice is resolved — show next selection."""
        return self._prompt_solstice_selection(state)

    def _finish_solstice(self, state: GameState) -> GameState:
        """Complete solstice: advance round and reset player turn."""
        state.round_number += 1
        state.phase = GamePhase.PLAYER_TURN
        state.player.turn_action_chosen = None
        state.player.cards_played_this_turn = []
        state.player.exploits_used_this_turn = []
        state.player.ability_exploit_used = False
        _draw_to_hand(state.player, state.player.hand_limit)
        state.add_log(f"Начинается раунд {state.round_number}")
        return state

    def _apply_solstice_card_effect(self, state: GameState, card: Card) -> GameState:
        """Применяет эффект солнцестояния карты."""
        # Data-driven solstice actions
        for action in getattr(card, 'solstice_actions', []):
            if isinstance(action, SolsticeOptionalGainProgressThenFateAction):
                state.pending_choice = {
                    "type": "solstice_gain_progress_optional",
                    "card_id": card.id,
                    "card_name": card.name,
                    "amount": action.amount,
                }
                state.add_log(f"Солнцестояние «{card.name}»: вы МОЖЕТЕ взять {action.amount} жет. прогресса")
                return state
            elif isinstance(action, SolsticeOptionalDiscardHandReturnDisorderAction):
                disorder_in_discard = [c for c in state.player.discard if self._is_disorder(c)]
                if not state.player.hand or not disorder_in_discard:
                    state.add_log(f"Солнцестояние «{card.name}»: нет карт для обмена — пропущено")
                    continue
                from .state import _card_info
                state.pending_choice = {
                    "type": "solstice_discard_hand_return_disorder",
                    "card_id": card.id,
                    "card_name": card.name,
                    "available_hand_cards": [_card_info(c) for c in state.player.hand],
                    "available_disorder_cards": [_card_info(c) for c in disorder_in_discard],
                }
                state.add_log(f"Солнцестояние «{card.name}»: вы МОЖЕТЕ сбросить карту из руки, чтобы вернуть карту беспорядков из сброса")
                return state
            elif isinstance(action, SolsticeChoiceAction):
                from .state import _card_info
                options_out = []
                for opt in action.options:
                    inner = opt.get("action", {})
                    opt_data = {"label": opt.get("label", ""), "action": inner}
                    if inner.get("type") == "draw_disorder":
                        opt_data["can_draw"] = len(state.shared.disorder_deck) > 0
                    elif inner.get("type") == "discard_hand":
                        opt_data["available_hand_cards"] = [_card_info(c) for c in state.player.hand]
                    options_out.append(opt_data)
                state.pending_choice = {
                    "type": "solstice_choice",
                    "card_id": card.id,
                    "card_name": card.name,
                    "options": options_out,
                }
                labels = " ИЛИ ".join(o["label"] for o in action.options)
                state.add_log(f"Солнцестояние «{card.name}»: выберите — {labels}")
                return state
            elif isinstance(action, SolsticeOptionalDiscardForChoiceAction):
                from .state import _card_info
                if not state.player.hand:
                    state.add_log(f"Солнцестояние «{card.name}»: нет карт в руке — пропущено")
                    continue
                state.pending_choice = {
                    "type": "solstice_discard_for_reward",
                    "card_id": card.id,
                    "card_name": card.name,
                    "options": action.options,
                    "available_hand_cards": [_card_info(c) for c in state.player.hand],
                }
                state.add_log(f"Солнцестояние «{card.name}»: МОЖНО сбросить карту из руки и выбрать награду")
                return state
        # Fallback: legacy hardcoded effects
        if "метрополия" in card.name.lower():
            state.player.resources.resource += 1
        elif "акрополь" in card.name.lower():
            state.player.resources.population += 1
        return state

    def resolve_solstice_gain_progress(self, state: GameState, take: bool) -> GameState:
        """Игрок решает, брать ли жетоны прогресса в солнцестояние."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "solstice_gain_progress_optional":
            raise ValueError("Нет активного выбора солнцестояния")
        card_id = pending["card_id"]
        card_name = pending["card_name"]
        amount = pending["amount"]
        state.pending_choice = None

        if take:
            state.player.resources.upgrade += amount
            state.add_log(f"+{amount} жет. прогресса")
            state.pending_choice = {
                "type": "solstice_fate_choice",
                "card_id": card_id,
                "card_name": card_name,
            }
            state.add_log(f"Выберите судьбу карты «{card_name}»: разрушить или занести в летопись")
        else:
            state.add_log(f"Вы отказались от жетонов прогресса")
            state = self._continue_solstice_effects(state)
        return state

    def resolve_solstice_fate(self, state: GameState, choice: str) -> GameState:
        """choice: 'destroy' or 'chronicle'"""
        pending = state.pending_choice
        if not pending or pending.get("type") != "solstice_fate_choice":
            raise ValueError("Нет активного выбора судьбы карты")
        card_id = pending["card_id"]
        card_name = pending["card_name"]
        state.pending_choice = None

        card = next((c for c in state.player.play_area if c.id == card_id), None)
        if card is not None:
            state.player.play_area.remove(card)
            if choice == "destroy":
                state.player.discard.append(card)
                state.add_log(f"«{card_name}» разрушена → сброс")
            elif choice == "chronicle":
                state.player.chronicle.append(card)
                state.add_log(f"«{card_name}» занесена в летопись")
            else:
                raise ValueError(f"Неизвестный выбор: {choice}. Допустимо: 'destroy', 'chronicle'")
        state = self._continue_solstice_effects(state)
        return state

    def resolve_solstice_discard_hand_return_disorder(self, state: GameState, hand_card_id: Optional[str], disorder_card_id: Optional[str]) -> GameState:
        """hand_card_id=None означает отказ от эффекта."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "solstice_discard_hand_return_disorder":
            raise ValueError("Нет активного выбора солнцестояния")
        state.pending_choice = None

        if hand_card_id is None:
            state.add_log("Вы отказались от эффекта солнцестояния")
        else:
            hand_card = self._find_in_hand(state, hand_card_id)
            if hand_card is None:
                raise ValueError(f"Карта {hand_card_id} не найдена в руке")
            disorder_card = next((c for c in state.player.discard if c.id == disorder_card_id and self._is_disorder(c)), None)
            if disorder_card is None:
                raise ValueError(f"Карта беспорядков {disorder_card_id} не найдена в личном сбросе")
            state.player.hand.remove(hand_card)
            state.player.discard.append(hand_card)
            state.player.discard.remove(disorder_card)
            state.shared.disorder_deck.append(disorder_card)
            state.add_log(f"«{hand_card.name}» сброшена; «{disorder_card.name}» возвращена в колоду беспорядков")

        return self._continue_solstice_effects(state)

    def resolve_solstice_choice(
        self, state: GameState, option_index: int, card_ids: Optional[List[str]] = None
    ) -> GameState:
        """Игрок выбирает опцию solstice_choice по индексу. card_ids — для действий, требующих выбора карт."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "solstice_choice":
            raise ValueError("Нет активного выбора солнцестояния")
        options = pending["options"]
        if option_index < 0 or option_index >= len(options):
            raise ValueError(f"Неверный индекс опции: {option_index}")
        action = options[option_index]["action"]
        action_type = action.get("type")
        state.pending_choice = None

        if action_type == "draw_disorder":
            count = action.get("count", 1)
            taken = 0
            for _ in range(count):
                if not state.shared.disorder_deck:
                    break
                card = state.shared.disorder_deck.pop(0)
                state.player.hand.append(card)
                taken += 1
            state.add_log(f"Взято {taken} карт беспорядков в руку" if taken else "Колода беспорядков пуста")

        elif action_type == "discard_hand":
            count = action.get("count", 1)
            ids = card_ids or []
            if len(ids) != count:
                raise ValueError(f"Нужно выбрать ровно {count} карты для сброса, выбрано {len(ids)}")
            for cid in ids:
                card = self._find_in_hand(state, cid)
                if card is None:
                    raise ValueError(f"Карта {cid} не найдена в руке")
                state.player.hand.remove(card)
                state.player.discard.append(card)
            state.add_log(f"Сброшено {count} карты из руки")

        else:
            raise ValueError(f"Неизвестный тип действия в solstice_choice: {action_type}")

        return self._continue_solstice_effects(state)

    def resolve_solstice_discard_for_reward(
        self, state: GameState, hand_card_id: Optional[str]
    ) -> GameState:
        """Шаг 1: игрок выбирает карту из руки для сброса (None — пропустить)."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "solstice_discard_for_reward":
            raise ValueError("Нет активного выбора солнцестояния")
        options = pending["options"]
        state.pending_choice = None

        if hand_card_id is None:
            state.add_log("Вы отказались от эффекта солнцестояния (сброс карты)")
            return self._continue_solstice_effects(state)

        card = self._find_in_hand(state, hand_card_id)
        if card is None:
            raise ValueError(f"Карта {hand_card_id} не найдена в руке")
        state.player.hand.remove(card)
        state.player.discard.append(card)
        state.add_log(f"«{card.name}» сброшена — выберите награду")

        from .state import _card_info
        state.pending_choice = {
            "type": "solstice_discard_reward_choice",
            "options": [{"label": o["label"], "action": o["action"]} for o in options],
        }
        return state

    def resolve_solstice_discard_reward_choice(
        self, state: GameState, option_index: int
    ) -> GameState:
        """Шаг 2: игрок выбирает награду после сброса карты."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "solstice_discard_reward_choice":
            raise ValueError("Нет активного выбора награды")
        options = pending["options"]
        if option_index < 0 or option_index >= len(options):
            raise ValueError(f"Неверный индекс опции: {option_index}")
        action = options[option_index]["action"]
        action_type = action.get("type")
        state.pending_choice = None

        _RESOURCE_ATTR = {"MATERIAL": "resource", "POPULATION": "population",
                          "PROGRESS": "upgrade", "ACTION": "action"}
        if action_type == "gain_resource":
            rt_name = action.get("resource_type", "MATERIAL")
            amount = action.get("amount", 1)
            attr = _RESOURCE_ATTR.get(rt_name, "resource")
            setattr(state.player.resources, attr, getattr(state.player.resources, attr) + amount)
            state.add_log(f"+{amount} {rt_name}")
        elif action_type == "draw_from_deck":
            count = action.get("count", 1)
            drawn = 0
            for _ in range(count):
                if not state.player.deck:
                    break
                state.player.hand.append(state.player.deck.pop(0))
                drawn += 1
            state.add_log(f"Взято {drawn} карт(ы) из личной колоды" if drawn else "Личная колода пуста")
        else:
            raise ValueError(f"Неизвестный тип награды: {action_type}")

        return self._continue_solstice_effects(state)

    # ── PLAYER UPDATE PHASE ─────────────────────────────────────────────────────

    def _end_player_turn(self, state: GameState) -> GameState:
        """
        Фаза обновления для игрока:
        1. Положить жетон > на карту рынка (игрок выбирает)
        2. Убрать жетоны ● и X с карты периода
        3. Сбросить любые карты с руки (handled by UI)
        4. Добрать до 5 карт
        Also trigger deck shuffle if needed.
        """
        # Ask player to choose which market slot gets the upgrade token
        if state.shared.market:
            state.pending_choice = {"type": "place_upgrade_token"}
            return state

        return self._finish_end_player_turn(state)

    def place_upgrade_token(self, state: GameState, slot_index: int) -> GameState:
        """Игрок выбирает слот рынка для жетона прогресса."""
        if slot_index < 0 or slot_index >= len(state.shared.market):
            raise ValueError("Неверный индекс слота рынка")
        slot = state.shared.market[slot_index]
        if slot.card is None:
            raise ValueError("В выбранном слоте нет карты")
        slot.upgrade_tokens += 1
        state.add_log(f"Жетон прогресса помещён на «{slot.card.name}»")
        state.pending_choice = None
        return self._finish_end_player_turn(state)

    def _finish_end_player_turn(self, state: GameState) -> GameState:
        """Завершает фазу обновления игрока после размещения жетона прогресса."""
        # Снять жетон эксплуатации с колоды усиления и вернуть в запас
        if state.player.boost_top_token:
            state.player.boost_top_token = False
            state.player.resources.exploit += 1
            state.add_log("Жетон эксплуатации снят с колоды усиления")

        # Reset tokens
        state.player.resources.action = 3
        state.player.resources.exploit = 5

        # TODO: bot turn not implemented — skip directly to next player turn
        state.add_log("─── Ход бота пропущен ───")
        return self._apply_solstice(state)

    # ── CARD SHUFFLING ─────────────────────────────────────────────────────────

    def _reshuffle_player_deck(self, state: GameState) -> GameState:
        """Перетасовка личной колоды — делегирует в _draw_to_hand через setup."""
        # Логика переноса карты усиления и перемешивания встроена в _draw_to_hand.
        # Этот метод оставлен для явного вызова, если колода пуста и нужно перемешать
        # сброс прямо сейчас (без добора карт).
        from .setup import _draw_to_hand as _dth
        player = state.player
        if not player.deck and player.discard:
            if player.boost_deck and not player.boost_top_token:
                top_boost = player.boost_deck.pop(0)
                player.discard.append(top_boost)
                state.add_log(f"Карта усиления «{top_boost.name}» перемещена в сброс")
                if getattr(top_boost, 'subtype', None) == CardSubtype.TRANSFORMATION:
                    player.period = Period.CIVILIZATION
                    state.add_log("Период изменён на Цивилизацию!")
                elif player.boost_deck:
                    player.boost_top_token = True
                    player.resources.exploit -= 1
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
            if card.vp_in_chronicle is not None:
                if any(c.id == card.id for c in player.chronicle):
                    vp += card.vp_in_chronicle
                else:
                    vp += card.vp_out_of_chronicle
            elif card.vp_condition or card.vp_per_condition:
                vp += self._evaluate_condition_vp(state, card, is_player=True) * card.vp_per_condition_value

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
            if card.vp_condition or card.vp_per_condition:
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
        elif "glory" in cond:
            count = sum(1 for c in all_cards if CardCategory.GLORY in getattr(c, 'categories', []))
            return count
        elif cond == "label_grain":
            from .enums import CardLabel
            count = sum(
                lb == CardLabel.GRAIN
                for c in all_cards
                for lb in getattr(c, 'labels', [])
            )
            return count
        elif cond == "label_water":
            from .enums import CardLabel
            count = sum(
                lb == CardLabel.WATER
                for c in all_cards
                for lb in getattr(c, 'labels', [])
            )
            return count
        elif cond == "label_grain_and_water":
            from .enums import CardLabel
            count = sum(
                lb in (CardLabel.GRAIN, CardLabel.WATER)
                for c in all_cards
                for lb in getattr(c, 'labels', [])
            )
            return count
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

        # Mandatory card draw
        for _ in range(card.draws_cards):
            if state.player.deck:
                drawn = state.player.deck.pop(0)
                state.player.hand.append(drawn)
                state.add_log(f"Взята карта из колоды в руку: {drawn.name}")
            else:
                state.add_log("Личная колода пуста — карту взять невозможно")
                break

        # Apply typed on_play_actions; if a pending_choice is set mid-loop, queue the rest
        actions_remaining = list(card.on_play_actions)
        while actions_remaining:
            action = actions_remaining.pop(0)
            had_pending = state.pending_choice is not None
            if isinstance(action, GainResourceAction):
                state = self._apply_gain_resource_action(state, action)
            elif isinstance(action, StealResourceAction):
                state = self._apply_steal_resource_action(state, action)
            elif isinstance(action, ReturnExploitTokenOptionalAction):
                state = self._apply_return_exploit_token_optional_action(state)
            elif isinstance(action, DrawFromDeckOptionalAction):
                state = self._apply_draw_from_deck_optional_action(state)
            elif isinstance(action, AcquireCardAction):
                state = self._apply_acquire_card_action(state, action)
            elif isinstance(action, AppropriateCardAction):
                state = self._apply_appropriate_card_action(state, action)
            elif isinstance(action, ChoiceAction):
                state = self._apply_choice_action(state, action)
            elif isinstance(action, ChronicleFromDiscardAction):
                state = self._apply_chronicle_from_discard_action(state, action)
            elif isinstance(action, ExileFromMarketAction):
                state = self._apply_exile_from_market_action(state)
            elif isinstance(action, DestroyFromPlayAreaAction):
                state = self._apply_destroy_from_play_area_action(state, action)
            elif isinstance(action, LookAtGloryDeckAction):
                state = self._apply_look_at_glory_deck_action(state, action)
            elif isinstance(action, MoveDiscardToDeckAction):
                state = self._apply_move_discard_to_deck_action(state, action)
            elif isinstance(action, DrawUpToNFromDeckAction):
                state = self._apply_draw_up_to_n_from_deck_action(state, action)
            elif isinstance(action, ReturnCardToDeckTopAction):
                state = self._apply_return_card_to_deck_top_action(state)
            elif isinstance(action, SpendResourceAction):
                state = self._apply_spend_resource_action(state, action)
            elif isinstance(action, BotGainsDisorderAction):
                state = self._apply_bot_gains_disorder_action(state, action)
            elif isinstance(action, DrawThenDiscardChoiceAction):
                state = self._apply_draw_then_discard_choice_action(state, action)
            elif isinstance(action, GuessDeckCategoryAction):
                state = self._apply_guess_deck_category_action(state, action)
            # If a new pending_choice appeared, queue remaining actions and stop
            if not had_pending and state.pending_choice is not None and actions_remaining:
                state.pending_card_play_actions.extend(
                    self._serialize_on_play_action(a) for a in actions_remaining
                )
                break

        return state

    def _apply_exploit_card_actions(self, state: GameState, card: Card) -> GameState:
        """Применяет кастомные exploit_actions карты."""
        from .state import _card_info
        for action in card.exploit_actions:
            if isinstance(action, PlayFromDiscardAction):
                if action.cost_action > 0:
                    state.player.resources.action -= action.cost_action
                    state.add_log(f"−{action.cost_action} жетон(ов) действия")

                allowed_cats = action.allowed_categories
                available = [
                    c for c in state.player.discard
                    if any(cat in getattr(c, 'categories', []) for cat in allowed_cats)
                ]

                if not available:
                    state.add_log("Нет подходящих карт в сбросе")
                    continue

                cat_values = [c.value for c in allowed_cats]
                state.pending_choice = {
                    "type": "play_from_discard",
                    "allowed_categories": cat_values,
                    "available_cards": [_card_info(c) for c in available],
                    "count": action.count,
                    "remaining": action.count,
                }
                state.add_log(f"Выберите карту из сброса ({', '.join(cat_values)})")
            elif isinstance(action, SacredPathExploitAction):
                state = self._apply_sacred_path_exploit_action(state, card)
            elif isinstance(action, AllPlayersGainResourceAction):
                _RESOURCE_ATTR = {
                    "MATERIAL": ("resource", "resource"),
                    "POPULATION": ("population", "population"),
                    "PROGRESS": ("upgrade", "upgrade"),
                }
                player_attr, bot_attr = _RESOURCE_ATTR.get(action.resource_type.name, ("resource", "resource"))
                setattr(state.player.resources, player_attr, getattr(state.player.resources, player_attr) + action.amount)
                setattr(state.bot, bot_attr, getattr(state.bot, bot_attr) + action.amount)
                state.add_log(f"Все игроки получают +{action.amount} {action.resource_type.value}")
            elif isinstance(action, ExploitSpendResourceDrawCardAction):
                _RESOURCE_ATTR = {"MATERIAL": "resource", "POPULATION": "population", "PROGRESS": "upgrade"}
                attr = _RESOURCE_ATTR.get(action.resource_type.name, "resource")
                available = getattr(state.player.resources, attr)
                if available < action.resource_cost:
                    raise ValueError(f"Недостаточно ресурсов: нужно {action.resource_cost} {action.resource_type.value}")
                setattr(state.player.resources, attr, available - action.resource_cost)
                state.add_log(f"−{action.resource_cost} {action.resource_type.value}")
                drawn = 0
                for _ in range(action.draw_count):
                    if not state.player.deck:
                        break
                    card_drawn = state.player.deck.pop(0)
                    state.player.hand.append(card_drawn)
                    drawn += 1
                if drawn > 0:
                    state.add_log(f"Взято {drawn} карт из личной колоды")
                else:
                    state.add_log("Личная колода пуста")
            elif isinstance(action, GainResourceAction):
                state = self._apply_gain_resource_action(state, action)
            elif isinstance(action, ExploitRecallLabelChoiceAction):
                state = self._apply_exploit_recall_label_choice(state, action)
        return state

    def _apply_exploit_recall_label_choice(self, state: GameState, action: ExploitRecallLabelChoiceAction) -> GameState:
        """Эксплуатация с отзывом карты по метке: строит pending_choice с доступными картами для каждой опции."""
        from .state import _card_info
        from .enums import CardLabel
        options_out = []
        for opt in action.options:
            label_str = opt.get("label", "")
            try:
                target_label = CardLabel(label_str)
            except ValueError:
                continue
            available = [c for c in state.player.play_area if target_label in getattr(c, 'labels', [])]
            options_out.append({
                "label": label_str,
                "gains": opt.get("gains", []),
                "available_cards": [_card_info(c) for c in available],
            })
        state.pending_choice = {
            "type": "exploit_recall_label_choice",
            "options": options_out,
        }
        state.add_log("Выберите опцию эксплуатации: отозвать карту по метке")
        return state

    def resolve_exploit_recall_choice(self, state: GameState, option_index: int, card_id: str) -> GameState:
        """Игрок выбирает опцию и карту для отзыва."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "exploit_recall_label_choice":
            raise ValueError("Нет активного выбора эксплуатации с отзывом")
        options = pending["options"]
        if option_index < 0 or option_index >= len(options):
            raise ValueError(f"Неверный индекс опции: {option_index}")
        opt = options[option_index]
        # Find and recall card from play area
        card = next((c for c in state.player.play_area if c.id == card_id), None)
        if card is None:
            raise ValueError(f"Карта {card_id} не найдена в игровой области")
        from .enums import CardLabel
        label_str = opt["label"]
        target_label = CardLabel(label_str)
        if target_label not in getattr(card, 'labels', []):
            raise ValueError(f"Карта «{card.name}» не имеет метки {label_str.upper()}")
        state.player.play_area.remove(card)
        state.player.hand.append(card)
        state.add_log(f"«{card.name}» отозвана в руку")
        # Apply gains
        _RESOURCE_ATTR = {"MATERIAL": "resource", "POPULATION": "population",
                          "PROGRESS": "upgrade", "ACTION": "action", "EXPLOIT": "exploit"}
        for gain in opt.get("gains", []):
            rt_name = gain.get("resource_type", "MATERIAL")
            amount = gain.get("amount", 0)
            attr = _RESOURCE_ATTR.get(rt_name, "resource")
            setattr(state.player.resources, attr, getattr(state.player.resources, attr) + amount)
            state.add_log(f"+{amount} {rt_name}")
        state.pending_choice = None
        state = self._check_deferred_choices(state)
        return state

    def select_play_from_discard(self, state: GameState, card_id: str) -> GameState:
        """
        Игрок выбирает карту из личного сброса для розыгрыша в игровую область
        (эффект эксплуатации карты типа play_from_discard).
        """
        pending = state.pending_choice
        if not pending or pending.get("type") != "play_from_discard":
            raise ValueError("Нет активного выбора карты из сброса")

        allowed_cats = pending.get("allowed_categories", [])
        card = next((c for c in state.player.discard if c.id == card_id), None)
        if card is None:
            raise ValueError(f"Карта {card_id} не найдена в сбросе")

        card_cats = [c.value for c in getattr(card, 'categories', [])]
        if not any(c in allowed_cats for c in card_cats):
            raise ValueError(f"Карта не подходит по категории. Требуется: {', '.join(allowed_cats)}")

        state.player.discard.remove(card)
        state = self._apply_player_card_effect(state, card)
        state.add_log(f"Сыграна из сброса: {card.name}")

        remaining = pending.get("remaining", 1) - 1
        if remaining <= 0:
            state.pending_choice = None
        else:
            pending["remaining"] = remaining
            # Обновляем список доступных карт
            from .state import _card_info
            available = [
                c for c in state.player.discard
                if any(cat in [cv.value for cv in getattr(c, 'categories', [])]
                       for cat in allowed_cats)
            ]
            pending["available_cards"] = [_card_info(c) for c in available]

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
                label = opt.label
            elif isinstance(a, AppropriateCardAction):
                action_data = {
                    "type": "appropriate",
                    "categories": [c.value for c in a.allowed_categories],
                    "source_decks": a.allowed_source_decks,
                    "include_main_deck": a.include_main_deck,
                    "count": a.count,
                }
                label = opt.label
            elif isinstance(a, GainPerLabelAction):
                count = sum(
                    1 for c in state.player.play_area
                    for lb in getattr(c, 'labels', []) if lb.value == a.label
                )
                action_data = {
                    "type": "gain_resource",
                    "resource_type": a.resource_type.name,
                    "amount": count,
                }
                label = f"{opt.label} ({count})"
            elif isinstance(a, GainPerCategoryAction):
                count = sum(
                    1 for c in state.player.play_area
                    if a.category in getattr(c, 'categories', [])
                )
                action_data = {
                    "type": "gain_resource",
                    "resource_type": a.resource_type.name,
                    "amount": count,
                }
                label = f"{opt.label} ({count})"
            elif isinstance(a, GainResourceAction):
                action_data = {
                    "type": "gain_resource",
                    "resource_type": a.resource_type.name,
                    "amount": a.amount,
                }
                label = opt.label
            elif isinstance(a, DrawUpToNFromDeckAction):
                action_data = {"type": "draw_up_to_n_from_deck", "count": a.count}
                label = opt.label
            elif isinstance(a, ChronicleFromDiscardAction):
                action_data = {"type": "chronicle_from_discard", "optional": a.optional}
                label = opt.label
            elif isinstance(a, ChronicleFromHandAction):
                action_data = {"type": "chronicle_from_hand", "optional": a.optional}
                label = opt.label
            else:
                continue
            options_data.append({
                "label": label,
                "cost_population": opt.cost_population,
                "cost_resource": opt.cost_resource,
                "action": action_data,
                "opponent_gains_progress": opt.opponent_gains_progress,
                "opponent_recalls_region": opt.opponent_recalls_region,
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

        opponent_progress = option.get("opponent_gains_progress", 0)
        if opponent_progress > 0:
            state.bot.upgrade += opponent_progress
            state.add_log(f"Бот получает {opponent_progress} жет. прогресса")

        recalls = option.get("opponent_recalls_region", 0)
        for _ in range(recalls):
            region_card = next(
                (c for c in state.bot.play_area if CardCategory.REGION in getattr(c, "categories", [])),
                None
            )
            if region_card:
                state.bot.play_area.remove(region_card)
                state.bot.bot_discard.append(region_card)
                state.add_log(f"Бот отзывает регион «{region_card.name}»")
            else:
                state.add_log("У бота нет карт регионов для отзыва")

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
        elif action_type == "gain_resource":
            _RESOURCE_ATTR: dict = {
                "MATERIAL": "resource", "POPULATION": "population",
                "PROGRESS": "upgrade", "ACTION": "action", "EXPLOIT": "exploit",
            }
            attr = _RESOURCE_ATTR.get(action_data.get("resource_type", "MATERIAL"), "resource")
            amount = action_data.get("amount", 0)
            setattr(state.player.resources, attr, getattr(state.player.resources, attr) + amount)
            state.add_log(f"+{amount} {action_data.get('resource_type', 'MATERIAL')}")
            state.pending_choice = None
            state = self._check_deferred_choices(state)
        elif action_type == "draw_up_to_n_from_deck":
            state.pending_choice = None
            state = self._apply_draw_up_to_n_from_deck_action(
                state, DrawUpToNFromDeckAction(count=action_data.get("count", 1))
            )
        elif action_type == "chronicle_from_discard":
            state.pending_choice = None
            state = self._apply_chronicle_from_discard_action(
                state, ChronicleFromDiscardAction(optional=action_data.get("optional", False))
            )
        elif action_type == "chronicle_from_hand":
            state.pending_choice = None
            state = self._apply_chronicle_from_hand_action(
                state, ChronicleFromHandAction(optional=action_data.get("optional", False))
            )
        else:
            state.pending_choice = None
            state = self._check_deferred_choices(state)

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
            state = self._check_deferred_choices(state)
        else:
            pending["remaining"] = remaining

        state = self._check_end_conditions(state)
        return state

    def resolve_chronicle_choice(self, state: GameState, send_to_chronicle: bool) -> GameState:
        """Игрок решает: занести карту в летопись или оставить в сбросе."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "chronicle_choice":
            raise ValueError("Нет активного выбора летописи")
        card_id = pending["card_id"]
        card_name = pending["card_name"]
        state.pending_choice = None

        if send_to_chronicle:
            card = next((c for c in state.player.discard if c.id == card_id), None)
            if card:
                state.player.discard.remove(card)
                state.player.chronicle.append(card)
                state.add_log(f"Карта «{card_name}» занесена в летопись")
            else:
                state.add_log(f"Карта «{card_name}» не найдена в сбросе — пропущено")
        else:
            state.add_log(f"Карта «{card_name}» оставлена в сбросе")

        return state

    def resolve_reinforce_choice(self, state: GameState, reinforce: bool) -> GameState:
        """Игрок решает: укрепить карту или нет."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "reinforce_choice":
            raise ValueError("Нет активного выбора укрепления")
        target_id = pending["card_id"]
        target_name = pending["card_name"]
        state.pending_choice = None

        if reinforce:
            excluded = [c.id for c in state.player.hand if getattr(c, 'cannot_be_reinforcement', False)]
            state.pending_choice = {
                "type": "reinforce_select_card",
                "target_card_id": target_id,
                "target_card_name": target_name,
                "excluded_card_ids": excluded,
            }
            state.add_log(f"Выберите карту из руки для укрепления «{target_name}»")
        else:
            state.add_log(f"Укрепление «{target_name}» пропущено")
            state = self._check_deferred_choices(state)

        return state

    def reinforce_with_card(self, state: GameState, hand_card_id: str) -> GameState:
        """Игрок выбирает карту из руки для укрепления."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "reinforce_select_card":
            raise ValueError("Нет активного выбора карты укрепления")
        target_id = pending["target_card_id"]
        target_name = pending["target_card_name"]

        # Find card in hand (can be from any period — no period restriction for reinforcement)
        reinf_card = next((c for c in state.player.hand if c.id == hand_card_id), None)
        if reinf_card is None:
            raise ValueError(f"Карта {hand_card_id} не найдена в руке")
        if getattr(reinf_card, 'cannot_be_reinforcement', False):
            raise ValueError(f"Карта «{reinf_card.name}» не может быть картой укрепления")

        # Check target is in play area
        target = next((c for c in state.player.play_area if c.id == target_id), None)
        if target is None:
            raise ValueError(f"Карта-цель {target_id} не найдена в игровой области")

        state.player.hand.remove(reinf_card)
        state.player.reinforcements[target_id] = reinf_card
        state.pending_choice = None
        state.add_log(f"«{reinf_card.name}» укрепляет «{target_name}»")

        state = self._check_deferred_choices(state)
        return state

    def _check_deferred_choices(self, state: GameState) -> GameState:
        """Активирует следующий отложенный выбор, если pending_choice свободен.
        Порядок приоритетов: очередь действий карты → укрепление → летопись.
        """
        if state.pending_choice is not None:
            return state

        # Выполняем следующее действие из очереди розыгрыша карты
        if state.pending_card_play_actions:
            action_data = state.pending_card_play_actions.pop(0)
            state = self._execute_queued_action(state, action_data)
            return state

        if state.pending_reinforce_card_id:
            card_id = state.pending_reinforce_card_id
            state.pending_reinforce_card_id = None
            card = next((c for c in state.player.play_area if c.id == card_id), None)
            if card:
                state.pending_choice = {
                    "type": "reinforce_choice",
                    "card_id": card.id,
                    "card_name": card.name,
                }
            return state

        if state.pending_chronicle_card_id:
            card_id = state.pending_chronicle_card_id
            state.pending_chronicle_card_id = None
            card = next((c for c in state.player.discard if c.id == card_id), None)
            if card:
                state.pending_choice = {
                    "type": "chronicle_choice",
                    "card_id": card.id,
                    "card_name": card.name,
                }

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

    def _apply_draw_from_deck_optional_action(self, state: GameState) -> GameState:
        """Бот тянет верхнюю карту из колоды автоматически; игрок получает выбор."""
        # Бот тянет автоматически (если есть карты)
        if state.bot.bot_deck:
            drawn = state.bot.bot_deck.pop(0)
            placed = False
            for i, slot in enumerate(state.bot.hand_slots):
                if slot is None:
                    state.bot.hand_slots[i] = drawn
                    placed = True
                    break
            if not placed:
                # Нет свободного слота — возвращаем карту в низ колоды
                state.bot.bot_deck.append(drawn)
                state.add_log("Бот: нет свободного слота — карта из колоды не взята")
            else:
                state.add_log(f"Бот берёт карту из личной колоды")

        # Игрок получает выбор
        state.pending_choice = {
            "type": "draw_from_deck_optional",
            "can_draw": len(state.player.deck) > 0,
        }
        state.add_log("Вы МОЖЕТЕ взять 1 карту из личной колоды в руку")
        return state

    def _apply_steal_resource_action(self, state: GameState, action: StealResourceAction) -> GameState:
        """Бот теряет указанное количество ресурсов."""
        if action.resource_type == ResourceType.MATERIAL:
            lost = min(state.bot.resource, action.amount)
            state.bot.resource = max(0, state.bot.resource - action.amount)
            state.add_log(f"Набег: бот теряет {lost} ресурс(а)")
        return state

    def _apply_return_exploit_token_optional_action(self, state: GameState) -> GameState:
        """Предлагает игроку вернуть жетон эксплуатации с карты игровой области в запас."""
        from .state import _card_info
        available = [c for c in state.player.exploits_used_this_turn
                     if c in state.player.play_area]
        if not available:
            state.add_log("Нет карт с жетонами эксплуатации в игровой области")
            return state
        state.pending_choice = {
            "type": "return_exploit_token_optional",
            "available_cards": [_card_info(c) for c in available],
        }
        state.add_log("Вы МОЖЕТЕ вернуть жетон эксплуатации с карты в запас")
        return state

    def return_exploit_token(self, state: GameState, card_id: Optional[str]) -> GameState:
        """Игрок возвращает жетон эксплуатации с выбранной карты в запас, или пропускает."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "return_exploit_token_optional":
            raise ValueError("Нет активного выбора возврата жетона эксплуатации")
        state.pending_choice = None
        if card_id:
            card = next((c for c in state.player.exploits_used_this_turn
                         if c.id == card_id and c in state.player.play_area), None)
            if card is None:
                raise ValueError(f"Карта {card_id} не найдена среди эксплуатированных карт игровой области")
            state.player.exploits_used_this_turn.remove(card)
            state.player.resources.exploit += 1
            state.add_log(f"Жетон эксплуатации с «{card.name}» возвращён в запас")
        else:
            state.add_log("Вы пропустили возврат жетона эксплуатации")
        state = self._check_deferred_choices(state)
        return state

    def resolve_draw_from_deck_optional(self, state: GameState, draw: bool) -> GameState:
        """Игрок отвечает на вопрос «взять карту из колоды?»."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "draw_from_deck_optional":
            raise ValueError("Нет активного выбора взятия карты из колоды")

        state.pending_choice = None

        if draw:
            if state.player.deck:
                card = state.player.deck.pop(0)
                state.player.hand.append(card)
                state.add_log(f"Вы берёте «{card.name}» из личной колоды в руку")
            else:
                state.add_log("В личной колоде нет карт")
        else:
            state.add_log("Вы отказались от взятия карты")

        state = self._check_deferred_choices(state)
        return state

    def _apply_chronicle_from_hand_action(self, state: GameState, action: ChronicleFromHandAction) -> GameState:
        """Предлагает занести карту из руки в летопись."""
        from .state import _card_info
        if not state.player.hand:
            state.add_log("Рука пуста — занесение в летопись пропущено")
            return state
        state.pending_choice = {
            "type": "chronicle_from_hand",
            "optional": action.optional,
            "available_cards": [_card_info(c) for c in state.player.hand],
        }
        msg = "Вы МОЖЕТЕ занести карту из руки в летопись" if action.optional else "Выберите карту из руки для занесения в летопись"
        state.add_log(msg)
        return state

    def chronicle_card_from_hand(self, state: GameState, card_id: Optional[str]) -> GameState:
        """Игрок выбирает карту из руки и заносит её в летопись. card_id=None — пропустить (только если optional)."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "chronicle_from_hand":
            raise ValueError("Нет активного выбора карты из руки для летописи")
        if card_id is None:
            if not pending.get("optional"):
                raise ValueError("Это действие обязательно — нельзя пропустить")
            state.add_log("Занесение в летопись пропущено")
            state.pending_choice = None
            state = self._check_deferred_choices(state)
            return state
        card = self._find_in_hand(state, card_id)
        if card is None:
            raise ValueError(f"Карта {card_id} не найдена в руке")
        state.player.hand.remove(card)
        state.player.chronicle.append(card)
        state.add_log(f"«{card.name}» занесена в летопись из руки")
        state.pending_choice = None
        state = self._check_deferred_choices(state)
        return state

    def _apply_chronicle_from_discard_action(self, state: GameState, action: 'ChronicleFromDiscardAction') -> GameState:
        """Предлагает занести карту из личного сброса в летопись."""
        from .state import _card_info
        if not state.player.discard:
            state.add_log("Личный сброс пуст — занесение в летопись пропущено")
            return state
        state.pending_choice = {
            "type": "chronicle_from_discard",
            "optional": action.optional,
            "available_cards": [_card_info(c) for c in state.player.discard],
        }
        msg = "Вы МОЖЕТЕ занести карту из сброса в летопись" if action.optional else "Выберите карту из сброса для занесения в летопись"
        state.add_log(msg)
        return state

    def chronicle_card_from_discard(self, state: GameState, card_id: Optional[str]) -> GameState:
        """Игрок выбирает карту из сброса и заносит её в летопись. card_id=None — пропустить (только если optional)."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "chronicle_from_discard":
            raise ValueError("Нет активного выбора карты из сброса для летописи")
        if card_id is None:
            if not pending.get("optional"):
                raise ValueError("Это действие обязательно — нельзя пропустить")
            state.add_log("Занесение в летопись пропущено")
            state.pending_choice = None
            state = self._check_deferred_choices(state)
            return state
        card = next((c for c in state.player.discard if c.id == card_id), None)
        if card is None:
            raise ValueError(f"Карта {card_id} не найдена в личном сбросе")
        state.player.discard.remove(card)
        state.player.chronicle.append(card)
        state.add_log(f"«{card.name}» занесена в летопись из сброса")
        state.pending_choice = None
        state = self._check_deferred_choices(state)
        return state

    def _apply_move_discard_to_deck_action(self, state: GameState, action: 'MoveDiscardToDeckAction') -> GameState:
        """Предлагает переместить карту из личного сброса на верх личной колоды."""
        from .state import _card_info
        if not state.player.discard:
            state.add_log("Личный сброс пуст — перемещение на верх колоды пропущено")
            return state
        state.pending_choice = {
            "type": "move_discard_to_deck",
            "optional": action.optional,
            "available_cards": [_card_info(c) for c in state.player.discard],
        }
        msg = "Вы МОЖЕТЕ переместить карту из сброса на верх личной колоды" if action.optional else "Выберите карту из сброса для перемещения на верх личной колоды"
        state.add_log(msg)
        return state

    def move_discard_to_deck_card(self, state: GameState, card_id: Optional[str]) -> GameState:
        """Игрок выбирает карту из сброса и кладёт её на верх личной колоды. card_id=None — пропустить."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "move_discard_to_deck":
            raise ValueError("Нет активного выбора карты из сброса для перемещения в колоду")
        if card_id is None:
            if not pending.get("optional"):
                raise ValueError("Это действие обязательно — нельзя пропустить")
            state.add_log("Перемещение на верх колоды пропущено")
            state.pending_choice = None
            state = self._check_deferred_choices(state)
            return state
        card = next((c for c in state.player.discard if c.id == card_id), None)
        if card is None:
            raise ValueError(f"Карта {card_id} не найдена в личном сбросе")
        state.player.discard.remove(card)
        state.player.deck.insert(0, card)
        state.add_log(f"«{card.name}» перемещена на верх личной колоды из сброса")
        state.pending_choice = None
        state = self._check_deferred_choices(state)
        return state

    def _apply_draw_up_to_n_from_deck_action(self, state: GameState, action: 'DrawUpToNFromDeckAction') -> GameState:
        """Берёт до N карт с верха личной колоды в руку."""
        drawn = 0
        for _ in range(action.count):
            if not state.player.deck:
                break
            card = state.player.deck.pop(0)
            state.player.hand.append(card)
            drawn += 1
        if drawn > 0:
            state.add_log(f"Взято {drawn} карт из личной колоды в руку")
        else:
            state.add_log("Личная колода пуста — карты не взяты")
        return state

    def _apply_return_card_to_deck_top_action(self, state: GameState) -> GameState:
        """Предлагает игроку выбрать карту из руки и вернуть её на верх личной колоды."""
        from .state import _card_info
        if not state.player.hand:
            state.add_log("Рука пуста — возврат карты на верх колоды пропущен")
            return state
        state.pending_choice = {
            "type": "return_card_to_deck_top",
            "available_cards": [_card_info(c) for c in state.player.hand],
        }
        state.add_log("Выберите карту из руки для возврата на верх личной колоды")
        return state

    def resolve_return_card_to_deck_top(self, state: GameState, card_id: str) -> GameState:
        """Игрок выбирает карту из руки и кладёт её на верх личной колоды."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "return_card_to_deck_top":
            raise ValueError("Нет активного выбора карты для возврата на верх колоды")
        card = self._find_in_hand(state, card_id)
        if card is None:
            raise ValueError(f"Карта {card_id} не найдена на руке")
        state.player.hand.remove(card)
        state.player.deck.insert(0, card)
        state.add_log(f"«{card.name}» возвращена на верх личной колоды")
        state.pending_choice = None
        state = self._check_deferred_choices(state)
        return state

    def _apply_spend_resource_action(self, state: GameState, action: SpendResourceAction) -> GameState:
        """Игрок тратит N ресурсов (валидация уже выполнена в play_card)."""
        _ATTR = {"MATERIAL": "resource", "POPULATION": "population", "PROGRESS": "upgrade",
                 "ACTION": "action", "EXPLOIT": "exploit"}
        attr = _ATTR.get(action.resource_type.name, "resource")
        setattr(state.player.resources, attr, getattr(state.player.resources, attr) - action.amount)
        state.add_log(f"−{action.amount} {action.resource_type.value}")
        return state

    def _apply_bot_gains_disorder_action(self, state: GameState, action: BotGainsDisorderAction) -> GameState:
        """Бот берёт N карт беспорядков из общей колоды."""
        taken = 0
        for _ in range(action.count):
            if not state.shared.disorder_deck:
                break
            card = state.shared.disorder_deck.pop(0)
            state.bot.bot_discard.append(card)
            taken += 1
        if taken > 0:
            state.add_log(f"Бот получает {taken} карт беспорядков")
        else:
            state.add_log("Колода беспорядков пуста — бот не получает карты беспорядков")
        return state

    def _apply_guess_deck_category_action(self, state: GameState, action: GuessDeckCategoryAction) -> GameState:
        """Игрок называет категорию — устанавливает pending_choice для выбора."""
        state.pending_choice = {
            "type": "guess_deck_category",
            "allowed_categories": action.allowed_categories,
        }
        state.add_log("Назовите категорию карты: регион, истоки, цивилизация или набеги")
        return state

    def resolve_guess_deck_category(self, state: GameState, category: str) -> GameState:
        """Вскрывает верхнюю карту основной колоды: совпала — в руку, нет — изгнать."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "guess_deck_category":
            raise ValueError("Нет активного выбора категории")
        allowed = pending["allowed_categories"]
        if category not in allowed:
            raise ValueError(f"Недопустимая категория '{category}'. Допустимо: {', '.join(allowed)}")
        state.pending_choice = None

        if not state.shared.main_deck:
            state.add_log("Основная колода пуста — карта не вскрыта")
            state = self._check_deferred_choices(state)
            return state

        revealed = state.shared.main_deck.pop(0)
        card_cats = [c.value for c in getattr(revealed, 'categories', [])]
        state.add_log(f"Вскрыта карта «{revealed.name}» (категории: {', '.join(card_cats) or '—'})")

        if category in card_cats:
            state.player.hand.append(revealed)
            state.add_log(f"Совпадение! «{revealed.name}» добавлена в руку")
        else:
            state.shared.exile_pile.append(revealed)
            state.add_log(f"Не совпало. «{revealed.name}» изгнана из игры")

        state = self._check_deferred_choices(state)
        return state

    def _check_well_crane_trigger(self, state: GameState, source_card_id: str) -> GameState:
        """Автоматически активирует «Колодец-журавль» если он в игровой области и доступен."""
        well_crane = next(
            (c for c in state.player.play_area
             if getattr(c, 'exploit_passive', '') == 'well_crane'
             and c not in state.player.exploits_used_this_turn
             and c.id != source_card_id),
            None
        )
        if well_crane is None or state.player.resources.exploit <= 0:
            return state
        state.player.resources.exploit -= 1
        state.player.exploits_used_this_turn.append(well_crane)
        state.player.resources.upgrade += 1
        state.add_log(f"«{well_crane.name}»: триггер — +1 жетон прогресса (MATERIAL с карты WATER)")
        return state

    def _apply_draw_then_discard_choice_action(self, state: GameState, action: DrawThenDiscardChoiceAction) -> GameState:
        """Берёт draw_count карт в руку, предлагает сбросить discard_count из них."""
        from .state import _card_info
        drawn_ids = []
        for _ in range(action.draw_count):
            if not state.player.deck:
                break
            card = state.player.deck.pop(0)
            state.player.hand.append(card)
            drawn_ids.append(card.id)
        state.add_log(f"Взято {len(drawn_ids)} карт(ы) из колоды")
        if len(drawn_ids) <= action.draw_count - action.discard_count:
            # Взяли меньше, чем нужно сбросить — сбрасывать нечего
            return state
        available = [c for c in state.player.hand if c.id in drawn_ids]
        state.pending_choice = {
            "type": "discard_from_drawn",
            "drawn_card_ids": drawn_ids,
            "discard_count": action.discard_count,
            "available_cards": [_card_info(c) for c in available],
        }
        state.add_log(f"Выберите {action.discard_count} карту для сброса из взятых")
        return state

    def resolve_draw_discard_choice(self, state: GameState, card_id: str) -> GameState:
        """Игрок выбирает карту из взятых для сброса."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "discard_from_drawn":
            raise ValueError("Нет активного выбора сброса карты")
        drawn_ids = pending["drawn_card_ids"]
        if card_id not in drawn_ids:
            raise ValueError(f"Карта {card_id} не из взятых карт")
        card = self._find_in_hand(state, card_id)
        if card is None:
            raise ValueError(f"Карта {card_id} не найдена в руке")
        state.player.hand.remove(card)
        state.player.discard.append(card)
        state.add_log(f"«{card.name}» сброшена в личный сброс")
        state.pending_choice = None
        state = self._check_deferred_choices(state)
        return state

    def _apply_sacred_path_exploit_action(self, state: GameState, card: Card) -> GameState:
        """Эксплуатация 1REG14: показывает верхнюю карту колоды усиления и предлагает разрушить карту."""
        from .state import _card_info
        top_boost = state.player.boost_deck[0] if state.player.boost_deck else None
        state.pending_choice = {
            "type": "sacred_path_exploit_choice",
            "top_boost_card": _card_info(top_boost) if top_boost else None,
            "source_card_id": card.id,
            "source_card_name": card.name,
        }
        if top_boost:
            state.add_log(f"Верхняя карта усиления: «{top_boost.name}». Разрушить «{card.name}» и обменять карту из руки?")
        else:
            state.add_log(f"Колода усиления пуста. Разрушить «{card.name}»?")
        return state

    def resolve_sacred_path_exploit(self, state: GameState, destroy: bool) -> GameState:
        """Игрок решает, разрушать ли 1REG14."""
        from .state import _card_info
        pending = state.pending_choice
        if not pending or pending.get("type") != "sacred_path_exploit_choice":
            raise ValueError("Нет активного выбора для Священного пути")
        state.pending_choice = None
        if not destroy:
            state.add_log("Разрушение «Священного пути» пропущено")
            return state
        source_card_id = pending["source_card_id"]
        source_card_name = pending["source_card_name"]
        card = next((c for c in state.player.play_area if c.id == source_card_id), None)
        if card is None:
            raise ValueError(f"Карта {source_card_id} не найдена в игровой области")
        state.player.play_area.remove(card)
        state.player.discard.append(card)
        state.add_log(f"«{source_card_name}» разрушена и отправлена в личный сброс")
        if not state.player.boost_deck:
            state.add_log("Колода усиления пуста — обмен невозможен")
            return state
        top_boost = state.player.boost_deck[0]
        state.pending_choice = {
            "type": "sacred_path_exchange_choice",
            "top_boost_card": _card_info(top_boost),
            "hand_cards": [_card_info(c) for c in state.player.hand],
        }
        state.add_log(f"Выберите карту из руки для обмена на «{top_boost.name}»")
        return state

    def resolve_sacred_path_exchange(self, state: GameState, hand_card_id: str) -> GameState:
        """Игрок отдаёт карту из руки и берёт верхнюю карту колоды усиления."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "sacred_path_exchange_choice":
            raise ValueError("Нет активного выбора обмена для Священного пути")
        state.pending_choice = None
        if not state.player.boost_deck:
            raise ValueError("Колода усиления пуста — обмен невозможен")
        top_boost = state.player.boost_deck.pop(0)
        hand_card = next((c for c in state.player.hand if c.id == hand_card_id), None)
        if hand_card is None:
            raise ValueError(f"Карта {hand_card_id} не найдена в руке")
        state.player.hand.remove(hand_card)
        state.player.discard.append(hand_card)
        state.player.hand.append(top_boost)
        state.add_log(f"Обмен: «{hand_card.name}» → сброс, «{top_boost.name}» → рука")
        return state

    def _apply_exile_from_market_action(self, state: GameState) -> GameState:
        """Ищет доступные слоты рынка без жетонов и устанавливает pending_choice."""
        eligible = [
            i for i, slot in enumerate(state.shared.market)
            if slot.card is not None and slot.upgrade_tokens == 0
        ]
        if not eligible:
            state.add_log("Нет карт без жетонов на рынке — изгнание пропущено")
            return state
        state.pending_choice = {
            "type": "exile_from_market",
            "eligible_slot_indices": eligible,
        }
        state.add_log("Выберите карту с рынка для изгнания (без жетонов прогресса)")
        return state

    def exile_card_from_market(self, state: GameState, slot_index: int) -> GameState:
        """Игрок выбирает слот рынка: карта изгоняется навсегда, слот перезаполняется."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "exile_from_market":
            raise ValueError("Нет активного изгнания с рынка")
        eligible = pending.get("eligible_slot_indices", [])
        if slot_index not in eligible:
            raise ValueError(f"Слот {slot_index} недоступен для изгнания")

        slot = state.shared.market[slot_index]
        card = slot.card

        # Move card to exile pile (permanently out of game)
        state.shared.exile_pile.append(card)
        state.add_log(f"«{card.name}» изгнана из игры навсегда")

        # Return disorder_under to disorder deck
        if slot.disorder_under:
            state.shared.disorder_deck.append(slot.disorder_under)
            slot.disorder_under = None

        slot.card = None
        slot.upgrade_tokens = 0

        # Refill the slot (with fallback to main deck if source deck is empty)
        state = self._refill_market_slot_after_exile(state, slot_index)

        state.pending_choice = None
        state = self._check_deferred_choices(state)
        return state

    def _refill_market_slot_after_exile(self, state: GameState, slot_index: int) -> GameState:
        """Перезаполняет слот после изгнания. Если источник пуст — берёт из основной колоды."""
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

        # Fallback to main deck if specific source was empty
        if card is None and slot.source_deck != "main":
            card = state.shared.main_deck.pop(0) if state.shared.main_deck else None

        if card:
            slot.card = card
            slot.upgrade_tokens = 0
            slot.disorder_under = None
            cats = getattr(card, 'categories', [])
            if (CardCategory.ORIGINS in cats or
                    CardCategory.CIVILIZATION in cats or
                    CardCategory.RAID in cats):
                if state.shared.disorder_deck:
                    slot.disorder_under = state.shared.disorder_deck.pop(0)
            if CardCategory.CIVILIZATION in cats and CardCategory.ORIGINS not in cats:
                slot.upgrade_tokens = 1
            state.add_log(f"Слот перезаполнен: «{card.name}»")

        return state

    def _apply_destroy_from_play_area_action(self, state: GameState, action: DestroyFromPlayAreaAction) -> GameState:
        """Разрушить N карт указанной категории из игровой области игрока."""
        from .state import _card_info
        available = [
            c for c in state.player.play_area
            if action.category in getattr(c, 'categories', [])
        ]
        if not available:
            state.add_log(f"Нет карт категории «{action.category.value}» в игровой области — эффект пропущен")
            return state
        if len(available) <= action.count:
            # Автоматически разрушаем все доступные
            for card in available:
                state.player.play_area.remove(card)
                state.player.discard.append(card)
                state.add_log(f"«{card.name}» разрушена")
            return state
        # Игрок выбирает карты
        state.pending_choice = {
            "type": "destroy_from_play_area",
            "category": action.category.value,
            "count": action.count,
            "available_cards": [_card_info(c) for c in available],
        }
        state.add_log(f"Выберите {action.count} карты категории «{action.category.value}» для разрушения")
        return state

    def select_destroy_cards(self, state: GameState, card_ids: list) -> GameState:
        """Игрок выбирает карты из игровой области для разрушения."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "destroy_from_play_area":
            raise ValueError("Нет активного выбора карт для разрушения")
        required = pending["count"]
        category = pending["category"]
        if len(card_ids) != required:
            raise ValueError(f"Нужно выбрать ровно {required} карты, передано {len(card_ids)}")
        for cid in card_ids:
            card = next((c for c in state.player.play_area if c.id == cid), None)
            if card is None:
                raise ValueError(f"Карта {cid} не найдена в игровой области")
            card_cats = [c.value for c in getattr(card, 'categories', [])]
            if category not in card_cats:
                raise ValueError(f"Карта «{card.name}» не относится к категории «{category}»")
        for cid in card_ids:
            card = next(c for c in state.player.play_area if c.id == cid)
            state.player.play_area.remove(card)
            state.player.discard.append(card)
            state.add_log(f"«{card.name}» разрушена")
        state.pending_choice = None
        state = self._check_deferred_choices(state)
        return state

    def _apply_look_at_glory_deck_action(self, state: GameState, action: LookAtGloryDeckAction) -> GameState:
        """Посмотреть верхние N карт колоды славы, взять M в руку."""
        from .state import _card_info
        if not state.shared.glory_deck:
            state.add_log("Колода славы пуста — эффект пропущен")
            return state
        revealed = state.shared.glory_deck[:action.look_count]
        if len(revealed) <= action.take_count:
            # Берём все в руку
            for card in revealed:
                state.shared.glory_deck.remove(card)
                state.player.hand.append(card)
                state.add_log(f"«{card.name}» взята из колоды славы в руку")
            return state
        state.pending_choice = {
            "type": "glory_deck_look",
            "look_count": action.look_count,
            "take_count": action.take_count,
            "revealed_cards": [_card_info(c) for c in revealed],
        }
        state.add_log(f"Посмотрите верхние {len(revealed)} карты колоды славы, возьмите {action.take_count}")
        return state

    def select_glory_deck_card(self, state: GameState, card_id: str) -> GameState:
        """Игрок берёт одну карту из открытых карт колоды славы в руку."""
        pending = state.pending_choice
        if not pending or pending.get("type") != "glory_deck_look":
            raise ValueError("Нет активного просмотра колоды славы")
        revealed_ids = [c["id"] for c in pending["revealed_cards"]]
        if card_id not in revealed_ids:
            raise ValueError(f"Карта {card_id} не среди открытых карт колоды славы")
        taken = next((c for c in state.shared.glory_deck if c.id == card_id), None)
        if taken is None:
            raise ValueError(f"Карта {card_id} не найдена в колоде славы")
        state.shared.glory_deck.remove(taken)
        state.player.hand.append(taken)
        state.add_log(f"«{taken.name}» взята из колоды славы в руку")
        state.pending_choice = None
        state = self._check_deferred_choices(state)
        return state

    def _serialize_on_play_action(self, action) -> dict:
        """Сериализует действие карты в dict для очереди pending_card_play_actions."""
        if isinstance(action, GainResourceAction):
            return {"type": "gain_resource",
                    "resource_type": action.resource_type.name,
                    "amount": action.amount}
        if isinstance(action, StealResourceAction):
            return {"type": "steal_resource",
                    "resource_type": action.resource_type.name,
                    "amount": action.amount}
        if isinstance(action, ReturnExploitTokenOptionalAction):
            return {"type": "return_exploit_token_optional"}
        if isinstance(action, DrawFromDeckOptionalAction):
            return {"type": "draw_from_deck_optional"}
        if isinstance(action, AcquireCardAction):
            return {"type": "acquire_from_market",
                    "categories": [c.value for c in action.allowed_categories],
                    "count": action.count}
        if isinstance(action, AppropriateCardAction):
            return {"type": "appropriate",
                    "categories": [c.value for c in action.allowed_categories],
                    "source_decks": action.allowed_source_decks,
                    "include_main_deck": action.include_main_deck,
                    "count": action.count}
        if isinstance(action, ChronicleFromDiscardAction):
            return {"type": "chronicle_from_discard", "optional": action.optional}
        if isinstance(action, ChronicleFromHandAction):
            return {"type": "chronicle_from_hand", "optional": action.optional}
        if isinstance(action, ExileFromMarketAction):
            return {"type": "exile_from_market"}
        if isinstance(action, DestroyFromPlayAreaAction):
            return {"type": "destroy_from_play_area",
                    "category": action.category.value,
                    "count": action.count}
        if isinstance(action, LookAtGloryDeckAction):
            return {"type": "look_at_glory_deck",
                    "look_count": action.look_count,
                    "take_count": action.take_count}
        if isinstance(action, MoveDiscardToDeckAction):
            return {"type": "move_discard_to_deck", "optional": action.optional}
        if isinstance(action, DrawUpToNFromDeckAction):
            return {"type": "draw_up_to_n_from_deck", "count": action.count}
        if isinstance(action, ReturnCardToDeckTopAction):
            return {"type": "return_card_to_deck_top"}
        if isinstance(action, ChoiceAction):
            options = []
            for opt in action.options:
                inner = opt.action
                if isinstance(inner, AcquireCardAction):
                    inner_data = {"type": "acquire_from_market",
                                  "categories": [c.value for c in inner.allowed_categories],
                                  "count": inner.count}
                elif isinstance(inner, AppropriateCardAction):
                    inner_data = {"type": "appropriate",
                                  "categories": [c.value for c in inner.allowed_categories],
                                  "source_decks": inner.allowed_source_decks,
                                  "include_main_deck": inner.include_main_deck,
                                  "count": inner.count}
                elif isinstance(inner, GainPerLabelAction):
                    inner_data = {"type": "gain_per_label",
                                  "label": inner.label,
                                  "resource_type": inner.resource_type.name}
                elif isinstance(inner, GainPerCategoryAction):
                    inner_data = {"type": "gain_per_category",
                                  "category": inner.category.value,
                                  "resource_type": inner.resource_type.name}
                elif isinstance(inner, GainResourceAction):
                    inner_data = {"type": "gain_resource",
                                  "resource_type": inner.resource_type.name,
                                  "amount": inner.amount}
                else:
                    continue
                options.append({"label": opt.label,
                                 "cost_population": opt.cost_population,
                                 "cost_resource": opt.cost_resource,
                                 "action": inner_data,
                                 "opponent_gains_progress": opt.opponent_gains_progress,
                                 "opponent_recalls_region": opt.opponent_recalls_region})
            return {"type": "choice", "options": options}
        if isinstance(action, SpendResourceAction):
            return {"type": "spend_resource",
                    "resource_type": action.resource_type.name,
                    "amount": action.amount}
        if isinstance(action, BotGainsDisorderAction):
            return {"type": "bot_gains_disorder", "count": action.count}
        if isinstance(action, DrawThenDiscardChoiceAction):
            return {"type": "draw_then_discard_choice",
                    "draw_count": action.draw_count,
                    "discard_count": action.discard_count}
        if isinstance(action, GuessDeckCategoryAction):
            return {"type": "guess_main_deck_category",
                    "allowed_categories": action.allowed_categories}
        return {}

    def _execute_queued_action(self, state: GameState, action_data: dict) -> GameState:
        """Выполняет одно сериализованное действие из очереди pending_card_play_actions."""
        action_type = action_data.get("type")

        if action_type == "draw_from_deck_optional":
            state = self._apply_draw_from_deck_optional_action(state)

        elif action_type == "return_exploit_token_optional":
            state = self._apply_return_exploit_token_optional_action(state)

        elif action_type == "steal_resource":
            if action_data.get("resource_type") == "MATERIAL":
                lost = min(state.bot.resource, action_data.get("amount", 0))
                state.bot.resource -= lost
                state.add_log(f"Набег: бот теряет {lost} ресурс(а)")

        elif action_type == "gain_resource":
            _RESOURCE_ATTR: dict = {
                "MATERIAL": "resource", "POPULATION": "population",
                "PROGRESS": "upgrade", "ACTION": "action", "EXPLOIT": "exploit",
            }
            attr = _RESOURCE_ATTR.get(action_data.get("resource_type", "MATERIAL"), "resource")
            amount = action_data.get("amount", 0)
            setattr(state.player.resources, attr, getattr(state.player.resources, attr) + amount)
            state.add_log(f"+{amount} {action_data.get('resource_type', 'MATERIAL')}")

        elif action_type == "acquire_from_market":
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

        elif action_type == "choice":
            # Вычисляем динамические количества и формируем player_choice
            options_data = []
            for opt in action_data.get("options", []):
                inner = opt.get("action", {})
                inner_type = inner.get("type")
                if inner_type == "gain_per_label":
                    label = inner["label"]
                    rt_name = inner["resource_type"]
                    count = sum(
                        1 for c in state.player.play_area
                        for lb in getattr(c, 'labels', []) if lb.value == label
                    )
                    action_out = {"type": "gain_resource", "resource_type": rt_name, "amount": count}
                    opt_label = f"{opt['label']} ({count})"
                elif inner_type == "gain_per_category":
                    category_val = inner["category"]
                    rt_name = inner["resource_type"]
                    count = sum(
                        1 for c in state.player.play_area
                        if CardCategory(category_val) in getattr(c, 'categories', [])
                    )
                    action_out = {"type": "gain_resource", "resource_type": rt_name, "amount": count}
                    opt_label = f"{opt['label']} ({count})"
                elif inner_type == "acquire_from_market":
                    action_out = inner
                    opt_label = opt["label"]
                elif inner_type == "appropriate":
                    action_out = inner
                    opt_label = opt["label"]
                elif inner_type == "gain_resource":
                    action_out = inner
                    opt_label = opt["label"]
                else:
                    continue
                options_data.append({
                    "label": opt_label,
                    "cost_population": opt.get("cost_population", 0),
                    "cost_resource": opt.get("cost_resource", 0),
                    "action": action_out,
                })
            state.pending_choice = {"type": "player_choice", "options": options_data}
            state.add_log("Выберите вариант действия")

        elif action_type == "chronicle_from_discard":
            state = self._apply_chronicle_from_discard_action(
                state,
                ChronicleFromDiscardAction(optional=action_data.get("optional", False)),
            )

        elif action_type == "chronicle_from_hand":
            state = self._apply_chronicle_from_hand_action(
                state,
                ChronicleFromHandAction(optional=action_data.get("optional", False)),
            )

        elif action_type == "exile_from_market":
            state = self._apply_exile_from_market_action(state)

        elif action_type == "destroy_from_play_area":
            state = self._apply_destroy_from_play_area_action(
                state,
                DestroyFromPlayAreaAction(
                    category=CardCategory(action_data["category"]),
                    count=action_data.get("count", 1),
                ),
            )

        elif action_type == "look_at_glory_deck":
            state = self._apply_look_at_glory_deck_action(
                state,
                LookAtGloryDeckAction(
                    look_count=action_data.get("look_count", 2),
                    take_count=action_data.get("take_count", 1),
                ),
            )

        elif action_type == "move_discard_to_deck":
            state = self._apply_move_discard_to_deck_action(
                state,
                MoveDiscardToDeckAction(optional=action_data.get("optional", True)),
            )

        elif action_type == "draw_up_to_n_from_deck":
            state = self._apply_draw_up_to_n_from_deck_action(
                state,
                DrawUpToNFromDeckAction(count=action_data.get("count", 3)),
            )

        elif action_type == "return_card_to_deck_top":
            state = self._apply_return_card_to_deck_top_action(state)

        elif action_type == "spend_resource":
            rt_name = action_data.get("resource_type", "MATERIAL")
            from .enums import ResourceType as _RT
            rt = next((r for r in _RT if r.name == rt_name), _RT.MATERIAL)
            state = self._apply_spend_resource_action(state, SpendResourceAction(rt, action_data.get("amount", 0)))

        elif action_type == "bot_gains_disorder":
            state = self._apply_bot_gains_disorder_action(state, BotGainsDisorderAction(count=action_data.get("count", 1)))

        elif action_type == "draw_then_discard_choice":
            state = self._apply_draw_then_discard_choice_action(
                state,
                DrawThenDiscardChoiceAction(
                    draw_count=action_data.get("draw_count", 2),
                    discard_count=action_data.get("discard_count", 1),
                ),
            )

        elif action_type == "guess_main_deck_category":
            state = self._apply_guess_deck_category_action(
                state,
                GuessDeckCategoryAction(
                    allowed_categories=action_data.get(
                        "allowed_categories", ["region", "origins", "civilization", "raid"]
                    ),
                ),
            )

        return state

    # ── UTILS ──────────────────────────────────────────────────────────────────

    def _find_in_hand(self, state: GameState,
                       card_id: str) -> Optional[Card]:
        return next((c for c in state.player.hand if c.id == card_id), None)

    @staticmethod
    def _is_disorder(card: Card) -> bool:
        """Карта является беспорядком — по типу карты или по категории."""
        from .enums import CardType
        return (card.card_type == CardType.DISORDER
                or CardCategory.DISORDER in getattr(card, 'categories', []))
