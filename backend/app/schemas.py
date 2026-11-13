from datetime import date, time

from pydantic import BaseModel, ConfigDict

from .models import Role, Status


class LoginIn(BaseModel):
    login: str
    password: str


class LoginOut(BaseModel):
    id: int
    login: str
    full_name: str
    role: Role


class GroupOut(BaseModel):
    id: int
    name: str


class StudentOut(BaseModel):
    id: int
    full_name: str
    group_id: int


class LessonOut(BaseModel):
    id: int
    subject_id: int
    group_id: int
    lesson_date: date
    pair_number: int
    start_time: time
    end_time: time


class AttendanceIn(BaseModel):
    lesson_id: int
    student_id: int
    status: Status
    marked_by: int | None = None


class AttendanceUpdate(BaseModel):
    status: Status


class AttendanceOut(BaseModel):
    id: int
    lesson_id: int
    student_id: int
    status: Status
    marked_by: int | None

    model_config = ConfigDict(from_attributes=True)


class StatsOut(BaseModel):
    student_id: int
    full_name: str
    total: int
    present: int
    absent: int
    late: int
    excused: int
    attendance_percent: float


class RiskOut(BaseModel):
    student_id: int
    full_name: str
    attendance_percent: float
    absences: int
    risk_level: str