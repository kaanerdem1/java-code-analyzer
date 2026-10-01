# Java Code Analyzer

Kaynak kodda **metod karmaşıklığı ve risk** (CC, LOC, nesting, hotspot, Türkçe Markdown/JSON). **Call-graph / servis→metod çağrı zinciri bu repoda yok** — etki analizi ve lineage ayrı uygulamada.

Terminal / parse hataları: [scan-error-management.md](scan-error-management.md).

Legacy/incremental katman dokümantasyonu: [standalone-java-code-analyzer-dokumantasyon.md](standalone-java-code-analyzer-dokumantasyon.md).

**Incremental cache (varsayılan açık):** `run-analyze.sh` / `.cmd` otomatik `--state=analysis-output/analyzer-state.json` gönderir. Sıra: **modül toplu skip** (pom/Gradle kökü) → **dosya byte hash** → parse sonrası **AST özeti** (yorum/boşluk-only ise skor motoru atlanır) → **metod hash**. Risk profili / şema değişince cache otomatik sıfırlanır. `NO_STATE=1` kapatır; `FRESH=1` tek koşuda state okumaz. Detay: [docs/incremental-cache-tr.md](docs/incremental-cache-tr.md). Markdown raporda **Incremental cache** ve **değişiklikler** bölümleri.

### JAR (`target/` — repoda yok)

Analyzer kökünde **bir kez** `mvn package` → `target/java-code-analyzer.jar`. Script JAR yoksa analyzer repodaysa otomatik derler. **JDK 17+** gerekir.

### Seçenek 1 — Dosyaları **taramak istediğin projeye** kopyala (önerilen)

Analyzer parçaları taranacak projenin kökünde:

```
D:\projeler\mobil-backend\
  scripts\run-analyze.cmd
  target\java-code-analyzer.jar
  src\main\java\...
  pom.xml
```

PowerShell dizini: proje kökü

```
cd D:\projeler\mobil-backend
$env:LANGUAGE_LEVEL = "JAVA_21"   # isteğe bağlı, taramadan önce
.\scripts\run-analyze.cmd
```

Argüman vermezsen `.` = şu anki klasör taranır (yani `mobil-backend`).

Sadece kaynak ağacı:

```
.\scripts\run-analyze.cmd .\src\main\java
```

Rapor: `D:\projeler\mobil-backend\analysis-output\parser-…-….md`  
 (`run-analyze.cmd` içinde `ROOT` = `scripts\` klasörünün bir üstü = proje kökü.)



## Alternatif: Proje ile kardeş (yan yana)

```
D:\projeler\
  mobil-backend\          ← taranacak
  scripts\run-analyze.cmd
  target\java-code-analyzer.jar
```

PowerShell dizini: `scripts` ve `target` ile aynı üst klasör (`D:\projeler`)

```
cd D:\projeler
$env:LANGUAGE_LEVEL = "JAVA_21"
.\scripts\run-analyze.cmd .\mobil-backend
```

Argüman zorunlu; yoksa `D:\projeler`’in tamamı taranır (istenmeyebilir).

Rapor: `D:\projeler\analysis-output\…` (analyzer kit’in kökünde, büyük projenin içinde değil).

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

## PMD komutları

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



## JDK ve `--language-level`

**Aracı çalıştırmak:** JDK **17 veya üstü** (21, 25 dahil). JAR `release 17` ile derlenir; `mvn package` ve `java -jar` aynı JDK ile yapılır.

**Taranan kaynak kodu:** Varsayılan parse seviyesi `JAVA_17` (`run-analyze.sh` → `LANGUAGE_LEVEL`; JAR → `--language-level=…`). Ayarı **taramadan önce** ver; script ortasında değiştirilmez.


| Ortam                    | Örnek                                                             |
| ------------------------ | ----------------------------------------------------------------- |
| Linux / macOS (tek koşu) | `LANGUAGE_LEVEL=JAVA_21 ./scripts/run-analyze.sh .`               |
| Linux / macOS (oturum)   | `export LANGUAGE_LEVEL=JAVA_21` → `./scripts/run-analyze.sh .`    |
| Windows CMD              | `set LANGUAGE_LEVEL=JAVA_21` → `scripts\run-analyze.cmd C:\proje` |
| JAR                      | `--language-level=JAVA_21`                                        |


---

1. Analyzer kökünde:

```bat
scripts\run-analyze.cmd C:\yol\TARAMAK_ISTEDIGIN_PROJE
```

Çıktı: `analysis-output\parser-raporu.md` (analyzer repoda).

---







