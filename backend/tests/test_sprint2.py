import csv
import io
from datetime import date

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine, event, inspect, select, text
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

from app.db import Base, get_db, initialize_schema
from app.main import app
from app.models import AccessSession, Group, Role, Student, Subject, User
from app.security import hash_password

PASSWORD = "test-password"


def login(client, username):
    response = client.post("/auth/login", json={"login": username, "password": PASSWORD})
    assert response.status_code == 200
    return {"Authorization": f"Bearer {response.json()['access_token']}"}


@pytest.fixture
def scenario():
    engine = create_engine("sqlite://", poolclass=StaticPool, connect_args={"check_same_thread": False})

    @event.listens_for(engine, "connect")
    def enable_foreign_keys(connection, _):
        connection.execute("PRAGMA foreign_keys=ON")

    Base.metadata.create_all(engine)
    session = sessionmaker(bind=engine, expire_on_commit=False, autoflush=False)
    with session() as db:
        db.add_all([
            User(login="admin", full_name="Admin", role=Role.admin, password_hash=hash_password(PASSWORD)),
            Group(name="A"),
            Group(name="B"),
            Subject(name="Programming"),
        ])
        db.flush()
        db.add_all([
            Student(full_name="First", group_id=1),
            Student(full_name="Second", group_id=1),
            Student(full_name="Other", group_id=2),
        ])
        db.commit()

    def provide_session():
        with session() as db:
            yield db

    app.dependency_overrides[get_db] = provide_session
    client = TestClient(app)
    client.headers.update(login(client, "admin"))
    try:
        yield client, session
    finally:
        client.close()
        app.dependency_overrides.pop(get_db)
        engine.dispose()


def lesson_payload(*, group_id=1, pair_number=1):
    return {
        "subject_id": 1, "group_id": group_id, "lesson_date": date.today().isoformat(),
        "pair_number": pair_number, "start_time": "09:00", "end_time": "10:30",
    }


def add_lesson(client, **fields):
    response = client.post("/lessons", json=lesson_payload(**fields))
    assert response.status_code == 201, response.text
    return response.json()["id"]


def add_attendance(client, lesson_id, student_id=1, status="present"):
    return client.post("/attendance", json={
        "lesson_id": lesson_id, "student_id": student_id, "status": status,
    })


def test_groups_crud(scenario):
    client, _ = scenario
    response = client.post("/groups", json={"name": "C"})
    assert response.status_code == 201
    url = f"/groups/{response.json()['id']}"
    assert client.get(url).json()["name"] == "C"
    assert client.put(url, json={"name": "Updated"}).json()["name"] == "Updated"
    assert client.post("/groups", json={"name": "Updated"}).status_code == 409
    assert client.delete(url).status_code == 204
    assert client.get(url).status_code == 404
    assert client.delete("/groups/1").status_code == 409


def test_subjects_crud(scenario):
    client, _ = scenario
    response = client.post("/subjects", json={"name": "Math"})
    assert response.status_code == 201
    url = f"/subjects/{response.json()['id']}"
    assert client.get(url).json()["name"] == "Math"
    assert client.put(url, json={"name": "Physics"}).json()["name"] == "Physics"
    assert client.delete(url).status_code == 204
    add_lesson(client)
    assert client.delete("/subjects/1").status_code == 409


def test_students_crud(scenario):
    client, _ = scenario
    response = client.post("/students", json={"full_name": "New", "group_id": 1})
    assert response.status_code == 201
    url = f"/students/{response.json()['id']}"
    assert client.get(url).json()["full_name"] == "New"
    response = client.put(url, json={"full_name": "Renamed", "group_id": 2})
    assert response.json()["group_id"] == 2
    assert client.delete(url).status_code == 204


def test_lessons_crud_and_slot_validation(scenario):
    client, _ = scenario
    lesson_id = add_lesson(client)
    url = f"/lessons/{lesson_id}"
    assert client.get(url).status_code == 200
    assert client.post("/lessons", json=lesson_payload()).status_code == 409
    assert client.put(url, json=lesson_payload(pair_number=2)).json()["pair_number"] == 2
    invalid = {**lesson_payload(), "end_time": "08:00"}
    assert client.post("/lessons", json=invalid).status_code == 422
    assert client.delete(url).status_code == 204


def test_delete_and_move_lesson_with_attendance_are_rejected(scenario):
    client, _ = scenario
    lesson_id = add_lesson(client)
    assert add_attendance(client, lesson_id).status_code == 201
    assert client.delete(f"/lessons/{lesson_id}").status_code == 409
    assert client.put(f"/lessons/{lesson_id}", json=lesson_payload(group_id=2)).status_code == 409


def test_mass_attendance_and_edit(scenario):
    client, _ = scenario
    lesson_id = add_lesson(client)
    url = f"/lessons/{lesson_id}/attendance"
    response = client.post(f"{url}/batch", json={
        "overrides": [{"student_id": 2, "status": "absent", "note": "Ill"}],
    })
    assert response.status_code == 201
    first, second = response.json()
    assert (first["status"], second["status"], second["note"]) == ("present", "absent", "Ill")
    first_url = f"/attendance/{first['id']}"
    second_url = f"/attendance/{second['id']}"
    assert client.patch(first_url, json={"status": "late"}).json()["status"] == "late"
    assert client.patch(second_url, json={"status": "excused"}).json()["note"] == "Ill"
    assert client.patch(second_url, json={"status": "excused", "note": None}).json()["note"] is None
    assert client.post(f"{url}/batch", json={}).status_code == 409


