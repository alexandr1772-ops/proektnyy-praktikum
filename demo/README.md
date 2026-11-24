# Демонстрация спринта 2

## Запуск

Из корня репозитория запустите `docker compose up --build`. Проверьте `http://localhost:8000/health/db` и откройте Swagger UI по адресу `http://localhost:8000/docs`.

## Журнал посещаемости

1. Через `POST /auth/login` войдите как администратор с данными из `.env` или демонстрационными значениями из `compose.yaml`. Скопируйте `access_token` в **Authorize**.
2. Создайте группу через `POST /groups`, двух студентов через `POST /students`, предмет через `POST /subjects` и пару на сегодняшнюю дату через `POST /lessons`. Для одного студента передайте `"account": {"login": "demo-student", "password": "demo-student-pass"}`.
3. Вызовите `POST /lessons/{lesson_id}/attendance/batch`. Передайте тело ниже и подставьте ID второго студента. Первый получит статус `present` автоматически.

```json
{"overrides": [{"student_id": 17, "status": "absent", "note": "Не пришёл"}]}
```

4. Возьмите два ID из ответа. Через `PATCH /attendance/{attendance_id}` поставьте первому `late`, второму `excused`. Через `GET /lessons/{lesson_id}/attendance` проверьте отметки. Повторите создание отдельной отметки для той же пары и студента. API вернёт `409`.
5. Покажите `GET /stats/students/{student_id}`, `GET /stats/groups/{group_id}` и `GET /stats/subjects/{subject_id}`. При двух отметках `late` и `excused` процент группы равен 50.
6. Импортируйте студента и скачайте CSV-отчёт по командам ниже. Создайте CSV с заголовком `full_name` и именем нового студента на второй строке.
7. Войдите под `demo-student`, обновите токен в Swagger и попробуйте открыть статистику второго студента. API вернёт `403`. С тем же токеном запросите `GET /risk?group_id={group_id}&student_id={student_id}` для своего ID и покажите `risk_probability`.
8. Покажите результаты pytest и метрики модели командами ниже. Для сдачи проверьте `git log --oneline` и наличие коммитов всех участников.

## CSV из PowerShell

Подставьте ID группы и токен администратора.

```powershell
$group = 3
$token = 'access_token_из_ответа'
curl.exe --fail-with-body -H "Authorization: Bearer $token" -H "Content-Type: text/csv" --data-binary "@students.csv" "http://localhost:8000/groups/$group/students/import"
curl.exe --fail-with-body -H "Authorization: Bearer $token" -o attendance.csv "http://localhost:8000/groups/$group/attendance.csv"
```

## Тесты и модель

```powershell
cd backend
python -m pip install -r requirements-dev.txt
python -m pytest -q --cov=app --cov-report=term-missing --cov-fail-under=40
python -m app.risk_model
```

## Проверка с телефона

На телефоне в той же Wi-Fi сети откройте `http://<локальный-IP-ноутбука>:8000/health`. Порт Compose опубликован как `8000:8000`. Пример успешной проверки в Android-приложении — [mobile_health_ok.jpg](mobile_health_ok.jpg).
