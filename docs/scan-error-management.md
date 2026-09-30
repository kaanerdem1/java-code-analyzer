# Tarama hata yönetimi

Bu dosya **repoda sabit bir rehberdir** (`docs/scan-error-management.md`). Her taramada `analysis-output/` altına **kopyalanmaz**.

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
