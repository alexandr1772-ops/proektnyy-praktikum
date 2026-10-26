
from fastapi import FastAPI

app = FastAPI(title="Трекер посещаемости")


@app.get("/health")
def health():
    return {"status": "ok"}
