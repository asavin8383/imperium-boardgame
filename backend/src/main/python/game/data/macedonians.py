"""Card data for the Macedonians nation deck."""

DECK: dict = {
    "1MAK1A": {
        "name": "Македоняне (A)",
        "subtype": "ability",
        "passive_effect": True,
        "vp_fixed": 1,
        "start_location": "ability",
    },
    "1MAK1B": {
        "name": "Македоняне (B)",
        "subtype": "ability",
        "passive_effect": True,
        "vp_fixed": 2,
        "start_location": "ability",
    },
    # Transformation — no start_location (placed differently from other nations)
    "1MAK2": {"name": "Царевич Александр", "subtype": "transformation"},
    # Progress cards (subtype="progress", no start_location — go to personal deck for Macedonians)
    "1MAK3": {"name": "Развитие",                 "subtype": "progress", "period": "civilization", "vp_fixed": 3},
    "1MAK4": {"name": "Парменион",                "subtype": "progress", "period": "civilization", "vp_fixed": 2},
    "1MAK5": {"name": "Гейтары",                  "subtype": "progress", "period": "civilization", "vp_fixed": 3},
    "1MAK6": {"name": "Александр Македонский",    "subtype": "progress", "period": "civilization", "vp_fixed": 4},
    "1MAK7": {"name": "Мозаики",                  "subtype": "progress", "period": "civilization", "vp_fixed": 3},
    "1MAK8": {"name": "Александрия Египетская",   "subtype": "progress", "period": "civilization", "vp_fixed": 2},
    "1MAK9": {"name": "Александрия в Ариане",     "subtype": "progress", "period": "civilization"},
    # Boost cards — no start_location (unlike other nations)
    "1MAK10": {"name": "Процветание", "subtype": "boost"},
    "1MAK11": {"name": "Уксии",       "subtype": "boost", "card_type": "attack"},
    "1MAK12": {"name": "Величие",     "subtype": "boost"},
    # START cards — go to personal deck
    "1MAK13": {"name": "Реформы",               "subtype": "start", "period": "barbarism"},
    "1MAK14": {"name": "Завоевание", "subtype": "start", "period": "barbarism",
               "on_play_actions": [{"type": "choice", "options": [
                   {"label": "Приобрести", "cost_population": 2,
                    "action": {"type": "acquire_from_market", "categories": ["region", "raid"], "count": 1}},
                   {"label": "Присвоить", "cost_population": 3,
                    "action": {"type": "appropriate", "categories": ["region", "raid"],
                               "source_decks": ["region"], "include_main_deck": True, "count": 1}},
               ]}]},
    "1MAK15": {"name": "Завоевание", "subtype": "start", "period": "barbarism",
               "on_play_actions": [{"type": "choice", "options": [
                   {"label": "Приобрести", "cost_population": 2,
                    "action": {"type": "acquire_from_market", "categories": ["region", "raid"], "count": 1}},
                   {"label": "Присвоить", "cost_population": 3,
                    "action": {"type": "appropriate", "categories": ["region", "raid"],
                               "source_decks": ["region"], "include_main_deck": True, "count": 1}},
               ]}]},
    "1MAK16": {"name": "Филипп II Македонский", "subtype": "start", "period": "barbarism",
               "on_play_actions": [{"type": "acquire_from_market",
                                    "categories": ["region", "origins", "civilization", "glory"],
                                    "count": 1}]},
    "1MAK17": {"name": "Македонские фаланги",   "subtype": "start", "period": "barbarism"},
    "1MAK18": {"name": "Долина реки Альякмон",  "subtype": "start", "card_type": "permanent",
               "on_play_actions": [{"resource_type": "POPULATION", "amount": 1}]},
    "1MAK19": {"name": "Орестида",              "subtype": "start", "card_type": "permanent",
               "on_play_actions": [{"resource_type": "MATERIAL", "amount": 2}]},
    "1MAK20": {"name": "Пелагония",             "subtype": "start", "card_type": "permanent",
               "on_play_actions": [{"resource_type": "POPULATION", "amount": 1}]},
    "1MAK21": {"name": "Беспорядки",            "subtype": "start", "card_type": "disorder"},
    "1MAK22": {"name": "Беспорядки",            "subtype": "start", "card_type": "disorder"},
    "1MAK23": {"name": "Беспорядки",            "subtype": "start", "card_type": "disorder"},
}
