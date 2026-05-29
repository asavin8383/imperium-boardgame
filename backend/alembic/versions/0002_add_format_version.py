"""Добавить столбец format_version в game_saves и очистить старые сохранения

Revision ID: 0002
Revises: 0001
Create Date: 2026-05-29
"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

revision: str = "0002"
down_revision: Union[str, None] = "0001"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # Добавляем столбец версии формата сериализации
    op.add_column("game_saves", sa.Column("format_version", sa.Integer, nullable=False, server_default="2"))
    # Удаляем старые сохранения формата v1 (pickle+base64) — они несовместимы с JSON v2
    op.execute("DELETE FROM game_saves WHERE format_version < 2")


def downgrade() -> None:
    op.drop_column("game_saves", "format_version")
