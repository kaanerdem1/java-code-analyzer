#!/usr/bin/env bash
# Ortak rapor dosya adı: parser-{etiket}-{YYYYMMDD-HHmmss}.md
# REPORT_TAG=ozel-etiket ile etiket zorlanır.

report_name_stamp() {
  date +%Y%m%d-%H%M%S
}

report_name_tag() {
  local source_path="${1:-.}"
  if [[ -n "${REPORT_TAG:-}" ]]; then
    echo "${REPORT_TAG}"
    return
  fi
  local name
  name="$(basename "${source_path}")"
  if [[ "${name}" == "." || -z "${name}" ]]; then
    name="$(basename "$(cd "${source_path}" 2>/dev/null && pwd || echo "${source_path}")")"
  fi
  echo "${name}" \
    | tr '[:upper:]' '[:lower:]' \
    | sed -E 's/[^a-z0-9._-]+/-/g' \
    | sed -E 's/^-+|-+$//g' \
    | cut -c1-48
}

# macOS varsayılan Bash 3.2 mapfile desteklemez; JSON/MD değişkenlerini set eder.
assign_report_paths() {
  local md_dir="$1"
  local source_path="$2"
  local json_dir="${3:-${md_dir}}"
  local stamp tag base
  stamp="$(report_name_stamp)"
  tag="$(report_name_tag "${source_path}")"
  base="parser-${tag}-${stamp}"
  if [[ "${FIXED_REPORT:-0}" == "1" ]]; then
    JSON="${json_dir}/standalone.json"
    MD="${md_dir}/parser-raporu.md"
  else
    JSON="${json_dir}/standalone-${tag}-${stamp}.json"
    MD="${md_dir}/${base}.md"
  fi
}
