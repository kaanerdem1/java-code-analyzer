#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=resolve-jar.sh
source "$(cd "$(dirname "$0")" && pwd)/resolve-jar.sh"
MOCK="${ROOT}/mock-modules"
REPORTS="${MOCK}/reports"
TARGET="${MOCK}/target"
mkdir -p "${REPORTS}" "${TARGET}/analysis"

rm -f "${REPORTS}/pmd-report.md" \
      "${REPORTS}/pmd-report.xml" \
      "${REPORTS}/pmd-console.log" \
      "${REPORTS}/compare-standalone-pmd.md"

MD="${REPORTS}/pmd-raporu.md"
PMD_XML="${TARGET}/pmd.xml"

echo "[PMD] mock-modules karmaşıklık taraması..."
cd "${MOCK}"
mvn -q generate-sources pmd:pmd 2>&1 | tee "${TARGET}/analysis/pmd-console.log"

if [[ ! -f "${PMD_XML}" ]]; then
  echo "[PMD] HATA: ${PMD_XML} bulunamadı"
  exit 1
fi

JAR="$(ensure_analyzer_jar "${ROOT}")"

java -cp "${JAR}" com.standalone.analyzer.ReadableReportMain \
  pmd "${PMD_XML}" "${MD}"

VIOLATIONS="$(grep -c '<violation ' "${PMD_XML}" || true)"
FILES="$(grep -c '<file name=' "${PMD_XML}" || true)"
echo "[PMD] Okunabilir rapor (üzerine yazıldı): ${MD}"
echo "[PMD] Özet: ${FILES} dosya, ${VIOLATIONS} PMD satırı | Ham XML: ${PMD_XML}"
