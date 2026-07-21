#!/usr/bin/env bash
# Downloads LibGDX 1.12.1 jars + macOS LWJGL natives into lib/gdx/
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
LIB_DIR="$ROOT/lib/gdx"
GDX_VER="1.12.1"
LWJGL_VER="3.3.3"
LWJGL_BUILD="5"

mkdir -p "$LIB_DIR"

case "$(uname -s)" in
  Darwin)
    case "$(uname -m)" in
      arm64) NATIVES_SUFFIX="natives-macos-arm64" ;;
      *)     NATIVES_SUFFIX="natives-macos" ;;
    esac
    ;;
  Linux)
    case "$(uname -m)" in
      aarch64|arm64) NATIVES_SUFFIX="natives-linux-arm64" ;;
      *)             NATIVES_SUFFIX="natives-linux" ;;
    esac
    ;;
  *)
    echo "Unsupported OS: $(uname -s). Use setup-gdx-libs.ps1 on Windows." >&2
    exit 1
    ;;
esac

download() {
  local url="$1"
  local dest="$2"
  if [[ -f "$dest" ]]; then
    echo "  skip (exists): $(basename "$dest")"
    return 0
  fi
  echo "  download: $(basename "$dest")"
  curl -fsSL "$url" -o "$dest"
}

echo "=== LibGDX $GDX_VER -> $LIB_DIR ==="

GDX_BASE="https://repo1.maven.org/maven2/com/badlogicgames/gdx"
download "$GDX_BASE/gdx/$GDX_VER/gdx-$GDX_VER.jar" \
  "$LIB_DIR/gdx-$GDX_VER.jar"
download "$GDX_BASE/gdx-jnigen-loader/2.3.1/gdx-jnigen-loader-2.3.1.jar" \
  "$LIB_DIR/gdx-jnigen-loader-2.3.1.jar"
download "$GDX_BASE/gdx-backend-lwjgl3/$GDX_VER/gdx-backend-lwjgl3-$GDX_VER.jar" \
  "$LIB_DIR/gdx-backend-lwjgl3-$GDX_VER.jar"
download "$GDX_BASE/gdx-freetype/$GDX_VER/gdx-freetype-$GDX_VER.jar" \
  "$LIB_DIR/gdx-freetype-$GDX_VER.jar"
download "$GDX_BASE/gdx-freetype-platform/$GDX_VER/gdx-freetype-platform-$GDX_VER-natives-desktop.jar" \
  "$LIB_DIR/gdx-freetype-platform-$GDX_VER-natives-desktop.jar"
download "$GDX_BASE/gdx-platform/$GDX_VER/gdx-platform-$GDX_VER-natives-desktop.jar" \
  "$LIB_DIR/gdx-platform-$GDX_VER-natives-desktop.jar"
download "$GDX_BASE/jlayer/1.0.1-gdx/jlayer-1.0.1-gdx.jar" \
  "$LIB_DIR/jlayer-1.0.1-gdx.jar"

download "https://repo1.maven.org/maven2/org/jcraft/jorbis/0.0.17/jorbis-0.0.17.jar" \
  "$LIB_DIR/jorbis-0.0.17.jar"

LWJGL_BASE="https://repo1.maven.org/maven2/org/lwjgl"
for mod in lwjgl lwjgl-glfw lwjgl-jemalloc lwjgl-openal lwjgl-opengl lwjgl-stb; do
  download "$LWJGL_BASE/$mod/$LWJGL_VER/$mod-$LWJGL_VER.jar" \
    "$LIB_DIR/$mod-$LWJGL_VER.jar"
  download "$LWJGL_BASE/$mod/$LWJGL_VER/$mod-$LWJGL_VER-$NATIVES_SUFFIX.jar" \
    "$LIB_DIR/$mod-$LWJGL_VER-$NATIVES_SUFFIX.jar"
done

echo ""
echo "Done. Jars in $LIB_DIR (platform: $NATIVES_SUFFIX)"
