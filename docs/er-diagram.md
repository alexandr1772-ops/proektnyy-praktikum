# ER-диаграмма базы данных

Это **начальная схема** учебного прототипа. Позже команда может добавить группы, преподавателей и пользователей.

```mermaid
erDiagram
    STUDENTS ||--o{ ATTENDANCE : "has"
    LESSONS ||--o{ ATTENDANCE : "tracks"
    STUDENTS {
        int id PK
        varchar full_name
    }
    LESSONS {
        int id PK
        varchar title
        datetime starts_at
    }
    ATTENDANCE {
        int id PK
        int student_id FK
        int lesson_id FK
        boolean present
        datetime created_at
    }
```

Ограничение уникальности `(student_id, lesson_id)` не позволяет добавить две отметки для одного студента на одном занятии.
