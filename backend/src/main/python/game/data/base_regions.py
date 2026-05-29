"""Card data for the base Regions deck (REG)."""

DECK: dict = {
    "1REG2":  {"name": "Джунгли", "categories": ["region"], "vp_fixed": 1,
               "can_be_reinforced": True,
               "on_play_actions": [
                   {"type": "gain_resource", "resource_type": "POPULATION", "amount": 1},
                   {"type": "exile_from_market"},
               ]},
    "1REG3":  {"name": "Горный хребет", "categories": ["region"], "vp_fixed": 1,
               "can_be_reinforced": True,
               "on_play_actions": [
                   {"type": "gain_resource", "resource_type": "MATERIAL", "amount": 3},
                   {"type": "exile_from_market"},
               ]},
    "1REG4":  {"name": "Побережье", "categories": ["region"],
               "can_be_reinforced": True,
               "draws_cards": 1,
               "labels": ["grain", "sack"],
               "on_play_actions": [
                   {"type": "exile_from_market"},
               ]},
    "1REG5":  {"name": "Пойма реки", "categories": ["region"],
               "can_be_reinforced": True,
               "labels": ["sack", "water"],
               "on_play_actions": [
                   {"type": "chronicle_from_discard", "optional": True},
                   {"type": "exile_from_market"},
               ]},
    "1REG6":  {"name": "Лес", "categories": ["region"],
               "can_be_reinforced": True,
               "labels": ["sack", "sack", "grain"],
               "on_play_actions": [
                   {"type": "exile_from_market"},
               ]},
    "1REG7":  {"name": "Оазис", "categories": ["region"],
               "can_be_reinforced": True,
               "labels": ["sack", "sack", "water"],
               "on_play_actions": [
                   {"type": "exile_from_market"},
               ]},
    "1REG8":  {"name": "Холмы", "categories": ["region"],
               "can_be_reinforced": True,
               "labels": ["sack", "grain"],
               "on_play_actions": [
                   {"type": "gain_resource", "resource_type": "POPULATION", "amount": 1},
                   {"type": "exile_from_market"},
               ]},
    "1REG9":  {"name": "Топи", "categories": ["region"],
               "can_be_reinforced": True,
               "labels": ["sack", "water"],
               "on_play_actions": [
                   {"type": "gain_resource", "resource_type": "MATERIAL", "amount": 1},
                   {"type": "exile_from_market"},
               ]},
    "1REG10": {"name": "Степи", "categories": ["region"],
               "can_be_reinforced": True,
               "labels": ["sack", "sack", "grain"],
               "on_play_actions": [
                   {"type": "exile_from_market"},
               ]},
    "1REG11": {"name": "Река", "categories": ["region"],
               "can_be_reinforced": True,
               "labels": ["sack", "sack", "water"],
               "on_play_actions": [
                   {"type": "chronicle_from_discard", "optional": True},
                   {"type": "exile_from_market"},
               ]},
    "1REG12": {"name": "Мыс", "categories": ["region"],
               "can_be_reinforced": True,
               "passive_effect": True,
               "labels": ["sack", "water"],
               "on_play_actions": [
                   {"type": "exile_from_market"},
               ]},
    "1REG13": {"name": "Чаща", "categories": ["region"],
               "can_be_reinforced": True,
               "labels": ["sack", "grain"],
               "on_play_actions": [
                   {"type": "move_discard_to_deck", "optional": True},
                   {"type": "exile_from_market"},
               ]},
    "1REG14": {"name": "Священный путь", "categories": ["region"], "vp_fixed": 2,
               "period": "barbarism", "is_exploit": True,
               "exploit_actions": [
                   {"type": "sacred_path_exploit"},
               ]},
}
