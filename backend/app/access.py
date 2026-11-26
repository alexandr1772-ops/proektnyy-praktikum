import hashlib
import secrets
from datetime import UTC, datetime, timedelta
from typing import Annotated

from fastapi import Depends, HTTPException
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy import delete, select
from sqlalchemy.orm import Session

from .db import get_db
from .models import AccessSession, Role, StaffGroup, Student, User

SESSION_SECONDS = 8 * 60 * 60
bearer = HTTPBearer(auto_error=False)
DB = Annotated[Session, Depends(get_db)]


def token_hash(token: str) -> str:
    return hashlib.sha256(token.encode()).hexdigest()


def utc_now() -> datetime:
    return datetime.now(UTC).replace(tzinfo=None)


def open_session(db: Session, user: User) -> str:
    now = utc_now()
    token = secrets.token_urlsafe(32)
    db.execute(delete(AccessSession).where(AccessSession.expires_at <= now))
    db.add(AccessSession(
        token_hash=token_hash(token),
        user_id=user.id,
        expires_at=now + timedelta(seconds=SESSION_SECONDS),
    ))
    db.commit()
    return token


def current_user(
    db: DB,
    credentials: Annotated[HTTPAuthorizationCredentials | None, Depends(bearer)],
) -> User:
    if credentials is not None:
        user = db.scalar(
            select(User)
            .join(AccessSession, AccessSession.user_id == User.id)
            .where(
                AccessSession.token_hash == token_hash(credentials.credentials),
                AccessSession.expires_at > utc_now(),
            )
        )
        if user is not None:
            return user
    raise HTTPException(401, "Требуется действующий токен", headers={"WWW-Authenticate": "Bearer"})


Actor = Annotated[User, Depends(current_user)]


def allowed_groups(actor: User):
    if actor.role == Role.student:
        return select(Student.group_id).where(Student.user_id == actor.id)
    return select(StaffGroup.group_id).where(StaffGroup.user_id == actor.id)


def scope_groups(query, column, actor: User):
    if actor.role == Role.admin:
        return query
    return query.where(column.in_(allowed_groups(actor)))


def check_group(db: Session, actor: User, group_id: int, *, write=False):
    if actor.role == Role.admin:
        return
    if write and actor.role == Role.student:
        raise HTTPException(403, "Недостаточно прав")
    if db.scalar(allowed_groups(actor).where(
        (Student.group_id if actor.role == Role.student else StaffGroup.group_id) == group_id
    )) is None:
        raise HTTPException(403, "Нет доступа к группе")


def check_student(db: Session, actor: User, student: Student):
    check_group(db, actor, student.group_id)
    if actor.role == Role.student and student.user_id != actor.id:
        raise HTTPException(403, "Нет доступа к данным другого студента")


def require_admin(actor: User):
    if actor.role != Role.admin:
        raise HTTPException(403, "Требуются права администратора")
