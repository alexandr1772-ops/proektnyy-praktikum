import os
from urllib.parse import quote_plus


def build_database_url() -> str:
    explicit_url = os.getenv("DATABASE_URL")
    if explicit_url:
        return explicit_url

    host = os.getenv("DB_HOST")
    if not host:
        return "sqlite:///./dev.db"

    user = quote_plus(os.getenv("POSTGRES_USER", "attendance"))
    password = quote_plus(os.getenv("POSTGRES_PASSWORD", ""))
    database = quote_plus(os.getenv("POSTGRES_DB", "attendance"))
    port = os.getenv("DB_PORT", "5432")
    return f"postgresql+psycopg://{user}:{password}@{host}:{port}/{database}"


DATABASE_URL = build_database_url()
