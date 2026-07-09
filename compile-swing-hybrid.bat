@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "SRC=%~dp0src\main\java"
set "RES=%~dp0src\main\resources"
set "OUT=%~dp0out\swing-run"
set "CORE=%~dp0core\src\main\java"
set "TMP=%~dp0.tmp"
set "STAMP_CLASS=%OUT%\main\java\com\witcher\gdx\bridge\HybridShopIcons.class"

if not exist "%JAVA_HOME%\bin\javac.exe" (
  echo JDK 17 ne najden: %JAVA_HOME%
  pause
  exit /b 1
)

if not exist "lib\gdx\gdx-1.12.1.jar" (
  echo === Pervyj zapusk: skachivayu LibGDX ===
  powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup-gdx-libs.ps1"
  if errorlevel 1 exit /b 1
)

set "LIB_CP="
for %%J in ("%~dp0lib\gdx\*.jar") do (
  if defined LIB_CP (set "LIB_CP=!LIB_CP!;%%~fJ") else (set "LIB_CP=%%~fJ")
)

if not exist "%OUT%" mkdir "%OUT%"
if not exist "%TMP%" mkdir "%TMP%"

echo === Kompilyaciya Swing (shared + UI) ===
dir /s /b "%SRC%\*.java" > "%TMP%\swing-sources.txt"
"%JAVA_HOME%\bin\javac.exe" -encoding UTF-8 -d "%OUT%" @"%TMP%\swing-sources.txt"
if errorlevel 1 (
  echo Oshibka kompilyacii Swing.
  exit /b 1
)

echo === Kompilyaciya GDX bridge (ikonki) ===
(
  echo %CORE%\main\java\com\witcher\gdx\bridge\DelegatingShopIcons.java
  echo %CORE%\main\java\com\witcher\gdx\bridge\GdxIconBaker.java
  echo %CORE%\main\java\com\witcher\gdx\bridge\GdxIconBakeSession.java
  echo %CORE%\main\java\com\witcher\gdx\bridge\GdxBakedArmourIconRegistry.java
  echo %CORE%\main\java\com\witcher\gdx\bridge\HybridShopIcons.java
  echo %CORE%\main\java\com\witcher\gdx\graphics\PixelTextures.java
  echo %CORE%\main\java\com\witcher\gdx\graphics\RenderQuality.java
  echo %CORE%\main\java\com\witcher\gdx\graphics\GdxTextureBridge.java
) > "%TMP%\swing-gdx-bridge.txt"
"%JAVA_HOME%\bin\javac.exe" -encoding UTF-8 -cp "%LIB_CP%;%OUT%" -d "%OUT%" @"%TMP%\swing-gdx-bridge.txt"
if errorlevel 1 (
  echo Oshibka kompilyacii GDX bridge.
  exit /b 1
)

echo === Kopirovanie resursov ===
if exist "%RES%" (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Copy-Item -Path '%RES%\*' -Destination '%OUT%' -Recurse -Force -ErrorAction SilentlyContinue"
)

echo Sborka OK (hybrid): %OUT%
exit /b 0
