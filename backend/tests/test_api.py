import os

os.environ["DATABASE_URL"] = "sqlite:///:memory:"

from fastapi.testclient import TestClient

from app.main import app


def test_health_and_seeded_database():
    with TestClient(app) as client:
        assert client.get("/health").json() == {"status": "ok"}

        database = client.get("/health/db")
        assert database.status_code == 200
        assert database.json()["database"] == "connected"
        assert database.json()["students"] >= 2
        assert database.json()["lessons"] >= 2
        assert database.json()["attendance"] >= 1

        students = client.get("/students")
        assert students.status_code == 200
        assert len(students.json()) >= 2


def test_create_attendance_flow():
    with TestClient(app) as client:
        student = client.post("/students", json={"full_name": "Тестовый студент"})
        lesson = client.post(
            "/lessons",
            json={"title": "Тест", "starts_at": "2026-11-12T10:00:00+03:00"},
        )
        attendance = client.post(
            "/attendance",
            json={
                "student_id": student.json()["id"],
                "lesson_id": lesson.json()["id"],
                "present": True,
            },
        )

        assert student.status_code == 201
        assert lesson.status_code == 201
        assert attendance.status_code == 201
        assert attendance.json()["present"] is True
