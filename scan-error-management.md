# Tarama hata yönetimi

Bu dosya **repoda sabit bir rehberdir** (`scan-error-management.md`, README ile aynı kök). Her taramada `analysis-output/` altına **kopyalanmaz**.

Her koşuda üretilen **o taramaya özel** özet:

- `analysis-output/parser-…-YYYYMMDD-HHmmss.md` → **## Tarama tanıları**, **## Okunamayan dosyalar**
- Eşleşen JSON → kök alan `scanDiagnostics` ve `errors[]`

Bu belge, olumsuz sonuçları nasıl okuyacağınızı, sürecin **nerede durduğunu** veya **nerede sorun yaşadığını** nereden göreceğinizi açıklar.

## Süreç kesildi mi, yoksa bitti mi?

| Durum | Konsol | Rapor (Markdown) | Exit kodu |
|--------|--------|------------------|-----------|
| **Normal bitiş** | `[STANDALONE] Done: X parsed, Y failed, …` + `--- Tarama tanıları ---` | Özet tablo + (varsa) **Okunamayan dosyalar** + **Tarama tanıları** | Genelde **0** |
| **Bitti ama çok parse hatası** | Aynı + `Exit 2: parse failure ratio …` (yalnızca `--max-failure-ratio` verildiyse) | Başarılı dosyaların metrikleri + hata listesi | **2** |
| **Risk gate** | `Exit 3: project risk level …` veya metod seviyesi | Rapor yine yazılmış olabilir | **3** |
| **Fatal (ortada kesildi)** | `[STANDALONE] FATAL aşama=…` + mesaj; parse edilen dosya varsa `son dosya=…` | JSON/Markdown yazılamamış olabilir | **1** |

**Önemli:** Tek bir dosyada `unexpected token` **tüm taramayı durdurmaz**. O dosya atlanır, kuyruk devam eder. “Saçma” hissi genelde çok sayıda `failed` satırı veya CI’da exit 2’den gelir.

## Konsolda nereye bakılır?

1. **Aşama satırları** — `Keşif`, `Parse`, `JSON yazımı`, `Markdown yazımı`
2. **`--- Parse / analysis errors ---`** — dosya yolu + kısa mesaj (ilk N satır; tam liste raporda)
3. **`--- Tarama tanıları ---`** — hata kategorileri, parse oranı, Türkçe öneriler
4. **`FATAL`** — yalnızca keşif/I/O/rapor yazımı gibi **run seviyesi** hata

## Terminal mesajları (tipik sıra ve anlamı)

Aşağıdaki sıra **başarılı bir koşunun** akışıdır. Hata olursa ilgili adımda durur veya (dosya hatalarında) devam edip sonunda listeler.

### 0 — Script henüz JAR’ı çalıştırmadan (`run-analyze.cmd` / `.sh`)

| Mesaj | Anlam | Ne yapılır |
|--------|--------|------------|
| `[HATA] java komutu bulunamadi…` | `java` PATH’te yok | JDK 17+ kur, PATH’e ekle |
| `[HATA] java-code-analyzer.jar bulunamadi…` | `target/` veya `dist/` içinde JAR yok | Analyzer kökünde `mvn package` veya JAR’ı kopyala |
| `[HATA] mvn package basarisiz…` | Otomatik derleme düştü | `JAVA_HOME`, `mvn`, ağ; elle `mvn package` |
| `[UYARI] JAVA_HOME tanimli degil…` | Windows CMD derleme uyarısı | `JAVA_HOME` = JDK 17 klasörü |

### 1 — Komut satırı / başlangıç (tarama **başlamaz**, exit **1**)

