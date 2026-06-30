@echo off
setlocal
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Java\jdk-17"
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

echo === LibGDX: desktop:run ===
call gradlew.bat desktop:run --no-daemon
endlocal
