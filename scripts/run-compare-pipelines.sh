#!/usr/bin/env bash
# Parser (run-analyze) ile PMD-native risk/metrik farkını JSON yazar.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SOURCE_DIR="${1:-${ROOT}/mock-modules}"
SOURCE="$(cd "${SOURCE_DIR}" && pwd)"
OUT="${OUTPUT_DIR:-${ROOT}/analysis-output}/pipeline-compare-$(basename "${SOURCE}")-$(date +%Y%m%d-%H%M%S).json"
# shellcheck source=resolve-jar.sh
source "$(cd "$(dirname "$0")" && pwd)/resolve-jar.sh"
JAR="$(ensure_analyzer_jar "${ROOT}")"
java -cp "${JAR}" com.standalone.analyzer.ScanPipelineCompareMain \
  --path="${SOURCE}" \
  --output="${OUT}"
echo "Compare JSON: ${OUT}"
