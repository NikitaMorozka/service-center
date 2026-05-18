# REST API — Сервисный центр

Базовый URL: `http://localhost:8080`

Авторизация: заголовок `Authorization: Bearer <JWT>`

## Аутентификация

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/api/auth/login` | Вход (логин/пароль) → JWT |

## Пользователи (ADMIN)

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/api/admin/users` | Список пользователей |
| GET | `/api/admin/users/{id}` | Пользователь по ID |
| POST | `/api/admin/users` | Создать пользователя |
| PUT | `/api/admin/users/{id}` | Обновить пользователя |
| DELETE | `/api/admin/users/{id}` | Удалить пользователя |

## Мастера

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/api/masters` | Список активных мастеров |

## Клиенты

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/api/clients?search=` | Список / поиск |
| GET | `/api/clients/{id}` | Клиент по ID |
| POST | `/api/clients` | Создать клиента |
| PUT | `/api/clients/{id}` | Обновить клиента |
| DELETE | `/api/clients/{id}` | Удалить клиента |

## Техника

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/api/devices?clientId=` | Список техники |
| GET | `/api/devices/{id}` | Устройство по ID |
| POST | `/api/devices` | Добавить технику |
| PUT | `/api/devices/{id}` | Обновить |
| DELETE | `/api/devices/{id}` | Удалить |

## Заявки на ремонт

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/api/repairs?status=&masterId=&search=` | Список с фильтрами |
| GET | `/api/repairs/{id}` | Заявка по ID |
| POST | `/api/repairs` | Создать заявку |
| PATCH | `/api/repairs/{id}/status` | Изменить статус |
| PATCH | `/api/repairs/{id}/assign-master` | Назначить мастера |
| GET | `/api/repairs/{id}/history` | История изменений |
