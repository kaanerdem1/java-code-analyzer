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

JAR="${ANALYZER_JAR:-${ROOT}/target/java-code-analyzer.jar}"
if [[ ! -f "${JAR}" ]]; then
  if [[ -f "${ROOT}/pom.xml" ]] && grep -q 'java-code-analyzer' "${ROOT}/pom.xml" 2>/dev/null; then
    echo "[STANDALONE] JAR yok, analyzer derleniyor (mvn package)..."
    mvn -q -f "${ROOT}/pom.xml" package
    JAR="${ROOT}/target/java-code-analyzer.jar"
  else
    echo "[HATA] java-code-analyzer.jar bulunamadı: ${JAR}"
    echo "       Gerçek Spring projenize sadece script kopyaladıysanız JAR'ı da koyun:"
    echo "         target/java-code-analyzer.jar  (analyzer repoda mvn package ile üretin)"
    echo "       veya ANALYZER_JAR=/tam/yol/java-code-analyzer.jar ./scripts/run-analyze.sh"
    exit 1
  fi
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
