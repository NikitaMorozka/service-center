-- Выполняется автоматически при первом запуске контейнера PostgreSQL (docker compose).
-- База service_center и пользователь sc_user создаются через переменные POSTGRES_* в docker-compose.yml.

CREATE TABLE IF NOT EXISTS roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(200) NOT NULL,
    email           VARCHAR(200),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    role_id         BIGINT NOT NULL REFERENCES roles(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS device_types (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS brands (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS clients (
    id          BIGSERIAL PRIMARY KEY,
    full_name   VARCHAR(200) NOT NULL,
    phone       VARCHAR(50) NOT NULL,
    email       VARCHAR(200),
    address     VARCHAR(500),
    notes       TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS devices (
    id              BIGSERIAL PRIMARY KEY,
    client_id       BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    brand           VARCHAR(100) NOT NULL,
    model           VARCHAR(100) NOT NULL,
    serial_number   VARCHAR(100),
    device_type     VARCHAR(100) NOT NULL,
    description     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS repair_requests (
    id              BIGSERIAL PRIMARY KEY,
    request_number  VARCHAR(50) NOT NULL UNIQUE,
    client_id       BIGINT NOT NULL REFERENCES clients(id),
    device_id       BIGINT NOT NULL REFERENCES devices(id),
    master_id       BIGINT REFERENCES users(id),
    status          VARCHAR(30) NOT NULL DEFAULT 'NEW',
    problem_description TEXT NOT NULL,
    diagnosis         TEXT,
    estimated_cost    NUMERIC(12, 2),
    final_cost        NUMERIC(12, 2),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at      TIMESTAMPTZ,
    CONSTRAINT chk_repair_status CHECK (
        status IN ('NEW', 'IN_PROGRESS', 'WAITING_PARTS', 'COMPLETED', 'CANCELED')
    )
);

CREATE TABLE IF NOT EXISTS service_history (
    id                  BIGSERIAL PRIMARY KEY,
    repair_request_id   BIGINT NOT NULL REFERENCES repair_requests(id) ON DELETE CASCADE,
    changed_by_id       BIGINT REFERENCES users(id),
    old_status          VARCHAR(30),
    new_status          VARCHAR(30) NOT NULL,
    comment             TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_role ON users(role_id);
CREATE INDEX IF NOT EXISTS idx_devices_client ON devices(client_id);
CREATE INDEX IF NOT EXISTS idx_repair_client ON repair_requests(client_id);
CREATE INDEX IF NOT EXISTS idx_repair_device ON repair_requests(device_id);
CREATE INDEX IF NOT EXISTS idx_repair_master ON repair_requests(master_id);
CREATE INDEX IF NOT EXISTS idx_repair_status ON repair_requests(status);
CREATE INDEX IF NOT EXISTS idx_history_request ON service_history(repair_request_id);

INSERT INTO roles (name) VALUES ('ADMIN'), ('MANAGER'), ('MASTER')
ON CONFLICT (name) DO NOTHING;

INSERT INTO device_types (name) VALUES
    ('Телефон'),
    ('Телевизор'),
    ('Стиральная машина'),
    ('Ноутбук'),
    ('Планшет'),
    ('Холодильник')
ON CONFLICT (name) DO NOTHING;

INSERT INTO brands (name) VALUES
    ('Samsung'),
    ('LG'),
    ('Realme'),
    ('Apple'),
    ('Xiaomi'),
    ('Sony'),
    ('Bosch'),
    ('Philips')
ON CONFLICT (name) DO NOTHING;

-- Пользователи и демо-заявки создаёт Spring Boot (DataInitializer, DemoDataSeeder).
