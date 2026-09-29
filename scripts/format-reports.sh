#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=resolve-jar.sh
source "$(cd "$(dirname "$0")" && pwd)/resolve-jar.sh"
REPORTS="${ROOT}/mock-modules/reports"
JAR="$(ensure_analyzer_jar "${ROOT}")"
JSON="${ROOT}/mock-modules/target/analysis/standalone.json"
PMD_XML="${ROOT}/mock-modules/target/pmd.xml"

CP=(java -cp "${JAR}" com.standalone.analyzer.ReadableReportMain)

if [[ -f "${JSON}" ]]; then
  "${CP[@]}" standalone "${JSON}" "${REPORTS}/parser-raporu.md"
else
  echo "[READABLE] Atlandı: ${JSON} yok (önce ./scripts/run-standalone-mock.sh)"
fi

if [[ -f "${PMD_XML}" ]]; then
  "${CP[@]}" pmd "${PMD_XML}" "${REPORTS}/pmd-raporu.md"
else
  echo "[READABLE] Atlandı: ${PMD_XML} yok (önce ./scripts/run-pmd-mock.sh)"
fi

echo "[READABLE] Önizleme: ${REPORTS}/parser-raporu.md ve pmd-raporu.md (Cmd+Shift+V)"
