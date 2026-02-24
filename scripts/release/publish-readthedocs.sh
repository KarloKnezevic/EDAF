#!/usr/bin/env bash
# Copyright (c) 2026 Dr. Karlo Knezevic
# Licensed under the Apache License, Version 2.0

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

RTD_PROJECT="${READTHEDOCS_PROJECT:-}"
RTD_TOKEN="${READTHEDOCS_TOKEN:-}"
RTD_VERSION="${READTHEDOCS_VERSION:-latest}"
API_BASE="${READTHEDOCS_API_BASE:-https://readthedocs.org/api/v3}"

if [[ -z "$RTD_PROJECT" || -z "$RTD_TOKEN" ]]; then
  echo "[rtd] Set READTHEDOCS_PROJECT and READTHEDOCS_TOKEN."
  echo "[rtd] Optional: READTHEDOCS_VERSION (default: latest)."
  exit 1
fi

echo "[rtd] Building docs locally (strict)..."
if command -v mkdocs >/dev/null 2>&1; then
  mkdocs build --strict
else
  echo "[rtd] mkdocs not found. Install it first, e.g.: pip install -r docs/requirements-rtd.txt"
  exit 1
fi

BUILD_ENDPOINT="$API_BASE/projects/$RTD_PROJECT/versions/$RTD_VERSION/builds/"
echo "[rtd] Triggering Read the Docs build: $RTD_PROJECT/$RTD_VERSION"

HTTP_CODE="$(
  curl -sS -o /tmp/edaf-rtd-response.json -w "%{http_code}" \
    -X POST \
    -H "Authorization: Token $RTD_TOKEN" \
    -H "Content-Type: application/json" \
    "$BUILD_ENDPOINT"
)"

if [[ "$HTTP_CODE" != "201" && "$HTTP_CODE" != "202" ]]; then
  echo "[rtd] Build trigger failed (HTTP $HTTP_CODE)."
  cat /tmp/edaf-rtd-response.json
  exit 1
fi

echo "[rtd] Build triggered successfully."
cat /tmp/edaf-rtd-response.json
