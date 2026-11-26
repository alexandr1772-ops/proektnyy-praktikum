import csv
import io
import os
from datetime import UTC, datetime, timedelta
from secrets import token_hex, token_urlsafe

import httpx
import pytest


BASE_URL = os.getenv("SPRINT2_E2E_BASE_URL")
pytestmark = pytest.mark.skipif(not BASE_URL, reason="Requires a disposable Compose stack")


def request(client, method, path, expected_status, **kwargs):
    response = client.request(method, path, **kwargs)
    assert response.status_code == expected_status, (
        f"{method} {path}: expected {expected_status}, got {response.status_code}"
    )
    return response


def login(client, username, password):
    response = request(client, "POST", "/auth/login", 200, json={
        "login": username,
        "password": password,
    })
    client.headers["Authorization"] = f"Bearer {response.json()['access_token']}"


def test_sprint2_demo_against_compose():
    admin_password = os.getenv("ADMIN_PASSWORD")
    assert admin_password, "Set ADMIN_PASSWORD to the disposable Compose stack's admin password"
    suffix = token_hex(4)
    student_login = f"qa-student-{suffix}"
    student_password = token_urlsafe(16)

    with httpx.Client(base_url=BASE_URL, timeout=10) as admin:
        login(admin, os.getenv("ADMIN_LOGIN", "admin"), admin_password)
        group_id = request(admin, "POST", "/groups", 201, json={
            "name": f"QA-{suffix}",
        }).json()["id"]
        subject_id = request(admin, "POST", "/subjects", 201, json={
            "name": f"QA subject {suffix}",
        }).json()["id"]
        first_id = request(admin, "POST", "/students", 201, json={
            "full_name": "Первый студент",
            "group_id": group_id,
            "account": {"login": student_login, "password": student_password},
        }).json()["id"]
        second_id = request(admin, "POST", "/students", 201, json={
            "full_name": "Второй студент",
            "group_id": group_id,
        }).json()["id"]
        lesson_date = (datetime.now(UTC).date() - timedelta(days=1)).isoformat()
        lesson_id = request(admin, "POST", "/lessons", 201, json={
            "subject_id": subject_id,
            "group_id": group_id,
            "lesson_date": lesson_date,
            "pair_number": 1,
            "start_time": "09:00",
            "end_time": "10:30",
        }).json()["id"]

        journal = f"/lessons/{lesson_id}/attendance"
        rows = request(admin, "POST", f"{journal}/batch", 201, json={
            "overrides": [{"student_id": second_id, "status": "absent", "note": "По болезни"}],
        }).json()
        records = {row["student_id"]: row for row in rows}
        assert len(rows) == 2 and set(records) == {first_id, second_id}
        assert records[first_id]["status"] == "present"
        assert records[second_id]["status"] == "absent"
        request(admin, "PATCH", f"/attendance/{records[first_id]['id']}", 200, json={
            "status": "late",
        })
        request(admin, "PATCH", f"/attendance/{records[second_id]['id']}", 200, json={
            "status": "excused",
        })
        request(admin, "POST", "/attendance", 409, json={
            "lesson_id": lesson_id, "student_id": first_id, "status": "absent",
        })
        request(admin, "DELETE", f"/lessons/{lesson_id}", 409)
        rows = request(admin, "GET", journal, 200).json()
        assert [(row["student_id"], row["status"], row["note"]) for row in rows] == [
            (first_id, "late", None), (second_id, "excused", "По болезни"),
        ]

        for path, total, percent in (
            (f"/stats/students/{first_id}", 1, 100),
            (f"/stats/groups/{group_id}", 2, 50),
            (f"/stats/subjects/{subject_id}", 2, 50),
        ):
            stats = request(admin, "GET", path, 200).json()
            assert (stats["total"], stats["late"], stats["attendance_percent"]) == (total, 1, percent)

        imported, = request(admin, "POST", f"/groups/{group_id}/students/import", 201,
                            content="full_name\r\nИванов Иван\r\n".encode("utf-8-sig"),
                            headers={"Content-Type": "text/csv"}).json()
        roster = request(admin, "GET", f"/groups/{group_id}/students", 200).json()
        assert imported["full_name"] == "Иванов Иван"
        assert {row["id"] for row in roster} == {first_id, second_id, imported["id"]}

        report = request(admin, "GET", f"/groups/{group_id}/attendance.csv", 200)
        assert report.headers["content-type"].startswith("text/csv")
        exported = list(csv.DictReader(io.StringIO(report.content.decode("utf-8-sig"))))
        assert {(row["student"], row["status"], row["note"]) for row in exported} == {
            ("Первый студент", "late", ""), ("Второй студент", "excused", "По болезни"),
        }
        assert len(exported) == 2

    with httpx.Client(base_url=BASE_URL, timeout=10) as student:
        login(student, student_login, student_password)
        request(student, "GET", f"/stats/students/{second_id}", 403)
        rows = request(student, "GET", journal, 200).json()
        assert [row["student_id"] for row in rows] == [first_id]
        prediction, = request(student, "GET", "/risk", 200, params={
            "group_id": group_id, "student_id": first_id,
        }).json()
        assert prediction["student_id"] == first_id
        assert 0 <= prediction["risk_probability"] <= 1
