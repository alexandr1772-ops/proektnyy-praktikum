import os
from datetime import date, time, timedelta

from sqlalchemy import select

from .db import Base, SessionLocal, engine
from .models import Attendance, Group, Lesson, Role, Status, Student, Subject, User
from .security import hash_password


def seed():
    Base.metadata.create_all(bind=engine)
    admin_login = os.getenv("ADMIN_LOGIN", "admin")
    admin_password = os.environ["ADMIN_PASSWORD"]

    with SessionLocal() as db:
        admin = db.scalar(select(User).where(User.login == admin_login))
        if admin is None:
            admin = User(
                login=admin_login,
                password_hash=hash_password(admin_password),
                full_name="Администратор",
                role=Role.admin,
            )
            db.add(admin)
            db.flush()

        groups = {}
        for name in ["IS-21", "IS-22"]:
            group = db.scalar(select(Group).where(Group.name == name))
            if group is None:
                group = Group(name=name)
                db.add(group)
                db.flush()
            groups[name] = group

        subjects = {}
        for name in ["Программирование", "Математика", "Базы данных"]:
            subject = db.scalar(select(Subject).where(Subject.name == name))
            if subject is None:
                subject = Subject(name=name)
                db.add(subject)
                db.flush()
            subjects[name] = subject

        students_data = [
            ("Иванов Иван", "IS-21"), ("Петров Пётр", "IS-21"),
            ("Сидоров Алексей", "IS-21"), ("Смирнов Дмитрий", "IS-21"),
            ("Кузнецов Максим", "IS-21"), ("Попов Артём", "IS-21"),
            ("Васильев Кирилл", "IS-21"), ("Соколов Михаил", "IS-21"),
            ("Михайлов Андрей", "IS-22"), ("Новиков Егор", "IS-22"),
            ("Фёдоров Никита", "IS-22"), ("Морозов Даниил", "IS-22"),
            ("Волков Роман", "IS-22"), ("Алексеев Тимур", "IS-22"),
            ("Лебедев Матвей", "IS-22"),
        ]
        students = {}
        for full_name, group_name in students_data:
            student = db.scalar(
                select(Student).where(
                    Student.full_name == full_name,
                    Student.group_id == groups[group_name].id,
                )
            )
            if student is None:
                student = Student(full_name=full_name, group_id=groups[group_name].id)
                db.add(student)
                db.flush()
            students[full_name] = student

        today = date.today()
        subjects_list = list(subjects.values())
        for group in groups.values():
            group_students = [s for s in students.values() if s.group_id == group.id]
            for week in range(-8, 2):
                for weekday in [0, 2, 4]:
                    lesson_date = today + timedelta(
                        weeks=week, days=weekday - today.weekday()
                    )
                    subject = subjects_list[(week + 8) % len(subjects_list)]
                    lesson = db.scalar(
                        select(Lesson).where(
                            Lesson.group_id == group.id,
                            Lesson.lesson_date == lesson_date,
                            Lesson.pair_number == 1,
                        )
                    )
                    if lesson is None:
                        lesson = Lesson(
                            subject_id=subject.id,
                            group_id=group.id,
                            lesson_date=lesson_date,
                            pair_number=1,
                            start_time=time(9, 0),
                            end_time=time(10, 30),
                        )
                        db.add(lesson)
                        db.flush()
                    if lesson_date > today:
                        continue

                    statuses = [Status.present, Status.present, Status.present,
                                Status.absent, Status.late]
                    for index, student in enumerate(group_students):
                        attendance = db.scalar(
                            select(Attendance).where(
                                Attendance.lesson_id == lesson.id,
                                Attendance.student_id == student.id,
                            )
                        )
                        if attendance is None:
                            db.add(Attendance(
                                lesson_id=lesson.id,
                                student_id=student.id,
                                status=statuses[(index + abs(week)) % len(statuses)],
                                marked_by=admin.id,
                            ))
        db.commit()


if __name__ == "__main__":
    seed()
