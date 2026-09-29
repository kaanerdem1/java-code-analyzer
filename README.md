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

Raporlar (`analysis-output/`, `mock-modules/reports/`) **repoda yok** — `.gitignore`; pull sonrası `mvn package` + script ile **yerelde otomatik oluşur**. JAR da repoda yok (`target/`) — bir kez `mvn package` gerekir.

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

**Çıktı:** `analysis-output/parser-{proje-adı}-YYYYMMDD-HHmmss.md` (proje adı = taranan klasörün son parçası; `REPORT_TAG=etiket` ile değiştirilir; eski tek dosya: `FIXED_REPORT=1`)

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

## `mvn package` ve JAR ne?

| Kavram | Anlamı |
|--------|--------|
| **Kaynak kod** | `src/main/java/...` — geliştirdiğiniz analyzer |
| **`mvn package`** | Maven derler + bağımlılıkları birleştirir → **`target/java-code-analyzer.jar`** |
| **JAR** | Çift tıklanabilir program paketi; taramada sadece `java -jar ...` çalışır |

**Ne zaman `mvn package`?** İlk kurulumda veya analyzer **kaynak kodunu değiştirdiğinizde**. JAR zaten varsa script **yeniden derlemez** (`run-analyze.sh` / `run-analyze.cmd`; mock script de aynı). Taranan Spring projenizin `mvn package`’ı ile karıştırmayın — o sizin uygulamanızı derler, analyzer’ı değil.

Script “JAR yok, derleniyor” diyorsa: `target/java-code-analyzer.jar` silinmiş veya hiç üretilmemiş demektir.

---

## Windows

CMD / PowerShell’de **`./scripts/run-analyze.sh` çalışmaz** (Bash script). Explorer veya VS Code `.sh` dosyasına tıklayınca “açmak için uygulama seç” çıkar; bu **terminalde çalıştırma değildir**, çıktı da oluşmaz.

**Yapılacaklar:**

1. JDK 17+ ve Maven kurulu olsun; analyzer kökünde bir kez: `mvn package`
2. **VS Code terminali:** sağ altta **Command Prompt** veya **PowerShell** (Git Bash seçtiysen `./scripts/run-analyze.sh` de olur)
3. Analyzer kökünde:

```bat
scripts\run-analyze.cmd C:\yol\TARAMAK_ISTEDIGIN_PROJE
```

Çıktı: `analysis-output\parser-raporu.md` (analyzer repoda).

**Alternatif — doğrudan JAR (PowerShell):**

```powershell
cd C:\yol\java-code-analyzer
java -jar target\java-code-analyzer.jar `
  --path="C:\yol\TARAMAK_ISTEDIGIN_PROJE" `
  --output=analysis-output\standalone.json `
  --markdown=analysis-output\parser-raporu.md
```

WSL veya **Git Bash** kullanıyorsan macOS/Linux ile aynı `./scripts/run-analyze.sh` komutları geçerlidir.

---

## Sorun

| Sorun | Çözüm |
|-------|--------|
| JAR yok | Analyzer repoda **bir kez** `mvn package` (JAR varken her taramada tekrar gerekmez) |
| Eski tek dosya (`parser-raporu.md`) | `FIXED_REPORT=1 ./scripts/run-analyze.sh ...` veya Windows: `set FIXED_REPORT=1` |
| Raporda anlamlı isim | `REPORT_TAG=mobil-backend ./scripts/run-analyze.sh ...` → `parser-mobil-backend-20250929-143052.md` |
| Parse hataları | `LANGUAGE_LEVEL=JAVA_17 ./scripts/run-analyze.sh` |
| Windows, `.sh` açılıyor / çıktı yok | `scripts\run-analyze.cmd` veya yukarıdaki `java -jar`; dosyaya çift tıklama |
| `JAR yok` + `JAVA_HOME` / `mvn package` hata | JDK **17** kur; ortam değişkeni `JAVA_HOME` = JDK kökü (ör. `C:\Program Files\Java\jdk-17`); yeni terminal → `mvn package`. JAR hazırsa Maven gerekmez: Mac’te üretilen `target\java-code-analyzer.jar` kopyala |

---

## EN BASIT ÖZET

1. **Bu repoda denemek** → `mvn package` → `./scripts/run-standalone-mock.sh` → `mock-modules/reports/parser-raporu.md`

2. **Taramak istediğin projede parser** → analyzer’da `mvn package` → **o projeye** `scripts/run-analyze.sh` + `target/java-code-analyzer.jar` kopyala → VS Code’da **o projeyi** aç → `./scripts/run-analyze.sh`

3. **PMD mock** → `./scripts/run-pmd-mock.sh`  
   **PMD gerçek codebase** → `cd java-code-analyzer` → `./scripts/run-pmd-analyze.sh TARAMAK_ISTEDIGIN_PROJE`

4. **Kopyalamıyorsun:** `mock-modules`, analyzer `src/`, `pom.xml` (parser için). PMD, analyzer’ın `config/` klasöründen çalışır.

5. **Tarama:** Verdiğin codebase kökündeki **`.java` kaynak** dosyaları; `target`, `.git` atlanır. Rapor satırında ata zinciri: `modül › dosya › paket.Sınıf › metod(...)`.

6. **`.jar` içindeki derlenmiş sınıflar** (sadece `.class`) **henüz taranmaz** — parser kaynak okur. Taramak için `src/main/java` veya kaynak zip gerekir. Klasördeki `java-code-analyzer.jar` **aracın kendisi**, taranacak servisler değil.
