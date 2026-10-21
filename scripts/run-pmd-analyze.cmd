@echo off
setlocal EnableExtensions EnableDelayedExpansion
REM PMD-native tarama (JavaParser yok): ruleset + PMD metrik + enterprise-java-pmd risk.
REM   scripts\run-pmd-analyze.cmd C:\dev\mobil-backend
REM Argüman yoksa bulunduğunuz klasör taranır.
REM Hibrit (PMD ruleset + JavaParser metrik): set SCAN_PIPELINE=parser-hybrid

set "SOURCE_DIR=%~1"
if "%SOURCE_DIR%"=="" set "SOURCE_DIR=."

for %%I in ("%SOURCE_DIR%") do set "SOURCE=%%~fI"

set "ROOT=%~dp0.."
for %%I in ("%ROOT%") do set "ROOT=%%~fI"

if not defined OUTPUT_DIR set "OUTPUT_DIR=%ROOT%\analysis-output"
for %%I in ("%OUTPUT_DIR%") do set "OUTPUT_DIR=%%~fI"
if not defined LANGUAGE_LEVEL set "LANGUAGE_LEVEL=JAVA_17"
if not defined SCAN_PIPELINE set "SCAN_PIPELINE=pmd-native"
if not defined STANDALONE_RISK_PROFILE set "STANDALONE_RISK_PROFILE=enterprise-java-pmd"
if not defined FIXED_REPORT set "FIXED_REPORT=0"
if "%TIMESTAMP_REPORT%"=="1" set "FIXED_REPORT=0"
if "%TIMESTAMP_REPORT%"=="0" set "FIXED_REPORT=1"

set "JAR=%ANALYZER_JAR%"
if not defined JAR set "JAR=%ROOT%\target\java-code-analyzer.jar"

where java >nul 2>&1
if errorlevel 1 (
  echo [HATA] java komutu bulunamadi. JDK 17+ kurun.
  exit /b 1
)

if not exist "%JAR%" (
  if exist "%ROOT%\pom.xml" (
    echo [PMD-SCAN] JAR yok, mvn package...
    pushd "%ROOT%"
    call mvn -q package
    if errorlevel 1 (
      popd
      exit /b 1
    )
    popd
    set "JAR=%ROOT%\target\java-code-analyzer.jar"
  ) else (
    echo [HATA] java-code-analyzer.jar bulunamadi.
    exit /b 1
  )
)

if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

call :assign_pmd_paths

for %%I in ("%PMD_JSON%") do set "PMD_JSON=%%~fI"
for %%I in ("%PMD_MD%") do set "PMD_MD=%%~fI"

echo [PMD-SCAN] Taraniyor: %SOURCE%
echo [PMD-SCAN] pipeline=%SCAN_PIPELINE%
echo [PMD-SCAN] Rapor: %PMD_MD%

set "EXTRA="
if not "%WORKERS%"=="" if not "%WORKERS%"=="0" set "EXTRA=!EXTRA! --workers=%WORKERS%"
if /I "%REPORT_DETAIL%"=="summary" set "EXTRA=!EXTRA! --detail=summary"
if defined INCLUDE_GLOBS set "EXTRA=!EXTRA! --include=%INCLUDE_GLOBS%"
if not defined EXCLUDE_GLOBS set "EXCLUDE_GLOBS=**/src/test/**"
if defined EXCLUDE_GLOBS set "EXTRA=!EXTRA! --exclude=%EXCLUDE_GLOBS%"

pushd "%ROOT%"
java -cp "%JAR%" com.standalone.analyzer.PmdScanMain ^
  "--path=%SOURCE%" ^
  "--output=%PMD_JSON%" ^
  "--markdown=%PMD_MD%" ^
  --top=20 ^
  "--pipeline=%SCAN_PIPELINE%" ^
  "--risk-profile=%STANDALONE_RISK_PROFILE%" ^
  "--language-level=%LANGUAGE_LEVEL%" ^
  !EXTRA!
set "JAVA_EXIT=!ERRORLEVEL!"
popd
if !JAVA_EXIT! neq 0 exit /b !JAVA_EXIT!

if not exist "%PMD_JSON%" (
  echo [HATA] JSON olusturulamadi: %PMD_JSON%
  exit /b 1
)
if not exist "%PMD_MD%" (
  echo [HATA] Markdown olusturulamadi: %PMD_MD%
  exit /b 1
)

echo.
echo Done.
echo   Markdown: %PMD_MD%
echo   JSON:     %PMD_JSON%
endlocal
exit /b 0

:assign_pmd_paths
for %%I in ("%SOURCE%") do set "TAG=%%~nxI"
if defined REPORT_TAG set "TAG=%REPORT_TAG%"
if not defined TAG set "TAG=scan"
set "STAMP=unknown"
for /f "skip=1 tokens=1" %%T in ('wmic os get localdatetime 2^>nul') do (
  if not defined WMIC_DT set "WMIC_DT=%%T"
)
if defined WMIC_DT set "STAMP=!WMIC_DT:~0,8!-!WMIC_DT:~8,4!"
if "!STAMP!"=="unknown" (
  for /f "delims=" %%T in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd-HHmmss" 2^>nul') do (
    if not defined PS_STAMP set "STAMP=%%T" & set "PS_STAMP=1"
  )
)
if "%FIXED_REPORT%"=="1" (
  set "PMD_JSON=%OUTPUT_DIR%\pmd-standalone.json"
  set "PMD_MD=%OUTPUT_DIR%\pmd-raporu.md"
) else (
  set "PMD_JSON=%OUTPUT_DIR%\pmd-!TAG!-!STAMP!.json"
  set "PMD_MD=%OUTPUT_DIR%\pmd-!TAG!-!STAMP!.md"
)
exit /b 0
