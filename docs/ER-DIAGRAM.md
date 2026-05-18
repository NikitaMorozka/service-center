# ER-диаграмма — Сервисный центр

```mermaid
erDiagram
    ROLES ||--o{ USERS : has
    USERS ||--o{ REPAIR_REQUESTS : assigned
    CLIENTS ||--o{ DEVICES : owns
    CLIENTS ||--o{ REPAIR_REQUESTS : submits
    DEVICES ||--o{ REPAIR_REQUESTS : repaired
    REPAIR_REQUESTS ||--o{ SERVICE_HISTORY : logs
    USERS ||--o{ SERVICE_HISTORY : changed_by

    ROLES {
        bigint id PK
        varchar name UK
    }

    USERS {
        bigint id PK
        varchar username UK
        varchar password_hash
        varchar full_name
        varchar email
        boolean active
        bigint role_id FK
        timestamptz created_at
        timestamptz updated_at
    }

    CLIENTS {
        bigint id PK
        varchar full_name
        varchar phone
        varchar email
        varchar address
        text notes
        timestamptz created_at
    }

    DEVICES {
        bigint id PK
        bigint client_id FK
        varchar brand
        varchar model
        varchar serial_number
        varchar device_type
        text description
        timestamptz created_at
    }

    REPAIR_REQUESTS {
        bigint id PK
        varchar request_number UK
        bigint client_id FK
        bigint device_id FK
        bigint master_id FK
        varchar status
        text problem_description
        text diagnosis
        numeric estimated_cost
        numeric final_cost
        timestamptz created_at
        timestamptz updated_at
        timestamptz completed_at
    }

    SERVICE_HISTORY {
        bigint id PK
        bigint repair_request_id FK
        bigint changed_by_id FK
        varchar old_status
        varchar new_status
        text comment
        timestamptz created_at
    }
```

## Статусы ремонта

| Статус | Описание |
|--------|----------|
| NEW | Новая заявка |
| IN_PROGRESS | В работе |
| WAITING_PARTS | Ожидание запчастей |
| COMPLETED | Завершена |
| CANCELED | Отменена |

## Роли

| Роль | Права |
|------|-------|
| ADMIN | Управление пользователями |
| MANAGER | Клиенты, техника, заявки |
| MASTER | Заявки, смена статуса |
