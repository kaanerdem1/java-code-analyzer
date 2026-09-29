#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=report-name.sh
source "$(cd "$(dirname "$0")" && pwd)/report-name.sh"
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

JAR="${ROOT}/target/java-code-analyzer.jar"
if [[ ! -f "${JAR}" ]]; then
  echo "[STANDALONE] JAR yok, bir kez derleniyor (mvn package)..."
  mvn -q package
fi

echo "[STANDALONE] mock-modules taranıyor..."
java -jar target/java-code-analyzer.jar \
  --path="${ROOT}/mock-modules" \
  --output="${JSON}" \
  --markdown="${MD}" \
  --top=15 \
  2>&1 | tee "${TARGET}/analysis/standalone-console.log"

echo "[STANDALONE] Okunabilir rapor: ${MD}"
echo "[STANDALONE] Ham JSON: ${JSON}"
