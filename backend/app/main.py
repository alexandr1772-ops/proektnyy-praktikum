from contextlib import asynccontextmanager

from fastapi import FastAPI

from . import models
from .db import Base, engine
from .routes import router


@asynccontextmanager
async def lifespan(app: FastAPI):
    Base.metadata.create_all(bind=engine)
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