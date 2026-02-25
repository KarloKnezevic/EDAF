#!/usr/bin/env bash
# Copyright (c) 2026 Dr. Karlo Knezevic
# Licensed under the Apache License, Version 2.0

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
INPUT_VERSION="${1:-}"

if [[ -z "$INPUT_VERSION" ]]; then
  echo "Usage: scripts/release/cut-release.sh <version|tag>"
  echo "Examples:"
  echo "  scripts/release/cut-release.sh 3.0.1"
  echo "  scripts/release/cut-release.sh v3.0.1"
  exit 1
fi

if [[ "$INPUT_VERSION" == v* ]]; then
  TAG="$INPUT_VERSION"
  VERSION="${INPUT_VERSION#v}"
else
  VERSION="$INPUT_VERSION"
  TAG="v$VERSION"
fi

if [[ ! "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+([-.][0-9A-Za-z.]+)?$ ]]; then
  echo "[edaf-release] Invalid version '$VERSION'. Expected semantic version, e.g. 3.0.1"
  exit 1
fi

cd "$ROOT_DIR"

if [[ -n "$(git status --porcelain)" ]]; then
  echo "[edaf-release] Working tree is not clean. Commit/stash local changes first."
  exit 1
fi

if git rev-parse "$TAG" >/dev/null 2>&1; then
  echo "[edaf-release] Tag '$TAG' already exists locally."
  exit 1
fi

echo "[edaf-release] Updating revision to $VERSION in pom.xml"
perl -0777 -i -pe "s#<revision>[^<]+</revision>#<revision>$VERSION</revision>#s" pom.xml

echo "[edaf-release] Running quick validation build"
mvn -q -DskipTests validate

git add pom.xml
git commit -m "release: prepare $TAG"
git push origin master

git tag -a "$TAG" -m "EDAF $TAG"
git push origin "$TAG"

echo "[edaf-release] Done."
echo "[edaf-release] Tag pushed: $TAG"
echo "[edaf-release] This now triggers automated workflows:"
echo "  - Release on Tag (GitHub Release + CLI/Web jars + checksums)"
echo "  - Publish GitHub Packages"
echo "  - Publish Maven Central (auto publish)"
