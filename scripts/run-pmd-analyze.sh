#!/usr/bin/env bash
# PMD karmaşıklık taraması — gerçek proje kökü (veya verilen klasör).
set -euo pipefail

SOURCE_DIR="${1:-.}"
SOURCE="$(cd "${SOURCE_DIR}" && pwd)"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=resolve-jar.sh
source "$(cd "$(dirname "$0")" && pwd)/resolve-jar.sh"
OUT_DIR="${OUTPUT_DIR:-${ROOT}/analysis-output}"
MD="${OUT_DIR}/pmd-raporu.md"
PMD_XML="${ROOT}/target/pmd-scan/pmd.xml"

mkdir -p "${OUT_DIR}"

echo "[PMD] Taranıyor: ${SOURCE}"
mvn -q -f "${ROOT}/config/pmd-scan-pom.xml" -Dscan.root="${SOURCE}" pmd:pmd

if [[ ! -f "${PMD_XML}" ]]; then
  echo "[HATA] ${PMD_XML} oluşmadı"
  exit 1
fi

JAR="$(ensure_analyzer_jar "${ROOT}")"

java -cp "${JAR}" com.standalone.analyzer.ReadableReportMain pmd "${PMD_XML}" "${MD}"

echo "[PMD] Rapor: ${MD}"
