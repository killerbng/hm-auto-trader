@echo off
setlocal
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0BUILD-WINDOWS.ps1"
if errorlevel 1 (
  echo.
  echo BUILD FAILED. Please copy the error from above if you need help.
)
pause
