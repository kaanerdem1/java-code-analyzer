#!/usr/bin/env bash
# JDBC bağlantı testi (psql gerekmez). .env.catalog okur.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
if [[ -f "${ROOT}/.env.catalog" ]]; then
  set -a
  # shellcheck source=/dev/null
  source "${ROOT}/.env.catalog"
  set +a
fi
# shellcheck source=resolve-jar.sh
source "$(cd "$(dirname "$0")" && pwd)/resolve-jar.sh"
JAR="$(ensure_analyzer_jar "${ROOT}")"
exec java -cp "${JAR}" com.standalone.analyzer.CatalogJdbcPingMain "$@"
