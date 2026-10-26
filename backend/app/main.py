from contextlib import asynccontextmanager

from fastapi import Depends, FastAPI, HTTPException, Query, status
from sqlalchemy import select, text
from sqlalchemy.exc import IntegrityError, SQLAlchemyError
from sqlalchemy.orm import Session

from .database import Base, engine, get_db
from .models import Attendance, Lesson, Student
from .schemas import (
    AttendanceCreate,
    AttendanceOut,
    LessonCreate,
    LessonOut,
    StudentCreate,
    StudentOut,
)


@asynccontextmanager
async def lifespan(app: FastAPI):
    Base.metadata.create_all(bind=engine)  # Учебный прототип; позже нужны миграции Alembic.
    yield


app = FastAPI(title="Attendance Tracker", version="0.1.0", lifespan=lifespan)


@app.get("/health", tags=["system"])
def health():
    return {"status": "ok"}


@app.get("/health/db", tags=["system"])
def database_health(db: Session = Depends(get_db)):
    try:
        db.execute(text("SELECT 1"))
    except SQLAlchemyError as exc:
        raise HTTPException(status_code=503, detail="Database unavailable") from exc
    return {"status": "ok", "database": "connected"}


@app.post("/students", response_model=StudentOut, status_code=status.HTTP_201_CREATED, tags=["students"])
def create_student(data: StudentCreate, db: Session = Depends(get_db)):
    student = Student(full_name=data.full_name.strip())
    db.add(student)
    db.commit()
    db.refresh(student)
    return student


@app.get("/students", response_model=list[StudentOut], tags=["students"])
def get_students(db: Session = Depends(get_db)):
    return db.scalars(select(Student).order_by(Student.id)).all()


@app.post("/lessons", response_model=LessonOut, status_code=status.HTTP_201_CREATED, tags=["lessons"])
def create_lesson(data: LessonCreate, db: Session = Depends(get_db)):
    lesson = Lesson(title=data.title.strip(), starts_at=data.starts_at)
    db.add(lesson)
    db.commit()
    db.refresh(lesson)
    return lesson


@app.get("/lessons", response_model=list[LessonOut], tags=["lessons"])
def get_lessons(db: Session = Depends(get_db)):
    return db.scalars(select(Lesson).order_by(Lesson.id)).all()


@app.post("/attendance", response_model=AttendanceOut, status_code=status.HTTP_201_CREATED, tags=["attendance"])
def record_attendance(data: AttendanceCreate, db: Session = Depends(get_db)):
    if db.get(Student, data.student_id) is None:
        raise HTTPException(status_code=404, detail="Student not found")
    if db.get(Lesson, data.lesson_id) is None:
        raise HTTPException(status_code=404, detail="Lesson not found")
    record = Attendance(**data.model_dump())
    db.add(record)
    try:
        db.commit()
    except IntegrityError as exc:
        db.rollback()
        raise HTTPException(status_code=409, detail="Attendance already recorded") from exc
    db.refresh(record)
    return record


@app.get("/attendance", response_model=list[AttendanceOut], tags=["attendance"])
def get_attendance(lesson_id: int | None = Query(default=None, gt=0), db: Session = Depends(get_db)):
    query = select(Attendance).order_by(Attendance.id)
    if lesson_id is not None:
        query = query.where(Attendance.lesson_id == lesson_id)
    return db.scalars(query).all()
