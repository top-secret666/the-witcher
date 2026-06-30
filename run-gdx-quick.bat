@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"

set "ROOT=%~dp0"
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "ASSETS_SRC=%ROOT%src\main\resources\assets"
set "ASSETS_OUT=%ROOT%out\production\the-witcher\assets"

if not exist "%JAVA_HOME%\bin\java.exe" (
  echo JDK 17 ne najden: %JAVA_HOME%
  pause
  exit /b 1
)

if not exist "%ROOT%lib\gdx\gdx-jnigen-loader-2.3.1.jar" (
  echo Net gdx-jnigen-loader — zapusti fix-gdx-jnigen.bat
  pause
  exit /b 1
)

if not exist "%ROOT%desktop\build\classes\javac\main\java\com\witcher\desktop\DesktopLauncher.class" (
  echo Net klassov. Zapusti compile-gdx-javac.bat ili run-gdx.bat
  pause
  exit /b 1
)

rem Kak run.bat — копируем ассеты в out (PowerShell, т.к. xcopy может отсутствовать в PATH)
if exist "%ASSETS_SRC%" (
  if not exist "%ASSETS_OUT%" mkdir "%ASSETS_OUT%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "Copy-Item -LiteralPath '%ASSETS_SRC%\*' -Destination '%ASSETS_OUT%' -Recurse -Force -ErrorAction SilentlyContinue"
)

set "GDX_CP=%ROOT%desktop\build\classes\javac;%ROOT%core\build\classes\javac"
for %%J in ("%ROOT%lib\gdx\*.jar") do set "GDX_CP=!GDX_CP!;%%~fJ"

if not exist "%ASSETS_SRC%" (
  echo Net papki: %ASSETS_SRC%
  pause
  exit /b 1
)

echo Zapusk igry (cwd: %ASSETS_SRC%)...
cd /d "%ASSETS_SRC%"
"%JAVA_HOME%\bin\java.exe" -Xms64m -Xmx512m ^
  -Dwitcher.assets="%ASSETS_SRC%" ^
  -cp "!GDX_CP!" main.java.com.witcher.desktop.DesktopLauncher
set ERR=%ERRORLEVEL%

if %ERR% NEQ 0 echo. & echo [OSHIBKA] Kod vyhoda: %ERR%
pause
endlocal
exit /b %ERR%
