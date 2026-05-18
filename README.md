# Модуль ИС сервисного центра по ремонту техники

Клиент-серверное desktop-приложение: **JavaFX** (клиент) + **Spring Boot** (сервер) + **PostgreSQL 15**.

## Структура проекта

```
service-center/
├── build.gradle              # Корневой Gradle
├── settings.gradle
├── docker-compose.yml        # PostgreSQL 15
├── database/
│   ├── docker-init.sql       # Схема для Docker (автозапуск)
│   └── init.sql              # Полная ручная установка БД
├── docs/
│   ├── ER-DIAGRAM.md         # ER-диаграмма (Mermaid)
│   └── API.md                # REST endpoints
├── server/                   # Spring Boot backend
│   ├── build.gradle
│   └── src/main/java/ru/servicecenter/server/
│       ├── config/           # Security, DataInitializer
│       ├── controller/       # REST API
│       ├── domain/entity/    # JPA сущности
│       ├── dto/              # DTO
│       ├── mapper/           # MapStruct
│       ├── repository/       # JPA Repository
│       ├── security/         # JWT
│       └── service/          # Бизнес-логика
└── client/                   # JavaFX desktop
    ├── build.gradle
    └── src/main/
        ├── java/.../controller/
        └── resources/
            ├── fxml/         # Scene Builder совместимые экраны
            └── css/app.css
```

## Технологии

| Слой | Стек |
|------|------|
| Backend | Java 21, Spring Boot 3.3, Spring Security, JWT, JPA/Hibernate, Lombok, MapStruct |
| Frontend | Java 21, JavaFX 21, FXML, REST (HttpClient + Jackson) |
| БД | PostgreSQL 15 |
| Сборка | Gradle 8.10 |

## Быстрый старт

### 1. PostgreSQL (Docker)

```bash
docker compose up -d
```

Проверка, что БД готова:

```bash
docker compose ps
docker compose logs postgres
```

Параметры контейнера (должны совпадать с `application.yml`):

| Параметр | Значение |
|----------|----------|
| База | `service_center` |
| Пользователь | `sc_user` |
| Пароль | `sc_password` |
| Порт | `5432` |

При **первом** запуске выполняется `database/docker-init.sql` (таблицы, роли, справочники типов и брендов).  
Пользователи и демо-данные создаёт Spring Boot при старте сервера.

Пересоздать БД с нуля (сброс демо-данных):

```bash
docker compose down -v
docker compose up -d
```

Ручная установка без Docker: `psql -U postgres -f database/init.sql`

### 2. Backend

```bash
./gradlew :server:bootRun
```

Сервер: http://localhost:8080

### 3. Desktop-клиент

```bash
./gradlew :client:run
```

## Учётные записи по умолчанию

| Логин | Пароль | Роль |
|-------|--------|------|
| admin | admin123 | ADMIN |
| manager | manager123 | MANAGER |
| manager2 | manager123 | MANAGER |
| master | master123 | MASTER |
| master2 | master123 | MASTER |
| master3 | master123 | MASTER |

При **первом запуске** сервера автоматически создаются демо-данные: **30 клиентов**, **10 заявок**, **2 менеджера** (`manager`, `manager2`), **3 мастера** (`master`, `master2`, `master3`), устройства и справочники типов/брендов.

Чтобы загрузить демо-данные заново, очистите таблицы в БД (или пересоздайте volume PostgreSQL) и перезапустите сервер.

## Экраны JavaFX

- Авторизация (`login.fxml`)
- Главное меню с боковой навигацией (`main.fxml`)
- Заявки на ремонт — поиск, фильтр, статусы, мастер, история
- Клиенты — CRUD
- Техника — CRUD, фильтр по клиенту
- Пользователи — CRUD (только ADMIN)

## Статусы ремонта

`NEW` → `IN_PROGRESS` → `WAITING_PARTS` → `COMPLETED` / `CANCELED`

## Конфигурация

`server/src/main/resources/application.yml` — подключение к PostgreSQL, JWT secret, порт сервера.

Клиент: `client/.../AppConfig.java` — URL API (`http://localhost:8080`).
