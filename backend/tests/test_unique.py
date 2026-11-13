import pytest
from sqlalchemy import create_engine, select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

import backend.app.seed as seed_module
from backend.app.models import Attendance, Status


def test_duplicate_attendance_is_rejected(monkeypatch):
    test_engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    test_session = sessionmaker(bind=test_engine, autoflush=False)

    # Направляем seed и тест на отдельную временную базу
    monkeypatch.setattr(seed_module, "engine", test_engine)
    monkeypatch.setattr(seed_module, "SessionLocal", test_session)

    try:
        seed_module.seed()

        with test_session() as db:
            existing = db.scalars(select(Attendance)).first()
            assert existing is not None

            duplicate = Attendance(
                lesson_id=existing.lesson_id,
                student_id=existing.student_id,
                status=Status.present,
            )
            db.add(duplicate)

            with pytest.raises(IntegrityError):
                db.commit()
    finally:
        test_engine.dispose()