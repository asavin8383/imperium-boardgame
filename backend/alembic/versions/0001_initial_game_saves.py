"""Создание таблицы game_saves

Revision ID: 0001
Revises:
Create Date: 2026-05-29
"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa

revision: str = "0001"
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table(
        "game_saves",
        sa.Column("id", sa.String(36), primary_key=True),
        sa.Column("name", sa.String(255), nullable=False),
        sa.Column("player_nation", sa.String(50), nullable=False),
        sa.Column("bot_nation", sa.String(50), nullable=False),
        sa.Column("difficulty", sa.String(50), nullable=False),
        sa.Column("game_phase", sa.String(50), nullable=False),
        sa.Column("round_number", sa.Integer, nullable=False, server_default="1"),
        sa.Column("player_period", sa.String(50), nullable=False),
        sa.Column("created_at", sa.DateTime, nullable=False),
        sa.Column("updated_at", sa.DateTime, nullable=False),
        sa.Column("state_data", sa.Text, nullable=False),
    )


def downgrade() -> None:
    op.drop_table("game_saves")
