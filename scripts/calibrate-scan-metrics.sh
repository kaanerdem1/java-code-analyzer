#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
MOCK="${ROOT}/mock-modules"
OUT="${ROOT}/scripts/mock-modules-scan-percentiles.md"
# shellcheck source=resolve-jar.sh
source "$(cd "$(dirname "$0")" && pwd)/resolve-jar.sh"

cd "$ROOT"
if ! JAR="$(ensure_analyzer_jar "${ROOT}")"; then
  echo "[HATA] java-code-analyzer.jar bulunamadi; once: mvn -q package"
  exit 1
fi

echo "[CALIBRATE] Taraniyor: ${MOCK}"
java -cp "${JAR}" com.standalone.analyzer.ScanMetricsCalibration "${MOCK}" > "${OUT}"
echo "[CALIBRATE] Yazildi: ${OUT}"
