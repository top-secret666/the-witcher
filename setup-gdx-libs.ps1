# Skachivaet LibGDX 1.12.1 (Windows) v lib/gdx/ — bez Gradle.
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Lib = Join-Path $Root "lib\gdx"
New-Item -ItemType Directory -Force -Path $Lib | Out-Null

$Base = "https://repo1.maven.org/maven2"
$Files = @(
    "com/badlogicgames/gdx/gdx/1.12.1/gdx-1.12.1.jar",
    "com/badlogicgames/gdx/gdx-jnigen-loader/2.3.1/gdx-jnigen-loader-2.3.1.jar",
    "com/badlogicgames/gdx/gdx-freetype/1.12.1/gdx-freetype-1.12.1.jar",
    "com/badlogicgames/gdx/gdx-freetype-platform/1.12.1/gdx-freetype-platform-1.12.1-natives-desktop.jar",
    "com/badlogicgames/gdx/gdx-backend-lwjgl3/1.12.1/gdx-backend-lwjgl3-1.12.1.jar",
    "com/badlogicgames/gdx/gdx-platform/1.12.1/gdx-platform-1.12.1-natives-desktop.jar",
    "com/badlogicgames/jlayer/jlayer/1.0.1-gdx/jlayer-1.0.1-gdx.jar",
    "org/jcraft/jorbis/0.0.17/jorbis-0.0.17.jar",
    "org/lwjgl/lwjgl/3.3.3/lwjgl-3.3.3.jar",
    "org/lwjgl/lwjgl-glfw/3.3.3/lwjgl-glfw-3.3.3.jar",
    "org/lwjgl/lwjgl-jemalloc/3.3.3/lwjgl-jemalloc-3.3.3.jar",
    "org/lwjgl/lwjgl-openal/3.3.3/lwjgl-openal-3.3.3.jar",
    "org/lwjgl/lwjgl-opengl/3.3.3/lwjgl-opengl-3.3.3.jar",
    "org/lwjgl/lwjgl-stb/3.3.3/lwjgl-stb-3.3.3.jar",
    "org/lwjgl/lwjgl/3.3.3/lwjgl-3.3.3-natives-windows.jar",
    "org/lwjgl/lwjgl-glfw/3.3.3/lwjgl-glfw-3.3.3-natives-windows.jar",
    "org/lwjgl/lwjgl-jemalloc/3.3.3/lwjgl-jemalloc-3.3.3-natives-windows.jar",
    "org/lwjgl/lwjgl-openal/3.3.3/lwjgl-openal-3.3.3-natives-windows.jar",
    "org/lwjgl/lwjgl-opengl/3.3.3/lwjgl-opengl-3.3.3-natives-windows.jar",
    "org/lwjgl/lwjgl-stb/3.3.3/lwjgl-stb-3.3.3-natives-windows.jar"
)

foreach ($rel in $Files) {
    $name = Split-Path $rel -Leaf
    $dest = Join-Path $Lib $name
    if (Test-Path $dest) {
        Write-Host "OK (est): $name"
        continue
    }
    $url = "$Base/$rel"
    Write-Host "Skachivayu: $name"
    Invoke-WebRequest -Uri $url -OutFile $dest -UseBasicParsing
}

Write-Host ""
Write-Host "Gotovo. Biblioteki v: $Lib"
