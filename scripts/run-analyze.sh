#!/usr/bin/env bash
# Gerçek (veya herhangi bir) Java kaynak ağacını tarar.
#
# Kullanım:
#   ./scripts/run-analyze.sh /path/to/proje
#   ./scripts/run-analyze.sh /path/to/proje/src/main/java
#
# Ortam değişkenleri (isteğe bağlı):
#   LANGUAGE_LEVEL=JAVA_11   (varsayılan JAVA_17; eski DWH için JAVA_6)
#   OUTPUT_DIR=./my-reports  (varsayılan: ./analysis-output)
#   FIXED_REPORT=1           (1 = parser-raporu.md üzerine yaz)
#   REPORT_TAG=mobil-backend (dosya adı: parser-mobil-backend-YYYYMMDD-HHmmss.md)
set -euo pipefail

# shellcheck source=report-name.sh
source "$(cd "$(dirname "$0")" && pwd)/report-name.sh"
# shellcheck source=resolve-jar.sh
source "$(cd "$(dirname "$0")" && pwd)/resolve-jar.sh"

# Argüman yoksa bulunduğunuz klasör taranır (proje kökünde çalıştırın).
SOURCE_DIR="${1:-.}"
SOURCE="$(cd "${SOURCE_DIR}" && pwd)"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT_DIR="${OUTPUT_DIR:-${ROOT}/analysis-output}"
LANG="${LANGUAGE_LEVEL:-JAVA_17}"
WORKERS="${WORKERS:-0}"
DETAIL="${REPORT_DETAIL:-full}"
EXTRA=(--language-level="${LANG}")
if [[ "${WORKERS}" != "0" ]]; then EXTRA+=(--workers="${WORKERS}"); fi
if [[ "${DETAIL}" == "summary" ]]; then EXTRA+=(--detail=summary); fi
if [[ -n "${INCLUDE_GLOBS:-}" ]]; then EXTRA+=(--include="${INCLUDE_GLOBS}"); fi
if [[ -n "${EXCLUDE_GLOBS:-}" ]]; then EXTRA+=(--exclude="${EXCLUDE_GLOBS}"); fi

mkdir -p "${OUT_DIR}"

assign_report_paths "${OUT_DIR}" "${SOURCE}"
echo "[STANDALONE] Rapor dosyasi: $(basename "${MD}")"

if ! JAR="$(ensure_analyzer_jar "${ROOT}")"; then
  echo "[HATA] java-code-analyzer.jar bulunamadi (dist/ veya target/)"
  echo "       git pull veya dist/java-code-analyzer.jar kopyalayin"
  echo "       ANALYZER_JAR=/tam/yol/java-code-analyzer.jar ./scripts/run-analyze.sh"
  exit 1
fi

java -jar "${JAR}" \
  --path="${SOURCE}" \
  --output="${JSON}" \
  --markdown="${MD}" \
  --top=20 \
  "${EXTRA[@]}"

echo ""
echo "Bitti."
echo "  Markdown: ${MD}"
echo "  JSON:     ${JSON}"
