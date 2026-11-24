from collections import defaultdict
from datetime import date, timedelta
from typing import Annotated

from fastapi import APIRouter, HTTPException, Query
from sqlalchemy import func, select

from .access import Actor, DB, check_group, check_student, scope_groups
from .models import Attendance, Group, Lesson, Role, Status, Student, Subject
from .registry import find, students_query
from .risk_model import features, trained_model
from .schemas import CountsOut, RiskOut, StatsOut

router = APIRouter()
Weeks = Annotated[int, Query(ge=1, le=52)]


def records_query(weeks):
    today = date.today()
    return (
        select(Attendance.student_id, Attendance.status, func.count())
        .join(Lesson, Lesson.id == Attendance.lesson_id)
        .where(Lesson.lesson_date.between(today - timedelta(weeks=weeks), today))
        .group_by(Attendance.student_id, Attendance.status)
    )


def counts(status_counts):
    total = sum(status_counts.values())
    percent = (status_counts.get(Status.present, 0) + status_counts.get(Status.late, 0)) / total if total else 0
    return {
        "total": total,
        **{status.value: status_counts.get(status, 0) for status in Status},
        "attendance_percent": round(percent * 100, 2),
    }


def by_student(db, query):
    result = defaultdict(dict)
    for student_id, status, total in db.execute(query):
        result[student_id][status] = total
    return result


@router.get("/stats", response_model=list[StatsOut])
def group_student_stats(group_id: int, db: DB, actor: Actor, weeks: Weeks = 8):
    check_group(db, actor, group_id)
    find(db, Group, group_id)
    students = db.scalars(students_query(actor, group_id)).all()
    grouped = by_student(db, records_query(weeks).where(
        Lesson.group_id == group_id,
        Attendance.student_id.in_([student.id for student in students]),
    ))
    return [StatsOut(student_id=student.id, full_name=student.full_name, **counts(grouped[student.id]))
            for student in students]


@router.get("/stats/students/{student_id}", response_model=StatsOut)
def student_stats(student_id: int, db: DB, actor: Actor, weeks: Weeks = 8):
    student = find(db, Student, student_id)
    check_student(db, actor, student)
    grouped = by_student(db, records_query(weeks).where(Attendance.student_id == student_id))
    return StatsOut(student_id=student.id, full_name=student.full_name, **counts(grouped[student.id]))


def aggregate(db, query):
    totals = defaultdict(int)
    for _, status, total in db.execute(query):
        totals[status] += total
    return CountsOut(**counts(totals))


@router.get("/stats/groups/{group_id}", response_model=CountsOut)
def group_stats(group_id: int, db: DB, actor: Actor, weeks: Weeks = 8):
    check_group(db, actor, group_id, write=True)
    find(db, Group, group_id)
    return aggregate(db, records_query(weeks).where(Lesson.group_id == group_id))


@router.get("/stats/subjects/{subject_id}", response_model=CountsOut)
def subject_stats(subject_id: int, db: DB, actor: Actor, weeks: Weeks = 8, group_id: int | None = None):
    if actor.role == Role.student:
        raise HTTPException(403, "Общая статистика доступна преподавателю или администратору")
    find(db, Subject, subject_id)
    query = scope_groups(records_query(weeks), Lesson.group_id, actor).where(Lesson.subject_id == subject_id)
    if group_id is not None:
        check_group(db, actor, group_id)
        find(db, Group, group_id)
        query = query.where(Lesson.group_id == group_id)
    return aggregate(db, query)


@router.get("/risk", response_model=list[RiskOut])
def risk(group_id: int, db: DB, actor: Actor, student_id: int | None = None):
    check_group(db, actor, group_id)
    find(db, Group, group_id)
    students = students_query(actor, group_id)
    if student_id is not None:
        student = find(db, Student, student_id)
        check_student(db, actor, student)
        if student.group_id != group_id:
            raise HTTPException(404, "Студент не состоит в группе")
        students = students.where(Student.id == student_id)
    roster = db.scalars(students).all()
    today = date.today()
    histories = defaultdict(list)
    for identifier, status in db.execute(
        select(Attendance.student_id, Attendance.status)
        .join(Lesson, Lesson.id == Attendance.lesson_id)
        .where(
            Lesson.group_id == group_id,
            Lesson.lesson_date.between(today - timedelta(weeks=8), today),
            Attendance.student_id.in_([student.id for student in roster]),
        )
        .order_by(Lesson.lesson_date, Lesson.pair_number, Lesson.id)
    ):
        histories[identifier].append(status)
    model, _ = trained_model()
    result = []
    for student in roster:
        history = histories[student.id]
        if not history:
            continue
        values = features(history)
        probability = model.predict(values)
        result.append(RiskOut(
            student_id=student.id,
            full_name=student.full_name,
            attendance_percent=round(values[0] * 100, 2),
            absences=history.count(Status.absent),
            risk_level="high" if probability >= 0.5 else "medium" if probability >= 0.3 else "low",
            risk_probability=round(probability, 4),
        ))
    return result
