"""
Imperium: Classics — Game Engine
Enums, constants and core types
"""
from enum import Enum, auto
from typing import Optional


class Period(str, Enum):
    BARBARISM = "barbarism"   # период варварства
    CIVILIZATION = "civilization"  # период цивилизации


class CardCategory(str, Enum):
    REGION = "region"         # регион
    ORIGINS = "origins"       # истоки
    CIVILIZATION = "civilization"  # цивилизация
    RAID = "raid"             # набеги
    GLORY = "glory"           # слава
    DISORDER = "disorder"     # беспорядки

class CardLabel(str, Enum):
    GRAIN = "grain"   # зерно
    WATER = "water"   # вода
    SACK = "sack"     # деньги
    TOWN = "town"     # город


class CardSubtype(str, Enum):
    ABILITY = "ability"       # способность (символ шестерёнки)
    TRANSFORMATION = "transformation"  # трансформация (символ ●)
    BOOST = "boost"           # усиление (символ ⌒)
    PROGRESS = "progress"     # прогресс (символ +)
    RESERVE = "reserve"       # запас (символ →)
    START = "start"           # стартовая колода


class CardType(str, Enum):
    PERMANENT = "permanent"   # постоянного действия (∞)
    ATTACK = "attack"         # атака (символ перекрещенных мечей)
    NORMAL = "normal"
    DISORDER = "disorder"



class ResourceType(str, Enum):
    MATERIAL = "ресурс"     # ресурс (монеты/зерно)
    POPULATION = "population" # население
    PROGRESS = "progress"       # жетон прогресса
    ACTION = "action"         # жетон действия (квадрат)
    EXPLOIT = "exploit"       # жетон эксплуатации (X)


class Nation(str, Enum):
    # Классика
    VIKINGS = "vikings"
    GREEKS = "greeks"
    CARTHAGINIANS = "carthaginians"
    CELTS = "celts"
    MACEDONIANS = "macedonians"
    PERSIANS = "persians"
    ROMANS = "romans"
    SCYTHIANS = "scythians"


class GamePhase(str, Enum):
    SETUP = "setup"
    PLAYER_TURN = "player_turn"
    PLAYER_DISCARD = "player_discard"  # игрок сбрасывает лишние карты
    BOT_TURN = "bot_turn"
    SOLSTICE = "solstice"         # солнцестояние
    FINAL_ROUND = "final_round"
    SCORING = "scoring"
    GAME_OVER = "game_over"


class TurnAction(str, Enum):
    ACTIVATION = "activation"    # активация
    INNOVATION = "innovation"    # инновация
    REVOLUTION = "revolution"    # революция


class EndCondition(str, Enum):
    MAIN_DECK_EMPTY = "main_deck_empty"
    PROGRESS_EMPTY = "progress_empty"
    KING_OF_KINGS_B = "king_of_kings_b"
    DECLINE = "decline"           # упадок — колода беспорядков пуста


# Difficulty levels for solo mode
class Difficulty(str, Enum):
    CHIEFTAIN = "chieftain"       # вождь
    COMMANDER = "commander"       # полководец
    EMPEROR = "emperor"           # император
    OVERLORD = "overlord"         # повелитель
    SOVEREIGN = "sovereign"       # властелин
