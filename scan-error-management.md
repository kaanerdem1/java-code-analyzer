# Tarama hata yönetimi

Bu dosya **repoda sabit bir rehberdir** (`scan-error-management.md`, README ile aynı kök). Her taramada `analysis-output/` altına **kopyalanmaz**.

**Bu koşuya özel** özet her zaman raporda:

- `analysis-output/parser-…-YYYYMMDD-HHmmss.md` → **Tarama tanıları**, **Okunamayan dosyalar**
- JSON → `scanDiagnostics`, `errors[]`

---

## Hızlı karar: ne oldu?

| Gördüğün şey | Ne anlama gelir | İlk adım |
|--------------|-----------------|----------|
| Sadece `[ERROR] …` ve program bitti | Tarama **hiç başlamadı** (path, flag, JAR) | Aşağı **§1**; komutu `run-analyze.cmd` ile dene |
| `Done: X parsed, Y failed` ve `Y > 0` | Tarama **bitti**; Y dosya metrik dışı | Rapordaki **Okunamayan dosyalar** + **§4** |
| `Exit 2` | Rapor var; CI parse oranı eşiğini aştı | Oranı düşür veya kaynakları düzelt; **§6** |
| `Exit 3` | Rapor var; risk gate tetiklendi | Beklenen CI davranışı; eşiği veya kodu gözden geçir |
| `FATAL aşama=…` | Run **ortada kesildi** | **§7**; aşama + mesaj; rapor eksik olabilir |

**Unutma:** Bir `.java` dosyasında `unexpected token` **tüm taramayı durdurmaz**. O dosya atlanır, diğerleri rapora girer.

---

## Süreç kesildi mi, yoksa bitti mi?

| Durum | Konsol | Raporda | Exit |
|--------|--------|---------|------|
| Normal | `Done: … failed=0` + Tarama tanıları | Tam tablo | 0 |
| Kısmi parse hatası | `failed > 0`, genelde exit 0 | Okunan dosyalar + hata listesi | 0 |
| CI parse oranı | `Exit 2: parse failure ratio …` | Aynı (rapor üretilmiştir) | 2 |
| CI risk gate | `Exit 3: project/method risk …` | Aynı | 3 |
| Fatal | `FATAL aşama=…` | JSON/MD eksik olabilir | 1 |

---

## Konsolda okuma sırası

1. **`[ERROR]`** — Başlangıç hatası (§1); scan yok.
2. **`N .java file(s) queued`** — Kaç dosya taranacak; 0 ise path/ include yanlış.
3. **`Done: X parsed, Y failed`** — Asıl sonuç özeti.
4. **`--- Parse / analysis errors ---`** — İlk 25 dosya; tam liste raporda.
5. **`--- Tarama tanıları ---`** — Oran, kategori, **→ öneriler** (Türkçe).

---

## §0 — Script, JAR çalışmadan önce

