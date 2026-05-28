# Модуль ИС сервисного центра по ремонту техники

Клиент-серверное desktop-приложение: **JavaFX** (клиент) + **Spring Boot** (сервер) + **PostgreSQL 15**.

## Структура проекта

```
service-center/
├── build.gradle              # Корневой Gradle
├── settings.gradle
├── docker-compose.yml        # PostgreSQL + backend (Docker)
├── database/
│   ├── docker-init.sql       # legacy-скрипт (оставлен для справки)
│   └── init.sql              # Полная ручная установка БД (legacy)
├── docs/
│   ├── ER-DIAGRAM.md         # ER-диаграмма (Mermaid)
│   └── API.md                # REST endpoints
├── server/                   # Spring Boot backend
│   ├── build.gradle
│   └── src/main/
│       ├── java/ru/servicecenter/server/
│       ├── config/           # Security, DataInitializer
│       ├── controller/       # REST API
│       ├── domain/entity/    # JPA сущности
│       ├── dto/              # DTO
│       ├── mapper/           # MapStruct
│       ├── repository/       # JPA Repository
│       ├── security/         # JWT
│       ├── service/          # Бизнес-логика и bootstrap-сервисы
│       └── resources/db/changelog/ # Liquibase миграции
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
| Backend | Java 21, Spring Boot 3.3, Spring Security, JWT, JPA/Hibernate, Liquibase, Lombok, MapStruct |
| Frontend | Java 21, JavaFX 21, FXML, REST (HttpClient + Jackson) |
| БД | PostgreSQL 15 |
| Сборка | Gradle 8.10 |

## Тесты (backend)

```bash
./gradlew :server:test
```

Профиль **`test`**, БД **H2** (режим PostgreSQL).

| Категория | Классы |
|-----------|--------|
| Сервисы (unit) | `RepairServiceTest`, `ClientServiceTest`, `DeviceServiceTest`, `UserServiceTest`, `BrandCatalogServiceTest`, `DeviceTypeCatalogServiceTest` |
| Безопасность | `JwtServiceTest` |
| Репозитории (JPA) | `RepairRequestRepositoryTest`, `ClientRepositoryTest`, `BrandRepositoryTest`, `DeviceRepositoryTest`, `DeviceTypeRepositoryTest`, `UserRepositoryTest`, `ServiceHistoryRepositoryTest` |
| API (integration) | `RepairApiIntegrationTest`, `AuthApiIntegrationTest`, `CatalogApiIntegrationTest` |

Покрываются: номер заявки, архив, поиск, история статусов, справочники, JWT, права ADMIN/MANAGER/MASTER, логин и валидация.

## Быстрый старт (рекомендуемый для курсовой)

Целевая схема запуска:
- **Docker**: PostgreSQL + Spring Boot backend
- **Локально на ПК**: JavaFX desktop-клиент

### 1. Запуск backend + PostgreSQL через Docker

```bash
docker compose up -d --build
```

Проверка состояния контейнеров:

```bash
docker compose ps
docker compose logs server
docker compose logs postgres
```

Backend после старта доступен на: http://localhost:8080

Чтобы остановить:

```bash
docker compose down
```

Чтобы полностью пересоздать БД с нуля:

```bash
docker compose down -v
docker compose up -d --build
```

Схема и базовые справочники поднимаются Liquibase-миграциями при старте backend.
Дефолтные пользователи и демо-данные создаются Spring Boot при старте backend.

### 2. Запуск desktop-клиента локально

```bash
./gradlew :client:run
```

По умолчанию клиент подключается к `http://localhost:8080`.

Если backend запущен на другом хосте/порту, укажите URL через JVM-параметр:

```bash
./gradlew :client:run -Dsc.api.base-url=http://HOST:8080
```

### 3. Альтернатива без Docker (при необходимости)

- Поднять PostgreSQL вручную (без применения SQL-схемы вручную)
- Запустить backend локально: `./gradlew :server:bootRun`
- Запустить клиент: `./gradlew :client:run`

## Дистрибутив для записи на диск/флешку

Чтобы не переносить весь проект, соберите переносимый комплект:

```bash
./gradlew prepareDistribution
```

Готовая папка: `build/distribution`

В ней уже есть:
- `server/service-center-server.jar`
- `client/` (скрипт запуска + зависимости клиента)
- `docker-compose.db.yml` и `docker-init.sql` для PostgreSQL
- `README-DISK.md`, `start-server.sh`, `start-client.sh`

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

`server/src/main/resources/application.yml` — подключение к PostgreSQL, Liquibase changelog, JWT secret, порт сервера.

Клиент: `client/.../AppConfig.java` — URL API (`http://localhost:8080`).

## Best practices: Liquibase + SOLID

- Все изменения БД вносите только через `server/src/main/resources/db/changelog/`.
- Каждому изменению — отдельный changeset с уникальным `id`, `author` и `rollback`.
- Для существующих сред используйте preconditions (`onFail: MARK_RAN`) в baseline-миграциях.
- Для seed-данных используйте идемпотентные SQL (`ON CONFLICT DO NOTHING`), а не дублирующую Java-инициализацию.
- Бизнес-инициализацию (дефолтные пользователи, демо-данные) держите в сервисах, а `CommandLineRunner` оставляйте тонким оркестратором (SRP, DIP).
