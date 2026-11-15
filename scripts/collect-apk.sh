#!/usr/bin/env bash
# Copies the built release APK to dist/TestModule-<version>.apk for the GitHub release.
set -euo pipefail

version="${1:?usage: collect-apk.sh <semver>}"

cd "$(dirname "$0")/.."
apk="$(ls app/build/outputs/apk/release/*.apk 2>/dev/null | head -n 1 || true)"
if [[ -z "$apk" ]]; then
  echo "No release APK found under app/build/outputs/apk/release/" >&2
  exit 1
fi

mkdir -p dist
cp "$apk" "dist/TestModule-${version}.apk"
echo "dist/TestModule-${version}.apk"
