#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

echo "[edaf] Generating aggregated JavaDoc..."
mvn -q -P apidocs -DskipTests verify

SOURCE_APIDOC_DIR="$ROOT_DIR/target/reports/apidocs"
DOCS_APIDOC_DIR="$ROOT_DIR/docs/api/javadocs"

echo "[edaf] JavaDoc generated at:"
echo "  $SOURCE_APIDOC_DIR/index.html"

# Optional sync for docs publishing workflows.
if [[ "${EDAF_SYNC_DOCS_JAVADOC:-0}" == "1" ]]; then
  mkdir -p "$DOCS_APIDOC_DIR"
  rsync -a --delete "$SOURCE_APIDOC_DIR"/ "$DOCS_APIDOC_DIR"/
  echo "[edaf] Synced JavaDoc bundle to:"
  echo "  $DOCS_APIDOC_DIR/index.html"
fi
