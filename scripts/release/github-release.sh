#!/usr/bin/env bash
# Copyright (c) 2026 Dr. Karlo Knezevic
# Licensed under the Apache License, Version 2.0

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TAG="${1:-}"

if [[ -z "$TAG" ]]; then
  echo "Usage: scripts/release/github-release.sh <tag> [--push-tag]"
  echo "Example: scripts/release/github-release.sh v3.0.0 --push-tag"
  exit 1
fi

PUSH_TAG="${2:-}"

if ! command -v gh >/dev/null 2>&1; then
  echo "[edaf-release] GitHub CLI (gh) is required."
  exit 1
fi

cd "$ROOT_DIR"

if [[ -n "$(git status --porcelain)" ]]; then
  echo "[edaf-release] Working tree is not clean. Commit/stash changes first."
  exit 1
fi

echo "[edaf-release] Running clean verification build..."
mvn -q clean test -DskipITs

echo "[edaf-release] Building release artifacts..."
mvn -q -pl edaf-cli,edaf-web -am package -DskipTests

DIST_DIR="$ROOT_DIR/dist/$TAG"
mkdir -p "$DIST_DIR"

cp "$ROOT_DIR/edaf-cli/target/edaf-cli.jar" "$DIST_DIR/edaf-cli.jar"
WEB_JAR_PATH="$(find "$ROOT_DIR/edaf-web/target" -maxdepth 1 -type f -name 'edaf-web-*.jar' ! -name '*.original' | head -n 1)"
if [[ -z "$WEB_JAR_PATH" || ! -f "$WEB_JAR_PATH" ]]; then
  echo "[edaf-release] Missing web jar under edaf-web/target."
  exit 1
fi
cp "$WEB_JAR_PATH" "$DIST_DIR/edaf-web.jar"
cp "$ROOT_DIR/README.md" "$DIST_DIR/README.md"
cp "$ROOT_DIR/LICENSE" "$DIST_DIR/LICENSE"

(cd "$DIST_DIR" && shasum -a 256 edaf-cli.jar edaf-web.jar > SHA256SUMS.txt)

if ! git rev-parse "$TAG" >/dev/null 2>&1; then
  git tag "$TAG"
  echo "[edaf-release] Created local tag $TAG"
fi

if [[ "$PUSH_TAG" == "--push-tag" ]]; then
  git push origin "$TAG"
  echo "[edaf-release] Pushed tag $TAG"
fi

if gh release view "$TAG" >/dev/null 2>&1; then
  echo "[edaf-release] GitHub release $TAG already exists. Uploading/overwriting assets..."
  gh release upload "$TAG" \
    "$DIST_DIR/edaf-cli.jar" \
    "$DIST_DIR/edaf-web.jar" \
    "$DIST_DIR/SHA256SUMS.txt" \
    --clobber
else
  gh release create "$TAG" \
    "$DIST_DIR/edaf-cli.jar" \
    "$DIST_DIR/edaf-web.jar" \
    "$DIST_DIR/SHA256SUMS.txt" \
    --title "EDAF $TAG" \
    --notes "EDAF release $TAG"
fi

echo "[edaf-release] Done. Artifacts: $DIST_DIR"
