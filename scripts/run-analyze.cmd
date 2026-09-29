@echo off
setlocal EnableExtensions
REM Windows (CMD / VS Code terminal): gerçek proje yolunu argüman verin.
REM   scripts\run-analyze.cmd C:\dev\mobil-backend
REM Argüman yoksa bulunduğunuz klasör taranır.

set "SOURCE_DIR=%~1"
if "%SOURCE_DIR%"=="" set "SOURCE_DIR=."

for %%I in ("%SOURCE_DIR%") do set "SOURCE=%%~fI"

set "ROOT=%~dp0.."
for %%I in ("%ROOT%") do set "ROOT=%%~fI"

if not defined OUTPUT_DIR set "OUTPUT_DIR=%ROOT%\analysis-output"
if not defined LANGUAGE_LEVEL set "LANGUAGE_LEVEL=JAVA_17"

set "JAR=%ANALYZER_JAR%"
if not defined JAR set "JAR=%ROOT%\target\java-code-analyzer.jar"

if not exist "%JAR%" (
  if exist "%ROOT%\pom.xml" (
    echo [STANDALONE] JAR yok, analyzer derleniyor ^(mvn package^)...
    pushd "%ROOT%"
    call mvn -q package
    if errorlevel 1 exit /b 1
    popd
    set "JAR=%ROOT%\target\java-code-analyzer.jar"
  ) else (
    echo [HATA] java-code-analyzer.jar bulunamadi: %JAR%
    echo        Once analyzer repoda: mvn package
    exit /b 1
  )
)

if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

set "JSON=%OUTPUT_DIR%\standalone.json"
set "MD=%OUTPUT_DIR%\parser-raporu.md"

java -jar "%JAR%" --path="%SOURCE%" --output="%JSON%" --markdown="%MD%" --top=20 --language-level=%LANGUAGE_LEVEL%
if errorlevel 1 exit /b 1

echo.
echo Bitti.
echo   Markdown: %MD%
echo   JSON:     %JSON%
endlocal
