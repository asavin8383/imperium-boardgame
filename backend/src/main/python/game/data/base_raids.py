"""Card data for the base Raids deck (NAB)."""

DECK: dict = {
    "1NAB1":  {"name": "Армяне",     "categories": ["raid"], "vp_per_condition": "region"},
    "1NAB2":  {"name": "Ассирийцы",        "categories": ["raid"], "vp_per_condition": "population"},
    "1NAB3":  {"name": "Египтяне",        "categories": ["raid"], "vp_per_condition": "civilization"},
    "1NAB4":  {"name": "Ионийцы",       "categories": ["raid"], "vp_per_condition": "origins"},
    "1NAB5":  {"name": "Империя Цинь", "categories": ["raid"], "passive_effect": True, "vp_per_condition": "region"},
    "1NAB7":  {"name": "Шумеры",       "categories": ["raid"], "vp_fixed": 3},
    "1NAB8":  {"name": "Аксумиты",    "categories": ["raid"], "vp_per_condition": "attack"},
    "1NAB9":  {"name": "Минойцы",        "categories": ["raid"], "vp_fixed": 2},
    "1NAB11": {"name": "Хетты",        "categories": ["raid"], "vp_per_condition": "region"},
}
