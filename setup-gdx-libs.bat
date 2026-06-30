@echo off
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup-gdx-libs.ps1"
if errorlevel 1 pause
exit /b %ERRORLEVEL%
