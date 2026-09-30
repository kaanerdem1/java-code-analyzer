#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="${ROOT}/scripts/java-code-analyzer-scan-percentiles.md"
# shellcheck source=resolve-jar.sh
source "$(cd "$(dirname "$0")" && pwd)/resolve-jar.sh"

cd "$ROOT"
if ! JAR="$(ensure_analyzer_jar "${ROOT}")"; then
  echo "[HATA] java-code-analyzer.jar bulunamadi; once: mvn -q package"
  exit 1
fi

echo "[CALIBRATE] Taraniyor: ${ROOT} (test haric, enterprise-java profili)"
java -cp "${JAR}" com.standalone.analyzer.ScanMetricsCalibration "${ROOT}" > "${OUT}"
echo "[CALIBRATE] Yazildi: ${OUT}"
echo "[CALIBRATE] YAML onerileri dosyanin sonunda; config/risk-parameters-proposal.yaml ile karsilastir."
