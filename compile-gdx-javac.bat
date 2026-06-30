@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "TMP=%~dp0.tmp"
set "TEMP=%~dp0.tmp"
if not exist "%TMP%" mkdir "%TMP%"

if not exist "%JAVA_HOME%\bin\javac.exe" (
  echo JDK 17 ne najden: %JAVA_HOME%
  pause
  exit /b 1
)

if not exist "lib\gdx\gdx-1.12.1.jar" (
  echo === Pervyj zapusk: skachivayu LibGDX ===
  powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup-gdx-libs.ps1"
  if errorlevel 1 (
    echo Oshibka skachivaniya bibliotek.
    pause
    exit /b 1
  )
)

if not exist "lib\gdx\gdx-jnigen-loader-2.3.1.jar" (
  echo === Dokachivayu gdx-jnigen-loader ===
  call "%~dp0fix-gdx-jnigen.bat"
  if errorlevel 1 exit /b 1
)

if not exist "lib\gdx\gdx-freetype-platform-1.12.1-natives-desktop.jar" (
  echo === Dokachivayu gdx-freetype ===
  call "%~dp0fix-gdx-freetype.bat"
  if errorlevel 1 exit /b 1
)

set "LIB_CP="
for %%J in ("%~dp0lib\gdx\*.jar") do (
  if defined LIB_CP (set "LIB_CP=!LIB_CP!;%%~fJ") else (set "LIB_CP=%%~fJ")
)

set "OUT_CORE=%~dp0core\build\classes\javac"
set "OUT_DESK=%~dp0desktop\build\classes\javac"
if not exist "%OUT_CORE%" mkdir "%OUT_CORE%"
if not exist "%OUT_DESK%" mkdir "%OUT_DESK%"

echo === Kompilyaciya core ===
dir /s /b "%~dp0core\src\main\java\*.java" > "%TMP%\gdx-sources-core.txt"
"%JAVA_HOME%\bin\javac.exe" -encoding UTF-8 -cp "%LIB_CP%" -d "%OUT_CORE%" @"%TMP%\gdx-sources-core.txt"
if errorlevel 1 goto :fail

echo === Kompilyaciya desktop ===
dir /s /b "%~dp0desktop\src\main\java\*.java" > "%TMP%\gdx-sources-desktop.txt"
"%JAVA_HOME%\bin\javac.exe" -encoding UTF-8 -cp "%LIB_CP%;%OUT_CORE%" -d "%OUT_DESK%" @"%TMP%\gdx-sources-desktop.txt"
if errorlevel 1 goto :fail

echo.
echo Sborka OK (bez Gradle).
echo Zapusk: run-gdx-quick.bat
exit /b 0

:fail
echo.
echo [OSHIBKA] Kompilyaciya ne udalas.
pause
exit /b 1
