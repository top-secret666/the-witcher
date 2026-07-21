#!/usr/bin/env bash
# Hybrid compile: Swing UI + minimal GDX bridge (javac, no Gradle).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

SRC="$ROOT/src/main/java"
CORE_ROOT="$ROOT/core/src/main/java"
OUT="$ROOT/out/swing-run"
RES="$ROOT/src/main/resources"
TMP="$ROOT/.tmp"
BRIDGE_LIST="$TMP/swing-gdx-bridge.txt"
SWING_LIST="$TMP/swing-sources.txt"

mkdir -p "$TMP" "$OUT"

# JDK 17: JAVA_HOME or macOS java_home helper
if [[ -z "${JAVA_HOME:-}" ]] && [[ -x /usr/libexec/java_home ]]; then
  JAVA_HOME="$(/usr/libexec/java_home -v 17 2>/dev/null || /usr/libexec/java_home 2>/dev/null || true)"
  export JAVA_HOME
fi

if [[ -n "${JAVA_HOME:-}" ]]; then
  JAVAC="$JAVA_HOME/bin/javac"
else
  JAVAC="$(command -v javac || true)"
fi

if [[ -z "$JAVAC" || ! -x "$JAVAC" ]]; then
  echo "JDK 17 not found. Install Temurin 17 and set JAVA_HOME, or use macOS: brew install openjdk@17" >&2
  exit 1
fi

if [[ ! -f "$ROOT/lib/gdx/gdx-1.12.1.jar" ]]; then
  echo "=== First run: downloading LibGDX ==="
  bash "$ROOT/setup-gdx-libs.sh"
fi

echo "=== Hybrid compile: Swing UI + GDX bridge ==="
echo "Using javac: $JAVAC"

bash "$ROOT/tools/gen-swing-gdx-bridge.sh"

find "$SRC" -name '*.java' -type f | sort > "$SWING_LIST"
bridge_count="$(wc -l < "$BRIDGE_LIST" | tr -d ' ')"
swing_count="$(wc -l < "$SWING_LIST" | tr -d ' ')"
total=$((bridge_count + swing_count))
echo "Compiling $total files ($swing_count swing + $bridge_count gdx bridge)..."

LIB_CP=""
shopt -s nullglob
for jar in "$ROOT"/lib/gdx/*.jar; do
  if [[ -z "$LIB_CP" ]]; then
    LIB_CP="$jar"
  else
    LIB_CP="$LIB_CP:$jar"
  fi
done
shopt -u nullglob

if [[ -z "$LIB_CP" ]]; then
  echo "lib/gdx is empty. Run ./setup-gdx-libs.sh first." >&2
  exit 1
fi

echo "=== Compiling Swing (shared + UI) ==="
# Phase 1: Swing sources (no GDX on classpath)
"$JAVAC" -encoding UTF-8 -source 17 -target 17 -d "$OUT" @"$SWING_LIST"

echo "=== Compiling GDX bridge (icons) ==="
# Phase 2: GDX bridge (needs lib + compiled Swing classes)
"$JAVAC" -encoding UTF-8 -source 17 -target 17 -d "$OUT" -cp "$LIB_CP:$OUT" @"$BRIDGE_LIST"

echo "=== Copying resources ==="
if [[ -d "$RES" ]]; then
  cp -R "$RES"/. "$OUT"/
fi

echo "OK -> $OUT"