def test_duplicate_and_partial_batch_rollback(scenario):
    client, _ = scenario
    lesson_id = add_lesson(client)
    assert add_attendance(client, lesson_id, 2).status_code == 201
    assert add_attendance(client, lesson_id, 2).status_code == 409
    assert add_attendance(client, lesson_id, 3).status_code == 400
    url = f"/lessons/{lesson_id}/attendance"
    assert client.post(f"{url}/batch", json={}).status_code == 409
    assert [row["student_id"] for row in client.get(url).json()] == [2]


def test_rejects_override_for_student_outside_lesson_group(scenario):
    client, _ = scenario
    lesson_id = add_lesson(client)
    url = f"/lessons/{lesson_id}/attendance"
    assert client.post(f"{url}/batch", json={
        "overrides": [{"student_id": 3, "status": "absent"}],
    }).status_code == 422
    assert client.get(url).json() == []


def test_stats_use_counts_and_include_late(scenario):
    client, _ = scenario
    lesson_id = add_lesson(client)
    assert add_attendance(client, lesson_id, 1, "late").status_code == 201
    assert add_attendance(client, lesson_id, 2, "excused").status_code == 201
    assert client.get("/stats/students/1").json()["attendance_percent"] == 100.0
    assert client.get("/stats/groups/1").json()["attendance_percent"] == 50.0
    assert client.get("/stats/subjects/1").json()["attendance_percent"] == 50.0
    assert client.get("/stats?group_id=1").json()[1]["excused"] == 1


def test_import_and_export_csv(scenario):
    client, _ = scenario
    url = "/groups/1/students/import"
    response = client.post(url, content="full_name\r\nСидоров Сергей\r\n".encode("utf-8-sig"),
                           headers={"Content-Type": "text/csv"})
    assert response.status_code == 201, response.text
    lesson_id = add_lesson(client)
    student_id = response.json()[0]["id"]
    assert add_attendance(client, lesson_id, student_id, "late").status_code == 201
    response = client.get("/groups/1/attendance.csv")
    assert response.status_code == 200
    rows = list(csv.DictReader(io.StringIO(response.content.decode("utf-8-sig"))))
    assert len(rows) == 1
    assert rows[0]["student"] == "Сидоров Сергей" and rows[0]["status"] == "late"


def test_invalid_csv_does_not_add_students(scenario):
    client, _ = scenario
    url = "/groups/1/students/import"
    headers = {"Content-Type": "text/csv"}
    assert client.post(url, content=b"full_name\nNew\nBad,Extra\n", headers=headers).status_code == 422
    assert client.post(url, content=b"full_name\nNew\nFirst\n", headers=headers).status_code == 409
    assert len(client.get("/groups/1/students").json()) == 2


def test_student_can_only_see_own_data(scenario):
    client, _ = scenario
    assert client.put("/students/1/account", json={"login": "student", "password": PASSWORD}).status_code == 201
    lesson_id = add_lesson(client)
    assert client.post(f"/lessons/{lesson_id}/attendance/batch", json={}).status_code == 201
    client.headers.update(login(client, "student"))
    assert [row["id"] for row in client.get("/groups/1/students").json()] == [1]
    assert client.get("/students/2").status_code == 403
    assert client.get("/stats/students/2").status_code == 403
    assert client.get("/stats/groups/1").status_code == 403
    assert client.get("/groups/1/attendance.csv").status_code == 403
    records = client.get(f"/lessons/{lesson_id}/attendance").json()
    assert [row["student_id"] for row in records] == [1]
    assert add_attendance(client, lesson_id).status_code == 403


def test_teacher_can_only_access_assigned_groups(scenario):
    client, _ = scenario
    response = client.post("/staff", json={
        "login": "teacher", "password": PASSWORD, "full_name": "Teacher", "group_ids": [1],
    })
    assert response.status_code == 201
    client.headers.update(login(client, "teacher"))
    assert [row["id"] for row in client.get("/groups").json()] == [1]
    assert client.get("/groups/2/students").status_code == 403
    assert client.get("/groups/2/attendance.csv").status_code == 403
    assert client.post("/lessons", json=lesson_payload(group_id=2)).status_code == 403
    assert client.post("/lessons", json=lesson_payload()).status_code == 201


def test_authentication_and_token_storage(scenario):
    client, session = scenario
    assert client.post("/auth/login", json={"login": "admin", "password": "wrong"}).status_code == 401
    with session() as db:
        assert db.scalar(select(AccessSession.token_hash)) not in client.headers["Authorization"]
    client.headers.pop("Authorization")
    assert client.get("/students").status_code == 401
    assert client.get("/students", headers={"Authorization": "Bearer forged"}).status_code == 401


def test_risk_can_filter_by_student(scenario):
    client, _ = scenario
    lesson_id = add_lesson(client)
    assert add_attendance(client, lesson_id, 1, "absent").status_code == 201
    response = client.get("/risk?group_id=1&student_id=1")
    assert response.status_code == 200
    result, = response.json()
    assert result["student_id"] == 1 and 0 <= result["risk_probability"] <= 1


def test_sprint1_schema_upgrades_without_erasing_rows():
    engine = create_engine("sqlite://")
    try:
        with engine.begin() as connection:
            connection.execute(text("CREATE TABLE students (id INTEGER PRIMARY KEY)"))
            connection.execute(text("CREATE TABLE attendance (id INTEGER PRIMARY KEY)"))
            connection.execute(text("INSERT INTO students (id) VALUES (42)"))
            connection.execute(text("INSERT INTO attendance (id) VALUES (99)"))
        initialize_schema(engine)
        initialize_schema(engine)
        inspector = inspect(engine)
        assert inspector.get_indexes("students")[0]["unique"]
        with engine.connect() as connection:
            assert connection.execute(text("SELECT id, user_id FROM students")).one() == (42, None)
            assert connection.execute(text("SELECT id, note FROM attendance")).one() == (99, None)
    finally:
        engine.dispose()
