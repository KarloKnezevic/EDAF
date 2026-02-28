#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

echo "[edaf] Generating aggregated JavaDoc..."
mvn -q -P apidocs -DskipTests verify

echo "[edaf] JavaDoc generated at:"
echo "  $ROOT_DIR/target/site/apidocs/index.html"
