# Запуск с диска (без исходников)

Этот дистрибутив предназначен для запуска приложения без полного проекта.

## Состав

- `server/service-center-server.jar` — backend (Spring Boot)
- `client/` — desktop-клиент со всеми зависимостями
- `docker-compose.db.yml` + `docker-init.sql` — PostgreSQL в Docker
- `start-server.sh` — запуск backend
- `start-client.sh` — запуск клиента
- `start-db.sh` / `stop-db.sh` — запуск и остановка БД

## Требования

- Docker Desktop (или Docker Engine + Compose)
- Java 21 (JDK/JRE с поддержкой запуска JavaFX через модули)

## Порядок запуска

1. Поднять БД:

```bash
./start-db.sh
```

2. Запустить сервер:

```bash
./start-server.sh
```

3. Запустить клиент (в новом терминале):

```bash
./start-client.sh
```

Скрипт запускает JavaFX с параметрами `--module-path` и `--add-modules`, вручную ничего дописывать не нужно.

По умолчанию клиент подключается к `http://localhost:8080`.

Если backend запущен на другом хосте:

```bash
./start-client.sh http://HOST:8080
```

## Остановка

Остановить БД:

```bash
./stop-db.sh
```

Полный сброс БД:

```bash
docker compose -f docker-compose.db.yml down -v
docker compose -f docker-compose.db.yml up -d
```
