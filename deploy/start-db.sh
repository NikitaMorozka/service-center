#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

docker compose -f "${SCRIPT_DIR}/docker-compose.db.yml" up -d
docker compose -f "${SCRIPT_DIR}/docker-compose.db.yml" ps
