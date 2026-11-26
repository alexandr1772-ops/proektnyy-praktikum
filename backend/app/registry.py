from datetime import date

from fastapi import APIRouter, HTTPException, Response
from sqlalchemy import exists, select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from .access import Actor, DB, check_group, check_student, require_admin, scope_groups
from .models import Attendance, Group, Lesson, Role, StaffGroup, Student, Subject, User
from .schemas import (
    AccountIn, GroupIn, GroupOut, LessonIn, LessonOut, StaffIn, StaffOut,
    StudentCreate, StudentIn, StudentOut, SubjectIn, SubjectOut,
)
from .security import hash_password

router = APIRouter()


def find(db, model, item_id, *, lock=False):
    row = db.get(model, item_id, with_for_update=lock)
    if row is None:
        raise HTTPException(404, "Запись не найдена")
    return row


def save(db: Session, message="Запись уже существует или используется"):
    try:
        db.commit()
    except IntegrityError as error:
        db.rollback()
        raise HTTPException(409, message) from error


def create_account(db, data, full_name, role):
    account = User(
        login=data.login,
        password_hash=hash_password(data.password),
        full_name=full_name,
        role=role,
    )
    db.add(account)
    try:
        db.flush()
    except IntegrityError as error:
        db.rollback()
        raise HTTPException(409, "Логин уже занят") from error
    return account


@router.post("/staff", status_code=201, response_model=StaffOut)
def create_staff(data: StaffIn, db: DB, actor: Actor):
    require_admin(actor)
    if data.role not in (Role.teacher, Role.starosta):
        raise HTTPException(422, "Выберите роль teacher или starosta")
    group_ids = sorted(set(data.group_ids))
    for group_id in group_ids:
        find(db, Group, group_id)
    user = create_account(db, data, data.full_name, data.role)
    db.add_all(StaffGroup(user_id=user.id, group_id=group_id) for group_id in group_ids)
    save(db)
    return StaffOut(id=user.id, login=user.login, full_name=user.full_name,
                    role=user.role, group_ids=group_ids)


@router.get("/groups", response_model=list[GroupOut])
def list_groups(db: DB, actor: Actor):
    return db.scalars(scope_groups(select(Group), Group.id, actor).order_by(Group.name)).all()


@router.get("/groups/{group_id}", response_model=GroupOut)
def read_group(group_id: int, db: DB, actor: Actor):
    check_group(db, actor, group_id)
    return find(db, Group, group_id)


@router.post("/groups", status_code=201, response_model=GroupOut)
def create_group(data: GroupIn, db: DB, actor: Actor):
    require_admin(actor)
    group = Group(name=data.name)
    db.add(group)
    save(db, "Группа с таким названием уже существует")
    return group


@router.put("/groups/{group_id}", response_model=GroupOut)
def update_group(group_id: int, data: GroupIn, db: DB, actor: Actor):
    require_admin(actor)
    group = find(db, Group, group_id, lock=True)
    group.name = data.name
    save(db, "Группа с таким названием уже существует")
    return group


@router.delete("/groups/{group_id}", status_code=204)
def delete_group(group_id: int, db: DB, actor: Actor):
    require_admin(actor)
    db.delete(find(db, Group, group_id, lock=True))
    save(db, "Группа используется")
    return Response(status_code=204)


@router.get("/subjects", response_model=list[SubjectOut])
def list_subjects(db: DB, actor: Actor):
    return db.scalars(select(Subject).order_by(Subject.name)).all()


@router.get("/subjects/{subject_id}", response_model=SubjectOut)
def read_subject(subject_id: int, db: DB, actor: Actor):
    return find(db, Subject, subject_id)


@router.post("/subjects", status_code=201, response_model=SubjectOut)
def create_subject(data: SubjectIn, db: DB, actor: Actor):
    require_admin(actor)
    subject = Subject(name=data.name)
    db.add(subject)
    save(db, "Предмет с таким названием уже существует")
    return subject


@router.put("/subjects/{subject_id}", response_model=SubjectOut)
def update_subject(subject_id: int, data: SubjectIn, db: DB, actor: Actor):
    require_admin(actor)
    subject = find(db, Subject, subject_id, lock=True)
    subject.name = data.name
    save(db, "Предмет с таким названием уже существует")
    return subject


@router.delete("/subjects/{subject_id}", status_code=204)
def delete_subject(subject_id: int, db: DB, actor: Actor):
    require_admin(actor)
    db.delete(find(db, Subject, subject_id, lock=True))
    save(db, "Предмет используется в занятиях")
    return Response(status_code=204)


def students_query(actor, group_id=None):
    query = scope_groups(select(Student), Student.group_id, actor)
    if actor.role == Role.student:
        query = query.where(Student.user_id == actor.id)
    if group_id is not None:
        query = query.where(Student.group_id == group_id)
    return query.order_by(Student.full_name, Student.id)


