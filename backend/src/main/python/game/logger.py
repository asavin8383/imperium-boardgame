"""
Централизованная настройка логгирования.
Лог-файл ротируется раз в сутки (в полночь), хранится 7 последних файлов.
"""
import logging
import os
from logging.handlers import TimedRotatingFileHandler

_LOG_DIR = os.path.join(
    os.path.dirname(__file__),          # .../python/game/
    "..", "..", "..", "..",             # → backend/
    "logs",
)
_LOG_DIR = os.path.normpath(_LOG_DIR)
_LOG_FILE = os.path.join(_LOG_DIR, "imperium.log")

_FMT = "%(asctime)s | %(levelname)-8s | %(message)s"
_DATE_FMT = "%Y-%m-%d %H:%M:%S"


def _build_logger() -> logging.Logger:
    os.makedirs(_LOG_DIR, exist_ok=True)

    logger = logging.getLogger("imperium")
    if logger.handlers:          # не добавлять дублирующие хендлеры при reload
        return logger

    logger.setLevel(logging.DEBUG)

    # --- файловый хендлер с суточной ротацией ---
    fh = TimedRotatingFileHandler(
        _LOG_FILE,
        when="midnight",
        interval=1,
        backupCount=7,
        encoding="utf-8",
        utc=False,
    )
    fh.suffix = "%Y-%m-%d"
    fh.setLevel(logging.DEBUG)
    fh.setFormatter(logging.Formatter(_FMT, _DATE_FMT))
    logger.addHandler(fh)

    # --- консольный хендлер (только WARNING+) ---
    ch = logging.StreamHandler()
    ch.setLevel(logging.WARNING)
    ch.setFormatter(logging.Formatter(_FMT, _DATE_FMT))
    logger.addHandler(ch)

    return logger


log = _build_logger()
