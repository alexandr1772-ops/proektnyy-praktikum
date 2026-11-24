from datetime import date, time
from typing import Annotated

from pydantic import BaseModel, ConfigDict, Field, StringConstraints, model_validator

from .models import Role, Status


Name = Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=128)]
GroupName = Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=32)]
LoginName = Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=64)]
Password = Annotated[str, StringConstraints(min_length=8, max_length=256)]
Note = Annotated[str, StringConstraints(max_length=500)]


class LoginIn(BaseModel):
    login: LoginName
    password: str = Field(min_length=1, max_length=256)


class LoginOut(BaseModel):
    id: int
    login: str
    full_name: str
    role: Role
    access_token: str
    token_type: str = "bearer"
    expires_in: int


class AccountIn(BaseModel):
    login: LoginName
    password: Password


class StaffIn(AccountIn):
    full_name: Name
    role: Role = Role.teacher
    group_ids: list[int] = Field(min_length=1, max_length=100)


class StaffOut(BaseModel):
    id: int
    login: str
    full_name: str
    role: Role
    group_ids: list[int]


class GroupIn(BaseModel):
    name: GroupName


class GroupOut(GroupIn):
    id: int


class SubjectIn(BaseModel):
    name: Name


class SubjectOut(SubjectIn):
    id: int


class StudentIn(BaseModel):
    full_name: Name
    group_id: int


class StudentCreate(StudentIn):
    account: AccountIn | None = None


class StudentOut(StudentIn):
    id: int
    user_id: int | None


class LessonIn(BaseModel):
    subject_id: int
    group_id: int
    lesson_date: date
    pair_number: int = Field(gt=0)
    start_time: time
    end_time: time

    @model_validator(mode="after")
    def validate_times(self):
        if self.start_time.tzinfo or self.end_time.tzinfo:
            raise ValueError("Укажите время без часового пояса")
        if self.start_time >= self.end_time:
            raise ValueError("Время окончания должно быть позже времени начала")
        return self


class LessonOut(LessonIn):
    id: int


class AttendanceIn(BaseModel):
    lesson_id: int
    student_id: int
    status: Status
    note: Note | None = None


class AttendanceUpdate(BaseModel):
    status: Status
    note: Note | None = None


class AttendanceOverride(BaseModel):
    student_id: int
    status: Status
    note: Note | None = None


class AttendanceBatch(BaseModel):
    overrides: list[AttendanceOverride] = Field(default_factory=list, max_length=1000)


class AttendanceOut(BaseModel):
    id: int
    lesson_id: int
    student_id: int
    status: Status
    note: str | None
    marked_by: int | None

    model_config = ConfigDict(from_attributes=True)


class CountsOut(BaseModel):
    total: int = 0
    present: int = 0
    absent: int = 0
    late: int = 0
    excused: int = 0
    attendance_percent: float = 0.0


class StatsOut(CountsOut):
    student_id: int
    full_name: str


class RiskOut(BaseModel):
    student_id: int
    full_name: str
    attendance_percent: float
    absences: int
    risk_level: str
    risk_probability: float
