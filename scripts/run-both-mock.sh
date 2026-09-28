#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REPORTS="${ROOT}/mock-modules/reports"

# reports/ altında yalnızca iki Markdown kalacak şekilde eski çıktıları sil
mkdir -p "${REPORTS}"
rm -f "${REPORTS}"/*.md "${REPORTS}"/*.json "${REPORTS}"/*.xml "${REPORTS}"/*.log 2>/dev/null || true

echo "========== 1/2 Parser (standalone) =========="
"${ROOT}/scripts/run-standalone-mock.sh"

echo ""
echo "========== 2/2 PMD =========="
"${ROOT}/scripts/run-pmd-mock.sh"

echo ""
echo "Okunabilir raporlar (her çalıştırmada üzerine yazılır):"
echo "  ${REPORTS}/parser-raporu.md"
echo "  ${REPORTS}/pmd-raporu.md"