| Mesaj | Anlam | Ne yapılır |
|--------|--------|------------|
| `[ERROR] Missing required argument --path=<dir>` | `--path` verilmemiş | `run-analyze.cmd C:\yol\proje` veya `--path=…` |
| `[ERROR] Path does not exist or is not readable: …` | Klasör yok veya okuma izni yok | Yolu düzelt, erişim ver |
| `[ERROR] Unknown argument: …` | Tanınmayan flag | `--help` ile listeye bak |
| `[ERROR] --path contains an illegal " character…` | Windows’ta tırnak/`--output` birleşmiş path | `run-analyze.cmd` kullan veya her flag ayrı; bkz. `internal" --output=` hatası |
| `[ERROR] Illegal char <"> at index …` | (Eski JAR) path içinde `"` | Yukarıdaki gibi komutu düzelt; güncel JAR’da daha açıklayıcı mesaj |
| `[ERROR] Unknown --language-level: …` | Geçersiz dil seviyesi | `JAVA_17`, `JAVA_21` vb. (mesajda desteklenen liste) |
| `[ERROR] Failed to load risk profile '…'` | YAML / profil adı | `config/risk-parameters-proposal.yaml`, `--risk-profile=enterprise-java` |
| `[ERROR] --max-failure-ratio must be between 0 and 1` | CI eşiği yanlış | 0.0–1.0 arası ver |
| `[ERROR] --detail must be full or summary` | Yanlış `--detail` | `full` veya `summary` |
| `[ERROR] …` (diğer `IllegalArgumentException`) | Boş `--top`, `--workers` negatif vb. | Mesajdaki flag’i düzelt |

### 2 — Tarama başladı (bilgi satırları, hata değil)

| Mesaj | Anlam |
|--------|--------|
| `[STANDALONE] =====…` | Risk modeli özeti |
| `[STANDALONE] N .java file(s) queued…` | Kuyruğa alınan dosya sayısı |
| `[STANDALONE] Skipped M directory subtree(s)…` | `target`/`build`/exclude ile atlanan klasörler |
| `[STANDALONE] k/N files processed` | İlerleme |
| `[STANDALONE] Parse wall time: … ms` | Parse süresi |
| `[STANDALONE] Report written to …` / `Readable Markdown: …` | JSON/Markdown yazıldı |

### 3 — Özet (tarama bitti, dosya hataları olsa bile)

| Mesaj | Anlam |
|--------|--------|
| `[STANDALONE] --- Summary ---` | Dosya/metod sayıları, proje riski |
| `[STANDALONE] Done: X parsed, Y failed, …` | **X** okundu, **Y** dosya listede hata (tarama durmadı) |

### 4 — Dosya bazlı hatalar (tarama **devam etmişti**)

`--- Parse / analysis errors ---` altında (en fazla 25 satır konsolda; tamamı raporda):

| Mesaj kalıbı | Anlam | Kategori |
|----------------|--------|----------|
| `… [PARSE_SYNTAX]: Parse error: … Unexpected token …` | Java sözdizimi / bozuk dosya | PARSE_SYNTAX |
| `… Parse error: … (tried language levels: JAVA_17, JAVA_21, …)` | Dil seviyesi uyumsuz | PARSE_LANGUAGE_LEVEL |
| `… [IO]: I/O error: …` | Dosya okunamadı | IO |
| `… [ACCESS]: Cannot access: …` | Keşif sırasında klasör/dosya erişimi | ACCESS |
| `… [STACK_OVERFLOW]: Analysis aborted: expression nesting too deep…` | Aşırı derin ifade ağacı | STACK_OVERFLOW |
| `… [OUT_OF_MEMORY]: Analysis aborted: out of memory…` | Bellek | OUT_OF_MEMORY |
| `… [UNEXPECTED]: Unexpected error: …` | Beklenmeyen runtime | UNEXPECTED |

### 5 — Tarama tanıları (her koşu sonu)

| Mesaj | Anlam |
|--------|--------|
| `[STANDALONE] --- Tarama tanıları ---` | Özet blok başlığı |
| `Durum: COMPLETED_OK` | Dosya hatası yok |
| `Durum: COMPLETED_WITH_FILE_ERRORS` | Bazı `.java` okunamadı, diğerleri raporda |
| `Durum: FAILED_FATAL` | Run ortada kesildi |
| `Parse hata oranı: …%` | `failed / scanned` |
| `Hata kategorileri: {…}` | Sayım |
| `→ …` | Türkçe öneri satırları |
| `Ayrıntı: scan-error-management.md` | Bu rehber |

### 6 — CI / exit kodu (rapor yine oluşmuş olabilir)

| Mesaj | Exit | Anlam |
|--------|------|--------|
| `[STANDALONE] Exit 2: parse failure ratio … > …` | **2** | `--max-failure-ratio` aşıldı |
| `[STANDALONE] Exit 3: project risk level … >= gate …` | **3** | `--fail-on-risk` proje eşiği |
| `[STANDALONE] Exit 3: method … level …` | **3** | `--fail-on-risk` metod eşiği |

### 7 — Fatal (run kesildi, exit **1**)

