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
echo "========== 2/3 PMD hizalı metrik (parser parity JSON) =========="
"${ROOT}/scripts/run-pmd-mock.sh"

echo ""
echo "========== 3/3 Klasik PMD (ruleset → pmd.xml) =========="
set +e
"${ROOT}/scripts/run-classic-pmd-mock.sh"
CLASSIC_EXIT=$?
set -e
echo "(Klasik PMD exit ${CLASSIC_EXIT}; 4 = violation beklenen mock için normal)"

echo ""
echo "Okunabilir raporlar:"
echo "  Parser:      ${REPORTS}/ (run-standalone-mock)"
echo "  PMD metrik:  ${ROOT}/analysis-output/pmd-mock-modules-*.md"
echo "  Klasik PMD:  ${ROOT}/analysis-output/classic-pmd-mock-modules-*.md"
