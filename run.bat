@echo off
setlocal
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Java\jdk-17"

call "%~dp0compile-swing.bat"
if errorlevel 1 exit /b 1

set "BIN=%~dp0out\swing-run"

if not exist "%BIN%\main\java\com\witcher\ui\graphics\GameWindow.class" (
  echo Net GameWindow.class v %BIN%
  pause
  exit /b 1
)

echo Zapusk iz: %BIN%
"%JAVA_HOME%\bin\java.exe" -cp "%BIN%" main.java.com.witcher.ui.graphics.GameWindow
endlocal
