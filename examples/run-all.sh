#!/usr/bin/env bash

# Simple test runner for all example configurations in the examples module.
# Builds the project, iterates over all YAML configs and runs them, reporting pass/fail.

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
JAR="$SCRIPT_DIR/target/edaf.jar"

# Settings (can be overridden via env)
TIMEOUT_SEC="${TIMEOUT_SEC:-120}"
METRICS_FLAG="${METRICS_FLAG:-}"            # set to "--metrics" to enable
PROM_PORT="${PROM_PORT:-}"                   # e.g. 9464
PURE_JAVA_BLAS="${PURE_JAVA_BLAS:-0}"       # set to 1 to force pure Java BLAS

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

log() { echo -e "$1"; }

# Resolve timeout command
TIMEOUT_CMD=""
if command -v timeout >/dev/null 2>&1; then
  TIMEOUT_CMD="timeout $TIMEOUT_SEC"
elif command -v gtimeout >/dev/null 2>&1; then
  TIMEOUT_CMD="gtimeout $TIMEOUT_SEC"
else
  log "${YELLOW}Warning:${NC} 'timeout' not found; runs will not be forcibly terminated."
fi

# Build project
log "Building project (skip tests)..."
(cd "$ROOT_DIR" && mvn -q -DskipTests package) || { log "${RED}Build failed.${NC}"; exit 1; }

if [ ! -f "$JAR" ]; then
  log "${RED}Shaded jar not found at $JAR${NC}"
  exit 1
fi

# Prepare JVM flags
JVM_FLAGS=()
# Suppress netlib native warnings or switch to pure Java BLAS
if [ "$PURE_JAVA_BLAS" = "1" ]; then
  JVM_FLAGS+=(
    -Dcom.github.fommil.netlib.BLAS=com.github.fommil.netlib.F2jBLAS
    -Dcom.github.fommil.netlib.LAPACK=com.github.fommil.netlib.F2jLAPACK
  )
else
  JVM_FLAGS+=(--enable-native-access=ALL-UNNAMED)
fi

# CLI flags
CLI_FLAGS=()
if [ -n "$METRICS_FLAG" ]; then
  CLI_FLAGS+=($METRICS_FLAG)
fi
if [ -n "$PROM_PORT" ]; then
  CLI_FLAGS+=(--prometheus-port "$PROM_PORT")
fi

# Logs
LOG_DIR="$SCRIPT_DIR/test-logs"
mkdir -p "$LOG_DIR"
SUMMARY_FILE="$LOG_DIR/summary.txt"
PASS_CNT=0
FAIL_CNT=0
TOTAL=0
: > "$SUMMARY_FILE"

# Iterate configs
CONFIG_LIST=$(find "$SCRIPT_DIR/config" -type f -name "*.yaml" | sort)
if [ -z "$CONFIG_LIST" ]; then
  log "${YELLOW}No configuration files found under examples/config${NC}"
  exit 0
fi

COUNT=$(printf "%s\n" "$CONFIG_LIST" | wc -l | awk '{print $1}')
log "Found ${COUNT} configurations. Starting runs...\n"

for cfg in $CONFIG_LIST; do
  ((TOTAL++))
  rel_cfg="${cfg#"$SCRIPT_DIR/"}"
  name="${rel_cfg//\//_}"
  log_file="$LOG_DIR/${name%.yaml}.log"
  : > "$log_file"

  # Compose command
  CMD=(java "${JVM_FLAGS[@]}" -jar "$JAR" "$cfg" "${CLI_FLAGS[@]}")

  printf "Running %-60s " "$rel_cfg"

  # Run with timeout if available
  if [ -n "$TIMEOUT_CMD" ]; then
    $TIMEOUT_CMD "${CMD[@]}" >"$log_file" 2>&1
  else
    "${CMD[@]}" >"$log_file" 2>&1
  fi
  rc=$?

  if [ $rc -eq 0 ]; then
    echo -e "${GREEN}OK${NC}"
    echo "PASS  $rel_cfg" >> "$SUMMARY_FILE"
    ((PASS_CNT++))
  else
    echo -e "${RED}FAIL (rc=$rc)${NC}"
    echo "FAIL  $rel_cfg (rc=$rc)" >> "$SUMMARY_FILE"
    ((FAIL_CNT++))
  fi
done

echo "" >> "$SUMMARY_FILE"
echo "Total: $TOTAL  Pass: $PASS_CNT  Fail: $FAIL_CNT" >> "$SUMMARY_FILE"

log "\n==================== Summary ===================="
cat "$SUMMARY_FILE"
log "================================================\n"

if [ $FAIL_CNT -ne 0 ]; then
  log "${YELLOW}Some configurations failed. See logs in:${NC} $LOG_DIR"
  exit 1
fi

exit 0


