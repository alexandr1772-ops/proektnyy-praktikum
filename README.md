# Проектный практикум — трекер посещаемости

Учебный групповой проект для учёта посещаемости занятий: REST API на FastAPI, PostgreSQL и мобильный Android-клиент. Backend и база запускаются одной командой через Docker Compose; таблицы и демонстрационные данные создаются автоматически.

## Команда и роли

| Участник | Роль | Ответственность |
| --- | --- | --- |
| **Климов Н.** | Mobile | Android-приложение на Kotlin и Jetpack Compose |
| **Сурков Г.** | Backend, БД и ML | API, модели, схема PostgreSQL и ML-функция |
| **Гусаров А.** | QA / DevOps | GitHub, Docker Compose, PostgreSQL, pytest, CI и сетевой доступ |

Подробнее: [docs/team.md](docs/team.md).

## Запуск одной командой

Нужен Docker Desktop или Docker Engine с Compose v2. Из корня репозитория:

```bash
docker compose up --build
```

После запуска доступны:

- `http://localhost:8000/health` — состояние API;
- `http://localhost:8000/health/db` — соединение с PostgreSQL и количество записей;
- `http://localhost:8000/docs` — Swagger UI;
- `/groups`, `/lessons`, `/attendance`, `/stats`, `/risk` — рабочие эндпоинты проекта.

`GET /health/db` подтверждает соединение и возвращает счётчики всех шести таблиц: `users`, `groups`, `students`, `subjects`, `lessons`, `attendance`.

Остановка без удаления данных:

```bash
docker compose down
```

Полностью чистый повторный запуск:

```bash
docker compose down --volumes
docker compose up --build
```

## Переменные окружения и секреты

В репозитории хранится только [.env.example](.env.example). Файл `.env` исключён через `.gitignore` и не должен попадать в GitHub. Для собственных значений создайте локальный `.env`:

```dotenv
POSTGRES_DB=attendance
POSTGRES_USER=attendance
POSTGRES_PASSWORD=your_local_password
ADMIN_LOGIN=admin
ADMIN_PASSWORD=your_local_admin_password
```

Compose автоматически читает `.env`. Без него используются только локальные демонстрационные значения, поэтому проверочный запуск не требует ручных правок.

## Доступ к API с телефона

Порт опубликован в `compose.yaml` как `8000:8000`, без привязки к `127.0.0.1`. Телефон и ноутбук должны находиться в одной Wi-Fi сети.

1. Узнайте локальный IPv4-адрес ноутбука (`ipconfig`, `ip addr` или `ifconfig`).
2. Разрешите входящие TCP-подключения к порту 8000 в брандмауэре.
3. На телефоне откройте `http://<IP-НОУТБУКА>:8000/health`.

Например: `http://192.168.1.25:8000/health`. Для Android-клиента с обычным HTTP в отладочной конфигурации должен быть разрешён cleartext-трафик.

## База данных и ER-диаграмма

Реализованы шесть таблиц:

- `users` — пользователи и роли;
- `groups` — учебные группы;
- `students` — студенты;
- `subjects` — предметы;
- `lessons` — занятия;
- `attendance` — отметки посещаемости.

ER-диаграмма: [docs/er_diagram.png](docs/er_diagram.png). Исходная схема: [docs/schema.dbml](docs/schema.dbml).

При первом запуске создаются администратор, две группы, 15 студентов, три предмета, занятия и отметки за последние недели. Повторный запуск не дублирует данные.

## API

| Метод | Путь | Назначение |
| --- | --- | --- |
| `POST` | `/auth/login` | Вход пользователя |
| `GET` | `/groups` | Список групп |
| `GET` | `/groups/{id}/students` | Студенты группы |
| `GET` | `/lessons` | Занятия |
| `GET`, `POST` | `/attendance` | Просмотр и создание отметок |
| `GET` | `/stats` | Статистика посещаемости |
| `GET` | `/risk` | Группа риска |
| `GET` | `/health/db` | Проверка БД и начальных данных |

## Тестирование и CI

```bash
cd backend
python -m pip install -r requirements-dev.txt
python -m pytest -q
```

GitHub Actions выполняет pytest, проверяет Compose, собирает контейнеры, запускает FastAPI с PostgreSQL и проверяет заполнение БД через `/health/db`.

## ML-функция

Выбранное направление — прогноз студентов группы риска по посещаемости. Сейчас `/risk` использует интерпретируемые пороги посещаемости; план дальнейшей модели на синтетических данных описан в [docs/ml_plan.md](docs/ml_plan.md). Демонстрационные результаты не следует считать надёжным прогнозом для реальных студентов.

## Структура

```text
backend/       FastAPI, SQLAlchemy, заполнение БД, тесты и Dockerfile
mobile/        Android-клиент и материалы
demo/          сценарий демонстрации
docs/          архитектура, роли, ER-схема и ML-план
compose.yaml   API + PostgreSQL
.github/       CI для pytest и Docker Compose
```
