#!/usr/bin/env bash
# Varsayılan: yalnizca PMD (ruleset + PMD AST metrik). Parser: run-analyze.sh
# Hibrit (PMD + JavaParser): SCAN_PIPELINE=parser-hybrid
# Include/exclude / dil seviyesi run-analyze.sh ile aynı.
#
# Kullanım:
#   ./scripts/run-pmd-analyze.sh /path/to/proje
#
# Ortam (run-analyze.sh ile aynı):
#   LANGUAGE_LEVEL=JAVA_17
#   OUTPUT_DIR=./analysis-output
#   INCLUDE_GLOBS=**/src/main/java/**
#   EXCLUDE_GLOBS=**/src/test/**   (varsayılan)
#   WORKERS=8
#   REPORT_DETAIL=summary|full
#   FIXED_REPORT=1  → pmd-raporu.md + pmd-standalone.json
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
if [[ -f "${ROOT}/.env.catalog" ]]; then
  set -a
  # shellcheck source=/dev/null
  source "${ROOT}/.env.catalog"
  set +a
fi

# shellcheck source=report-name.sh
source "$(cd "$(dirname "$0")" && pwd)/report-name.sh"
# shellcheck source=resolve-jar.sh
source "$(cd "$(dirname "$0")" && pwd)/resolve-jar.sh"

SOURCE_DIR="${1:-.}"
SOURCE="$(cd "${SOURCE_DIR}" && pwd)"
OUT_DIR="${OUTPUT_DIR:-${ROOT}/analysis-output}"
LANG="${LANGUAGE_LEVEL:-JAVA_17}"
WORKERS="${WORKERS:-0}"
DETAIL="${REPORT_DETAIL:-full}"

EXTRA=(--language-level="${LANG}")
if [[ "${WORKERS}" != "0" ]]; then EXTRA+=(--workers="${WORKERS}"); fi
if [[ "${DETAIL}" == "summary" ]]; then EXTRA+=(--detail=summary); fi
if [[ -n "${INCLUDE_GLOBS:-}" ]]; then EXTRA+=(--include="${INCLUDE_GLOBS}"); fi
EXCLUDE_GLOBS="${EXCLUDE_GLOBS:-**/src/test/**}"
if [[ -n "${EXCLUDE_GLOBS}" ]]; then EXTRA+=(--exclude="${EXCLUDE_GLOBS}"); fi
if [[ -n "${SOURCE_ENCODING:-${ENCODING:-}}" ]]; then EXTRA+=(--encoding="${SOURCE_ENCODING:-${ENCODING}}"); fi

mkdir -p "${OUT_DIR}"
assign_pmd_report_paths "${OUT_DIR}" "${SOURCE}"
if [[ -z "${PMD_JSON:-}" || -z "${PMD_MD:-}" ]]; then
  echo "[HATA] PMD rapor yolu uretilemedi" >&2
  exit 1
fi

if ! JAR="$(ensure_analyzer_jar "${ROOT}")"; then
  echo "[HATA] java-code-analyzer.jar bulunamadi; mvn package"
  exit 1
fi

echo "[PMD-SCAN] Taranıyor: ${SOURCE}"
echo "[PMD-SCAN] Rapor: $(basename "${PMD_MD}")"
PIPELINE="${SCAN_PIPELINE:-pmd-native}"
echo "[PMD-SCAN] pipeline=${PIPELINE}; violations XML: ${PMD_JSON%.json}-violations.xml"

RISK_PROFILE="${STANDALONE_RISK_PROFILE:-enterprise-java-pmd}"
RISK_ARGS=(--risk-profile="${RISK_PROFILE}")

java -cp "${JAR}" com.standalone.analyzer.PmdScanMain \
  --path="${SOURCE}" \
  --output="${PMD_JSON}" \
  --markdown="${PMD_MD}" \
  --top=20 \
  --pipeline="${PIPELINE}" \
  "${RISK_ARGS[@]}" \
  "${EXTRA[@]}"

if [[ ! -f "${PMD_JSON}" || ! -f "${PMD_MD}" ]]; then
  echo "[HATA] PMD rapor dosyasi olusturulamadi" >&2
  exit 1
fi

echo ""
echo "Done."
echo "  Markdown: ${PMD_MD}"
echo "  JSON:     ${PMD_JSON}"
