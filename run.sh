#!/usr/bin/env bash
# Run Witcher on macOS / Linux (Swing + GDX bridge).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

mkdir -p "$ROOT/.tmp"
export TMPDIR="$ROOT/.tmp"

echo "=== Compile (hybrid) ==="
bash "$ROOT/compile-swing-hybrid.sh"
if [[ $? -ne 0 ]]; then
  exit 1
fi

echo ""
echo "=== Run ==="

if [[ -z "${JAVA_HOME:-}" ]] && [[ -x /usr/libexec/java_home ]]; then
  JAVA_HOME="$(/usr/libexec/java_home -v 17 2>/dev/null || /usr/libexec/java_home 2>/dev/null || true)"
  export JAVA_HOME
fi

if [[ -n "${JAVA_HOME:-}" ]]; then
  JAVA="$JAVA_HOME/bin/java"
else
  JAVA="$(command -v java || true)"
fi

if [[ -z "$JAVA" || ! -x "$JAVA" ]]; then
  echo "java not found. Install JDK 17." >&2
  exit 1
fi

OUT="$ROOT/out/swing-run"
ASSETS="$ROOT/src/main/resources/assets"

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

CP="$OUT:$LIB_CP"

if [[ ! -f "$OUT/main/java/com/witcher/ui/graphics/GameWindow.class" ]]; then
  echo "GameWindow.class not found in $OUT" >&2
  exit 1
fi

echo "Launching Swing+GDX from: $OUT"
"$JAVA" -Xms128m -Xmx768m \
  -Dwitcher.assets="$ASSETS" \
  -cp "$CP" \
  main.java.com.witcher.ui.graphics.GameWindow
