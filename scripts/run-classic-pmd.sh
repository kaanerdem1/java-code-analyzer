#!/usr/bin/env bash
# Klasik PMD ruleset taraması → standart pmd.xml (violation satırları).
# Parser / run-pmd-analyze (metrik+risk) ile karşılaştırma için.
#
# Kullanım:
#   ./scripts/run-classic-pmd.sh /path/to/proje
#
# Ortam (run-analyze.sh ile aynı keşif):
#   LANGUAGE_LEVEL=JAVA_17
#   OUTPUT_DIR=./analysis-output
#   INCLUDE_GLOBS / EXCLUDE_GLOBS (varsayılan **/src/test/**)
#   WORKERS=8
#   PMD_RULESET=config/pmd-ruleset-complexity.xml
set -euo pipefail

# shellcheck source=report-name.sh
source "$(cd "$(dirname "$0")" && pwd)/report-name.sh"
# shellcheck source=resolve-jar.sh
source "$(cd "$(dirname "$0")" && pwd)/resolve-jar.sh"

SOURCE_DIR="${1:-.}"
SOURCE="$(cd "${SOURCE_DIR}" && pwd)"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT_DIR="${OUTPUT_DIR:-${ROOT}/analysis-output}"
LANG="${LANGUAGE_LEVEL:-JAVA_17}"
WORKERS="${WORKERS:-0}"
RULESET="${PMD_RULESET:-${ROOT}/config/pmd-ruleset-complexity.xml}"

EXTRA=(--language-level="${LANG}" --ruleset="${RULESET}")
if [[ "${WORKERS}" != "0" ]]; then EXTRA+=(--workers="${WORKERS}"); fi
if [[ -n "${INCLUDE_GLOBS:-}" ]]; then EXTRA+=(--include="${INCLUDE_GLOBS}"); fi
EXCLUDE_GLOBS="${EXCLUDE_GLOBS:-**/src/test/**}"
if [[ -n "${EXCLUDE_GLOBS}" ]]; then EXTRA+=(--exclude="${EXCLUDE_GLOBS}"); fi

mkdir -p "${OUT_DIR}"
assign_classic_pmd_report_paths "${OUT_DIR}" "${SOURCE}" "${OUT_DIR}"
if [[ -z "${CLASSIC_PMD_XML:-}" || -z "${CLASSIC_PMD_MD:-}" ]]; then
  echo "[HATA] Klasik PMD rapor yolu uretilemedi" >&2
  exit 1
fi

if ! JAR="$(ensure_analyzer_jar "${ROOT}")"; then
  echo "[HATA] java-code-analyzer.jar bulunamadi; mvn package"
  exit 1
fi

echo "[CLASSIC-PMD] Taranıyor: ${SOURCE}"
echo "[CLASSIC-PMD] Ruleset: ${RULESET}"
echo "[CLASSIC-PMD] XML: $(basename "${CLASSIC_PMD_XML}")"

set +e
java -cp "${JAR}" com.standalone.analyzer.ClassicPmdMain \
  --path="${SOURCE}" \
  --output="${CLASSIC_PMD_XML}" \
  "${EXTRA[@]}"
EXIT=$?
set -e

if [[ ! -f "${CLASSIC_PMD_XML}" ]]; then
  echo "[HATA] pmd.xml olusturulamadi: ${CLASSIC_PMD_XML}" >&2
  exit 1
fi

java -cp "${JAR}" com.standalone.analyzer.ReadableReportMain \
  pmd "${CLASSIC_PMD_XML}" "${CLASSIC_PMD_MD}"

echo ""
echo "Done (exit ${EXIT}; 4 = violation var, 0 = yok)."
echo "  Markdown: ${CLASSIC_PMD_MD}"
echo "  XML:      ${CLASSIC_PMD_XML}"
exit "${EXIT}"
