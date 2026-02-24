#!/usr/bin/env bash
# Copyright (c) 2026 Dr. Karlo Knezevic
# Licensed under the Apache License, Version 2.0

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="$ROOT_DIR/docker-compose.yml"

if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
  COMPOSE_CMD=(docker compose -f "$COMPOSE_FILE")
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE_CMD=(docker-compose -f "$COMPOSE_FILE")
else
  echo "[edaf] Docker Compose not found."
  exit 1
fi

ACTION="${1:-up}"

usage() {
  cat <<'EOF'
Usage: scripts/docker-stack.sh [up|up-all|down|down-volumes|restart|status|logs]

  up           Start DB + web services in background.
  up-all       Start DB + web + runner in background.
  down         Stop and remove containers.
  down-volumes Stop/remove containers and named volumes.
  restart      Restart DB + web services.
  status       Show service status.
  logs         Follow logs for all services.
EOF
}

case "$ACTION" in
  up)
    "${COMPOSE_CMD[@]}" up -d db web
    echo "[edaf] Web dashboard: http://localhost:7070"
    ;;
  up-all)
    "${COMPOSE_CMD[@]}" up -d db web runner
    echo "[edaf] Web dashboard: http://localhost:7070"
    ;;
  down)
    "${COMPOSE_CMD[@]}" down
    ;;
  down-volumes)
    "${COMPOSE_CMD[@]}" down -v
    ;;
  restart)
    "${COMPOSE_CMD[@]}" down
    "${COMPOSE_CMD[@]}" up -d db web
    echo "[edaf] Web dashboard: http://localhost:7070"
    ;;
  status)
    "${COMPOSE_CMD[@]}" ps
    ;;
  logs)
    "${COMPOSE_CMD[@]}" logs -f
    ;;
  *)
    usage
    exit 1
    ;;
esac
