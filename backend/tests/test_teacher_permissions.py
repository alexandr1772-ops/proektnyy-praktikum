"""Integration tests for teacher access control.

The tests use the in-memory SQLite database configured before importing the app.
They exercise the real FastAPI routes and authorization dependencies.
"""
import os
import uuid

os.environ["DATABASE_URL"] = "sqlite:///:memory:"
os.environ["ADMIN_LOGIN"] = "admin"
os.environ["ADMIN_PASSWORD"] = "test-password"

import pytest
from fastapi.testclient import TestClient

from app.main import app


@pytest.fixture
def teacher_context():
    """Create an admin, two seeded groups, and a teacher assigned to one group."""
    with TestClient(app) as client:
        admin_login = client.post(
            "/auth/login",
            json={"login": "admin", "password": "test-password"},
        )
        assert admin_login.status_code == 200, admin_login.text
        admin_headers = {
            "Authorization": f"Bearer {admin_login.json()['access_token']}"
        }

        groups_response = client.get("/groups", headers=admin_headers)
        assert groups_response.status_code == 200, groups_response.text
        groups = groups_response.json()
        assert len(groups) >= 2, "The seeded test database must contain two groups"
        own_group_id = groups[0]["id"]
        foreign_group_id = groups[1]["id"]

        suffix = uuid.uuid4().hex[:10]
        teacher_login = f"teacher_{suffix}"
        teacher_password = "teacher-test-password"
        staff_response = client.post(
            "/staff",
            headers=admin_headers,
            json={
                "login": teacher_login,
                "password": teacher_password,
                "full_name": "Тестовый преподаватель",
                "role": "teacher",
                "group_ids": [own_group_id],
            },
        )
        assert staff_response.status_code == 201, staff_response.text
        teacher = staff_response.json()

        login_response = client.post(
            "/auth/login",
            json={"login": teacher_login, "password": teacher_password},
        )
        assert login_response.status_code == 200, login_response.text
        teacher_headers = {
            "Authorization": f"Bearer {login_response.json()['access_token']}"
        }

        yield {
            "client": client,
            "headers": teacher_headers,
            "teacher": teacher,
            "own_group_id": own_group_id,
            "foreign_group_id": foreign_group_id,
        }


def test_teacher_sees_only_assigned_group(teacher_context):
    ctx = teacher_context
    client, headers = ctx["client"], ctx["headers"]

    groups = client.get("/groups", headers=headers)
    assert groups.status_code == 200
    assert [group["id"] for group in groups.json()] == [ctx["own_group_id"]]

    own_group = client.get(
        f"/groups/{ctx['own_group_id']}", headers=headers
    )
    assert own_group.status_code == 200

    foreign_group = client.get(
        f"/groups/{ctx['foreign_group_id']}", headers=headers
    )
    assert foreign_group.status_code == 403

    foreign_students = client.get(
        f"/groups/{ctx['foreign_group_id']}/students", headers=headers
    )
    assert foreign_students.status_code == 403


def test_teacher_can_read_assigned_group_data_and_statistics(teacher_context):
    ctx = teacher_context
    client, headers = ctx["client"], ctx["headers"]
    group_id = ctx["own_group_id"]

    students = client.get(f"/groups/{group_id}/students", headers=headers)
    assert students.status_code == 200
    assert len(students.json()) > 0

    lessons = client.get(
        "/lessons", params={"group_id": group_id}, headers=headers
    )
    assert lessons.status_code == 200
    assert len(lessons.json()) > 0

    stats = client.get(f"/stats?group_id={group_id}", headers=headers)
    assert stats.status_code == 200

    group_stats = client.get(f"/stats/groups/{group_id}", headers=headers)
    assert group_stats.status_code == 200

    foreign_stats = client.get(
        f"/stats/groups/{ctx['foreign_group_id']}", headers=headers
    )
    assert foreign_stats.status_code == 403


def test_teacher_can_update_attendance_in_assigned_group(teacher_context):
    ctx = teacher_context
    client, headers = ctx["client"], ctx["headers"]
    lessons = client.get(
        "/lessons",
        params={"group_id": ctx["own_group_id"]},
        headers=headers,
    )
    assert lessons.status_code == 200 and lessons.json()
    lesson_id = lessons.json()[0]["id"]

    attendance = client.get(
        f"/lessons/{lesson_id}/attendance", headers=headers
    )
    assert attendance.status_code == 200 and attendance.json()
    attendance_id = attendance.json()[0]["id"]

    updated = client.patch(
        f"/attendance/{attendance_id}",
        headers=headers,
        json={"status": "present", "note": "Проверка прав преподавателя"},
    )
    assert updated.status_code == 200, updated.text
    assert updated.json()["marked_by"] == ctx["teacher"]["id"]


def test_teacher_cannot_write_to_foreign_group(teacher_context):
    ctx = teacher_context
    client, headers = ctx["client"], ctx["headers"]

    foreign_students = client.get(
        "/students",
        params={"group_id": ctx["foreign_group_id"]},
        headers={
            # This request must be made as the teacher, not as the administrator.
            "Authorization": headers["Authorization"]
        },
    )
    assert foreign_students.status_code == 403

    foreign_lessons = client.get(
        "/lessons",
        params={"group_id": ctx["foreign_group_id"]},
        headers=headers,
    )
    assert foreign_lessons.status_code == 403

    admin_login = client.post(
        "/auth/login",
        json={"login": "admin", "password": "test-password"},
    )
    assert admin_login.status_code == 200
    admin_headers = {
        "Authorization": f"Bearer {admin_login.json()['access_token']}"
    }
    foreign_students_data = client.get(
        "/students",
        params={"group_id": ctx["foreign_group_id"]},
        headers=admin_headers,
    ).json()
    foreign_lessons_data = client.get(
        "/lessons",
        params={"group_id": ctx["foreign_group_id"]},
        headers=admin_headers,
    ).json()
    assert foreign_students_data and foreign_lessons_data

    denied_mark = client.post(
        "/attendance",
        headers=headers,
        json={
            "lesson_id": foreign_lessons_data[0]["id"],
            "student_id": foreign_students_data[0]["id"],
            "status": "present",
        },
    )
    assert denied_mark.status_code == 403

    denied_student_create = client.post(
        "/students",
        headers=headers,
        json={
            "full_name": f"Тестовый студент {uuid.uuid4().hex[:8]}",
            "group_id": ctx["foreign_group_id"],
        },
    )
    assert denied_student_create.status_code == 403


def test_teacher_cannot_perform_admin_operations(teacher_context):
    ctx = teacher_context
    client, headers = ctx["client"], ctx["headers"]

    create_group = client.post(
        "/groups", headers=headers, json={"name": f"TEST-{uuid.uuid4().hex[:6]}"}
    )
    assert create_group.status_code == 403

    create_staff = client.post(
        "/staff",
        headers=headers,
        json={
            "login": f"another_{uuid.uuid4().hex[:8]}",
            "password": "another-test-password",
            "full_name": "Другой сотрудник",
            "role": "teacher",
            "group_ids": [ctx["own_group_id"]],
        },
    )
    assert create_staff.status_code == 403

    create_subject = client.post(
        "/subjects",
        headers=headers,
        json={"name": f"Тестовый предмет {uuid.uuid4().hex[:6]}"},
    )
    assert create_subject.status_code == 403

    delete_group = client.delete(
        f"/groups/{ctx['own_group_id']}", headers=headers
    )
    assert delete_group.status_code == 403
