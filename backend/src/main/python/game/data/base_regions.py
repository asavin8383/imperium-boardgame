"""Card data for the base Regions deck (REG)."""

DECK: dict = {
    "1REG1":  {"name": "Плодородные земли", "categories": ["region"], "region_types": ["land"],     "vp_fixed": 1, "requires_action_token": True},
    "1REG2":  {"name": "Речная долина",     "categories": ["region"], "region_types": ["land"],     "vp_fixed": 1, "is_exploit": True},
    "1REG3":  {"name": "Горный перевал",    "categories": ["region"], "region_types": ["mountain"], "vp_fixed": 1},
    "1REG4":  {"name": "Прибрежные земли",  "categories": ["region"], "region_types": ["sea"],      "vp_fixed": 1},
    "1REG5":  {"name": "Пойма реки",        "categories": ["region"], "region_types": ["land"],     "vp_fixed": 2, "is_exploit": True},
    "1REG6":  {"name": "Степи",             "categories": ["region"], "region_types": ["land"],     "vp_fixed": 1},
    "1REG7":  {"name": "Оазис",             "categories": ["region"], "region_types": ["land"],     "vp_fixed": 2, "is_exploit": True},
    "1REG8":  {"name": "Острова",           "categories": ["region"], "region_types": ["sea"],      "vp_fixed": 2},
    "1REG9":  {"name": "Горная крепость",   "categories": ["region"], "region_types": ["mountain"], "vp_fixed": 2},
    "1REG10": {"name": "Морской путь",      "categories": ["region"], "region_types": ["sea"],      "vp_fixed": 1},
    "1REG11": {"name": "Лесные угодья",     "categories": ["region"], "region_types": ["land"],     "vp_fixed": 1},
    "1REG12": {"name": "Болота",            "categories": ["region"], "region_types": ["land"]},
    "1REG13": {"name": "Побережье",         "categories": ["region"], "region_types": ["sea"],      "vp_fixed": 1},
    "1REG14": {"name": "Священный путь",    "categories": ["region"], "region_types": ["land"],     "vp_fixed": 2},
}
