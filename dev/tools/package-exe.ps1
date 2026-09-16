# Windows .exe via jpackage. Does NOT change main sources — only build output.
# - Keeps assets already edited under out/swing-run/assets
# - Fat jar (classes + assets) so shop_materialize.gif loads from classpath
# - EXE-only overlay classes in dev/tools/release-overlay/
param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$AppVersion = "0.1.0",
    [string]$AppName = "The Witcher"
)

$ErrorActionPreference = "Stop"
$DevRoot = Split-Path -Parent $PSScriptRoot
$Root = Split-Path -Parent $DevRoot
$Out = Join-Path $Root "out\swing-run"
$AssetBackup = Join-Path $Root ".tmp\release-assets-backup"
$InputDir = Join-Path $Root "dist\jpackage-input"
$ReleaseDir = Join-Path $Root "release"
$AppJar = Join-Path $InputDir "witcher-prototype.jar"
$IconPng = Join-Path $DevRoot "src\main\resources\assets\sprites\app_icon.png"
$IconIco = Join-Path $Root "dist\app-icon.ico"
$OverlayDir = Join-Path $PSScriptRoot "release-overlay"
$BossOverride = Join-Path $Out "assets\sprites\chapter1\battle\boss_duke_portrait.png"

if (-not $JavaHome -or -not (Test-Path "$JavaHome\bin\jpackage.exe")) {
    $JavaHome = "C:\Program Files\Java\jdk-17"
}
if (-not (Test-Path "$JavaHome\bin\jpackage.exe")) {
    throw "jpackage not found. Install JDK 17+ with jpackage."
}

if (Test-Path (Join-Path $Out "assets")) {
    Write-Host "=== Backup out/swing-run/assets (your edits) ==="
    if (Test-Path $AssetBackup) { Remove-Item $AssetBackup -Recurse -Force }
    Copy-Item -LiteralPath (Join-Path $Out "assets") -Destination $AssetBackup -Recurse -Force
}

Write-Host "=== Compile (compile-swing-hybrid.bat) ==="
& (Join-Path $Root "compile-swing-hybrid.bat")
if ($LASTEXITCODE -ne 0) { throw "compile-swing-hybrid.bat failed." }

if (Test-Path $AssetBackup) {
    Write-Host "=== Restore your out/assets over compile output ==="
    Copy-Item -Path (Join-Path $AssetBackup "*") -Destination (Join-Path $Out "assets") -Recurse -Force
}

# Always ship latest audio from sources (backup must not keep stale/missing mp3).
$AudioSrc = Join-Path $DevRoot "src\main\resources\assets\audio"
$AudioOut = Join-Path $Out "assets\audio"
if (Test-Path $AudioSrc) {
    Write-Host "=== Sync audio mp3 into out/swing-run ==="
    New-Item -ItemType Directory -Force -Path $AudioOut | Out-Null
    Copy-Item -Path (Join-Path $AudioSrc "*") -Destination $AudioOut -Force
}

$ReleaseBossPortrait = Join-Path $PSScriptRoot "release-assets\boss_duke_portrait.png"
if (Test-Path $ReleaseBossPortrait) {
    Write-Host "=== EXE: wolf boss map portrait ==="
    $bossDir = Split-Path -Parent $BossOverride
    if (-not (Test-Path $bossDir)) { New-Item -ItemType Directory -Force -Path $bossDir | Out-Null }
    Copy-Item -LiteralPath $ReleaseBossPortrait -Destination $BossOverride -Force
}

$materialize = Join-Path $Out "assets\sprites\lavka\shop_materialize.gif"
if (-not (Test-Path $materialize)) {
    throw "Missing shop_materialize.gif: $materialize"
}
if (-not (Test-Path $BossOverride)) {
    throw "Missing boss_duke_portrait.png: $BossOverride"
}
Write-Host "OK: shop_materialize.gif + boss_duke_portrait.png"

$OverlayFiles = @(Get-ChildItem $OverlayDir -Filter "*.java" -File | ForEach-Object { $_.FullName })
Write-Host "=== EXE overlay: compile $($OverlayFiles.Count) release classes ==="
$libCp = (Get-ChildItem (Join-Path $Root "lib\gdx\*.jar") | ForEach-Object { $_.FullName }) -join ";"
& "$JavaHome\bin\javac.exe" -encoding UTF-8 -cp "$Out;$libCp" -d $Out @OverlayFiles
if ($LASTEXITCODE -ne 0) { throw "release overlay compile failed." }

Write-Host "=== Preparing app icon (.ico) ==="
python (Join-Path $PSScriptRoot "png-to-ico.py") $IconPng $IconIco
if ($LASTEXITCODE -ne 0) { throw "Icon conversion failed." }

if (Test-Path $InputDir) { Remove-Item $InputDir -Recurse -Force }
New-Item -ItemType Directory -Force -Path $InputDir, $ReleaseDir, (Split-Path $IconIco) | Out-Null

Write-Host "=== Packing FAT jar (classes + assets from out/swing-run) ==="
if (Test-Path $AppJar) { Remove-Item $AppJar -Force }
& "$JavaHome\bin\jar.exe" --create --file $AppJar -C $Out .
if ($LASTEXITCODE -ne 0) { throw "jar failed." }

Write-Host "=== Copying LibGDX jars ==="
Copy-Item -Path (Join-Path $Root "lib\gdx\*.jar") -Destination $InputDir

Write-Host "=== Running jpackage (.exe) ==="
$BundleDir = Join-Path $ReleaseDir $AppName
if (Test-Path $BundleDir) { Remove-Item $BundleDir -Recurse -Force }

& "$JavaHome\bin\jpackage.exe" `
    --type app-image `
    --name $AppName `
    --app-version $AppVersion `
    --input $InputDir `
    --main-jar witcher-prototype.jar `
    --main-class main.java.com.witcher.ui.graphics.GameWindow `
    --icon $IconIco `
    --dest $ReleaseDir `
    --java-options "-Xms128m" `
    --java-options "-Xmx768m"

$ExePath = Join-Path $BundleDir "$AppName.exe"
if (-not (Test-Path $ExePath)) {
    throw "Expected exe not found: $ExePath"
}

Write-Host "=== Publishing bundle to repo root ==="
$RootApp = Join-Path $Root "app"
$RootRuntime = Join-Path $Root "runtime"
$RootExe = Join-Path $Root "$AppName.exe"

# Старый EXE/jars часто заняты запущенной игрой — иначе Remove-Item падает.
Get-Process -ErrorAction SilentlyContinue |
    Where-Object { $_.Path -and ($_.Path -eq $RootExe -or $_.Path -like "$RootApp\*" -or $_.Path -like "$RootRuntime\*") } |
    ForEach-Object {
        Write-Host "Stopping locked process: $($_.ProcessName) ($($_.Id))"
        Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
    }
Start-Sleep -Milliseconds 400

if (Test-Path $RootApp) { Remove-Item $RootApp -Recurse -Force }
if (Test-Path $RootRuntime) { Remove-Item $RootRuntime -Recurse -Force }
if (Test-Path $RootExe) { Remove-Item $RootExe -Force }

Copy-Item -LiteralPath (Join-Path $BundleDir "app") -Destination $RootApp -Recurse -Force
Copy-Item -LiteralPath (Join-Path $BundleDir "runtime") -Destination $RootRuntime -Recurse -Force
Copy-Item -LiteralPath $ExePath -Destination $RootExe -Force

Write-Host ""
Write-Host "Done. Launch:"
Write-Host "  $RootExe"
Write-Host "Dev run unchanged: run.bat"
