from contextlib import asynccontextmanager
from datetime import datetime, timedelta, timezone

from fastapi import Depends, FastAPI, HTTPException, Query, status
from sqlalchemy import func, select, text
from sqlalchemy.orm import Session

from .database import Base, SessionLocal, engine
from .models import Attendance, Lesson, Student
from .schemas import (
    AttendanceCreate,
    AttendanceRead,
    LessonCreate,
    LessonRead,
    StudentCreate,
    StudentRead,
)


def get_db():
    with SessionLocal() as session:
        yield session


def seed_database(session: Session) -> None:
    if session.scalar(select(func.count(Student.id))) == 0:
        session.add_all(
            [Student(full_name="Иван Петров"), Student(full_name="Анна Смирнова")]
        )
        session.commit()

    if session.scalar(select(func.count(Lesson.id))) == 0:
        now = datetime.now(timezone.utc).replace(microsecond=0)
        session.add_all(
            [
                Lesson(title="Математика", starts_at=now),
                Lesson(title="Информатика", starts_at=now + timedelta(hours=2)),
            ]
        )
        session.commit()

    if session.scalar(select(func.count(Attendance.id))) == 0:
        student_id = session.scalar(select(Student.id).order_by(Student.id))
        lesson_id = session.scalar(select(Lesson.id).order_by(Lesson.id))
        session.add(
            Attendance(student_id=student_id, lesson_id=lesson_id, present=True)
        )
        session.commit()


@asynccontextmanager
async def lifespan(_: FastAPI):
    Base.metadata.create_all(bind=engine)
    with SessionLocal() as session:
        seed_database(session)
    yield


app = FastAPI(title="Трекер посещаемости", version="1.0.0", lifespan=lifespan)


@app.get("/")
def root():
    return {"service": "attendance-api", "docs": "/docs"}


@app.get("/health")
def health():
    return {"status": "ok"}


@app.get("/health/db")
def database_health(session: Session = Depends(get_db)):
    session.execute(text("SELECT 1"))
    return {
        "database": "connected",
        "students": session.scalar(select(func.count(Student.id))),
        "lessons": session.scalar(select(func.count(Lesson.id))),
        "attendance": session.scalar(select(func.count(Attendance.id))),
    }


@app.get("/students", response_model=list[StudentRead])
def list_students(session: Session = Depends(get_db)):
    return session.scalars(select(Student).order_by(Student.id)).all()


@app.post(
    "/students", response_model=StudentRead, status_code=status.HTTP_201_CREATED
)
def create_student(payload: StudentCreate, session: Session = Depends(get_db)):
    student = Student(full_name=payload.full_name)
    session.add(student)
    session.commit()
    session.refresh(student)
    return student


@app.get("/lessons", response_model=list[LessonRead])
def list_lessons(session: Session = Depends(get_db)):
    return session.scalars(select(Lesson).order_by(Lesson.id)).all()


@app.post("/lessons", response_model=LessonRead, status_code=status.HTTP_201_CREATED)
def create_lesson(payload: LessonCreate, session: Session = Depends(get_db)):
    lesson = Lesson(title=payload.title, starts_at=payload.starts_at)
    session.add(lesson)
    session.commit()
    session.refresh(lesson)
    return lesson


@app.get("/attendance", response_model=list[AttendanceRead])
def list_attendance(
    lesson_id: int | None = Query(default=None),
    session: Session = Depends(get_db),
):
    query = select(Attendance).order_by(Attendance.id)
    if lesson_id is not None:
        query = query.where(Attendance.lesson_id == lesson_id)
    return session.scalars(query).all()


@app.post(
    "/attendance", response_model=AttendanceRead, status_code=status.HTTP_201_CREATED
)
def create_attendance(payload: AttendanceCreate, session: Session = Depends(get_db)):
    if session.get(Student, payload.student_id) is None:
        raise HTTPException(status_code=404, detail="Student not found")
    if session.get(Lesson, payload.lesson_id) is None:
        raise HTTPException(status_code=404, detail="Lesson not found")

    attendance = Attendance(**payload.model_dump())
    session.add(attendance)
    session.commit()
    session.refresh(attendance)
    return attendance
