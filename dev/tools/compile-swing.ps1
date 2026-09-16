# Build Swing sources + optional GDX bridge into out/swing-run.
param(
    [string]$JavaHome = $env:JAVA_HOME
)

$ErrorActionPreference = "Stop"
$DevRoot = Split-Path -Parent $PSScriptRoot
$Root = Split-Path -Parent $DevRoot

if (-not $JavaHome -or -not (Test-Path "$JavaHome\bin\javac.exe")) {
    $JavaHome = "C:\Program Files\Java\jdk-17"
}
if (-not (Test-Path "$JavaHome\bin\javac.exe")) {
    throw "JDK 17 not found. Set JAVA_HOME or install JDK 17."
}

$Src = Join-Path $DevRoot "src\main\java"
$Res = Join-Path $DevRoot "src\main\resources"
$Out = Join-Path $Root "out\swing-run"
$Core = Join-Path $DevRoot "core\src\main\java"
$Tmp = Join-Path $Root ".tmp"
$LibDir = Join-Path $Root "lib\gdx"

if (-not (Test-Path "$LibDir\gdx-1.12.1.jar")) {
    Write-Host "=== First run: downloading LibGDX ==="
    & (Join-Path $Root "setup-gdx-libs.ps1")
}

$LibCp = (Get-ChildItem "$LibDir\*.jar" | ForEach-Object { $_.FullName }) -join ";"

New-Item -ItemType Directory -Force -Path $Out, $Tmp | Out-Null

Write-Host "=== Compiling Swing (shared + UI) ==="
$swingList = Join-Path $Tmp "swing-sources.txt"
$swingPaths = Get-ChildItem -Path $Src -Filter "*.java" -Recurse -File | ForEach-Object { $_.FullName }
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllLines($swingList, $swingPaths, $utf8NoBom)

& "$JavaHome\bin\javac.exe" -encoding UTF-8 -d $Out "@$swingList"
if ($LASTEXITCODE -ne 0) { throw "Swing compile failed." }

Write-Host "=== Compiling GDX bridge (icons) ==="
$bridgeList = Join-Path $Tmp "swing-gdx-bridge.txt"
$genBridge = Join-Path $PSScriptRoot "gen-swing-gdx-bridge.ps1"
if (Test-Path $genBridge) {
    & $genBridge -CoreRoot $Core -OutFile $bridgeList
} else {
    $bridgePaths = New-Object System.Collections.Generic.List[string]
    $bridgeDir = Join-Path $Core "com\witcher\gdx\bridge"
    if (Test-Path $bridgeDir) {
        Get-ChildItem -Path $bridgeDir -Filter "*.java" -Recurse -File | ForEach-Object { [void]$bridgePaths.Add($_.FullName) }
    }
    foreach ($rel in @(
        "com\witcher\gdx\graphics\GdxTextureBridge.java",
        "com\witcher\gdx\graphics\PixelTextures.java",
        "com\witcher\gdx\graphics\PixelSpriteSheet.java",
        "com\witcher\gdx\graphics\RenderQuality.java"
    )) {
        $p = Join-Path $Core $rel
        if (Test-Path $p) { [void]$bridgePaths.Add($p) }
    }
    if ($bridgePaths.Count -eq 0) {
        Write-Host "No GDX bridge sources found - skipping."
    } else {
        [System.IO.File]::WriteAllLines($bridgeList, $bridgePaths.ToArray(), $utf8NoBom)
    }
}
if ((Test-Path $bridgeList) -and ((Get-Item $bridgeList).Length -gt 0)) {
    $bridgeArg = "@" + $bridgeList
    & "$JavaHome\bin\javac.exe" -encoding UTF-8 -cp "$LibCp;$Out" -d $Out $bridgeArg
    if ($LASTEXITCODE -ne 0) { throw "GDX bridge compile failed." }
}

Write-Host "=== Copying resources ==="
if (Test-Path $Res) {
    Copy-Item -Path "$Res\*" -Destination $Out -Recurse -Force
}

Write-Host "Build OK: $Out"
