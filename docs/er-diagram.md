# ER-диаграмма базы данных

Диаграмма описывает модели SQLAlchemy после спринта 2. Схему можно импортировать в dbdiagram.io из [schema.dbml](schema.dbml).

```mermaid
erDiagram
    direction TB
    USERS ||--o| STUDENTS : account
    USERS ||--o{ STAFF_GROUPS : receives
    GROUPS ||--o{ STAFF_GROUPS : permits
    USERS ||--o{ ACCESS_SESSIONS : opens
    USERS o|--o{ ATTENDANCE : marks
    GROUPS ||--o{ STUDENTS : includes
    GROUPS ||--o{ LESSONS : attends
    SUBJECTS ||--o{ LESSONS : schedules
    STUDENTS ||--o{ ATTENDANCE : has
    LESSONS ||--o{ ATTENDANCE : records

    USERS {
        int id PK
        varchar login UK
        varchar password_hash
        varchar full_name
        role role
    }
    GROUPS {
        int id PK
        varchar name UK
    }
    STUDENTS {
        int id PK
        varchar full_name
        int group_id FK
        int user_id FK,UK "optional"
    }
    STAFF_GROUPS {
        int user_id PK,FK
        int group_id PK,FK
    }
    ACCESS_SESSIONS {
        varchar token_hash PK
        int user_id FK
        datetime expires_at
    }
    SUBJECTS {
        int id PK
        varchar name UK
    }
    LESSONS {
        int id PK
        int subject_id FK
        int group_id FK
        date lesson_date
        int pair_number
        time start_time
        time end_time
    }
    ATTENDANCE {
        int id PK
        int lesson_id FK
        int student_id FK
        status status
        varchar note "optional"
        int marked_by FK "optional"
    }
```

- Для занятий уникальна комбинация `(group_id, lesson_date, pair_number)`.
- Для посещаемости уникальна пара `(lesson_id, student_id)`. Внешние ключи защищают занятие и студента от удаления, пока у них есть отметки.
- `user_id` студента может быть пустым, например у записи после импорта CSV. Администратор может создать аккаунт позднее.
- `staff_groups` связывает преподавателя или старосту с доступными группами. `access_sessions` хранит хеш токена и срок действия.
