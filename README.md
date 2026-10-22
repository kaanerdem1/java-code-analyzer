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

## Pull sonrası — PostgreSQL katalog (özet)

Metod risk kayıtları `env.method_risk_analyze`, benzerlik grupları `env.method_similarity` tablolarına gider. **`CATALOG_JDBC_URL` tanımlıysa** tarama bitince otomatik yazılır.

**`.env` dosyası yok.** Repoda yalnızca şablon vardır: [`.env.catalog.example`](.env.catalog.example). Bağlantı için ya bu dosyayı kopyalarsınız ya da aynı değişkenleri terminalde `export` edersiniz — ikisi de aynı iş.

| Kavram | Ne? |
|--------|-----|
| `.env.catalog` | **Senin oluşturduğun** yerel dosya (repo kökü). Git’e girmez (`.gitignore`). Script varsa `source` eder. |
| `.env.catalog.example` | Commit’li örnek; `cp .env.catalog.example .env.catalog` ile kopyala, şifreyi doldur. |
| `CATALOG_SCHEMA=env` | PostgreSQL’deki **şema adı** (`env.method_risk_analyze`). Dotenv dosyası değil. pgAdmin’de `Schemas → env`. |
| `inventory_db` | Veritabanı **adı** (JDBC URL’de `/inventory_db`). Envanter + katalog tabloları burada. |

Dosya kullanmadan tek seferlik tarama:

```bash
export CATALOG_JDBC_URL='jdbc:postgresql://localhost:5432/inventory_db'
export PGUSER=kaanerdem
export PGPASSWORD='...'
export CATALOG_SCHEMA=env
./scripts/run-analyze.sh /path/to/proje
```

**Ekip kısa rehber:** [docs/db-baglanti-tr.md](docs/db-baglanti-tr.md).  
Ayrıntı: [docs/catalog-db-tr.md](docs/catalog-db-tr.md), parser/PMD sorguları: [docs/catalog-queries-parser-pmd-tr.md](docs/catalog-queries-parser-pmd-tr.md), envanter bağlantısı: [docs/catalog-env-inventory-link-tr.md](docs/catalog-env-inventory-link-tr.md).

DB yazımı istemiyorsanız: `CATALOG_JDBC_URL` vermeyin, `.env.catalog` oluşturmayın veya `--no-catalog-write`.

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
| `CATALOG_JDBC_URL` | Tanımlıysa tarama sonrası PostgreSQL katalog yazımı (JDBC) |
| `CATALOG_SCHEMA` | Katalog şeması (varsayılan `env`) |
| `CATALOG_SCAN_RUN` | Aynı koşu UUID (parser + PMD satırlarını eşleştirmek için) |
| `PGUSER` / `PGPASSWORD` | Katalog DB kimlik bilgisi (`.env.catalog`) |

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

Mock-modules: `./scripts/run-analyze.sh mock-modules` ve `./scripts/run-pmd-analyze.sh mock-modules`.

---

## PostgreSQL katalog — ekip checklist (adım adım)

Tam liste: **[docs/db-baglanti-tr.md](docs/db-baglanti-tr.md)**. Özet tablo:

Kurumsal kaynak + dolu `env.java_method` envanteri olan makine için. Mock-modules ile envantere bağ (`java_method_id`) genelde **olmaz**; gerçek proje path’i kullanın.

| Adım | Ne zaman | Komut / iş |
|------|----------|------------|
| 1 | Bir kez (ekip aynı commit) | `git pull` — analyzer repo güncel |
| 2 | Bir kez / şema değişince | `psql -h HOST -U USER -d inventory_db -f scripts/catalog-schema.sql` — tablolar + `java_method_id` FK |
| 3 | Bir kez (view + eski satır backfill) | `psql -d inventory_db -f scripts/catalog-inventory-link.sql` — isteğe bağlı tekrar |
| 4 | Makinede bir kez | `cp .env.catalog.example .env.catalog` → `CATALOG_JDBC_URL`, `PGUSER`, `PGPASSWORD`, `CATALOG_SCHEMA=env` doldur **veya** aynı değişkenleri `export` et |
| 5 | Tarama öncesi | `mvn package` |
| 6 | Her parser+PMD koşusu | Aşağıdaki `CATALOG_SCAN_RUN` export’u — aynı UUID ile parser + PMD |
| 7 | Her tarama | `./scripts/run-analyze.sh /path/to/kurumsal/kaynak` |
| 8 | Aynı `CATALOG_SCAN_RUN` ile | `./scripts/run-pmd-analyze.sh /path/to/kurumsal/kaynak` |

Adım 6 için (macOS/Linux):

```bash
export CATALOG_SCAN_RUN=$(uuidgen | tr '[:upper:]' '[:lower:]')
```

**Beklenen:** Konsolda `[CATALOG] PostgreSQL: N satır yazıldı | scan_run_id=...`

**Doğrulama (pgAdmin / psql):**

```sql
SELECT COUNT(*) AS satir, COUNT(java_method_id) AS envantere_bagli
FROM env.method_risk_analyze
WHERE scan_run_id = 'SCAN-RUN-UUID-BURAYA'::uuid;
```

Envanter servisleri ile risk:

```sql
SELECT service_name, fqcn, risk_score, java_method_id
FROM env.v_method_risk_with_service
WHERE scan_run_id = 'SCAN-RUN-UUID-BURAYA'::uuid AND java_method_id IS NOT NULL
LIMIT 20;
```

Örnek sorgu dosyası: `scripts/catalog-queries-examples.sql`. Uzak DB: JDBC URL’de `?sslmode=require` — [docs/catalog-db-tr.md](docs/catalog-db-tr.md).
