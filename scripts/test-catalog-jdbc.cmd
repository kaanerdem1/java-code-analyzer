@echo off
setlocal EnableExtensions EnableDelayedExpansion
set "ROOT=%~dp0.."
for %%I in ("%ROOT%") do set "ROOT=%%~fI"
call "%~dp0load-env-catalog.cmd" "%ROOT%"
set "JAR=%ROOT%\target\java-code-analyzer.jar"
if not exist "%JAR%" (
  echo [HATA] JAR yok: mvn package
  exit /b 1
)
java -cp "%JAR%" com.standalone.analyzer.CatalogJdbcPingMain
exit /b %ERRORLEVEL%
