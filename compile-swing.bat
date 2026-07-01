@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "SRC=%~dp0src\main\java"
set "RES=%~dp0src\main\resources"
set "OUT=%~dp0out\swing-run"

if not exist "%JAVA_HOME%\bin\javac.exe" (
  echo JDK 17 ne najden: %JAVA_HOME%
  pause
  exit /b 1
)

if not exist "%OUT%" mkdir "%OUT%"

echo === Kompilyaciya Swing ===
dir /s /b "%SRC%\*.java" > "%~dp0.tmp\swing-sources.txt"
"%JAVA_HOME%\bin\javac.exe" -encoding UTF-8 -d "%OUT%" @"%~dp0.tmp\swing-sources.txt"
if errorlevel 1 (
  echo Oshibka kompilyacii.
  pause
  exit /b 1
)

echo === Kopirovanie resursov ===
if exist "%RES%" (
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "Copy-Item -Path '%RES%\*' -Destination '%OUT%' -Recurse -Force -ErrorAction SilentlyContinue"
)

echo Sborka OK: %OUT%
exit /b 0
