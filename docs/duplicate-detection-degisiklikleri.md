# Duplicate / Benzer Kod Tespiti — Değişiklik Dokümantasyonu

Bu doküman, Standalone Java Code Analyzer'a eklenen **proje geneli metod-klonu tespiti**
özelliğini kapsar: Type-1/2 (birebir / isim değişmiş) duplicate'ler kendi yapısal hash'imizle,
Type-3 (yakın-benzer) duplicate'ler ise gömülü **PMD CPD** kütüphanesiyle bulunuyor.

## Neden

Taramada farklı isimli ama mantığı aynı/çok benzer metodları ("kopyala-yapıştır" kod) tespit edip
raporlamak için.

## Değişen / Eklenen Dosyalar

| Dosya | Durum |
|---|---|
| `HashService.java` | güncellendi — `structuralHash(BlockStmt)` eklendi |
| `MethodMetric.java` | güncellendi — `structuralHash` alanı eklendi |
| `ComplexityVisitor.java` | güncellendi — her metod için `structuralHash` hesaplanıp `MethodMetric`'e ekleniyor |
| `DuplicateDetectionEngine.java` | **yeni** — Type-1/2/3 tespitinin tamamı burada |
| `AnalysisReport.java` | güncellendi — `DuplicateGroup`, `DuplicateMember`, `DuplicateStatistics` DTO'ları + kök alanlar |
| `ProjectAnalyzer.java` | güncellendi — tarama sonunda duplicate tespiti çağrısı, konsol özeti |
| `Java6CodeAnalyzerMain.java` | güncellendi — `--no-duplicates`, `--min-duplicate-tokens=` bayrakları |
| `StandaloneReportMarkdown.java` | güncellendi — rapor sonunda duplicate özet + grup tabloları |
| `pom.xml` | güncellendi — `pmd-core` + `pmd-java` (7.27.0) bağımlılıkları |

---

## Nasıl Çalışıyor

### 1) Type-1 / Type-2 — kendi yapısal hash'imiz (ek kütüphane yok, bellek içi)

