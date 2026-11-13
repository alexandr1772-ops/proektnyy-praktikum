import os

os.environ["DATABASE_URL"] = "sqlite:///:memory:"
os.environ["ADMIN_LOGIN"] = "admin"
os.environ["ADMIN_PASSWORD"] = "test-password"

from fastapi.testclient import TestClient

from app.main import app


def test_health_and_seeded_database():
    with TestClient(app) as client:
        assert client.get("/health").json() == {"status": "ok"}

        response = client.get("/health/db")
        assert response.status_code == 200
        data = response.json()
        assert data["database"] == "connected"
        assert data["users"] >= 1
        assert data["groups"] == 2
        assert data["students"] == 15
        assert data["subjects"] == 5
        assert data["lessons"] > 0
        assert data["attendance"] > 0


def test_login_and_demo_endpoints():
    with TestClient(app) as client:
        login = client.post(
            "/auth/login",
            json={"login": "admin", "password": "test-password"},
        )
        assert login.status_code == 200
        assert login.json()["role"] == "admin"

        groups = client.get("/groups")
        assert groups.status_code == 200
        assert len(groups.json()) == 2

        lessons = client.get("/lessons")
        assert lessons.status_code == 200
        assert len(lessons.json()) > 0
