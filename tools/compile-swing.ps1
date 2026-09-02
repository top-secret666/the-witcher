# Сборка Swing + GDX bridge (замена compile-swing-hybrid.bat).
param(
    [string]$JavaHome = $env:JAVA_HOME
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot

if (-not $JavaHome -or -not (Test-Path "$JavaHome\bin\javac.exe")) {
    $JavaHome = "C:\Program Files\Java\jdk-17"
}
if (-not (Test-Path "$JavaHome\bin\javac.exe")) {
    throw "JDK 17 not found. Set JAVA_HOME or install JDK 17."
}

$Src = Join-Path $Root "src\main\java"
$Res = Join-Path $Root "src\main\resources"
$Out = Join-Path $Root "out\swing-run"
$Core = Join-Path $Root "core\src\main\java"
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
& (Join-Path $PSScriptRoot "gen-swing-gdx-bridge.ps1") -CoreRoot $Core -OutFile $bridgeList
& "$JavaHome\bin\javac.exe" -encoding UTF-8 -cp "$LibCp;$Out" -d $Out "@$bridgeList"
if ($LASTEXITCODE -ne 0) { throw "GDX bridge compile failed." }

Write-Host "=== Copying resources ==="
if (Test-Path $Res) {
    Copy-Item -Path "$Res\*" -Destination $Out -Recurse -Force
}

Write-Host "Build OK: $Out"
