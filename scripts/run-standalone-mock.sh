#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
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

JSON="${TARGET}/analysis/standalone.json"
MD="${REPORTS}/parser-raporu.md"

echo "[STANDALONE] JAR derleniyor..."
mvn -q package

echo "[STANDALONE] mock-modules taranıyor..."
java -jar target/java-code-analyzer.jar \
  --path="${ROOT}/mock-modules" \
  --output="${JSON}" \
  --markdown="${MD}" \
  --top=15 \
  2>&1 | tee "${TARGET}/analysis/standalone-console.log"

echo "[STANDALONE] Okunabilir rapor (üzerine yazıldı): ${MD}"
echo "[STANDALONE] Ham JSON: ${JSON}"
