#!/usr/bin/env bash
# env şemasına tablolar + mock-modules parser/PMD JSON → PostgreSQL
#
# Bağlantı: PGHOST PGPORT PGUSER PGPASSWORD PGDATABASE
#   veya CATALOG_JDBC_URL=jdbc:postgresql://host:5432/db
#
# Kullanım:
#   ./scripts/run-catalog-load-env.sh [kaynak-dizin]
#   ./scripts/run-catalog-load-env.sh mock-modules
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
if [[ -f "${ROOT}/.env.catalog" ]]; then
  set -a
  # shellcheck source=/dev/null
  source "${ROOT}/.env.catalog"
  set +a
fi
SOURCE_DIR="${1:-mock-modules}"
if [[ -d "${ROOT}/${SOURCE_DIR}" ]]; then
  SOURCE="$(cd "${ROOT}/${SOURCE_DIR}" && pwd)"
elif [[ -d "${SOURCE_DIR}" ]]; then
  SOURCE="$(cd "${SOURCE_DIR}" && pwd)"
else
  echo "[HATA] Kaynak bulunamadı: ${SOURCE_DIR}"
  exit 1
fi
# shellcheck source=resolve-jar.sh
source "$(cd "$(dirname "$0")" && pwd)/resolve-jar.sh"

PGDATABASE="${PGDATABASE:-postgres}"
export PGDATABASE
PSQL=(psql -v ON_ERROR_STOP=1)
if [[ -n "${PGHOST:-}" ]]; then PSQL+=(-h "${PGHOST}"); fi
if [[ -n "${PGPORT:-}" ]]; then PSQL+=(-p "${PGPORT}"); fi
if [[ -n "${PGUSER:-}" ]]; then PSQL+=(-U "${PGUSER}"); fi

echo "[CATALOG] env şeması (tablolar)..."
"${PSQL[@]}" -f "${ROOT}/scripts/catalog-schema.sql"

if ! JAR="$(ensure_analyzer_jar "${ROOT}")"; then
  echo "[HATA] mvn package gerekli"
  exit 1
fi

SCAN_RUN="$(uuidgen | tr '[:upper:]' '[:lower:]')"
WORKDIR="${ROOT}/analysis-output/catalog-${SCAN_RUN}"
mkdir -p "${WORKDIR}"

PARSER_JSON="${WORKDIR}/parser.json"
PMD_JSON="${WORKDIR}/pmd.json"
PARSER_MD="${WORKDIR}/parser.md"
PMD_MD="${WORKDIR}/pmd.md"

echo "[CATALOG] Parser taraması → ${PARSER_JSON}"
set +e
java -jar "${JAR}" \
  --path="${SOURCE}" \
  --risk-profile=enterprise-java \
  --output="${PARSER_JSON}" \
  --markdown="${PARSER_MD}" \
  --top=15 \
  --state="${WORKDIR}/parser-state.json"
set -e
if [[ ! -f "${PARSER_JSON}" ]]; then
  echo "[HATA] Parser JSON oluşmadı"
  exit 1
fi

echo "[CATALOG] PMD-native taraması → ${PMD_JSON}"
set +e
java -cp "${JAR}" com.standalone.analyzer.PmdScanMain \
  --path="${SOURCE}" \
  --risk-profile=enterprise-java-pmd \
  --output="${PMD_JSON}" \
  --markdown="${PMD_MD}" \
  --top=15
set -e
if [[ ! -f "${PMD_JSON}" ]]; then
  echo "[HATA] PMD JSON oluşmadı"
  exit 1
fi

echo "[CATALOG] PostgreSQL yükleme (scan_run_id=${SCAN_RUN})..."
java -cp "${JAR}" com.standalone.analyzer.CatalogEnvLoaderMain \
  --catalog-scan-run="${SCAN_RUN}" \
  --parser-json="${PARSER_JSON}" \
  --pmd-json="${PMD_JSON}"

echo ""
echo "Tamam. Son koşu: scan_run_id=${SCAN_RUN}"
echo "Örnek sorgular: scripts/catalog-queries.sql"
echo "JSON dosyalar: ${WORKDIR}"
