@echo off
setlocal EnableExtensions EnableDelayedExpansion
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

where java >nul 2>&1
if errorlevel 1 (
  echo [HATA] java komutu bulunamadi. JDK 17+ kurun ve PATH'e ekleyin.
  exit /b 1
)

if not exist "%JAR%" (
  if exist "%ROOT%\pom.xml" (
    echo [STANDALONE] JAR yok, analyzer derleniyor ^(mvn package^)...
    if not defined JAVA_HOME (
      echo [UYARI] JAVA_HOME tanimli degil. Maven bazen hata verir; asagidaki adimlara bakin.
    )
    pushd "%ROOT%"
    call mvn package
    if errorlevel 1 (
      echo.
      echo [HATA] mvn package basarisiz. Sik nedenler:
      echo   - JAVA_HOME yok veya yanlis ^(JDK 17 klasoru olmali, JRE degil^)
      echo   - Maven yok ^(where mvn^)
      echo Manuel: set JAVA_HOME=C:\Program Files\Java\jdk-17
      echo          set PATH=%%JAVA_HOME%%\bin;%%PATH%%
      echo          cd /d "%ROOT%" ^&^& mvn package
      popd
      exit /b 1
    )
    popd
    set "JAR=%ROOT%\target\java-code-analyzer.jar"
  ) else (
    echo [HATA] java-code-analyzer.jar bulunamadi: target\
    echo        mvn package veya JAR kopyalayin
    exit /b 1
  )
)

if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

set "JSON="
set "MD="

if "%FIXED_REPORT%"=="1" (
  set "JSON=%OUTPUT_DIR%\standalone.json"
  set "MD=%OUTPUT_DIR%\parser-raporu.md"
  goto :paths_ok
)

REM PowerShell: yollar ortam degiskeninden (tırnak/path kaçış hatası azalır)
set "SOURCE_FULL=%SOURCE%"
set "OUTPUT_DIR_FULL=%OUTPUT_DIR%"
for /f "usebackq tokens=1,2 delims=|" %%a in (`powershell -NoProfile -ExecutionPolicy Bypass -Command "$src=$env:SOURCE_FULL; $out=$env:OUTPUT_DIR_FULL; $tag=$env:REPORT_TAG; if (-not $tag) { $tag=(Split-Path $src -Leaf) }; if ([string]::IsNullOrWhiteSpace($tag)) { $tag='scan' }; $tag=$tag.ToLower() -replace '[^a-z0-9._-]','-'; if ($tag.Length -gt 48) { $tag=$tag.Substring(0,48) }; $stamp=Get-Date -Format 'yyyyMMdd-HHmmss'; Write-Output ($out + '\standalone-' + $tag + '-' + $stamp + '.json|' + $out + '\parser-' + $tag + '-' + $stamp + '.md')"`) do (
  set "JSON=%%a"
  set "MD=%%b"
)

if not defined JSON call :assign_paths_cmd_fallback
if not defined MD call :assign_paths_cmd_fallback

:paths_ok
if not defined JSON (
  echo [UYARI] Rapor yolu uretilemedi; varsayilan analysis-output kullaniliyor.
  set "JSON=%OUTPUT_DIR%\standalone-fallback.json"
  set "MD=%OUTPUT_DIR%\parser-raporu-fallback.md"
)

for %%F in ("%MD%") do echo [STANDALONE] Report file: %%~nxF

set "STATE_ARGS="
if not "%NO_STATE%"=="1" (
  set "STATE_ARGS=--state=%OUTPUT_DIR%\analyzer-state.json"
  if "%FRESH%"=="1" set "STATE_ARGS=!STATE_ARGS! --fresh"
)

java -jar "%JAR%" --path="%SOURCE%" --output="%JSON%" --markdown="%MD%" --top=20 --language-level=%LANGUAGE_LEVEL% !STATE_ARGS!
if errorlevel 1 exit /b 1

echo.
echo Done.
echo   Markdown: %MD%
echo   JSON:     %JSON%
endlocal
exit /b 0

:assign_paths_cmd_fallback
for %%I in ("%SOURCE%") do set "TAG=%%~nxI"
if defined REPORT_TAG set "TAG=%REPORT_TAG%"
if not defined TAG set "TAG=scan"
set "STAMP=unknown"
for /f "skip=1 tokens=1" %%T in ('wmic os get localdatetime 2^>nul') do (
  if not defined WMIC_DT set "WMIC_DT=%%T"
)
if defined WMIC_DT set "STAMP=!WMIC_DT:~0,8!-!WMIC_DT:~8,4!"
set "JSON=%OUTPUT_DIR%\standalone-!TAG!-!STAMP!.json"
set "MD=%OUTPUT_DIR%\parser-!TAG!-!STAMP!.md"
exit /b 0
