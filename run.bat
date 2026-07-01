@echo off
setlocal
cd /d "%~dp0"

set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "RES=%~dp0src\main\resources"
set "BIN="

if exist "%~dp0out\production\the-witcher\main\java\com\witcher\ui\graphics\GameWindow.class" (
  set "BIN=%~dp0out\production\the-witcher"
)
if not defined BIN if exist "%APPDATA%\Code\User\workspaceStorage\fae98f21f2fe38732848cb82e502ebb5\redhat.java\jdt_ws\the-witcher_71a45e8b\bin\main\java\com\witcher\ui\graphics\GameWindow.class" (
  set "BIN=%APPDATA%\Code\User\workspaceStorage\fae98f21f2fe38732848cb82e502ebb5\redhat.java\jdt_ws\the-witcher_71a45e8b\bin"
)

if not defined BIN (
  echo Не найден скомпилированный GameWindow.class
  echo Собери проект: Ctrl+Shift+B в VS Code
  pause
  exit /b 1
)

if exist "%RES%" (
  xcopy /E /I /Y "%RES%\*" "%BIN%\" >nul
)

echo Запуск из: %BIN%
"%JAVA_HOME%\bin\java.exe" -Dsun.java2d.uiScale.enabled=false -Dsun.java2d.dpiaware=true -Dsun.java2d.d3d=false -Dsun.java2d.opengl=false -Dawt.useSystemAAFontSettings=off -Dswing.aatext=false -cp "%BIN%" main.java.com.witcher.ui.graphics.GameWindow
endlocal
