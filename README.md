# Java Code Analyzer

Kaynak kodda **metod karmaşıklığı ve risk** (CC, LOC, nesting, hotspot, Türkçe Markdown/JSON). **Call-graph / servis→metod çağrı zinciri bu repoda yok** — etki analizi ve lineage ayrı uygulamada.

Terminal / parse hataları: [scan-error-management.md](scan-error-management.md). Çalıştırma ve `Empty value for --output=`: [docs/standalone-run-tr.md](docs/standalone-run-tr.md). **PMD hattı:** [docs/pmd-scan-tr.md](docs/pmd-scan-tr.md).

Legacy/incremental katman dokümantasyonu: [standalone-java-code-analyzer-dokumantasyon.md](standalone-java-code-analyzer-dokumantasyon.md).

**Incremental cache (parser yolu, varsayılan açık):** `run-analyze.sh` / `run-analyze.cmd` otomatik `--state=analysis-output/analyzer-state.json` gönderir. Detay: [docs/incremental-cache-tr.md](docs/incremental-cache-tr.md).

### JAR (`target/` — repoda yok)

Analyzer kökünde **bir kez** `mvn package` → `target/java-code-analyzer.jar`. Script JAR yoksa analyzer repodaysa otomatik derler. **JDK 17+** gerekir.

## İki ana tarama yolu

| Yol | Komut (Linux/macOS) | Windows (CMD / PowerShell) | Risk profili |
|-----|---------------------|----------------------------|--------------|
| **Parser** (JavaParser, tam metrik) | `./scripts/run-analyze.sh <proje>` | `scripts\run-analyze.cmd <proje>` | `enterprise-java` |
| **PMD-native** (ruleset + PMD AST/metrik, JavaParser yok) | `./scripts/run-pmd-analyze.sh <proje>` | `scripts\run-pmd-analyze.cmd <proje>` | `enterprise-java-pmd` |

**Çıktı klasörü:** `analysis-output/` (analyzer kökünde veya `OUTPUT_DIR`).

| Parser | PMD-native |
|--------|------------|
| `parser-<etiket>-<tarih>.md` + `standalone-….json` | `pmd-<etiket>-<tarih>.md` + `pmd-….json` + `pmd-…-violations.xml` |

Sabit dosya adları: `FIXED_REPORT=1` → `parser-raporu.md` / `pmd-raporu.md`.

Karşılaştırma JSON: `./scripts/run-compare-pipelines.sh <proje>`. Klasik PMD yalnızca ihlal XML: `./scripts/run-classic-pmd.sh <proje>`.

---

## Hızlı başlangıç (Linux / macOS)

```bash
cd java-code-analyzer
mvn package

./scripts/run-analyze.sh /path/to/proje      # parser
./scripts/run-pmd-analyze.sh /path/to/proje  # PMD-native
```

Örnek (bu repoda mock):

```bash
./scripts/run-analyze.sh mock-modules
./scripts/run-pmd-analyze.sh mock-modules
```

---

## Windows

### Proje kökünde analyzer dosyaları (önerilen)

```
D:\projeler\mobil-backend\
  scripts\run-analyze.cmd
  scripts\run-pmd-analyze.cmd
  target\java-code-analyzer.jar
  src\main\java\...
```

PowerShell — **parser:**

```powershell
cd D:\projeler\mobil-backend
$env:LANGUAGE_LEVEL = "JAVA_21"   # isteğe bağlı
.\scripts\run-analyze.cmd
# veya: .\scripts\run-analyze.cmd .\src\main\java
```

PowerShell — **PMD-native:**

```powershell
cd D:\projeler\mobil-backend
$env:LANGUAGE_LEVEL = "JAVA_21"
.\scripts\run-pmd-analyze.cmd
# veya: .\scripts\run-pmd-analyze.cmd .\src\main\java
```

CMD:

```bat
cd D:\projeler\mobil-backend
set LANGUAGE_LEVEL=JAVA_21
scripts\run-pmd-analyze.cmd C:\yol\mobil-backend
```

Rapor: `analysis-output\pmd-….md` ve `pmd-….json` (proje/analyzer kökündeki `OUTPUT_DIR`).

### Analyzer ayrı klasörde (kardeş dizin)

```powershell
cd D:\Developer\java-code-analyzer
mvn package
.\scripts\run-analyze.cmd D:\projeler\mobil-backend
.\scripts\run-pmd-analyze.cmd D:\projeler\mobil-backend
```

---

## Ortak ortam değişkenleri

| Değişken | Açıklama |
|----------|----------|
| `LANGUAGE_LEVEL` | `JAVA_17` (varsayılan), `JAVA_21`, … |
| `OUTPUT_DIR` | Rapor klasörü (varsayılan `analysis-output`) |
| `EXCLUDE_GLOBS` | Varsayılan `**/src/test/**` |
| `INCLUDE_GLOBS` | Örn. `**/src/main/java/**` |
| `REPORT_DETAIL` | `full` (varsayılan) veya `summary` |
| `WORKERS` | Paralel dosya sayısı (`0` = otomatik) |
| `STANDALONE_RISK_PROFILE` | Parser: `enterprise-java`; PMD: `enterprise-java-pmd` |
| `SCAN_PIPELINE` | PMD script: `pmd-native` (varsayılan) veya `parser-hybrid` |

**Linux/macOS örnek:**

```bash
LANGUAGE_LEVEL=JAVA_21 INCLUDE_GLOBS='**/src/main/java/**' \
  ./scripts/run-pmd-analyze.sh ~/projects/mobil-backend
```

**Windows örnek:**

```bat
set LANGUAGE_LEVEL=JAVA_21
set INCLUDE_GLOBS=**/src/main/java/**
scripts\run-pmd-analyze.cmd C:\dev\mobil-backend
```

---

## Büyük monorepo

```bash
INCLUDE_GLOBS='**/src/main/java/**' \
EXCLUDE_GLOBS='**/generated/**,**/build/**' \
./scripts/run-analyze.sh /path/to/monorepo
```

Küçük JSON (metod ağacı yok): `REPORT_DETAIL=summary WORKERS=8 ./scripts/run-analyze.sh`

---

## JDK ve `--language-level`

**Aracı çalıştırmak:** JDK **17+**. **Taranan kaynak:** varsayılan `JAVA_17`; taramadan **önce** `LANGUAGE_LEVEL` / `--language-level` verin.

Mock scriptleri: `run-standalone-mock.sh`, `run-pmd-mock.sh`, `run-classic-pmd-mock.sh`, `run-both-mock.sh`.
