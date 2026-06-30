@echo off
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "New-Item -ItemType Directory -Force -Path 'lib\gdx' | Out-Null;" ^
  "$files = @(" ^
  "  @{ U='https://repo1.maven.org/maven2/com/badlogicgames/gdx/gdx-freetype/1.12.1/gdx-freetype-1.12.1.jar'; D='lib\gdx\gdx-freetype-1.12.1.jar' }," ^
  "  @{ U='https://repo1.maven.org/maven2/com/badlogicgames/gdx/gdx-freetype-platform/1.12.1/gdx-freetype-platform-1.12.1-natives-desktop.jar'; D='lib\gdx\gdx-freetype-platform-1.12.1-natives-desktop.jar' }" ^
  ");" ^
  "foreach ($f in $files) { if (Test-Path $f.D) { Write-Host 'OK:' $f.D } else { Write-Host 'Skachivayu' $f.D; Invoke-WebRequest -Uri $f.U -OutFile $f.D -UseBasicParsing } }"
if errorlevel 1 pause
exit /b %ERRORLEVEL%
