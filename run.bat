@echo off
setlocal
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "OUT=%~dp0out\swing-run"

call "%~dp0compile-swing.bat"
if errorlevel 1 exit /b 1

if not exist "%OUT%\main\java\com\witcher\ui\graphics\GameWindow.class" (
  echo [ОШИБКА] GameWindow.class не найден после сборки
  pause
  exit /b 1
)

echo.
echo Запуск из: %OUT%
"%JAVA_HOME%\bin\java.exe" -Dsun.java2d.uiScale.enabled=false -Dawt.useSystemAAFontSettings=off -Dswing.aatext=false -cp "%OUT%" main.java.com.witcher.ui.graphics.GameWindow
endlocal
