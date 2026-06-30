@echo off
setlocal
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Java\jdk-17"
rem Gradle cache and Java temp on D: (C: drive is full)
set "GRADLE_USER_HOME=%~dp0.gradle-user-home"
set "TMP=%~dp0.tmp"
set "TEMP=%~dp0.tmp"
set "JAVA_OPTS=-Djava.io.tmpdir=%~dp0.tmp"
if not exist "%GRADLE_USER_HOME%" mkdir "%GRADLE_USER_HOME%"
if not exist "%TMP%" mkdir "%TMP%"
echo Gradle cache: %GRADLE_USER_HOME%
echo Java temp:   %TMP%
if not exist "%JAVA_HOME%\bin\java.exe" (
  echo JDK 17 ne najden: %JAVA_HOME%
  echo Ustanovi JDK ili poprav JAVA_HOME v etom fajle.
  pause
  exit /b 1
)

if not exist "gradlew.bat" (
  echo gradlew.bat ne najden. Zapusti iz kornya proekta.
  pause
  exit /b 1
)

echo === LibGDX: sborka ===
call gradlew.bat desktop:build --no-daemon
if errorlevel 1 (
  echo.
  echo [OSHIBKA] Sborka ne udalas. Smotri tekst vyshe.
  pause
  exit /b 1
)

echo.
echo === LibGDX: zapusk ===
call gradlew.bat desktop:run --no-daemon
if errorlevel 1 (
  echo.
  echo [OSHIBKA] Igra vyletela ili ne zapustilas. Smotri tekst vyshe.
  pause
  exit /b 1
)

echo.
echo Igra zakrylas normalno.
pause
endlocal
