import enum
from datetime import date, time

from sqlalchemy import (
    Date,
    Enum,
    ForeignKey,
    Integer,
    String,
    Time,
    UniqueConstraint,
)
from sqlalchemy.orm import Mapped, mapped_column

from .db import Base


class Role(str, enum.Enum):
    student = "student"
    starosta = "starosta"
    teacher = "teacher"
    admin = "admin"


class Status(str, enum.Enum):
    present = "present"
    absent = "absent"
    late = "late"
    excused = "excused"


class User(Base):
    __tablename__ = "users"

    id: Mapped[int] = mapped_column(primary_key=True)
    login: Mapped[str] = mapped_column(String(64), unique=True)
    password_hash: Mapped[str] = mapped_column(String(255))
    full_name: Mapped[str] = mapped_column(String(128))
    role: Mapped[Role] = mapped_column(Enum(Role))


class Group(Base):
    __tablename__ = "groups"

    id: Mapped[int] = mapped_column(primary_key=True)
    name: Mapped[str] = mapped_column(String(32), unique=True)


class Student(Base):
    __tablename__ = "students"

    id: Mapped[int] = mapped_column(primary_key=True)
    full_name: Mapped[str] = mapped_column(String(128))
    group_id: Mapped[int] = mapped_column(ForeignKey("groups.id"))


class Subject(Base):
    __tablename__ = "subjects"

    id: Mapped[int] = mapped_column(primary_key=True)
    name: Mapped[str] = mapped_column(String(128), unique=True)


class Lesson(Base):
    __tablename__ = "lessons"

    __table_args__ = (
        UniqueConstraint(
            "group_id",
            "lesson_date",
            "pair_number",
            name="uq_lesson_slot",
        ),
    )

    id: Mapped[int] = mapped_column(primary_key=True)
    subject_id: Mapped[int] = mapped_column(ForeignKey("subjects.id"))
    group_id: Mapped[int] = mapped_column(ForeignKey("groups.id"))
    lesson_date: Mapped[date] = mapped_column(Date)
    pair_number: Mapped[int] = mapped_column(Integer)
    start_time: Mapped[time] = mapped_column(Time)
    end_time: Mapped[time] = mapped_column(Time)


class Attendance(Base):
    __tablename__ = "attendance"

    __table_args__ = (
        UniqueConstraint(
            "lesson_id",
            "student_id",
            name="uq_attendance_lesson_student",
        ),
    )

    id: Mapped[int] = mapped_column(primary_key=True)
    lesson_id: Mapped[int] = mapped_column(ForeignKey("lessons.id"))
    student_id: Mapped[int] = mapped_column(ForeignKey("students.id"))
    status: Mapped[Status] = mapped_column(Enum(Status))
    marked_by: Mapped[int | None] = mapped_column(
        ForeignKey("users.id"),
        nullable=True,
    )