"""
Централизованная настройка логгирования.
Каждый день создаётся новый файл вида imperium_YYYY-MM-DD.log.
Хранится не более 30 файлов (старые удаляются при ротации).
"""
import glob
import logging
import os
import time
from datetime import date
from logging.handlers import TimedRotatingFileHandler

_LOG_DIR = os.path.normpath(os.path.join(
    os.path.dirname(__file__),   # .../python/game/
    "..", "..", "..", "..",      # → backend/
    "logs",
))

_FMT = "%(asctime)s | %(levelname)-8s | %(message)s"
_DATE_FMT = "%Y-%m-%d %H:%M:%S"
_BACKUP_COUNT = 30


class _DailyFileHandler(TimedRotatingFileHandler):
    """
    Расширение стандартного хендлера: при ротации открывает новый файл
    с датой в имени (imperium_YYYY-MM-DD.log) вместо того, чтобы
    переименовывать текущий.
    """

    def __init__(self, log_dir: str, backup_count: int = _BACKUP_COUNT):
        self._log_dir = log_dir
        filename = os.path.join(log_dir, f"imperium_{date.today():%Y-%m-%d}.log")
        super().__init__(
            filename,
            when="midnight",
            interval=1,
            backupCount=backup_count,
            encoding="utf-8",
            utc=False,
        )

    def doRollover(self) -> None:
        """Закрывает текущий файл, открывает новый с сегодняшней датой."""
        if self.stream:
            self.stream.close()
            self.stream = None  # type: ignore[assignment]

        self.baseFilename = os.path.join(
            self._log_dir, f"imperium_{date.today():%Y-%m-%d}.log"
        )
        self.stream = self._open()
        self.rolloverAt = self.computeRollover(int(time.time()))

        self._cleanup_old_logs()

    def _cleanup_old_logs(self) -> None:
        """Удаляет лог-файлы сверх лимита backupCount (по дате в имени)."""
        pattern = os.path.join(self._log_dir, "imperium_*.log")
        files = sorted(glob.glob(pattern))
        excess = len(files) - self.backupCount
        for path in files[:excess]:
            try:
                os.remove(path)
            except OSError:
                pass


def _build_logger() -> logging.Logger:
    os.makedirs(_LOG_DIR, exist_ok=True)

    logger = logging.getLogger("imperium")
    if logger.handlers:          # не добавлять дублирующие хендлеры при reload
        return logger

    logger.setLevel(logging.DEBUG)

    fh = _DailyFileHandler(_LOG_DIR)
    fh.setLevel(logging.DEBUG)
    fh.setFormatter(logging.Formatter(_FMT, _DATE_FMT))
    logger.addHandler(fh)

    ch = logging.StreamHandler()
    ch.setLevel(logging.WARNING)
    ch.setFormatter(logging.Formatter(_FMT, _DATE_FMT))
    logger.addHandler(ch)

    return logger


log = _build_logger()
