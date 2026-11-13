from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field


class StudentCreate(BaseModel):
    full_name: str = Field(min_length=1, max_length=200)


class StudentRead(StudentCreate):
    model_config = ConfigDict(from_attributes=True)
    id: int


class LessonCreate(BaseModel):
    title: str = Field(min_length=1, max_length=200)
    starts_at: datetime


class LessonRead(LessonCreate):
    model_config = ConfigDict(from_attributes=True)
    id: int


class AttendanceCreate(BaseModel):
    student_id: int
    lesson_id: int
    present: bool = True


class AttendanceRead(AttendanceCreate):
    model_config = ConfigDict(from_attributes=True)
    id: int
    created_at: datetime
