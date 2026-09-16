# Сборка portable .exe и копирование в корень репозитория (локальный dev-скрипт).
param(
    [string]$JavaHome = $env:JAVA_HOME,
    [string]$AppVersion = "0.1.0",
    [string]$AppName = "The Witcher"
)

$ErrorActionPreference = "Stop"
$DevRoot = Split-Path -Parent $PSScriptRoot
$Root = Split-Path -Parent $DevRoot

if (-not $JavaHome -or -not (Test-Path "$JavaHome\bin\jpackage.exe")) {
    $JavaHome = "C:\Program Files\Java\jdk-17"
}
if (-not (Test-Path "$JavaHome\bin\jpackage.exe")) {
    throw "jpackage not found. Install JDK 17+ with jpackage."
}

Write-Host "=== Sync app portrait edits ==="
python (Join-Path $PSScriptRoot "sync_app_portraits.py")
if ($LASTEXITCODE -ne 0) { throw "sync_app_portraits failed." }

& (Join-Path $PSScriptRoot "compile-swing.ps1") -JavaHome $JavaHome

$Out = Join-Path $Root "out\swing-run"
$InputDir = Join-Path $Root "dist\jpackage-input"
$ReleaseDir = Join-Path $Root "release"
$AppJar = Join-Path $InputDir "witcher-prototype.jar"
$IconPng = Join-Path $DevRoot "src\main\resources\assets\sprites\app_icon.png"
$IconIco = Join-Path $Root "dist\app-icon.ico"

if (-not (Test-Path $IconPng)) {
    throw "App icon not found: $IconPng"
}

Write-Host "=== Preparing app icon (.ico) ==="
python (Join-Path $PSScriptRoot "png-to-ico.py") $IconPng $IconIco
if ($LASTEXITCODE -ne 0) { throw "Icon conversion failed." }

if (Test-Path $InputDir) { Remove-Item $InputDir -Recurse -Force }
New-Item -ItemType Directory -Force -Path $InputDir, $ReleaseDir | Out-Null

Write-Host "=== Packing application jar (classes only) ==="
$JarStage = Join-Path $Root "dist\jar-stage"
if (Test-Path $JarStage) { Remove-Item $JarStage -Recurse -Force }
New-Item -ItemType Directory -Force -Path $JarStage | Out-Null
Get-ChildItem -Path $Out -Recurse -Filter "*.class" -File | ForEach-Object {
    $relative = $_.FullName.Substring($Out.Length + 1)
    $target = Join-Path $JarStage $relative
    $targetDir = Split-Path $target -Parent
    if (-not (Test-Path $targetDir)) { New-Item -ItemType Directory -Force -Path $targetDir | Out-Null }
    Copy-Item $_.FullName $target -Force
}
& "$JavaHome\bin\jar.exe" --create --file $AppJar `
    --main-class com.witcher.ui.graphics.GameWindow `
    -C $JarStage .
if ($LASTEXITCODE -ne 0) { throw "jar failed." }

Write-Host "=== Copying LibGDX jars ==="
Copy-Item -Path (Join-Path $Root "lib\gdx\*.jar") -Destination $InputDir

Write-Host "=== Running jpackage ==="
$BundleDir = Join-Path $ReleaseDir $AppName
if (Test-Path $BundleDir) { Remove-Item $BundleDir -Recurse -Force }

& "$JavaHome\bin\jpackage.exe" `
    --type app-image `
    --name $AppName `
    --app-version $AppVersion `
    --input $InputDir `
    --main-jar witcher-prototype.jar `
    --main-class com.witcher.ui.graphics.GameWindow `
    --icon $IconIco `
    --dest $ReleaseDir `
    --java-options "-Xms128m" `
    --java-options "-Xmx768m" `
    --java-options "-Dwitcher.assets=`$APPDIR"

$ExePath = Join-Path $BundleDir "$AppName.exe"
if (-not (Test-Path $ExePath)) {
    throw "Expected exe not found: $ExePath"
}

Write-Host "=== Publishing bundle to repo root ==="
$RootApp = Join-Path $Root "app"
$RootRuntime = Join-Path $Root "runtime"
if (Test-Path $RootApp) { Remove-Item $RootApp -Recurse -Force }
if (Test-Path $RootRuntime) { Remove-Item $RootRuntime -Recurse -Force }

Copy-Item -LiteralPath (Join-Path $BundleDir "app") -Destination $RootApp -Recurse -Force
Copy-Item -LiteralPath (Join-Path $DevRoot "src\main\resources\assets") -Destination (Join-Path $RootApp "assets") -Recurse -Force

$materializeGif = Join-Path $RootApp "assets\sprites\lavka\shop_materialize.gif"
if (-not (Test-Path $materializeGif)) {
    throw "Missing required asset: app/assets/sprites/lavka/shop_materialize.gif"
}
Write-Host "OK: shop_materialize.gif in bundle"
Copy-Item -LiteralPath (Join-Path $BundleDir "runtime") -Destination $RootRuntime -Recurse -Force
Copy-Item -LiteralPath $ExePath -Destination (Join-Path $Root "$AppName.exe") -Force

$CfgPath = Join-Path $Root "app\The Witcher.cfg"
if (Test-Path $CfgPath) {
    $cfg = Get-Content $CfgPath -Raw
    if ($cfg -notmatch 'witcher\.assets') {
        $cfg = $cfg.TrimEnd() + "`r`njava-options=-Dwitcher.assets=`$APPDIR`r`n"
        Set-Content -Path $CfgPath -Value $cfg -NoNewline
    }
}

Write-Host ""
Write-Host "Done. Launch:"
Write-Host "  $(Join-Path $Root "$AppName.exe")"
