@echo off
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$u='https://repo1.maven.org/maven2/com/badlogicgames/gdx/gdx-jnigen-loader/2.3.1/gdx-jnigen-loader-2.3.1.jar';" ^
  "$d='lib\gdx\gdx-jnigen-loader-2.3.1.jar';" ^
  "New-Item -ItemType Directory -Force -Path 'lib\gdx' | Out-Null;" ^
  "if (Test-Path $d) { Write-Host 'Uzhe est:' $d; exit 0 };" ^
  "Write-Host 'Skachivayu gdx-jnigen-loader...';" ^
  "Invoke-WebRequest -Uri $u -OutFile $d -UseBasicParsing;" ^
  "Write-Host 'Gotovo:' $d"
if errorlevel 1 pause
exit /b %ERRORLEVEL%
