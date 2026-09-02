# Сборка portable .exe через jpackage (JDK 17+).
param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$AppVersion = "0.1.0",
    [string]$AppName = "The Witcher"
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot

if (-not $JavaHome -or -not (Test-Path "$JavaHome\bin\jpackage.exe")) {
    $JavaHome = "C:\Program Files\Java\jdk-17"
}
if (-not (Test-Path "$JavaHome\bin\jpackage.exe")) {
    throw "jpackage not found. Install JDK 17+ with jpackage."
}

& (Join-Path $PSScriptRoot "compile-swing.ps1") -JavaHome $JavaHome

$Out = Join-Path $Root "out\swing-run"
$InputDir = Join-Path $Root "dist\jpackage-input"
$ReleaseDir = Join-Path $Root "release"
$AppJar = Join-Path $InputDir "witcher-prototype.jar"

if (Test-Path $InputDir) { Remove-Item $InputDir -Recurse -Force }
New-Item -ItemType Directory -Force -Path $InputDir, $ReleaseDir | Out-Null

Write-Host "=== Packing application jar ==="
& "$JavaHome\bin\jar.exe" --create --file $AppJar `
    --main-class main.java.com.witcher.ui.graphics.GameWindow `
    -C $Out .
if ($LASTEXITCODE -ne 0) { throw "jar failed." }

Write-Host "=== Copying LibGDX jars ==="
Copy-Item -Path (Join-Path $Root "lib\gdx\*.jar") -Destination $InputDir

Write-Host "=== Running jpackage ==="
if (Test-Path (Join-Path $ReleaseDir $AppName)) {
    Remove-Item (Join-Path $ReleaseDir $AppName) -Recurse -Force
}

& "$JavaHome\bin\jpackage.exe" `
    --type app-image `
    --name $AppName `
    --app-version $AppVersion `
    --input $InputDir `
    --main-jar witcher-prototype.jar `
    --main-class main.java.com.witcher.ui.graphics.GameWindow `
    --dest $ReleaseDir `
    --java-options "-Xms128m" `
    --java-options "-Xmx768m"

$ExePath = Join-Path $ReleaseDir "$AppName\$AppName.exe"
if (-not (Test-Path $ExePath)) {
    throw "Expected exe not found: $ExePath"
}

Write-Host ""
Write-Host "Done. Launch:"
Write-Host "  $ExePath"
