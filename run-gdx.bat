@echo off
setlocal
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "TMP=%~dp0.tmp"
set "TEMP=%~dp0.tmp"
if not exist "%TMP%" mkdir "%TMP%"

echo === LibGDX: sborka cherez javac (bez Gradle) ===
call "%~dp0compile-gdx-javac.bat"
if errorlevel 1 exit /b 1

echo.
call "%~dp0run-gdx-quick.bat"
endlocal
