#!/usr/bin/env bash
# Öncelik: ANALYZER_JAR → target/ (mvn package)

resolve_analyzer_jar() {
  local root="$1"
  if [[ -n "${ANALYZER_JAR:-}" && -f "${ANALYZER_JAR}" ]]; then
    printf '%s' "${ANALYZER_JAR}"
    return 0
  fi
  if [[ -f "${root}/target/java-code-analyzer.jar" ]]; then
    printf '%s' "${root}/target/java-code-analyzer.jar"
    return 0
  fi
  return 1
}

ensure_analyzer_jar() {
  local root="$1"
  local jar
  if jar="$(resolve_analyzer_jar "${root}")"; then
    printf '%s' "${jar}"
    return 0
  fi
  if [[ -f "${root}/pom.xml" ]] && grep -q 'java-code-analyzer' "${root}/pom.xml" 2>/dev/null; then
    echo "[STANDALONE] JAR yok, analyzer derleniyor (mvn package)..." >&2
    mvn -q -f "${root}/pom.xml" package
    printf '%s' "${root}/target/java-code-analyzer.jar"
    return 0
  fi
  return 1
}
