#!/usr/bin/env bash
# Generates list of GDX bridge sources for hybrid Swing compile.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CORE_ROOT="$ROOT/core/src/main/java"
OUT_FILE="$ROOT/.tmp/swing-gdx-bridge.txt"
BRIDGE_DIR="$CORE_ROOT/main/java/com/witcher/gdx/bridge"
GRAPHICS_DIR="$CORE_ROOT/main/java/com/witcher/gdx/graphics"

mkdir -p "$(dirname "$OUT_FILE")"
: > "$OUT_FILE"

if [[ -d "$BRIDGE_DIR" ]]; then
  find "$BRIDGE_DIR" -maxdepth 1 -name '*.java' -type f | sort >> "$OUT_FILE"
fi

for name in PixelTextures.java RenderQuality.java GdxTextureBridge.java; do
  f="$GRAPHICS_DIR/$name"
  if [[ -f "$f" ]]; then
    echo "$f" >> "$OUT_FILE"
  fi
done

count="$(wc -l < "$OUT_FILE" | tr -d ' ')"
if [[ "$count" -eq 0 ]]; then
  echo "No GDX bridge sources found under $CORE_ROOT" >&2
  exit 1
fi

echo "GDX bridge sources: $count -> $OUT_FILE"
