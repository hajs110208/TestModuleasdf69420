#!/usr/bin/env bash
# Writes version.properties from a semantic-release version (e.g. 1.2.3 or 1.3.0-beta.4).
#
# versionCode = major*1_000_000 + minor*10_000 + patch*100 + pre
#   pre = beta number (1..98) for prereleases, 99 for stable releases,
# so 1.0.0-beta.1 < 1.0.0-beta.2 < 1.0.0 and every release strictly increases.
set -euo pipefail

version="${1:?usage: set-version.sh <semver>}"

core="${version%%-*}"
IFS='.' read -r major minor patch <<<"$core"

pre=99
if [[ "$version" == *-* ]]; then
  suffix="${version#*-}"        # e.g. beta.4
  num="${suffix##*.}"           # e.g. 4
  if [[ "$num" =~ ^[0-9]+$ ]]; then
    pre=$(( num > 98 ? 98 : num ))
  else
    pre=1
  fi
fi

code=$(( major * 1000000 + minor * 10000 + patch * 100 + pre ))

cd "$(dirname "$0")/.."
printf 'versionName=%s\nversionCode=%s\n' "$version" "$code" > version.properties
echo "version.properties -> versionName=$version versionCode=$code"
