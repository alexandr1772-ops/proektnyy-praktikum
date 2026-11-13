import os
from urllib.parse import quote_plus

from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase, sessionmaker
from sqlalchemy.pool import StaticPool


def _database_url() -> str:
    explicit_url = os.getenv("DATABASE_URL")
    if explicit_url:
        return explicit_url

    host = os.getenv("DB_HOST")
    if not host:
        return "sqlite:///./attendance.db"

    user = quote_plus(os.getenv("POSTGRES_USER", "attendance"))
    password = quote_plus(os.getenv("POSTGRES_PASSWORD", ""))
    database = quote_plus(os.getenv("POSTGRES_DB", "attendance"))
    port = os.getenv("DB_PORT", "5432")
    return f"postgresql+psycopg://{user}:{password}@{host}:{port}/{database}"


DATABASE_URL = _database_url()
connect_args = {"check_same_thread": False} if DATABASE_URL.startswith("sqlite") else {}
engine_options = {"pool_pre_ping": True, "connect_args": connect_args}
if DATABASE_URL == "sqlite:///:memory:":
    engine_options["poolclass"] = StaticPool
engine = create_engine(DATABASE_URL, **engine_options)
SessionLocal = sessionmaker(bind=engine, autoflush=False, expire_on_commit=False)


class Base(DeclarativeBase):
    pass