| Mesaj | Ne oluyor? | Çözüm |
|--------|------------|--------|
| **`[HATA] java komutu bulunamadi`** | Windows/CMD `java` bulamıyor. | JDK 17+ kur. CMD: `where java`. `JAVA_HOME\bin` PATH’te olsun. |
| **`[HATA] java-code-analyzer.jar bulunamadi`** | `target\java-code-analyzer.jar` yok. | Analyzer kökünde `mvn package`. Veya JAR’ı başka makineden `target\` altına kopyala. |
| **`[HATA] mvn package basarisiz`** | Script JAR derleyemedi. | `JAVA_HOME` = JDK (JRE değil). `mvn -v`. Elle: `cd` analyzer kök → `mvn package`. |
| **`[UYARI] JAVA_HOME tanimli degil`** | Maven bazen yanlış Java seçer. | `set JAVA_HOME=C:\Program Files\Java\jdk-17` (sürümüne göre). |

---

## §1 — Komut satırı (tarama başlamaz, exit 1)

Bu hatalarda **hiç `.java` okunmaz**; rapor da oluşmayabilir.

| Mesaj | Ne oluyor? | Çözüm |
|--------|------------|--------|
| **`Missing required argument --path`** | Taranacak klasör verilmemiş. | `scripts\run-analyze.cmd C:\tam\yol\proje` veya `--path=C:\…` |
| **`Path does not exist or is not readable`** | Klasör yok, yanlış sürücü harfi, veya izin yok. | Explorer’da yolu doğrula. Ağ sürücüsüyse bağlantı/okuma izni. |
| **`Unknown argument`** | Yazım hatası veya eski JAR’da olmayan flag. | `java -jar … --help`. Flag’leri README ile karşılaştır. |
| **`--path contains an illegal " character`** (veya **`Illegal char <">`**) | Windows’ta `--path` değerine `"` veya sonraki `--output` aynı argümana yapışmış. Örnek hatalı birleşik metin: `…\internal" --output=C:\…` | **En kolay:** `run-analyze.cmd C:\yol\proje` (JAR satırını elle yazma). Elle JAR: yolda boşluk yoksa tırnaksız `--path=C:\…\internal`; boşluk varsa her parametre ayrı tırnak: `"--path=C:\…" "--output=C:\…"`. Güncel JAR (`897762d+`) daha açıklayıcı mesaj verir. |
| **`Unknown --language-level`** | `JAVA_XX` tanınmıyor. | Mesajdaki desteklenen listeyi kullan. Örn. `set LANGUAGE_LEVEL=JAVA_17` veya `JAVA_21`. |
| **`Failed to load risk profile`** | `--risk-profile` adı YAML’da yok veya config dosyası okunamadı. | Varsayılan: `enterprise-java`. Config: `config/risk-parameters-proposal.yaml` analyzer JAR ile aynı ağaçta. |
| **`--max-failure-ratio must be between 0 and 1`** | CI eşiği 0.2 gibi olmalı, 20 değil. | `0.15` = %15 parse hatasına kadar exit 0. |
| **`--detail must be full or summary`** | Yanlış detail değeri. | `full` = tüm metodlar (büyük JSON); `summary` = hotspot + özet. |

---

## §2 — Bilgi satırları (hata değil)

| Mesaj | Anlam |
|--------|--------|
| **`N .java file(s) queued`** | N dosya parse edilecek. N=0 → `--path` çok dar, include/exclude hepsini elemiş veya klasörde `.java` yok. |
| **`Skipped M directory subtree(s)`** | `target`, `build`, exclude glob — bilinçli atlama. Beklenen davranış. |
| **`k/N files processed`** | İlerleme; büyük repoda takılı kaldı mı diye buraya bak. |
| **`Report written to` / `Readable Markdown`** | JSON ve Markdown yolları; rapor hazır. |

---

## §3 — Özet satırı

**`Done: X parsed, Y failed, … methods`**

- **X** = metrik üretilen dosya sayısı.
- **Y** = okunamayan dosya (parse/IO/abort); **tarama yine tamamlandı**.
- **Y > 0** → Markdown **Okunamayan dosyalar** bölümünü aç; proje risk skoru yalnızca X dosyasına dayanır.

---

## §4 — Dosya bazlı hatalar (en sık sorular)

Konsol: `--- Parse / analysis errors ---` (max ~25 satır). **Tam liste:** rapor **Okunamayan dosyalar**.

### PARSE_SYNTAX — `Unexpected token`, `Parse error: …`

**Ne oluyor?** JavaParser dosyayı geçerli Java olarak okuyamadı: bozuk sözdizimi, Java olmayan içerik, merge conflict, eksik `}`.

**Çözüm adımları:**

1. Rapordaki **dosya yolunu** IDE’de aç; hata satırına yakın sözdizimine bak.
2. Gerçekten `.java` mı? (SQL/XML/snippet yanlış uzantılı olabilir.)
3. `<<<<<<<` / `=======` var mı? → merge’ü bitir.
4. Tek dosya çöp ise: `EXCLUDE_GLOBS` ile hariç tut veya taramayı `src\main\java` ile daralt.
5. Çok dosyada aynı hata → **§ Dil sürümü** (aslında LANGUAGE_LEVEL).

### PARSE_LANGUAGE_LEVEL — `(tried language levels: JAVA_17, JAVA_21, …)`

**Ne oluyor?** Dosyada seçilen parser seviyesinin anlamadığı sözdizimi var (`record`, pattern `switch`, vb.). Araç dosya başına tüm seviyeleri dener; hepsi fail ise buraya düşer.

**Çözüm:**

1. Taramadan önce: `LANGUAGE_LEVEL=JAVA_21` (repodaki en yeni sözdizimine yakın).
2. Windows: `set LANGUAGE_LEVEL=JAVA_21` → `run-analyze.cmd …`
3. Hâlâ fail → dosyayı IDE’de derle; gerçek syntax hatası olabilir (PARSE_SYNTAX).

### IO — `I/O error`

**Ne oluyor?** Dosya okuma sırasında OS hatası (kilit, kopmuş ağ sürücüsü, antivirüs).

**Çözüm:** Dosyayı yerel diske kopyala; başka process’in kilidini kaldır; tekrar tara.

### ACCESS — `Cannot access`

**Ne oluyor?** Klasör ağacında gezinirken erişim reddi.

**Çözüm:** `--path`’i erişebildiğin alt ağaca indir (`src\main\java`). Gereksiz dalları exclude et.

### STACK_OVERFLOW — `expression nesting too deep`

**Ne oluyor?** Tek dosyada aşırı derin iç içe ifade (genelde üretilmiş dev string/SQL).

**Çözüm:** Dosyayı exclude et (`**/generated/**`). O dosya olmadan rapor yeterliyse devam et.

### OUT_OF_MEMORY — `out of memory`

**Ne oluyor?** Çok büyük/ karmaşık tek dosya veya makine RAM yetersiz.

**Çözüm:** `WORKERS=1` veya `--workers=1` (paraleli düşür). `--detail=summary`. Problemli dosyayı exclude et. JVM’e daha fazla RAM (`java -Xmx4g -jar …`).

### UNEXPECTED — `Unexpected error: …`

**Ne oluyor?** Beklenmeyen runtime; mesajdaki exception adına bak.

**Çözüm:** Mesajı issue/ekibe ilet; geçici olarak o dosyayı exclude edip taramayı sürdür.

---

## §5 — Tarama tanıları bloğu

| Alan | Anlam | Ne yap |
|------|--------|--------|
| **COMPLETED_OK** | Tüm kuyruk dosyaları parse oldu. | Raporu normal kullan. |
| **COMPLETED_WITH_FILE_ERRORS** | Bir kısım dosya §4 listesinde. | Okunamayan dosyalar + öneri satırları (`→`). Oran yüksekse path/exclude/LANGUAGE_LEVEL. |
| **FAILED_FATAL** | Run kesildi (§7). | FATAL aşama; rapor eksik olabilir. |
| **Parse hata oranı** | `failed ÷ scanned` (yüzde). | %10 üstü → kökü daralt, exclude, çöp `.java` temizliği. |
| **Hata kategorileri** | Örn. `{PARSE_SYNTAX=12, IO=1}`. | Baskın kategoriye göre §4. |
| **`→ …` öneriler** | Otomatik Türkçe ipuçları. | Satır satır uygula. |

---

## §6 — Exit 2 ve Exit 3 (rapor genelde vardır)

### Exit 2 — `parse failure ratio … > …`

**Ne oluyor?** `--max-failure-ratio` (ör. 0.2) aşıldı; CI “çok fazla dosya okunamadı” diyor.

**Çözüm:** (1) Rapordaki hata dosyalarını §4 ile düzelt veya exclude. (2) Geçici CI: eşiği yükselt (kalite gate zayıflar). (3) `failed` kasıtlıysa (test/generated) exclude glob ekle.

### Exit 3 — `project risk level` / `method … level`

**Ne oluyor?** `--fail-on-risk=HIGH` (veya benzeri) tetiklendi; **parse başarısından bağımsız**.

**Çözüm:** Beklenen CI ise raporu incele. Yanlış alarm ise profil/eşik (`config/risk-parameters-proposal.yaml`) veya gate seviyesini gözden geçir.

---

## §7 — Fatal (exit 1)

| Mesaj | Ne oluyor? | Çözüm |
|--------|------------|--------|
| **`FATAL aşama=parse ve metrik`** | Parse veya rapor toplama sırasında yakalanmayan hata. | `Son dosya:` varsa o `.java`’yı exclude edip tekrar dene. `--workers=1`. |
| **`FATAL aşama=JSON/Markdown yazımı`** | Çıktı yolu yok, disk dolu, izin yok. | `analysis-output` oluşturulabilir mi? Yolu kısalt; yazma izni. |
| **`Parallel parse failed/interrupted`** | Paralel worker çöktü. | `--workers=1` ile tekrarla. |

---

## `[ERROR]` vs `[STANDALONE]` vs rapor

| Kaynak | Anlam |
|--------|--------|
| **`[ERROR]`** | CLI / başlangıç; çoğu zaman scan yok. |
| **`[STANDALONE]`** | Analyzer çalışıyor veya özet veriyor. |
| **`[HATA]`** | `run-analyze.cmd` / `.sh`; JAR öncesi. |
| **Rapor § Okunamayan dosyalar** | Dosya + kategori + tam parse mesajı. |

---

## Karışık Java sürümleri (monorepo)

- Tek koşuda **`LANGUAGE_LEVEL`** (varsayılan `JAVA_17`) tercih seviyesidir.
- Her dosyada fail olursa araç **diğer desteklenen seviyeleri** dener (yeni → eski).
- **`JAVA_21` seçmek Java 8/17 dosyalarını genelde silmez**; tersine 21 sözdizimini açar.
- **`pom.xml` `<release>` okunmaz** → repodaki en yeni sözdizimine yakın `LANGUAGE_LEVEL` seç veya modülü ayrı `--path` ile tara.

---

## Önleyici checklist (büyük proje)

1. `--path` = mümkünse `…\src\main\java` (tüm workspace değil).
2. Exclude: `**/src/test/**`, `**/target/**`, `**/build/**`, `**/generated/**`.
3. Windows: **`run-analyze.cmd C:\yol\proje`** — tırnak birleşme hatasından kaçın.
4. `LANGUAGE_LEVEL` = repodaki en yeni sözdizimi.
5. İlk koşudan sonra **Tarama tanıları → öneriler**.

---

## Teknik referans

- `ProjectAnalyzer` — dosya hatalarında tarama devam eder.
- `SourceParser` — dosya başına dil seviyesi fallback.
- `ScanExitEvaluator` — `--max-failure-ratio`, `--fail-on-risk`.
- `scripts/run-analyze.sh` — `LANGUAGE_LEVEL`, `EXCLUDE_GLOBS`, `STANDALONE_RISK_PROFILE`.
