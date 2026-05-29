"""
SQLAlchemy ORM models for persistent game storage
"""
import uuid
from datetime import datetime, timezone
from sqlalchemy import Column, String, Integer, Text, DateTime
from .database import Base

# Текущая версия формата сериализации состояния игры.
# v1 = pickle+base64 (устарело), v2 = explicit JSON
CURRENT_FORMAT_VERSION = 2


class GameSave(Base):
    """Сохранённая партия"""
    __tablename__ = "game_saves"

    id = Column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    name = Column(String(255), nullable=False)

    # Метаданные для отображения в списке сохранений
    player_nation = Column(String(50), nullable=False)
    bot_nation = Column(String(50), nullable=False)
    difficulty = Column(String(50), nullable=False)
    game_phase = Column(String(50), nullable=False)
    round_number = Column(Integer, nullable=False, default=1)
    player_period = Column(String(50), nullable=False)

    created_at = Column(DateTime, nullable=False, default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime, nullable=False,
                        default=lambda: datetime.now(timezone.utc),
                        onupdate=lambda: datetime.now(timezone.utc))

    # Полное состояние игры — explicit JSON (format_version=2)
    state_data = Column(Text, nullable=False)
    format_version = Column(Integer, nullable=False, default=CURRENT_FORMAT_VERSION)

    def to_dict(self):
        return {
            "id": self.id,
            "name": self.name,
            "player_nation": self.player_nation,
            "bot_nation": self.bot_nation,
            "difficulty": self.difficulty,
            "game_phase": self.game_phase,
            "round_number": self.round_number,
            "player_period": self.player_period,
            "created_at": self.created_at.isoformat() if self.created_at else None,
            "updated_at": self.updated_at.isoformat() if self.updated_at else None,
        }
