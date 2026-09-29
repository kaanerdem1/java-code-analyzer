# Java Code Analyzer

Java kaynak kodunda metod karmaşıklığı ve risk raporu (Türkçe `.md`).

**Kurulum:** JDK 17+, Maven.

> **TARAMAK_ISTEDIGIN_PROJE** = Taramak istediğin codebase’in kök klasörü (içinde `src/main/java` olan Maven/Spring projesi).  
> Örnek: `/Users/sen/mobil-backend` veya `../mobil-backend`

---

## Hızlı komutlar (bu repo — mock demo)

Proje kökünde, bir kez `mvn package`, sonra:

```bash
# Parser (standalone)
./scripts/run-standalone-mock.sh

# PMD
./scripts/run-pmd-mock.sh

# İkisi birden
./scripts/run-both-mock.sh
```

Mock raporlar: `mock-modules/reports/parser-raporu.md` ve `pmd-raporu.md`

---

## Gerçek codebase — ne alıyoruz, nereye koyuyoruz?

### Seçenek 1 — Dosyaları **taramak istediğin projeye** kopyala (önerilen)

Analyzer repoda **bir kez**:

```bash
mvn package
```

**Kopyalanacaklar** (sadece 2 dosya; modül / kaynak kod yok):

| Bu repodan | Taramak istediğin projeye |
|------------|---------------------------|
| `scripts/run-analyze.sh` | `scripts/run-analyze.sh` |
| `target/java-code-analyzer.jar` | `target/java-code-analyzer.jar` |

PMD için Seçenek 2 (analyzer ayrı klasörde kalır).

VS Code’da **taramak istediğin projeyi** aç → terminal proje kökünde:

```bash
./scripts/run-analyze.sh
```

**Çıktı:** `analysis-output/parser-raporu.md`

---

### Seçenek 2 — Analyzer **ayrı klasörde** kalsın

Taramak istediğin projeye dosya koyma. Terminal:

```bash
cd TARAMAK_ISTEDIGIN_PROJE/../java-code-analyzer
# veya doğrudan bu aracın kurulu olduğu klasör:
cd /Users/sen/java-code-analyzer

mvn package

./scripts/run-analyze.sh TARAMAK_ISTEDIGIN_PROJE
./scripts/run-pmd-analyze.sh TARAMAK_ISTEDIGIN_PROJE
```

Örnek (analyzer Masaüstü’nde, uygulama başka yerde):

```bash
cd ~/Desktop/java-code-analyzer
./scripts/run-analyze.sh ~/projects/mobil-backend
./scripts/run-pmd-analyze.sh ~/projects/mobil-backend
```

**Çıktı:** `java-code-analyzer/analysis-output/parser-raporu.md` ve `pmd-raporu.md`

---

## PMD komutları (kopyala-yapıştır)

**Mock (bu repodaki örnek modüller):**

```bash
cd java-code-analyzer
mvn package
./scripts/run-pmd-mock.sh
```

**Taramak istediğin proje** (analyzer repodan):

```bash
cd java-code-analyzer
./scripts/run-pmd-analyze.sh TARAMAK_ISTEDIGIN_PROJE
```

Örnek:

```bash
./scripts/run-pmd-analyze.sh ~/projects/mobil-backend
```

PMD raporu: `analysis-output/pmd-raporu.md` (mock’ta: `mock-modules/reports/pmd-raporu.md`).

Parser + PMD mock:

```bash
./scripts/run-both-mock.sh
```

---

## Büyük monorepo / dev codebase

Varsayılan: paralel parse (CPU’ya göre worker), `target`/`build`/`.gradle` atlanır, 500 dosyada bir ilerleme logu.

**Sadece production kaynağı (önerilen):**

```bash
INCLUDE_GLOBS='**/src/main/java/**' \
EXCLUDE_GLOBS='**/generated/**,**/build/**' \
./scripts/run-analyze.sh
```

**Büyük repo — küçük JSON (hotspot + dosya özeti, metod ağacı yok):**

```bash
REPORT_DETAIL=summary WORKERS=8 ./scripts/run-analyze.sh
```

**Doğrudan JAR:**

```bash
java -jar target/java-code-analyzer.jar \
  --path=TARAMAK_ISTEDIGIN_PROJE \
  --include='**/src/main/java/**' \
  --exclude='**/generated/**' \
  --workers=8 \
  --detail=summary \
  --output=analysis-output/standalone.json \
  --markdown=analysis-output/parser-raporu.md
```

| Parametre | Anlamı |
|-----------|--------|
| `--workers` | Paralel parse (0 = otomatik) |
| `--include` / `--exclude` | Glob filtre (path’e göre) |
| `--detail=summary` | JSON/Markdown’da tüm metod listesi yok; hotspot kalır |
| `--progress-every` | Log sıklığı (varsayılan 500) |

---

## Eski Java 6 kaynak

```bash
LANGUAGE_LEVEL=JAVA_6 ./scripts/run-analyze.sh
```

---

## Sorun

| Sorun | Çözüm |
|-------|--------|
| JAR yok | Analyzer repoda `mvn package` |
| Parse hataları | `LANGUAGE_LEVEL=JAVA_17 ./scripts/run-analyze.sh` |

---

## EN BASIT ÖZET

1. **Bu repoda denemek** → `mvn package` → `./scripts/run-standalone-mock.sh` → `mock-modules/reports/parser-raporu.md`

2. **Taramak istediğin projede parser** → analyzer’da `mvn package` → **o projeye** `scripts/run-analyze.sh` + `target/java-code-analyzer.jar` kopyala → VS Code’da **o projeyi** aç → `./scripts/run-analyze.sh`

3. **PMD mock** → `./scripts/run-pmd-mock.sh`  
   **PMD gerçek codebase** → `cd java-code-analyzer` → `./scripts/run-pmd-analyze.sh TARAMAK_ISTEDIGIN_PROJE`

4. **Kopyalamıyorsun:** `mock-modules`, analyzer `src/`, `pom.xml` (parser için). PMD, analyzer’ın `config/` klasöründen çalışır.

5. **Tarama:** Verdiğin codebase kökündeki **`.java` kaynak** dosyaları; `target`, `.git` atlanır. Rapor satırında ata zinciri: `modül › dosya › paket.Sınıf › metod(...)`.

6. **`.jar` içindeki derlenmiş sınıflar** (sadece `.class`) **henüz taranmaz** — parser kaynak okur. Taramak için `src/main/java` veya kaynak zip gerekir. Klasördeki `java-code-analyzer.jar` **aracın kendisi**, taranacak servisler değil.