**`HashService.structuralHash(BlockStmt body)`**
Metod gövdesinin bir **klonunu** alır (orijinal AST'ye dokunmaz), şunları yapar:
- Her `NameExpr`, `FieldAccessExpr`, `VariableDeclarator`, `Parameter` adını `ID` ile değiştirir
  (`setName("ID")`).
- Her `LiteralExpr`'i (`LIT` adlı) bir `NameExpr` ile değiştirir (`literal.replace(...)`).
- Sonucu JavaParser'ın pretty-printer'ıyla yazdırıp, yorum/boşlukları temizleyip SHA-256 hash'ler
  (mevcut `normalizedMethodHash` ile aynı temizleme regex'leri kullanılıyor).

Sonuç: değişken adları ve sabit değerler farklı olsa bile **aynı mantığa sahip iki metod aynı
hash'i üretir**. **EXACT_STRUCTURE** gruplarında ek olarak **dönüş tipi + metod imzası** (`returnType` + `name(paramTypes…)`) şarttır; farklı girdi/çıktı imzalı metodlar yapısal grupta birleşmez.

**PMD CPD (NEAR_MISS):** Varsayılan olarak **her zaman** çalışır (dosya sayısına göre sessiz atlama yok). `--no-near-duplicates` yalnızca CPD'yi kapatır. İsteğe bağlı performans: `--near-duplicate-auto-skip-files=N` (N&gt;0) ile büyük repoda CPD atlama **opt-in**.

**EXACT_TEXT** (ayrı pass): `normalizedBodyHash` — gövde metni birebir (literal ve isimler dahil, yalnızca yorum/boşluk normalize). Gerçek kopyala-yapıştır için en kesin katman.

**`MethodMetric.structuralHash`**
Bu hash artık her metod için (trivial filtre uygulanmadan) hesaplanıp saklanıyor —
`ComplexityVisitor.analyseCallable` içinde, gövde zaten gezildiği için ek maliyeti düşük.

**`DuplicateDetectionEngine.detectExactDuplicates(List<FileMetric> files)`**
Tüm dosyaların tüm metodlarını `structuralHash`'e göre gruplar (`Map<String, List<DuplicateMember>>`).
İki veya daha fazla üyesi olan her grup bir `DuplicateGroup` (`similarityType = EXACT_STRUCTURE`)
olarak rapora eklenir. **Yeni parse/IO yok** — tamamen önceki analiz adımının ürettiği veriden.

### 2) Type-3 — PMD CPD (gömülü kütüphane)

**`DuplicateDetectionEngine.detectNearMissDuplicates(Path root, int minimumTokens, List<FileMetric> files)`**
- `CPDConfiguration` ile PMD CPD'yi yapılandırır: `setMinimumTileSize(minimumTokens)`,
  `setOnlyRecognizeLanguage("java")`, `setIgnoreIdentifiers(true)`, `setIgnoreLiterals(false)`,
  `addInputPath(root)`.
- `CpdAnalysis.create(config)` ile analiz çalıştırılır (`try`-with-resources); PMD **kendi
  dosyalarını diskten ayrıca okur** — bizim AST geçişimizden bağımsız, ikinci bir tarama.
- Her `Match` (tekrar eden token dizisi) için, her `Mark` (dosya + satır aralığı) bizim
  `FileMetric`/`MethodMetric` verisiyle satır-aralığı çakışmasına göre sınıf+metoda eşlenir
  (`resolveMethod` — en büyük çakışmaya sahip metod seçilir).
- Sonuçlar `DuplicateGroup` (`similarityType = NEAR_MISS`, `matchedTokenCount` dolu) olarak döner.

### 3) Gürültü filtresi

`qualifiesForDuplicateCheck(MethodMetric)` — `cyclomaticComplexity ≥ 3 VEYA codeLines ≥ 8`
şartını sağlamayan metodlar (getter/setter, boş constructor vb.) hem Type-1/2 hem Type-3
taramasından **otomatik olarak** çıkarılır.

### 4) Rapora entegrasyon

`ProjectAnalyzer.analyze()` — tüm dosyalar işlendikten **sonra** çalışır:
```java
duplicateGroups.addAll(duplicateDetectionEngine.detectExactDuplicates(files));
duplicateGroups.addAll(duplicateDetectionEngine.detectNearMissDuplicates(absoluteRoot, minDuplicateTokens, files));
```
PMD CPD çağrısı `try/catch(RuntimeException)` ile korunuyor — başarısız olursa sadece `[WARN]`
loglanıp Type-1/2 sonuçlarıyla devam ediliyor, tüm analiz çökmüyor.

`AnalysisReport` kök nesnesine iki yeni alan eklendi: `duplicateStatistics` (grup/metod sayıları)
ve `duplicateGroups` (tüm gruplar, dosya/sınıf/metod/satır detaylarıyla).

### 5) CLI

- `--no-duplicates` — duplicate taramasını tamamen atlar (büyük repo'larda PMD'nin ikinci disk
  taramasını istemiyorsanız).
- `--min-duplicate-tokens=<n>` — PMD CPD'nin minimum token eşiği (varsayılan 50; düşürmek daha
  küçük/daha fazla eşleşme, yükseltmek daha az/daha büyük eşleşme demek).

Konsola tarama sonunda şu satır basılıyor:
```
[STANDALONE] Duplicates: 4 exact-structure group(s) (9 methods) | 7 near-miss group(s) (15 methods)
```

---

## PMD 7.27 API notu

`Mark` artık `getFilePath()` / `getBeginLine()` taşımıyor; dosya ve satır aralığı
`mark.getLocation()` üzerinden gelir (`getFileId().getAbsolutePath()`, `getStartLine()`, `getEndLine()`).
`CpdAnalysis` kapatılırken `IOException` fırlatabilir — engine bunu `RuntimeException` olarak sarıyor;
`ProjectAnalyzer` near-miss hatasında exact-duplicate sonuçlarıyla devam eder.
