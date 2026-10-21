#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export REPORT_TAG="${REPORT_TAG:-mock-modules}"
export OUTPUT_DIR="${OUTPUT_DIR:-${ROOT}/analysis-output}"
"${ROOT}/scripts/run-classic-pmd.sh" "${ROOT}/mock-modules"