| Mesaj | Anlam |
|--------|--------|
| `[STANDALONE] FATAL aşama=…: …` | Hangi aşamada: keşif, parse, JSON/Markdown yazımı |
| `Son dosya: …` | Paralel parse’da son işlenen göreli yol (varsa) |
| `Rapor eksik olabilir; scan-error-management.md` | JSON/MD tam yazılmamış olabilir |
| `[ERROR] I/O failure: …` | (Alternatif) yazma/okuma |
| `Parallel parse interrupted` / `Parallel parse failed` | Paralel worker çökmesi (nadir) |

---

**Not:** `[ERROR]` genelde **henüz tarama yok**; `[STANDALONE]` satırları **analyzer çalışıyor veya bitti** demektir. Tek dosyada parse hatası **`[ERROR]` ile bitmez**, `failed` sayısına ve **§4** listesine gider.

## Markdown / JSON

- **Okunamayan dosyalar** — her satır: `` `göreli/yol/Foo.java` `` + mesaj + `(kategori: …)`
- **Tarama tanıları** — özet sayılar, çıkış kodu açıklaması, öneriler
- JSON kök alanı `scanDiagnostics` — aynı özet (otomasyon için)

## “Unexpected token” ve sık parse senaryoları

| Senaryo | Tipik belirti | Ne yapılır |
|---------|----------------|------------|
| **Gerçekte Java değil** | `.java` uzantılı config, SQL, snippet | Dosyayı düzeltin veya `EXCLUDE_GLOBS` ile hariç tutun |
| **Dil sürümü uyumsuz** | `record`, pattern `switch`, sealed class; mesajda syntax | `LANGUAGE_LEVEL` yükseltin (ör. `JAVA_21`) veya modülü ayrı tarayın |
| **Merge / yarım dosya** | `<<<<<<<`, eksik `}` | Kaynağı düzeltin |
| **Encoding** | Garip karakterler, token hatası satır başında | `--encoding=UTF-8` (veya projeye uygun charset) |
| **Üretilmiş dev dosya** | Stack overflow / OOM mesajı | `**/generated/**` exclude; o dosyayı hariç tutun |
| **Yanlış tarama kökü** | `target/`, `build/` altında anlamsız `.java` | Kökü `src/main/java` yapın; exclude glob ekleyin |

Kategori etiketleri: `PARSE_SYNTAX`, `PARSE_LANGUAGE_LEVEL`, `IO`, `STACK_OVERFLOW`, `OUT_OF_MEMORY`, `ACCESS`, `UNEXPECTED`.

## Karışık Java sürümleri (monorepo)

Tek koşuda **tek bir “tercih” dil seviyesi** (`LANGUAGE_LEVEL`, varsayılan `JAVA_17`) kullanılır; fakat **her dosya için** parser, başarısız olursa **diğer desteklenen Java seviyelerini sırayla dener** (önce tercih, sonra daha yeni, sonra daha eski).

- **Java 8 + Java 17 modülleri** aynı ağaçta: çoğu dosya tek koşuda parse olur.
- **Yalnızca Java 21+ sözdizimi** içeren dosya: tercih `JAVA_17` iken fallback **JAVA_21** (ve javaparser destekliyorsa üzeri) ile kurtulabilir; yine de olmazsa hata listesine düşer.
- **Modül bazında farklı `--release`** (Maven toolchains): araç `.java` kaynağına bakar; `pom.xml` release değerini otomatik okumaz. En doğru sonuç için tercih seviyesini repodaki **en yeni** sözdizimine yakın seçin veya problemli modülü ayrı `--path` ile tarayın.

## Önleme checklist

1. `--path` mümkün olduğunca dar: `…/src/main/java`
2. `EXCLUDE_GLOBS='**/src/test/**,**/target/**,**/build/**,**/generated/**'`
3. `LANGUAGE_LEVEL` repodaki en yeni sözdizimine uygun
4. Rapordaki **Tarama tanıları → öneriler** satırlarını uygulayın

## İlgili dosyalar

- `ProjectAnalyzer` — dosya bazlı hata yakalama, tarama devam eder
- `SourceParser` — dosya başına dil seviyesi fallback
- `ScanExitEvaluator` — `--max-failure-ratio`, `--fail-on-risk`
- `scripts/run-analyze.sh` — `LANGUAGE_LEVEL`, `EXCLUDE_GLOBS`
