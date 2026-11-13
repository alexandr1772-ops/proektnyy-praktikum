import csv
import os
from pathlib import Path

from sqlalchemy import select

from .db import SessionLocal
from .models import Attendance, Group, Lesson, Student, Subject


OUT = Path(
    os.getenv(
        "DATASET_EXPORT_PATH",
        Path.cwd() / "data" / "attendance_history.csv",
    )
)


def export():
    OUT.parent.mkdir(parents=True, exist_ok=True)

    with SessionLocal() as db:
        rows = db.execute(
            select(
                Lesson.lesson_date,
                Group.name,
                Subject.name,
                Student.full_name,
                Attendance.status,
            )
            .join(Attendance, Attendance.lesson_id == Lesson.id)
            .join(Student, Student.id == Attendance.student_id)
            .join(Group, Group.id == Lesson.group_id)
            .join(Subject, Subject.id == Lesson.subject_id)
            .order_by(
                Lesson.lesson_date,
                Group.name,
                Lesson.pair_number,
                Student.full_name,
            )
        ).all()

    with OUT.open("w", newline="", encoding="utf-8") as csv_file:
        writer = csv.writer(csv_file)
        writer.writerow(["date", "group", "subject", "student", "status"])

        for lesson_date, group, subject, student, status in rows:
            writer.writerow(
                [lesson_date.isoformat(), group, subject, student, status.value]
            )

    if rows:
        period = f"{rows[0][0].isoformat()}..{rows[-1][0].isoformat()}"
    else:
        period = "empty"

    print(f"rows={len(rows)} period={period} file={OUT}")


if __name__ == "__main__":
    export()
