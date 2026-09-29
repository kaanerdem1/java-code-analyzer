#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=report-name.sh
source "$(cd "$(dirname "$0")" && pwd)/report-name.sh"
# shellcheck source=resolve-jar.sh
source "$(cd "$(dirname "$0")" && pwd)/resolve-jar.sh"
export REPORT_TAG="${REPORT_TAG:-mock-modules}"
REPORTS="${ROOT}/mock-modules/reports"
TARGET="${ROOT}/mock-modules/target"
cd "$ROOT"

mkdir -p "${REPORTS}" "${TARGET}/analysis"

# Eski rapor adları / karşılaştırma dosyaları
rm -f "${REPORTS}/standalone-report.md" \
      "${REPORTS}/standalone-report.json" \
      "${REPORTS}/standalone-console.log" \
      "${REPORTS}/compare-standalone-pmd.md" \
      "${ROOT}/mock-modules/report.json"

assign_report_paths "${REPORTS}" "${ROOT}/mock-modules" "${TARGET}/analysis"
echo "[STANDALONE] Rapor dosyasi: $(basename "${MD}")"

if ! JAR="$(ensure_analyzer_jar "${ROOT}")"; then
  echo "[HATA] java-code-analyzer.jar bulunamadi"
  exit 1
fi

echo "[STANDALONE] mock-modules taranıyor..."
java -jar "${JAR}" \
  --path="${ROOT}/mock-modules" \
  --output="${JSON}" \
  --markdown="${MD}" \
  --top=15 \
  2>&1 | tee "${TARGET}/analysis/standalone-console.log"

echo "[STANDALONE] Okunabilir rapor: ${MD}"
echo "[STANDALONE] Ham JSON: ${JSON}"
