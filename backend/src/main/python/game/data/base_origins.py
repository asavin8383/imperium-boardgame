"""Card data for the base Origins deck (IST)."""

DECK: dict = {
    "1IST2":  {"name": "Нефритовая маска",       "categories": ["origins"],
                "vp_in_chronicle": 1, "vp_out_of_chronicle": 0,
                "on_play_actions": [
                    {"type": "draw_up_to_n_from_deck", "count": 3},
                    {"type": "return_card_to_deck_top"}
                ]},
    "1IST3":  {"name": "Старейшины",     "categories": ["origins"],  "vp_fixed": 1,
                "on_play_actions": [{"type": "choice", "options": [
                    {"label": "Приобрести карту истоков", "action": {"type": "acquire_from_market", "categories": ["origins"]}},
                    {"label": "Присвоить карту истоков",  "action": {"type": "appropriate",         "categories": ["origins"]}, "opponent_gains_progress": 1}
                ]}]},
    "1IST4":  {"name": "Поселение",  "categories": ["origins"],
                "card_type": "permanent",  "period": "barbarism",
                "vp_in_chronicle": 2, "vp_out_of_chronicle": 0,
                "solstice_effect": True,
                "exploit_actions": [{"type": "all_players_gain_resource", "resource_type": "MATERIAL", "amount": 2}],
                "solstice_actions": [{"type": "optional_gain_progress_then_fate", "amount": 2}]},
    "1IST5":  {"name": "Поселение",  "categories": ["origins"],
                "card_type": "permanent",  "period": "barbarism",
                "vp_in_chronicle": 2, "vp_out_of_chronicle": 0,
                "solstice_effect": True,
                "exploit_actions": [{"type": "all_players_gain_resource", "resource_type": "MATERIAL", "amount": 2}],
                "solstice_actions": [{"type": "optional_gain_progress_then_fate", "amount": 2}]},
    "1IST6":  {"name": "Святилище",  "categories": ["origins"],
                "card_type": "permanent",  "period": "barbarism",
                "vp_fixed": 1,
                "exploit_actions": [{"type": "spend_resource_draw_card", "resource_type": "MATERIAL", "resource_cost": 1, "draw_count": 1}],
                "solstice_effect": True,
                "solstice_actions": [{"type": "optional_discard_hand_return_disorder"}]},
    "1IST7":  {"name": "Вторжение",  "categories": ["origins"],  "card_type": "attack",  "vp_fixed": 2,
                "on_play_actions": [
                    {"type": "spend_resource", "resource_type": "MATERIAL", "amount": 3},
                    {"type": "bot_gains_disorder", "count": 1},
                    {"type": "choice", "options": [
                        {"label": "Присвоить карту набегов",  "action": {"type": "appropriate", "categories": ["raid"]}},
                        {"label": "Присвоить карту региона",  "action": {"type": "appropriate", "categories": ["region"]}, "opponent_recalls_region": 1}
                    ]}
                ]},
    "1IST8":  {"name": "Таинства",  "categories": ["origins"],
                "card_type": "permanent",  "vp_fixed": 1,
                "exploit_actions": [{"type": "gain_resource", "resource_type": "ACTION", "amount": 1}],
                "solstice_effect": True,
                "solstice_actions": [{"type": "choice", "options": [
                    {"label": "Взять карту беспорядков", "action": {"type": "draw_disorder", "count": 1}},
                    {"label": "Сбросить 2 карты из руки", "action": {"type": "discard_hand", "count": 2}}
                ]}]},
    "1IST9":  {"name": "Оракул",            "categories": ["origins"],
                "period": "barbarism",
                "requires_action_token": False,
                "vp_per_condition": "glory", "vp_per_condition_value": 2,
                "on_play_actions": [
                    {"type": "draw_then_discard_choice", "draw_count": 2, "discard_count": 1}
                ]},
    "1IST10": {"name": "Земледелие",             "categories": ["origins"],
                "card_type": "permanent",
                "is_exploit": True,
                "exploit_passive": "grain_to_sack_3",
                "vp_per_condition": "label_grain", "vp_per_condition_value": 2},
    "1IST11": {"name": "Лодки",      "categories": ["origins"],
                "card_type": "permanent",
                "is_exploit": True,
                "vp_per_condition": "label_grain_and_water", "vp_per_condition_value": 1,
                "exploit_actions": [{"type": "recall_label_choice", "options": [
                    {"label": "water", "gains": [
                        {"resource_type": "MATERIAL", "amount": 2},
                        {"resource_type": "POPULATION", "amount": 1}
                    ]},
                    {"label": "grain", "gains": [
                        {"resource_type": "ACTION", "amount": 1}
                    ]}
                ]}]},
    "1IST12": {"name": "Колодец-журавль",  "categories": ["origins"],
                "card_type": "permanent",
                "is_exploit": True,
                "exploit_passive": "well_crane",
                "vp_per_condition": "label_water", "vp_per_condition_value": 2},
    "1IST15": {"name": "Город",  "categories": ["origins", "civilization"],  "card_type": "permanent",
                "labels": ["town"],  "vp_fixed": 1,
                "solstice_effect": True,
                "solstice_actions": [{"type": "optional_discard_hand_for_choice", "options": [
                    {"label": "Получить 1 население",     "action": {"type": "gain_resource", "resource_type": "POPULATION", "amount": 1}},
                    {"label": "Получить 1 ресурс",        "action": {"type": "gain_resource", "resource_type": "MATERIAL",   "amount": 1}},
                    {"label": "Взять 1 карту из колоды",  "action": {"type": "draw_from_deck", "count": 1}}
                ]}]},
    "1IST16": {"name": "Город",  "categories": ["origins", "civilization"],  "card_type": "permanent",
                "labels": ["town"],  "vp_fixed": 1,
                "solstice_effect": True,
                "solstice_actions": [{"type": "optional_discard_hand_for_choice", "options": [
                    {"label": "Получить 1 население",     "action": {"type": "gain_resource", "resource_type": "POPULATION", "amount": 1}},
                    {"label": "Получить 1 ресурс",        "action": {"type": "gain_resource", "resource_type": "MATERIAL",   "amount": 1}},
                    {"label": "Взять 1 карту из колоды",  "action": {"type": "draw_from_deck", "count": 1}}
                ]}]},
    "1IST17": {"name": "Город",  "categories": ["origins", "civilization"],  "card_type": "permanent",
                "labels": ["town"],  "vp_fixed": 1,
                "solstice_effect": True,
                "solstice_actions": [{"type": "optional_discard_hand_for_choice", "options": [
                    {"label": "Получить 1 население",     "action": {"type": "gain_resource", "resource_type": "POPULATION", "amount": 1}},
                    {"label": "Получить 1 ресурс",        "action": {"type": "gain_resource", "resource_type": "MATERIAL",   "amount": 1}},
                    {"label": "Взять 1 карту из колоды",  "action": {"type": "draw_from_deck", "count": 1}}
                ]}]},
    "1IST18": {"name": "Азартные игры",  "categories": ["origins", "civilization"],  "vp_fixed": 1,
                "requires_action_token": False,
                "on_play_actions": [
                    {"type": "spend_resource", "resource_type": "MATERIAL", "amount": 1},
                    {"type": "guess_main_deck_category",
                     "allowed_categories": ["region", "origins", "civilization", "raid"]}
                ]},
    "1IST20": {"name": "Дипломатия",  "categories": ["origins", "civilization"],  "vp_fixed": 1,
                "on_play_actions": [
                    {"type": "spend_resource", "resource_type": "MATERIAL", "amount": 3},
                    {"type": "appropriate", "categories": ["raid"]}
                ]},
    "1IST21": {"name": "Лидерство",  "categories": ["origins", "civilization"],  "vp_fixed": 1,
                "requires_action_token": False,
                "on_play_actions": [{"type": "choice", "options": [
                    {"label": "Взять 1 карту из колоды",              "action": {"type": "draw_up_to_n_from_deck",  "count": 1}},
                    {"label": "Занести карту с руки в летопись",      "action": {"type": "chronicle_from_hand",     "optional": False}},
                    {"label": "Занести карту из сброса в летопись",   "action": {"type": "chronicle_from_discard",  "optional": False}}
                ]}]},
}