@router.get("/students", response_model=list[StudentOut])
def list_students(db: DB, actor: Actor, group_id: int | None = None):
    if group_id is not None:
        check_group(db, actor, group_id)
        find(db, Group, group_id)
    return db.scalars(students_query(actor, group_id)).all()


@router.get("/groups/{group_id}/students", response_model=list[StudentOut])
def list_group_students(group_id: int, db: DB, actor: Actor):
    return list_students(db, actor, group_id)


@router.get("/students/{student_id}", response_model=StudentOut)
def read_student(student_id: int, db: DB, actor: Actor):
    student = find(db, Student, student_id)
    check_student(db, actor, student)
    return student


@router.post("/students", status_code=201, response_model=StudentOut)
def create_student(data: StudentCreate, db: DB, actor: Actor):
    check_group(db, actor, data.group_id, write=True)
    find(db, Group, data.group_id, lock=True)
    student = Student(full_name=data.full_name, group_id=data.group_id)
    if data.account is not None:
        require_admin(actor)
        student.user_id = create_account(db, data.account, data.full_name, Role.student).id
    db.add(student)
    save(db)
    return student


@router.put("/students/{student_id}/account", status_code=201, response_model=StudentOut)
def link_student_account(student_id: int, data: AccountIn, db: DB, actor: Actor):
    require_admin(actor)
    student = find(db, Student, student_id, lock=True)
    if student.user_id is not None:
        raise HTTPException(409, "У студента уже есть аккаунт")
    student.user_id = create_account(db, data, student.full_name, Role.student).id
    save(db)
    return student


@router.put("/students/{student_id}", response_model=StudentOut)
def update_student(student_id: int, data: StudentIn, db: DB, actor: Actor):
    student = find(db, Student, student_id, lock=True)
    check_group(db, actor, student.group_id, write=True)
    check_group(db, actor, data.group_id, write=True)
    find(db, Group, data.group_id)
    if student.group_id != data.group_id and db.scalar(
        select(exists().where(Attendance.student_id == student_id))
    ):
        raise HTTPException(409, "У студента есть отметки в его группе")
    student.full_name = data.full_name
    student.group_id = data.group_id
    if student.user_id is not None:
        db.get(User, student.user_id).full_name = data.full_name
    save(db)
    return student


@router.delete("/students/{student_id}", status_code=204)
def delete_student(student_id: int, db: DB, actor: Actor):
    student = find(db, Student, student_id, lock=True)
    check_group(db, actor, student.group_id, write=True)
    db.delete(student)
    save(db, "У студента есть отметки посещаемости")
    return Response(status_code=204)


@router.get("/lessons", response_model=list[LessonOut])
def list_lessons(db: DB, actor: Actor, group_id: int | None = None, day: date | None = None):
    query = scope_groups(select(Lesson), Lesson.group_id, actor)
    if group_id is not None:
        check_group(db, actor, group_id)
        query = query.where(Lesson.group_id == group_id)
    if day is not None:
        query = query.where(Lesson.lesson_date == day)
    return db.scalars(query.order_by(Lesson.lesson_date, Lesson.pair_number, Lesson.id)).all()


@router.get("/lessons/{lesson_id}", response_model=LessonOut)
def read_lesson(lesson_id: int, db: DB, actor: Actor):
    lesson = find(db, Lesson, lesson_id)
    check_group(db, actor, lesson.group_id)
    return lesson


@router.post("/lessons", status_code=201, response_model=LessonOut)
def create_lesson(data: LessonIn, db: DB, actor: Actor):
    check_group(db, actor, data.group_id, write=True)
    find(db, Group, data.group_id)
    find(db, Subject, data.subject_id)
    lesson = Lesson(**data.model_dump())
    db.add(lesson)
    save(db, "У группы уже есть занятие на этой паре")
    return lesson


@router.put("/lessons/{lesson_id}", response_model=LessonOut)
def update_lesson(lesson_id: int, data: LessonIn, db: DB, actor: Actor):
    lesson = find(db, Lesson, lesson_id, lock=True)
    check_group(db, actor, lesson.group_id, write=True)
    check_group(db, actor, data.group_id, write=True)
    find(db, Group, data.group_id)
    find(db, Subject, data.subject_id)
    if lesson.group_id != data.group_id and db.scalar(
        select(exists().where(Attendance.lesson_id == lesson_id))
    ):
        raise HTTPException(409, "Нельзя менять группу занятия с отметками")
    for field, value in data.model_dump().items():
        setattr(lesson, field, value)
    save(db, "У группы уже есть занятие на этой паре")
    return lesson


@router.delete("/lessons/{lesson_id}", status_code=204)
def delete_lesson(lesson_id: int, db: DB, actor: Actor):
    lesson = find(db, Lesson, lesson_id, lock=True)
    check_group(db, actor, lesson.group_id, write=True)
    db.delete(lesson)
    save(db, "Нельзя удалить занятие с отметками")
    return Response(status_code=204)
