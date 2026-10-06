# Standalone analyzer — çalıştırma ve `Empty value for --output=` hatası

JAR: `target/java-code-analyzer.jar` (bir kez `mvn package`). JDK **17+** gerekir.

## Çıktılar nereye gider?

`run-analyze` script’i kullanıldığında (varsayılan):

| Dosya | Konum |
|--------|--------|
| Markdown rapor | `{proje_kökü}/analysis-output/parser-{etiket}-{YYYYMMDD-HHmmss}.md` |
| JSON | `{proje_kökü}/analysis-output/standalone-{etiket}-{YYYYMMDD-HHmmss}.json` |
| Incremental cache | `{proje_kökü}/analysis-output/analyzer-state.json` |

`{proje_kökü}` = script’i çalıştırdığın dizin veya `--path` ile verdiğin proje.

`OUTPUT_DIR=C:\...\raporlar` ile klasör değiştirilir. `FIXED_REPORT=1` → `parser-raporu.md` / `standalone.json`.

---

## Kısa çalıştırma

### A) Analyzer repoda (`java-code-analyzer`)

Hedef projeyi **dışarıdan** path ile tara; rapor **analyzer kökünde** `analysis-output/` altında oluşur.

**Windows (proje kökünde):**

```powershell
cd C:\...\java-code-analyzer
mvn package
.\scripts\run-analyze.cmd C:\...\hedef-proje
```

**Mac / Linux:**

```bash
cd ~/Desktop/java-code-analyzer
mvn package
./scripts/run-analyze.sh /path/hedef-proje
```

### B) Hedef proje kökünde (`scripts/` + `target/java-code-analyzer.jar` kopyalı)

Rapor **hedef projenin içinde** `analysis-output/` altında oluşur.

**Windows:**

```powershell
cd C:\...\mobil-backend
.\scripts\run-analyze.cmd
```

Sadece kaynak:

```powershell
.\scripts\run-analyze.cmd .\src\main\java
```

**Mac / Linux:**

```bash
cd ~/projects/mobil-backend
./scripts/run-analyze.sh
./scripts/run-analyze.sh ./src/main/java
```

**Taşınacak kit:** `scripts/` klasörü **komple** + yalnızca `target/java-code-analyzer.jar` (tüm `target/` değil).

---

## `Empty value for --output=` ne demek?

Eski JAR’larda CLI, `--output=` sonrasında **boş** yol görünce dururdu. Güncel sürümde boş `--output=` / `--markdown=` **yok sayılır** ve `{taranan_kök}/analysis-output/standalone.json` + `parser-raporu.md` kullanılır; `run-analyze.cmd` ayrıca PowerShell başarısız olursa **CMD yedek yolu** üretir.

Yine de hata görürsen: **JAR’ı yenile** (`mvn package` → `target/java-code-analyzer.jar` kopyala).

### PowerShell’de görünüyor, Explorer’da `analysis-output` boş

1. **İki farklı klasör:** Raporlar analyzer kökünde `…\java-code-analyzer\analysis-output\` olur; taranan proje içinde aramayın (veya tam tersi — kit hedef projede ise rapor **hedefte**).
2. **`--output=` boş gitti:** Eski `run-analyze.cmd` bazen `%JSON%` / `%MD%` boş bırakırdı; JAR dosyayı **`--path` (taranan proje)\analysis-output\`** altına yazar, script başka yol yazar. Güncel script bunu düzeltir; yine de konsoldaki **`Output folder:`** ve **`Done.`** satırındaki **tam yolu** Explorer’da açın.
3. **Zaman damgalı isim:** `parser-raporu.md` yok; `parser-etiket-20261006-143022.md` arayın veya `set FIXED_REPORT=1`.
4. Terminalde gördüğünüz metin **konsol çıktısı** olabilir; dosya için `[STANDALONE] Report written to` / `Readable Markdown` satırındaki yolu `Test-Path` ile doğrulayın.
5. **JSON ekrana akıyorsa** `--output` Java’ya ulaşmamış demektir. Güncel `run-analyze.cmd` `STANDALONE_OUTPUT` / `STANDALONE_MARKDOWN` ortam değişkenlerini de set eder; tarama başında `[STANDALONE] JSON hedef:` satırına bakın. `stdout` yazıyorsa `git pull` + `mvn package` ile JAR/script yenileyin.

Sık nedenler:

- Elle `java -jar ... --output=` veya `--output` değeri unutulmuş
- Windows CMD: `%JSON%` / `%MD%` set edilmeden `java` çalışmış (PowerShell ad adımı boş kalmış)
- `--path="..." --output=...` tek argümanda birleşmiş (ayrıntı: [scan-error-management.md](../scan-error-management.md))

**Önce dene:** Script ile çalıştır; elle JAR satırı yazma.

---

## Hata tekrarlarsa — PowerShell (hedef proje kökü)

### 1) Script + sabit çıktı klasörü

```powershell
cd C:\...\mobil-backend
$env:OUTPUT_DIR = "$((Get-Location).Path)\analysis-output"
.\scripts\run-analyze.cmd
```

### 2) Script yok / değişken yine boş — yolları elle ver

```powershell
cd C:\...\mobil-backend
New-Item -ItemType Directory -Force -Path .\analysis-output | Out-Null

$Proje = (Get-Location).Path
$Jar   = Join-Path $Proje "target\java-code-analyzer.jar"
$Stamp = Get-Date -Format "yyyyMMdd-HHmmss"
$Json  = Join-Path $Proje "analysis-output\standalone-$Stamp.json"
$Md    = Join-Path $Proje "analysis-output\parser-$Stamp.md"
$State = Join-Path $Proje "analysis-output\analyzer-state.json"

# Boş olmamalı:
$Json; $Md

java -jar $Jar `
  "--path=$Proje" `
  "--output=$Json" `
  "--markdown=$Md" `
  "--state=$State" `
  --risk-profile=enterprise-java `
  --language-level=JAVA_17 `
  --top=20
```

Sadece `src\main\java`:

```powershell
$Src = Join-Path $Proje "src\main\java"
java -jar $Jar "--path=$Src" "--output=$Json" "--markdown=$Md" "--state=$State" `
  --risk-profile=enterprise-java --language-level=JAVA_17 --top=20
```

İlk taramada cache okumadan:

```powershell
java -jar $Jar "--path=$Proje" "--output=$Json" "--markdown=$Md" --fresh `
  --risk-profile=enterprise-java --language-level=JAVA_17 --top=20
```

(`--state=` verme veya `--fresh` kullan.)

### 3) Kontrol

```powershell
Write-Host "Jar=$Jar"
Write-Host "Json=$Json"
Write-Host "Md=$Md"
Test-Path $Jar
```

---

## İlgili dokümanlar

- [README.md](../README.md) — genel kurulum ve seçenekler
- [scan-error-management.md](../scan-error-management.md) — parse/CLI hataları, Windows tırnak birleşmesi
- [incremental-cache-tr.md](incremental-cache-tr.md) — `--state`, `--fresh`, `NO_STATE`
