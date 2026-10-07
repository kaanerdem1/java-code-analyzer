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
for %%I in ("%OUTPUT_DIR%") do set "OUTPUT_DIR=%%~fI"
if not defined LANGUAGE_LEVEL set "LANGUAGE_LEVEL=JAVA_17"
REM Varsayilan: zaman damgali rapor (parser-etiket-YYYYMMDD-HHmmss.md). Sabit dosya: set FIXED_REPORT=1
if not defined FIXED_REPORT set "FIXED_REPORT=0"
if "%TIMESTAMP_REPORT%"=="1" set "FIXED_REPORT=0"
if "%TIMESTAMP_REPORT%"=="0" set "FIXED_REPORT=1"

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

call :assign_paths_cmd_fallback

:paths_ok
if not defined JSON (
  echo [UYARI] Rapor yolu uretilemedi; varsayilan analysis-output kullaniliyor.
  set "JSON=%OUTPUT_DIR%\standalone-fallback.json"
  set "MD=%OUTPUT_DIR%\parser-raporu-fallback.md"
)
if "!JSON!"=="" (
  set "JSON=%OUTPUT_DIR%\standalone-fallback.json"
  set "MD=%OUTPUT_DIR%\parser-raporu-fallback.md"
)

for %%I in ("%JSON%") do set "JSON=%%~fI"
for %%I in ("%MD%") do set "MD=%%~fI"

echo [STANDALONE] Output folder: %OUTPUT_DIR%
for %%F in ("%MD%") do echo [STANDALONE] Report file: %%~nxF

set "STATE_ARGS="
if not "%NO_STATE%"=="1" (
  set "STATE_ARGS=--state=%OUTPUT_DIR%\analyzer-state.json"
  if "%FRESH%"=="1" set "STATE_ARGS=!STATE_ARGS! --fresh"
)

set "STANDALONE_OUTPUT=%JSON%"
set "STANDALONE_MARKDOWN=%MD%"
echo [STANDALONE] JSON path: %JSON%
echo [STANDALONE] MD path:   %MD%

pushd "%ROOT%"
java -jar "%JAR%" "--path=%SOURCE%" "--output=%JSON%" "--markdown=%MD%" --top=20 --language-level=%LANGUAGE_LEVEL% !STATE_ARGS!
set "JAVA_EXIT=!ERRORLEVEL!"
popd
if !JAVA_EXIT! neq 0 exit /b !JAVA_EXIT!

set "REPORT_OK=1"
if not exist "%JSON%" set "REPORT_OK=0"
if not exist "%MD%" set "REPORT_OK=0"

if "!REPORT_OK!"=="0" (
  echo.
  echo [HATA] Rapor dosyasi olusturulamadi — script'in yazdigi yol:
  echo   JSON:     %JSON%
  echo   Markdown: %MD%
  echo.
  echo JSON terminale aktiysa --output Java'ya ulasmamis demektir. JAR/script guncel mi? ^(git pull, mvn package^)
  if exist "%SOURCE%\analysis-output\standalone.json" (
    echo [BILGI] Dosya taranan proje altinda: %SOURCE%\analysis-output\
  )
  endlocal
  exit /b 1
)

echo.
echo Done.
echo   Markdown: %MD%
echo   JSON:     %JSON%
echo   VS Code:  analysis-output klasorunu acin ^(%OUTPUT_DIR%^)
dir /b "%JSON%" "%MD%" 2>nul
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
if "!STAMP!"=="unknown" (
  for /f "delims=" %%T in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd-HHmmss" 2^>nul') do (
    if not defined PS_STAMP set "STAMP=%%T" & set "PS_STAMP=1"
  )
)
set "JSON=%OUTPUT_DIR%\standalone-!TAG!-!STAMP!.json"
set "MD=%OUTPUT_DIR%\parser-!TAG!-!STAMP!.md"
exit /b 0
