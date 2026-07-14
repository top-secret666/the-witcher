@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "ROOT=%~dp0"

call "%~dp0compile-swing-hybrid.bat"
if errorlevel 1 exit /b 1

set "BIN=%ROOT%out\swing-run"
set "ASSETS=%ROOT%src\main\resources\assets"

if not exist "%BIN%\main\java\com\witcher\ui\graphics\GameWindow.class" (
  echo Net GameWindow.class v %BIN%
  pause
  exit /b 1
)

set "CP=%BIN%"
for %%J in ("%ROOT%lib\gdx\*.jar") do set "CP=!CP!;%%~fJ"

echo Zapusk Swing+GDX icons iz: %BIN%
"%JAVA_HOME%\bin\java.exe" -Xms128m -Xmx768m ^
  -Dwitcher.assets="%ASSETS%" ^
  -cp "!CP!" main.java.com.witcher.ui.graphics.GameWindow
endlocal
