@echo off
REM .env.catalog -> ortam degiskenleri (run-analyze.cmd / run-pmd-analyze.cmd)
set "ENV_ROOT=%~1"
if "%ENV_ROOT%"=="" exit /b 0
if not exist "%ENV_ROOT%\.env.catalog" exit /b 0
for /f "usebackq eol=# tokens=1* delims==" %%A in ("%ENV_ROOT%\.env.catalog") do (
  if not "%%~A"=="" set "%%~A=%%B"
)
exit /b 0
