@echo off
setlocal
cd /d "%~dp0"
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
echo === Tolko sborka LibGDX ===
call gradlew.bat desktop:build --no-daemon
if errorlevel 1 (
  echo Sborka ne udalas.
) else (
  echo Sborka OK.
)
pause
endlocal
