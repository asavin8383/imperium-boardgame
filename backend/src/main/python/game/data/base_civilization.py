"""Card data for the base Civilization deck (CIV)."""

DECK: dict = {
    "1CIV1":  {"name": "Величие",       "categories": ["civilization"], "period": "civilization", "vp_fixed": 3},
    "1CIV2":  {"name": "Образование",   "categories": ["civilization"], "period": "civilization", "vp_fixed": 2, "requires_action_token": False},
    "1CIV3":  {"name": "Реформы",       "categories": ["civilization"], "period": "civilization", "vp_fixed": 2},
    "1CIV4":  {"name": "Завоевание",    "categories": ["civilization"], "period": "civilization", "vp_fixed": 2},
    "1CIV5":  {"name": "Архитектура",   "categories": ["civilization"], "period": "civilization", "vp_fixed": 3},
    "1CIV6":  {"name": "Метрополия",    "categories": ["civilization"], "period": "civilization", "vp_fixed": 2, "solstice_effect": True},
    "1CIV7":  {"name": "Законы",        "categories": ["civilization"], "period": "civilization", "vp_fixed": 2},
    "1CIV8":  {"name": "Торговый путь", "categories": ["civilization"], "period": "civilization", "vp_fixed": 2},
    "1CIV9":  {"name": "Дипломатия",    "categories": ["civilization"], "period": "civilization", "vp_fixed": 2},
    "1CIV10": {"name": "Флот",          "categories": ["civilization"], "period": "civilization", "vp_fixed": 2},
    "1CIV11": {"name": "Легион",        "categories": ["civilization"], "period": "civilization", "vp_fixed": 2, "card_type": "attack"},
    "1CIV12": {"name": "Наёмники",      "categories": ["civilization"], "period": "civilization", "vp_fixed": 1},
    "1CIV13": {"name": "Таран",         "categories": ["civilization"], "period": "civilization", "vp_fixed": 1, "card_type": "attack"},
    "1CIV14": {"name": "Рынок",         "categories": ["civilization"], "period": "civilization", "vp_fixed": 2},
    "1CIV15": {"name": "Акрополь",      "categories": ["civilization"], "period": "civilization", "vp_fixed": 3, "solstice_effect": True},
}
