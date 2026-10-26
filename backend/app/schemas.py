from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field


class StudentCreate(BaseModel):
    full_name: str = Field(min_length=1, max_length=150)


class StudentOut(StudentCreate):
    id: int
    model_config = ConfigDict(from_attributes=True)


class LessonCreate(BaseModel):
    title: str = Field(min_length=1, max_length=150)
    starts_at: datetime


class LessonOut(LessonCreate):
    id: int
    model_config = ConfigDict(from_attributes=True)


class AttendanceCreate(BaseModel):
    student_id: int = Field(gt=0)
    lesson_id: int = Field(gt=0)
    present: bool


class AttendanceOut(AttendanceCreate):
    id: int
    model_config = ConfigDict(from_attributes=True)
