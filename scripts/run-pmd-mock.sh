#!/usr/bin/env bash
# mock-modules — PMD JavaMetrics taraması (parser ile aynı glob/env sözleşmesi).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export REPORT_TAG="${REPORT_TAG:-mock-modules}"
export OUTPUT_DIR="${OUTPUT_DIR:-${ROOT}/analysis-output}"
"${ROOT}/scripts/run-pmd-analyze.sh" "${ROOT}/mock-modules"
