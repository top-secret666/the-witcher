@echo off
setlocal
cd /d "%~dp0"
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "GRADLE_USER_HOME=%~dp0.gradle-user-home"
set "TMP=%~dp0.tmp"
set "TEMP=%~dp0.tmp"
set "JAVA_OPTS=-Djava.io.tmpdir=%~dp0.tmp"
if not exist "%GRADLE_USER_HOME%" mkdir "%GRADLE_USER_HOME%"
if not exist "%TMP%" mkdir "%TMP%"
echo Gradle cache: %GRADLE_USER_HOME%
echo === Tolko sborka LibGDX ===
call gradlew.bat desktop:build --no-daemon
if errorlevel 1 (
  echo Sborka ne udalas.
) else (
  echo Sborka OK.
)
pause
endlocal
