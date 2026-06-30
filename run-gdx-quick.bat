@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"

set "ROOT=%~dp0"
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
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
  echo Net klassov. Zapusti compile-gdx-javac.bat
  pause
  exit /b 1
)

set "GDX_CP=%ROOT%desktop\build\classes\javac;%ROOT%core\build\classes\javac"
for %%J in ("%ROOT%lib\gdx\*.jar") do set "GDX_CP=!GDX_CP!;%%~fJ"

cd /d "%ROOT%src\main\resources\assets"

echo Zapusk igry...
"%JAVA_HOME%\bin\java.exe" -Xms64m -Xmx512m -cp "!GDX_CP!" main.java.com.witcher.desktop.DesktopLauncher
set ERR=%ERRORLEVEL%

cd /d "%ROOT%"
if %ERR% NEQ 0 echo. & echo [OSHIBKA] Kod vyhoda: %ERR%
pause
endlocal
exit /b %ERR%
