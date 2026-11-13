from contextlib import asynccontextmanager

from fastapi import FastAPI
from sqlalchemy import func, select, text

from .db import SessionLocal
from .models import Attendance, Group, Lesson, Student, Subject, User
from .routes import router
from .seed import seed


@asynccontextmanager
async def lifespan(_: FastAPI):
    seed()
    yield


app = FastAPI(
    title="Student Attendance API",
    description="API для учёта посещаемости студентов",
    version="1.0.0",
    lifespan=lifespan,
)

app.include_router(router)


@app.get("/health")
def health_check():
    return {"status": "ok"}


@app.get("/health/db")
def database_health():
    with SessionLocal() as db:
        db.execute(text("SELECT 1"))
        return {
            "database": "connected",
            "users": db.scalar(select(func.count(User.id))),
            "groups": db.scalar(select(func.count(Group.id))),
            "students": db.scalar(select(func.count(Student.id))),
            "subjects": db.scalar(select(func.count(Subject.id))),
            "lessons": db.scalar(select(func.count(Lesson.id))),
            "attendance": db.scalar(select(func.count(Attendance.id))),
        }
