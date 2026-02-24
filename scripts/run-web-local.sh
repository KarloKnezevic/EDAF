#!/usr/bin/env bash
# Copyright (c) 2026 Dr. Karlo Knezevic
# Licensed under the Apache License, Version 2.0

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PORT="${EDAF_WEB_PORT:-7070}"
DB_PATH="${EDAF_DB_PATH:-$ROOT_DIR/edaf-v3.db}"
SKIP_BUILD="${EDAF_SKIP_BUILD:-0}"

if [[ "$SKIP_BUILD" != "1" ]]; then
  echo "[edaf] Building web module..."
  mvn -q -pl edaf-web -am package -DskipTests -f "$ROOT_DIR/pom.xml"
fi

JAR_PATH="$(find "$ROOT_DIR/edaf-web/target" -maxdepth 1 -type f -name 'edaf-web-*.jar' ! -name '*.original' | head -n 1)"
if [[ -z "$JAR_PATH" || ! -f "$JAR_PATH" ]]; then
  echo "[edaf] Missing web jar under: $ROOT_DIR/edaf-web/target"
  echo "[edaf] Run: mvn -q -pl edaf-web -am package -DskipTests"
  exit 1
fi

echo "[edaf] Starting web dashboard on http://localhost:$PORT"
echo "[edaf] DB: $DB_PATH"

EDAF_DB_URL="jdbc:sqlite:$DB_PATH" \
SERVER_PORT="$PORT" \
java --enable-native-access=ALL-UNNAMED -jar "$JAR_PATH"
