# Проектный практикум — трекер посещаемости

REST API на FastAPI с PostgreSQL для учёта студентов, занятий и посещаемости. Backend и база данных запускаются одной командой через Docker Compose; при первом старте таблицы создаются автоматически и заполняются демонстрационными данными.

## Команда и роли

| Участник | Роль | Ответственность |
| --- | --- | --- |
| Климов Н. | Mobile | Android-приложение на Kotlin и Jetpack Compose |
| Сурков Г. | Backend, БД и ML | API, модели и схема PostgreSQL |
| Гусаров А. | QA / DevOps | Docker, Compose, переменные окружения, CI и сетевой доступ к API |

Подробнее: [docs/team.md](docs/team.md).

## Запуск одной командой

Нужен Docker Desktop или Docker Engine с Compose v2. Из корня репозитория:

```bash
docker compose up --build
```

Команда поднимает два контейнера:

- `backend` — API на `0.0.0.0:8000`;
- `db` — PostgreSQL 16 с постоянным Docker volume.

После запуска доступны:

- `http://localhost:8000/health` — состояние API;
- `http://localhost:8000/health/db` — соединение с БД и количество записей;
- `http://localhost:8000/docs` — Swagger UI;
- `http://localhost:8000/students`, `/lessons`, `/attendance` — демонстрационные данные.

Ожидаемый ответ `GET /health/db` сразу после чистого запуска:

```json
{
  "database": "connected",
  "students": 2,
  "lessons": 2,
  "attendance": 1
}
```

Остановить контейнеры можно командой `docker compose down`. Данные сохранятся. Для полностью чистого повторного запуска удалите volume командой `docker compose down --volumes`, затем снова выполните `docker compose up --build`.

## Переменные окружения и секреты

В репозитории хранится только [.env.example](.env.example) с безопасными демонстрационными значениями. Файл `.env` добавлен в `.gitignore` и не должен попадать в GitHub.

Для собственных значений создайте локальный `.env` по образцу:

```dotenv
POSTGRES_DB=attendance
POSTGRES_USER=attendance
POSTGRES_PASSWORD=your_local_password
```

Compose автоматически прочитает `.env`. Без него используются только локальные значения по умолчанию, поэтому проверочный запуск не требует ручной настройки.

## Доступ к API с телефона

В `compose.yaml` порт опубликован как `8000:8000`, без привязки к `127.0.0.1`. Телефон и ноутбук должны находиться в одной Wi-Fi сети.

1. Узнайте локальный IPv4-адрес ноутбука (`ipconfig` в Windows, `ip addr` в Linux или `ifconfig` в macOS).
2. Разрешите входящие TCP-подключения к порту 8000 в брандмауэре.
3. На телефоне откройте `http://<IP-НОУТБУКА>:8000/health`.

Например: `http://192.168.1.25:8000/health`. Если Android-клиент использует обычный HTTP, в его отладочной конфигурации должен быть разрешён cleartext-трафик.

## API

| Метод | Путь | Назначение |
| --- | --- | --- |
| `GET` | `/health` | Проверка API |
| `GET` | `/health/db` | Проверка PostgreSQL и заполнения БД |
| `GET`, `POST` | `/students` | Список и создание студентов |
| `GET`, `POST` | `/lessons` | Список и создание занятий |
| `GET`, `POST` | `/attendance` | Список и создание отметок |

## Тесты

```bash
cd backend
python -m pip install -r requirements-dev.txt
python -m pytest -q
```

GitHub Actions дополнительно собирает образы, запускает API с PostgreSQL и проверяет наличие начальных данных.

## Структура

```text
backend/       FastAPI, SQLAlchemy, тесты и Dockerfile
mobile/        материалы Android-клиента
demo/          сценарий демонстрации
docs/          архитектура, роли и ER-диаграмма
compose.yaml   API + PostgreSQL
.github/       CI для pytest и Docker Compose
```
