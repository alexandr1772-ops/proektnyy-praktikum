from datetime import date, timedelta

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy import select
from sqlalchemy.orm import Session

from .db import get_db
from .models import (
    Attendance,
    Group,
    Lesson,
    Role,
    Status,
    Student,
    Subject,
    User,
)
from .schemas import (
    AttendanceIn,
    AttendanceOut,
    AttendanceUpdate,
    GroupOut,
    LessonOut,
    LoginIn,
    LoginOut,
    RiskOut,
    StatsOut,
    StudentOut,
)
from .security import verify_password


router = APIRouter()


@router.post("/auth/login", response_model=LoginOut)
def login(data: LoginIn, db: Session = Depends(get_db)):
    user = db.scalar(
        select(User).where(User.login == data.login)
    )

    if user is None or not verify_password(
        data.password, user.password_hash
    ):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Неверный логин или пароль",
        )

    return user


@router.get("/groups", response_model=list[GroupOut])
def get_groups(db: Session = Depends(get_db)):
    return db.scalars(select(Group).order_by(Group.name)).all()


@router.get(
    "/groups/{group_id}/students",
    response_model=list[StudentOut],
)
def get_students(group_id: int, db: Session = Depends(get_db)):
    group = db.get(Group, group_id)

    if group is None:
        raise HTTPException(status_code=404, detail="Группа не найдена")

    return db.scalars(
        select(Student)
        .where(Student.group_id == group_id)
        .order_by(Student.full_name)
    ).all()


@router.get("/lessons", response_model=list[LessonOut])
def get_lessons(
    group_id: int | None = None,
    day: date | None = None,
    db: Session = Depends(get_db),
):
    query = select(Lesson)

    if group_id is not None:
        query = query.where(Lesson.group_id == group_id)

    if day is not None:
        query = query.where(Lesson.lesson_date == day)

    return db.scalars(
        query.order_by(Lesson.lesson_date, Lesson.pair_number)
    ).all()


@router.get(
    "/lessons/{lesson_id}/attendance",
    response_model=list[AttendanceOut],
)
def get_lesson_attendance(
    lesson_id: int,
    db: Session = Depends(get_db),
):
    if db.get(Lesson, lesson_id) is None:
        raise HTTPException(status_code=404, detail="Занятие не найдено")

    return db.scalars(
        select(Attendance)
        .where(Attendance.lesson_id == lesson_id)
        .order_by(Attendance.student_id)
    ).all()


@router.post(
    "/attendance",
    response_model=AttendanceOut,
    status_code=status.HTTP_201_CREATED,
)
def create_attendance(
    data: AttendanceIn,
    db: Session = Depends(get_db),
):
    lesson = db.get(Lesson, data.lesson_id)
    student = db.get(Student, data.student_id)

    if lesson is None:
        raise HTTPException(status_code=404, detail="Занятие не найдено")

    if student is None:
        raise HTTPException(status_code=404, detail="Студент не найден")

    if student.group_id != lesson.group_id:
        raise HTTPException(
            status_code=400,
            detail="Студент не принадлежит группе занятия",
        )

    existing = db.scalar(
        select(Attendance).where(
            Attendance.lesson_id == data.lesson_id,
            Attendance.student_id == data.student_id,
        )
    )

    if existing is not None:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Посещаемость уже отмечена",
        )

    attendance = Attendance(**data.model_dump())
    db.add(attendance)
    db.commit()
    db.refresh(attendance)

    return attendance


@router.patch(
    "/attendance/{attendance_id}",
    response_model=AttendanceOut,
)
def update_attendance(
    attendance_id: int,
    data: AttendanceUpdate,
    db: Session = Depends(get_db),
):
    attendance = db.get(Attendance, attendance_id)

    if attendance is None:
        raise HTTPException(status_code=404, detail="Запись не найдена")

    attendance.status = data.status
    db.commit()
    db.refresh(attendance)

    return attendance


@router.get("/stats", response_model=list[StatsOut])
def get_stats(
    group_id: int,
    weeks: int = Query(default=8, ge=1, le=52),
    db: Session = Depends(get_db),
):
    group = db.get(Group, group_id)

    if group is None:
        raise HTTPException(status_code=404, detail="Группа не найдена")

    date_from = date.today() - timedelta(weeks=weeks)

    students = db.scalars(
        select(Student)
        .where(Student.group_id == group_id)
        .order_by(Student.full_name)
    ).all()

    result = []

    for student in students:
        records = db.scalars(
            select(Attendance)
            .join(Lesson, Attendance.lesson_id == Lesson.id)
            .where(
                Attendance.student_id == student.id,
                Lesson.lesson_date >= date_from,
            )
        ).all()

        total = len(records)
        present = sum(r.status == Status.present for r in records)
        absent = sum(r.status == Status.absent for r in records)
        late = sum(r.status == Status.late for r in records)
        excused = sum(r.status == Status.excused for r in records)

        percent = (
            (present + late) / total * 100
            if total > 0
            else 0.0
        )

        result.append(
            StatsOut(
                student_id=student.id,
                full_name=student.full_name,
                total=total,
                present=present,
                absent=absent,
                late=late,
                excused=excused,
                attendance_percent=round(percent, 2),
            )
        )

    return result


@router.get("/risk", response_model=list[RiskOut])
def get_risk(
    group_id: int,
    db: Session = Depends(get_db),
):
    stats = get_stats(group_id=group_id, weeks=8, db=db)

    result = []

    for item in stats:
        if item.total == 0:
            continue

        if item.attendance_percent < 60:
            risk_level = "high"
        elif item.attendance_percent < 80:
            risk_level = "medium"
        else:
            risk_level = "low"

        result.append(
            RiskOut(
                student_id=item.student_id,
                full_name=item.full_name,
                attendance_percent=item.attendance_percent,
                absences=item.absent,
                risk_level=risk_level,
            )
        )

    return result