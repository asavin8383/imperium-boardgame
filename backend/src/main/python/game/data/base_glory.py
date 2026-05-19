"""Card data for the base Glory deck (SLV)."""

DECK: dict = {
    "1SLV1":  {"name": "Триумф",             "categories": ["glory"], "vp_fixed": 3},
    "1SLV2":  {"name": "Великий поход",      "categories": ["glory"], "vp_per_condition": "region"},
    "1SLV3":  {"name": "Золотой век",        "categories": ["glory"], "vp_per_condition": "civilization"},
    "1SLV4":  {"name": "Мировое господство", "categories": ["glory"], "vp_per_condition": "region"},
    "1SLV5":  {"name": "Наследие",           "categories": ["glory"], "vp_per_condition": "origins"},
    "1SLV6":  {"name": "Эпоха процветания",  "categories": ["glory"], "vp_per_condition": "resource"},
    "1SLV7":  {"name": "Слава народа",       "categories": ["glory"], "vp_per_condition": "population"},
    "1SLV8":  {"name": "Легенда",            "categories": ["glory"], "vp_per_condition": "glory"},
    "1SLV9A": {"name": "Царь царей",         "categories": ["glory"], "solstice_effect": True, "vp_condition": "period_based"},
}
