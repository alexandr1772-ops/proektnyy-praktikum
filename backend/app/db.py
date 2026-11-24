from sqlalchemy import create_engine, event, inspect, text
from sqlalchemy.orm import DeclarativeBase, sessionmaker
from sqlalchemy.pool import StaticPool

from .config import DATABASE_URL


class Base(DeclarativeBase):
    pass


engine_options = {"pool_pre_ping": True}
if DATABASE_URL.startswith("sqlite"):
    engine_options["connect_args"] = {"check_same_thread": False}
if DATABASE_URL == "sqlite:///:memory:":
    engine_options["poolclass"] = StaticPool

engine = create_engine(DATABASE_URL, **engine_options)
SessionLocal = sessionmaker(bind=engine, autoflush=False, expire_on_commit=False)


if engine.dialect.name == "sqlite":
    @event.listens_for(engine, "connect")
    def enable_foreign_keys(connection, _):
        connection.execute("PRAGMA foreign_keys=ON")


def initialize_schema(bind):
    with bind.begin() as connection:
        if connection.dialect.name == "postgresql":
            connection.execute(text("SELECT pg_advisory_xact_lock(251002)"))
        Base.metadata.create_all(connection)
        inspector = inspect(connection)
        if "note" not in {column["name"] for column in inspector.get_columns("attendance")}:
            connection.execute(text("ALTER TABLE attendance ADD COLUMN note VARCHAR(500)"))
        if "user_id" not in {column["name"] for column in inspector.get_columns("students")}:
            connection.execute(text("ALTER TABLE students ADD COLUMN user_id INTEGER REFERENCES users(id)"))
        student_index, = Base.metadata.tables["students"].indexes
        student_index.create(connection, checkfirst=True)


def get_db():
    with SessionLocal() as db:
        yield db
