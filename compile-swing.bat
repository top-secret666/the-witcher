@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "SRC=%~dp0src\main\java\com\witcher"
set "RES=%~dp0src\main\resources"
set "OUT=%~dp0out\swing-run"
set "TMP=%~dp0.tmp"
if not exist "%TMP%" mkdir "%TMP%"

if not exist "%JAVA_HOME%\bin\javac.exe" (
  echo [ОШИБКА] JDK 17 не найден: %JAVA_HOME%
  echo Укажи верный JAVA_HOME в compile-swing.bat
  pause
  exit /b 1
)

if not exist "%OUT%" mkdir "%OUT%"

echo === Компиляция Swing (все .java из src\main\java\com\witcher) ===
dir /s /b "%SRC%\*.java" > "%TMP%\swing-sources.txt"
"%JAVA_HOME%\bin\javac.exe" -encoding UTF-8 -d "%OUT%" @"%TMP%\swing-sources.txt"
if errorlevel 1 (
  echo.
  echo [ОШИБКА] Компиляция не удалась. Исправь ошибки выше.
  pause
  exit /b 1
)

if exist "%RES%" (
  echo === Копирование ресурсов ===
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Copy-Item -Path '%RES%\*' -Destination '%OUT%' -Recurse -Force"
)

echo.
echo Сборка OK: %OUT%
exit /b 0
