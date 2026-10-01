#!/usr/bin/env bash
# Gerçek (veya herhangi bir) Java kaynak ağacını tarar.
#
# Kullanım:
#   ./scripts/run-analyze.sh /path/to/proje
#   ./scripts/run-analyze.sh /path/to/proje/src/main/java
#
# Ortam değişkenleri (isteğe bağlı):
#   LANGUAGE_LEVEL=JAVA_11   (varsayılan JAVA_17; eski DWH için JAVA_6)
#   OUTPUT_DIR=./my-reports  (varsayılan: ./analysis-output)
#   FIXED_REPORT=1           (1 = parser-raporu.md üzerine yaz)
#   REPORT_TAG=mobil-backend (dosya adı: parser-mobil-backend-YYYYMMDD-HHmmss.md)
#   STANDALONE_RISK_PROFILE=enterprise-java (varsayılan enterprise-java)
#   EXCLUDE_GLOBS=**/src/test/** (varsayılan; test kaynaklarını hariç tutar — tam repo tarayınca fout şişmesin)
#   NO_STATE=1            (1 = incremental cache kapalı)
#   FRESH=1                 (1 = state dosyasını okuma, tek seferlik tam tarama)
set -euo pipefail

# shellcheck source=report-name.sh
source "$(cd "$(dirname "$0")" && pwd)/report-name.sh"
# shellcheck source=resolve-jar.sh
source "$(cd "$(dirname "$0")" && pwd)/resolve-jar.sh"

# Argüman yoksa bulunduğunuz klasör taranır (proje kökünde çalıştırın).
SOURCE_DIR="${1:-.}"
SOURCE="$(cd "${SOURCE_DIR}" && pwd)"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT_DIR="${OUTPUT_DIR:-${ROOT}/analysis-output}"
LANG="${LANGUAGE_LEVEL:-JAVA_17}"
WORKERS="${WORKERS:-0}"
DETAIL="${REPORT_DETAIL:-full}"
EXTRA=(--language-level="${LANG}")
if [[ "${WORKERS}" != "0" ]]; then EXTRA+=(--workers="${WORKERS}"); fi
if [[ "${DETAIL}" == "summary" ]]; then EXTRA+=(--detail=summary); fi
if [[ -n "${INCLUDE_GLOBS:-}" ]]; then EXTRA+=(--include="${INCLUDE_GLOBS}"); fi
EXCLUDE_GLOBS="${EXCLUDE_GLOBS:-**/src/test/**}"
if [[ -n "${EXCLUDE_GLOBS}" ]]; then EXTRA+=(--exclude="${EXCLUDE_GLOBS}"); fi

mkdir -p "${OUT_DIR}"

assign_report_paths "${OUT_DIR}" "${SOURCE}"
if [[ -z "${JSON:-}" || -z "${MD:-}" ]]; then
  echo "[HATA] Rapor yolu uretilemedi (JSON/MD bos). OUTPUT_DIR=${OUT_DIR}" >&2
  exit 1
fi
echo "[STANDALONE] Report file: $(basename "${MD}")"

if ! JAR="$(ensure_analyzer_jar "${ROOT}")"; then
  echo "[HATA] java-code-analyzer.jar bulunamadi (dist/ veya target/)"
  echo "       git pull veya dist/java-code-analyzer.jar kopyalayin"
  echo "       ANALYZER_JAR=/tam/yol/java-code-analyzer.jar ./scripts/run-analyze.sh"
  exit 1
fi

RISK_PROFILE="${STANDALONE_RISK_PROFILE:-enterprise-java}"
RISK_ARGS=()
if [[ -n "${RISK_PROFILE}" ]]; then RISK_ARGS=(--risk-profile="${RISK_PROFILE}"); fi

STATE_ARGS=()
if [[ "${NO_STATE:-0}" != "1" ]]; then
  STATE_FILE="${OUT_DIR}/analyzer-state.json"
  STATE_ARGS=(--state="${STATE_FILE}")
  if [[ "${FRESH:-0}" == "1" ]]; then STATE_ARGS+=(--fresh); fi
fi

java -jar "${JAR}" \
  --path="${SOURCE}" \
  --output="${JSON}" \
  --markdown="${MD}" \
  --top=20 \
  "${RISK_ARGS[@]}" \
  "${STATE_ARGS[@]}" \
  "${EXTRA[@]}"

echo ""
echo "Done."
echo "  Markdown: ${MD}"
echo "  JSON:     ${JSON}"
