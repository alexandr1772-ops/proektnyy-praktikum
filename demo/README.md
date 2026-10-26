# Демонстрация

## 1. Запуск

Из корня проекта:

```bash
docker compose up --build
```

Открыть http://localhost:8000/docs (Swagger UI) и http://localhost:8000/health.

## 2. Пример сценария

1. В Swagger UI вызвать `POST /students`: `{ "full_name": "Иван Петров" }`.
2. Вызвать `POST /lessons`: `{ "title": "Математика", "starts_at": "2026-11-01T10:00:00+03:00" }`.
3. Вызвать `POST /attendance`: `{ "student_id": 1, "lesson_id": 1, "present": true }`.
4. Вызвать `GET /attendance?lesson_id=1` и увидеть запись.
5. Проверить `GET /health/db` (ответ `database: connected`).

Номера `id` в вашем запуске могут отличаться.

## 3. Проверка pytest

```bash
cd backend
python -m pip install -r requirements-dev.txt
python -m pytest -q
```

Демонстрация мобильного приложения пока не готова — не выдавайте Swagger UI за мобильный клиент.
