@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "SRC=%~dp0src\main\java"
set "RES=%~dp0src\main\resources"
set "OUT=%~dp0out\swing-run"
set "STAMP_CLASS=%OUT%\main\java\com\witcher\ui\graphics\GameWindow.class"

if not exist "%JAVA_HOME%\bin\javac.exe" (
  echo JDK 17 ne najden: %JAVA_HOME%
  pause
  exit /b 1
)

if not exist "%OUT%" mkdir "%OUT%"

if exist "%STAMP_CLASS%" (
  powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\check-swing-build.ps1" -Src "%SRC%" -Res "%RES%" -StampClass "%STAMP_CLASS%"
  if errorlevel 2 (
    echo === Proverka: ishodniki aktualny, tolko resursy ===
    goto COPY_RESOURCES
  )
  if not errorlevel 1 (
    echo === Proverka: sborka aktualna, propusk ===
    echo Sborka OK: %OUT%
    exit /b 0
  )
)

echo === Kompilyaciya Swing ===
if not exist "%~dp0.tmp" mkdir "%~dp0.tmp"
dir /s /b "%SRC%\*.java" > "%~dp0.tmp\swing-sources.txt"
"%JAVA_HOME%\bin\javac.exe" -encoding UTF-8 -d "%OUT%" @"%~dp0.tmp\swing-sources.txt"
if errorlevel 1 (
  echo Oshibka kompilyacii.
  exit /b 1
)

:COPY_RESOURCES
echo === Kopirovanie resursov ===
if exist "%RES%" (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Copy-Item -Path '%RES%\*' -Destination '%OUT%' -Recurse -Force -ErrorAction SilentlyContinue"
)

echo Sborka OK: %OUT%
exit /b 0
