#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DIST_DIR="${SCRIPT_DIR}"

if ! (echo > /dev/tcp/localhost/5432) >/dev/null 2>&1; then
    echo "PostgreSQL недоступен на localhost:5432."
    echo "Сначала запустите БД:"
    echo "  docker compose -f \"${DIST_DIR}/docker-compose.db.yml\" up -d"
    exit 1
fi

java -jar "${DIST_DIR}/server/service-center-server.jar"
