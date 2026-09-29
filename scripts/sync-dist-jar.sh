#!/usr/bin/env bash
# Geliştirici: kaynak değişince dist/ JAR'ını güncelle (commit + push için).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "${ROOT}"
mvn -q -DskipTests package
mkdir -p dist
cp target/java-code-analyzer.jar dist/java-code-analyzer.jar
echo "[OK] dist/java-code-analyzer.jar güncellendi ($(du -h dist/java-code-analyzer.jar | cut -f1))"
