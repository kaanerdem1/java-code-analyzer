# Java Code Analyzer

Kaynak kodda **metod karmaşıklığı ve risk** (CC, LOC, nesting, hotspot, Türkçe Markdown/JSON). **Call-graph / servis→metod çağrı zinciri bu repoda yok** — etki analizi ve lineage ayrı uygulamada.

### Hazır JAR (`dist/`)

Repoda **`dist/java-code-analyzer.jar`** vardır. **`git pull`** → makinede **JDK 17+** → Maven **gerekmez**:

```bat
scripts\run-analyze.cmd C:\yol\TARAMAK_ISTEDIGIN_PROJE
```

```bash
./scripts/run-analyze.sh /path/TARAMAK_ISTEDIGIN_PROJE
```

Analyzer kaynağını değiştiren geliştirici: `./scripts/sync-dist-jar.sh` → commit + push.

### Seçenek 1 — Dosyaları **taramak istediğin projeye** kopyala (önerilen)

**Kopyalanacaklar:**


| Bu repodan                    | Taramak istediğin projeye     |
| ----------------------------- | ----------------------------- |
| `scripts/run-analyze.sh`      | `scripts/run-analyze.sh`      |
| `dist/java-code-analyzer.jar` | `dist/java-code-analyzer.jar` |

Windows: `scripts/run-analyze.cmd` + aynı JAR.


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


| Parametre                 | Anlamı                                                |
| ------------------------- | ----------------------------------------------------- |
| `--workers`               | Paralel parse (0 = otomatik)                          |
| `--include` / `--exclude` | Glob filtre (path’e göre)                             |
| `--detail=summary`        | JSON/Markdown’da tüm metod listesi yok; hotspot kalır |
| `--progress-every`        | Log sıklığı (varsayılan 500)                          |


---



## JDK ve `--language-level`

**Aracı çalıştırmak:** JDK **17 veya üstü** (21, 25 dahil). JAR `release 17` ile derlenir; `mvn package` ve `java -jar` aynı JDK ile yapılır.

**Taranan kaynak kodu:** Varsayılan parse seviyesi `JAVA_17` (`--language-level` / `LANGUAGE_LEVEL`). Taranan proje **Java 8 / 11 / 17** ise genelde ek ayar gerekmez.

Daha yeni dil özellikleri (**21+**, **25 preview** vb.) parse hatası verirse seviyeyi JavaParser’ın desteklediği en yüksek uygun değere yükselt:

```bash
LANGUAGE_LEVEL=JAVA_21 ./scripts/run-analyze.sh /path/proje
```

Eski **Java 6** kaynak (DWH vb.):

```bash
LANGUAGE_LEVEL=JAVA_6 ./scripts/run-analyze.sh /path/proje
```

Windows CMD: `set LANGUAGE_LEVEL=JAVA_21` sonra `scripts\run-analyze.cmd ...` (veya JAR’a `--language-level=JAVA_21`).

---



## Windows

CMD / PowerShell’de `./scripts/run-analyze.sh` **çalışmaz** (Bash script). Explorer veya VS Code `.sh` dosyasına tıklayınca “açmak için uygulama seç” çıkar; bu **terminalde çalıştırma değildir**, çıktı da oluşmaz.

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

---

