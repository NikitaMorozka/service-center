#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DIST_DIR="${SCRIPT_DIR}"
API_URL="${1:-http://localhost:8080}"

java \
  -Dsc.api.base-url="${API_URL}" \
  --module-path "${DIST_DIR}/client/lib" \
  --add-modules javafx.controls,javafx.fxml \
  -cp "${DIST_DIR}/client/lib/*" \
  ru.servicecenter.client.ServiceCenterApp
