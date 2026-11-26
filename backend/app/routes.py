import csv
import io

from fastapi import APIRouter, Body, HTTPException, Response
from sqlalchemy import select

from .access import Actor, DB, SESSION_SECONDS, check_group, open_session
from .models import Attendance, Group, Lesson, Role, Status, Student, Subject, User
from .registry import find, router as registry_router, save
from .reporting import router as reporting_router
from .schemas import AttendanceBatch, AttendanceIn, AttendanceOut, AttendanceUpdate, LoginIn, LoginOut, StudentOut
from .security import verify_password

router = APIRouter()
router.include_router(registry_router)
router.include_router(reporting_router)


@router.post("/auth/login", response_model=LoginOut)
def login(data: LoginIn, db: DB):
    user = db.scalar(select(User).where(User.login == data.login))
    if user is None or not verify_password(data.password, user.password_hash):
        raise HTTPException(401, "Неверный логин или пароль")
    token = open_session(db, user)
    return LoginOut(id=user.id, login=user.login, full_name=user.full_name, role=user.role,
                    access_token=token, expires_in=SESSION_SECONDS)


@router.get("/lessons/{lesson_id}/attendance", response_model=list[AttendanceOut])
def get_lesson_attendance(lesson_id: int, db: DB, actor: Actor):
    lesson = find(db, Lesson, lesson_id)
    check_group(db, actor, lesson.group_id)
    query = select(Attendance).where(Attendance.lesson_id == lesson_id)
    if actor.role == Role.student:
        query = query.join(Student, Student.id == Attendance.student_id).where(Student.user_id == actor.id)
    return db.scalars(query.order_by(Attendance.student_id)).all()


@router.post("/attendance", response_model=AttendanceOut, status_code=201)
def create_attendance(data: AttendanceIn, db: DB, actor: Actor):
    lesson = find(db, Lesson, data.lesson_id, lock=True)
    check_group(db, actor, lesson.group_id, write=True)
    student = find(db, Student, data.student_id, lock=True)
    if student.group_id != lesson.group_id:
        raise HTTPException(400, "Студент не принадлежит группе занятия")
    attendance = Attendance(**data.model_dump(), marked_by=actor.id)
    db.add(attendance)
    save(db, "Посещаемость уже отмечена")
    return attendance


@router.post("/lessons/{lesson_id}/attendance/batch", response_model=list[AttendanceOut], status_code=201)
def create_attendance_batch(lesson_id: int, data: AttendanceBatch, db: DB, actor: Actor):
    lesson = find(db, Lesson, lesson_id, lock=True)
    check_group(db, actor, lesson.group_id, write=True)
    student_ids = db.scalars(
        select(Student.id).where(Student.group_id == lesson.group_id).order_by(Student.id).with_for_update()
    ).all()
    if not student_ids:
        raise HTTPException(409, "В группе нет студентов")
    overrides = {item.student_id: item for item in data.overrides}
    if len(overrides) != len(data.overrides):
        raise HTTPException(422, "В запросе повторяется студент")
    if set(overrides).difference(student_ids):
        raise HTTPException(422, "В запросе есть студент из другой группы")
    records = []
    for student_id in student_ids:
        override = overrides.get(student_id)
        records.append(Attendance(
            lesson_id=lesson_id,
            student_id=student_id,
            status=override.status if override else Status.present,
            note=override.note if override else None,
            marked_by=actor.id,
        ))
    db.add_all(records)
    save(db, "По занятию уже есть отметки. Измените их точечно")
    return records


@router.patch("/attendance/{attendance_id}", response_model=AttendanceOut)
def update_attendance(attendance_id: int, data: AttendanceUpdate, db: DB, actor: Actor):
    attendance = find(db, Attendance, attendance_id, lock=True)
    lesson = find(db, Lesson, attendance.lesson_id)
    check_group(db, actor, lesson.group_id, write=True)
    attendance.status = data.status
    if "note" in data.model_fields_set:
        attendance.note = data.note
    attendance.marked_by = actor.id
    save(db)
    return attendance


@router.post("/groups/{group_id}/students/import", response_model=list[StudentOut], status_code=201)
def import_students(group_id: int, db: DB, actor: Actor, content: bytes = Body(media_type="text/csv")):
    check_group(db, actor, group_id, write=True)
    find(db, Group, group_id, lock=True)
    if len(content) > 256_000:
        raise HTTPException(413, "CSV превышает 256 КБ")
    try:
        reader = csv.reader(io.StringIO(content.decode("utf-8-sig"), newline=""), strict=True)
        if next(reader, None) != ["full_name"]:
            raise HTTPException(422, "Заголовок CSV должен быть full_name")
        names = []
        for row in reader:
            if not row:
                continue
            if len(row) != 1 or not 1 <= len(row[0].strip()) <= 128:
                raise HTTPException(422, f"Некорректное имя в строке {reader.line_num}")
            names.append(row[0].strip())
    except (UnicodeError, csv.Error) as error:
        raise HTTPException(422, "Ожидается корректный CSV в UTF-8") from error
    if not names:
        raise HTTPException(422, "В CSV нет студентов")
    existing = set(db.scalars(select(Student.full_name).where(Student.group_id == group_id)))
    if len(set(names)) != len(names) or existing.intersection(names):
        raise HTTPException(409, "CSV содержит повторяющееся имя для этой группы")
    students = [Student(full_name=name, group_id=group_id) for name in names]
    db.add_all(students)
    save(db)
    return students


def csv_cell(value):
    if isinstance(value, str) and value.lstrip().startswith(("=", "+", "-", "@", "\t", "\r", "\n")):
        return "'" + value
    return value


@router.get("/groups/{group_id}/attendance.csv", response_class=Response)
def export_group(group_id: int, db: DB, actor: Actor):
    check_group(db, actor, group_id, write=True)
    group = find(db, Group, group_id)
    records = db.execute(
        select(Lesson.lesson_date, Lesson.pair_number, Student.full_name, Subject.name, Attendance.status, Attendance.note)
        .select_from(Attendance)
        .join(Lesson, Lesson.id == Attendance.lesson_id)
        .join(Student, Student.id == Attendance.student_id)
        .join(Subject, Subject.id == Lesson.subject_id)
        .where(Lesson.group_id == group_id)
        .order_by(Lesson.lesson_date, Lesson.pair_number, Student.full_name, Attendance.id)
    )
    output = io.StringIO(newline="")
    writer = csv.writer(output)
    writer.writerow(["date", "group", "pair_number", "subject", "student", "status", "note"])
    for day, pair_number, student, subject, status, note in records:
        writer.writerow([
            day, csv_cell(group.name), pair_number, csv_cell(subject),
            csv_cell(student), status.value, csv_cell(note),
        ])
    return Response(
        output.getvalue().encode("utf-8-sig"),
        media_type="text/csv; charset=utf-8",
        headers={"Content-Disposition": f'attachment; filename="attendance-group-{group_id}.csv"'},
    )
