# Standalone Java Code Analyzer — Teknik Dokümantasyon

> **Repo durumu (2026-10):** Ana repo; enterprise YAML (**enterprise-java v3**) tek risk skoru (Halstead,
> kod/exception kokuları, sınıf LCOM3/Ce dahil). Incremental cache: modül toplu skip, dosya byte hash,
> AST `semanticFileHash` (kozmetik değişiklikte analiz skip), metod hash. Ayrıntı:
> [docs/incremental-cache-tr.md](docs/incremental-cache-tr.md). Script varsayılan `--state`; `NO_STATE` / `FRESH`
> ortam değişkenleri.

CI/CD veya harici sunucu olmadan Java kaynak ağacını tarayan, JavaParser ile metrik/risk üreten,
Türkçe Markdown + JSON raporlayan, `analyzer-state.json` ile artımlı çalışan JDK 17+ konsol uygulaması.

Paket: `com.standalone.analyzer` (genişleyen monorepo; bu belgedeki “17 dosya” satır sayısı artık geçerli değil).

## İçindekiler

1. [Genel Akış](#genel-akış)
2. [Veri Modeli (DTO) Sınıfları](#veri-modeli-dto-sınıfları)
3. [Risk Hesaplama Sınıfları](#risk-hesaplama-sınıfları)
4. [AST Analiz Motoru](#ast-analiz-motoru)
5. [İki Aşamalı Lazy Parsing (Cache) Sınıfları](#i̇ki-aşamalı-lazy-parsing-cache-sınıfları)
6. [Orkestrasyon ve Giriş Noktası](#orkestrasyon-ve-giriş-noktası)
7. [Uçtan Uca Akış Özeti](#uçtan-uca-akış-özeti)

---

## Genel Akış

```
Java6CodeAnalyzerMain (CLI)  —  --state, --fresh, --risk-profile=enterprise-java, --markdown
        │
        ▼
  ProjectAnalyzer.analyze()
        │  ├─ analyzer-state.json yükle (cacheIdentity uyuşmazsa sıfırla)
        │  ├─ collectJavaFiles
        │  ├─ IncrementalModulePlanner → modül fingerprint (pom/Gradle kökü)
        │  │      └─ modül aynıysa → tüm modül dosyaları state'ten (parse yok)
        │  ├─ her dosya:
        │  │      Stage 1 byte hash → aynıysa state'ten
        │  │      else parse → semanticFileHash aynıysa state'ten (kozmetik; analiz yok)
        │  │      else FileMetricsBuilder + ComplexityVisitor + SupplementalMetrics
        │  │      Stage 2 metod hash → değişmeyen metod reuse
        │  ├─ IncrementalChangeDetector → incrementalChanges (JSON/Markdown)
        │  └─ state kaydet (fileHash, semanticFileHash, module fingerprints)
        ▼
  AnalysisReport → JSON (--output) + Türkçe Markdown (--markdown)
```

---

## Veri Modeli (DTO) Sınıfları

Bu sınıflar salt veri taşıyıcısıdır (Java `record`), iş mantığı içermezler; JSON çıktısının
şeklini belirlerler.

### `RiskLevel.java`
Risk skorunu (0.0–1.0) dört banda ayıran `enum`: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.
- **`fromScore(double score)`** — verilen skora karşılık gelen bandı döndürür.
- **`maxScore()`** — bu bandın kapsadığı en yüksek skor değeri (üst sınır hesapları için).

### `MethodMetric.java`
Tek bir metod/constructor'ın tüm analiz sonucunu tutan `record`. Alanlar: `name`, `kind`
(METHOD/CONSTRUCTOR), `signature`, `startLine`/`endLine`, `cyclomaticComplexity`, `physicalLines`,
`codeLines`, `logicalStatements`, `maxNestingDepth`, `parameterCount`, `godMethod`, `riskScore`,
`riskLevel`, `riskFactors`, `halstead` (bkz. `HalsteadMetrics`), `exceptionSmells`, `codeSmells`,
`methodHash` (Stage-2 cache anahtarı), `analysisReused`, Halstead/koku listeleri,
`accessedFieldNames` (LCOM3). Tek risk alanı: `riskScore` (enterprise-java v3).

### `AnalysisReport.java`
Kök JSON DTO'su; iç içe altı `record` barındırır:
- **`AnalysisReport`** — `tool`, `generatedAt`, `analyzedPath`, `parserLanguageLevel`, `summary`,
  `cacheStatistics`, `topRiskyMethods`, `files`, `errors`.
- **`Summary`** — proje genelinde dosya/sınıf/metod sayıları, ortalama CC, `projectRiskScore`,
  `projectGranularRiskScore`, God Method/Class sayıları, exception/code-smell toplamları, ortalama
  efferent coupling ve LCOM3.
- **`CacheStatistics`** — `filesSkippedViaFileHash`, `filesSkippedViaModuleBulk`,
  `filesSemanticCosmeticOnly`, `filesReanalyzed`, `modulesUnchanged`, metod sayaçları, `scanTimeMillis`.
- **`IncrementalChanges`** — önceki state’e göre dosya/metod diff; `filesCosmeticOnly`, `COSMETIC` dosya türü.
- **`FileMetric`** — dosya bazlı toplamlar, `fileHash`, `fullyReusedFromCache`,
  `methodsReusedFromCache`. Ayrıca bir yardımcı fonksiyon içerir:
  - **`reusedCopy()`** — önceki taramadan gelen bir `FileMetric`'i, "bu taramada tamamen cache'ten
    geldi" bayrağıyla işaretlenmiş yeni bir kopyasını üretir.
- **`ClassMetric`** — sınıf bazlı toplamlar: WMC, `efferentCoupling`, `dependentTypes`, `lcom3`,
  `godClassCandidate`, exception/code-smell sayaçları, `granularRiskScore`.
- **`RiskHotspot`** — en riskli metodların özet görünümü (dosya, sınıf, metod, skor, faktörler).
- **`FileError`** — parse edilemeyen dosyalar için hata kaydı.

### `HalsteadMetrics.java`
Bir metod/sınıf/dosyanın Halstead metriklerini tutan `record` (`distinctOperators`,
`totalOperators`, `distinctOperands`, `totalOperands`, `vocabulary`, `length`, `volume`,
`difficulty`, `effort`).
- **`from(Map<String,Integer> operators, Map<String,Integer> operands)`** — toplanan operatör/
  operand frekans haritalarından Halstead formüllerini (`n1`, `n2`, `N1`, `N2`, hacim=`N·log2(n)`,
  zorluk=`(n1/2)·(N2/n2)`, efor=`zorluk·hacim`) hesaplayıp `record`'u üretir.
- **`round2(double)`** *(private)* — sonuçları 2 ondalığa yuvarlar.

### `ExceptionSmell.java`
Bir `catch` bloğunda tespit edilen anti-pattern'i tutan `record` (`type`, `exceptionType`, `line`).
`Type` enum: `SWALLOWED_EXCEPTION` (yutulan istisna), `GENERIC_EXCEPTION_CATCH` (genel tip
yakalama). Fonksiyon içermez.

### `CodeSmell.java`
Bir Java 6 code smell bulgusunu tutan `record` (`type`, `line`, `detail`). `Type` enum:
`RAW_TYPE`, `STRING_CONCAT_IN_LOOP`, `HARDCODED_IP`, `HARDCODED_SQL`, `HARDCODED_URL`. Fonksiyon
içermez.

---

## Risk Hesaplama Sınıfları

### `RiskCalculator.java`
Yapısal (CC/LOC/nesting/parametre) riski 0.0–1.0 aralığında hesaplayan çekirdek sınıf.
- **`assessMethod(int cyclomatic, int codeLines, int nesting, int parameters)`** — dört metriği
  ayrı ayrı `subScore` ile normalize edip en baskın (dominant) değeri + küçük bir "bileşik bonus"
  ile birleştirerek metod risk skorunu üretir (`Assessment` record: skor, bant, sebep listesi).
- **`isGodMethod(int cyclomatic, int codeLines, int parameters)`** — LOC>200, CC>30 veya
  (LOC>100 ve (CC>15 veya parametre>7)) koşuluyla "God Method" tespiti yapar.
- **`assessAggregate(List<MethodMetric> methods, int codeLines, int wmc, Scope scope)`** — sınıf
  veya dosya seviyesinde, en kötü metodun etkisini + toplam boyutu + WMC'yi birleştirerek toplu
  risk değerlendirmesi yapar (`Scope` enum'u `CLASS`/`FILE` eşiklerini ayırır).
- **`compose(double dominant, double blend, List<String> factors)`** *(private)* — dominant alt
  skor ile ortalama alt skoru birleştirip bandın üst sınırını aşmayacak şekilde nihai skoru üretir.
- **`subScore(int value, int medium, int high, int critical)`** *(package-private)* — bir ham
  değeri (örn. CC=18) parçalı doğrusal bir eğriyle 0–1 aralığına normalize eder; `MEDIUM`/`HIGH`/
  `CRITICAL` eşiklerini bant sınırlarıyla hizalar. `GranularRiskCalculator` tarafından da kullanılır.
- **`round3(double value)`** — skoru 3 ondalığa yuvarlar.

### `HalsteadMetrics`, `RiskCalculator` dışında iki yardımcı analiz sınıfı daha var:

### `TypeDependencyAnalyzer.java`
Sembol çözümleyici (classpath) kullanmadan, salt AST üzerinden **efferent coupling (Ce)**
tahmini yapan yardımcı sınıf (statik, instance edilemez).
- **`collectEfferentTypes(TypeDeclaration<?> declaration)`** — sınıfın gövdesindeki tüm
  `ClassOrInterfaceType` referanslarını toplar, kendi adını ve generic tip parametrelerini
  (`T`, `E` vb.) eleyerek dış bağımlılık kümesini (isim bazlı, sıralı `Set<String>`) döndürür.

### `CohesionAnalyzer.java`
Henderson-Sellers **LCOM3** kohezyon formülünü uygulayan yardımcı sınıf (statik, instance
edilemez).
- **`lcom3(Set<String> fieldNames, List<Set<String>> methodFieldAccesses)`** — `((Σμ(f)/a) - m) /
  (1 - m)` formülüyle 0 (tam kohezif) ile 2 (God Class adayı) arası bir değer üretir; alan sayısı
  0 veya metod sayısı ≤1 ise 0 döner (uygulanamaz durum).

### `GranularRiskCalculator.java`
Tüm sinyalleri (yapısal risk, Halstead, exception anti-pattern, coupling, cohesion, code smell)
ağırlıklı toplayarak **0.00–100.00 aralığında hassas ve sürekli bir skor** üreten sınıf — bant
sınırlarındaki kümelenmeyi önlemek için tasarlandı.
- **`methodScore(double structuralRisk01, HalsteadMetrics h, int swallowedExceptions,
  int genericCatches, int rawTypeUsages, int concatInLoop, int hardcodedLiterals)`** — metod
  seviyesinde ağırlıklı skor (yapısal %35, Halstead %20, exception %25, code smell %20).
- **`classScore(...)`** — sınıf seviyesinde aynı mantık + coupling (%15) ve cohesion (%15)
  ağırlıklarıyla genişletilmiş versiyon.
- **`round(double)`**, **`clamp01(double)`**, **`round2(double)`** *(private yardımcılar)* —
  sırasıyla tam sayıya yuvarlama, [0,1] aralığına sıkıştırma, 2 ondalığa yuvarlama.

---

## AST Analiz Motoru

### `ComplexityVisitor.java` (en büyük dosya, ~856 satır)
JavaParser `VoidVisitorAdapter`'dan türeyen, tek bir `CompilationUnit`'i (bir `.java` dosyasını)
gezip tüm metrikleri toplayan çekirdek ziyaretçi sınıf.

**Kurucular**
- **`ComplexityVisitor(RiskCalculator, BitSet codeLines)`** — cache'siz (soğuk) çalışma.
- **`ComplexityVisitor(RiskCalculator, BitSet codeLines, Map<String,MethodMetric> previousMethods)`**
  — Stage-2 cache haritasıyla çalışma; `previousMethods` önceki taramanın `"SınıfAdı#imza"` →
  `MethodMetric` eşlemesidir.

**Statik/genel yardımcılar**
- **`computeCodeLines(CompilationUnit cu)`** — dosyadaki her satırın en az bir gerçek (boşluk/
  yorum olmayan) token içerip içermediğini işaretleyen bir `BitSet` üretir (net LOC hesabı için).
- **`getClassMetrics()`** — bu dosyada tamamlanan tüm `ClassMetric`'leri satır sırasına göre
  döndürür.
- **`getFileOperators()` / `getFileOperands()`** — dosya genelinde birleştirilmiş Halstead
  operatör/operand frekans haritaları.

**Tip (sınıf/interface/enum) analizi**
- **`visit(ClassOrInterfaceDeclaration, Void)` / `visit(EnumDeclaration, Void)`** — `analyseType`'ı
  çağırarak tip gezimini başlatır.
- **`analyseType(...)`** *(private)* — yeni bir `TypeContext` açar, alt ağacı gezer, bitince
  `buildClassMetric` ile `ClassMetric`'i üretip `completedTypes` listesine ekler.
- **`extractFieldNames(...)`** *(private static)* — sınıfın static olmayan alan adlarını toplar
  (LCOM3 hesabında kullanılacak alan kümesi).
- **`buildClassMetric(...)`** *(private)* — sınıfın WMC'sini, Halstead'ini, exception/code-smell
  toplamlarını, `TypeDependencyAnalyzer` ile efferent coupling'ini, `CohesionAnalyzer` ile
  LCOM3'ünü hesaplayıp `GranularRiskCalculator.classScore` ile nihai `ClassMetric`'i kurar.
- **`visit(FieldDeclaration, Void)`** — alan tipinin raw generic kullanımı (örn. `List` yerine
  `List<String>` beklenirken) olup olmadığını kontrol eder.

**Metod/constructor analizi**
- **`visit(MethodDeclaration, Void)` / `visit(ConstructorDeclaration, Void)`** —
  `analyseCallable`'ı tetikler (gövdesi olmayan abstract/native metodlar atlanır).
- **`analyseCallable(...)`** *(private)* — bu sınıfın kalbi: gövdeyi gezer (iç içe anonim
  sınıfların keşfi için her zaman gerekli), ardından `HashService.normalizedMethodHash` ile
  metod hash'ini hesaplar, `previousMethods` haritasında arar; hash eşleşirse eski
  `riskScore`/`riskLevel`/`riskFactors`/`halstead`/`godMethod`/`granularRiskScore`'u aynen
  kopyalar (Stage 2 cache hit), eşleşmezse `RiskCalculator`, `HalsteadMetrics.from` ve
  `GranularRiskCalculator.methodScore`'u çalıştırıp taze sonuç üretir. Son olarak yeni
  `MethodMetric`'i sahibi olan `TypeContext`'e ekler ve operatör/operand haritalarını hem sınıf
  hem dosya seviyesine birleştirir (`mergeInto`).

**Karar noktası / operatör kaydı (`visit` override'ları — her biri tek bir AST düğüm tipini işler)**
`IfStmt`, `ForStmt`, `ForEachStmt`, `WhileStmt`, `DoStmt`, `SwitchStmt`, `SwitchEntry`, `TryStmt`,
`CatchClause`, `SynchronizedStmt`, `ConditionalExpr`, `BinaryExpr`, `UnaryExpr`, `AssignExpr`,
`ReturnStmt`, `ThrowStmt`, `BreakStmt`, `ContinueStmt`, `MethodCallExpr`, `ObjectCreationExpr`,
`ArrayCreationExpr`, `ArrayAccessExpr`, `CastExpr`, `InstanceOfExpr` — her biri hem cyclomatic
complexity'yi (uygun olanlarda `addDecisionPoint()`), hem de Halstead operatör sayacını
(`recordOperator(...)`) günceller; döngü ifadeleri ayrıca nesting derinliğini artırır.

**Operand kaydı**
`NameExpr`, `FieldAccessExpr` (aynı zamanda `markFieldAccessIfMatch` ile LCOM3 için alan erişimini
işaretler), `VariableDeclarator`, `Parameter` (raw type kontrolü de burada), `IntegerLiteralExpr`,
`LongLiteralExpr`, `DoubleLiteralExpr`, `CharLiteralExpr`, `BooleanLiteralExpr`, `NullLiteralExpr`,
`StringLiteralExpr` (ayrıca `detectHardcodedLiteral` ile IP/SQL/URL taraması yapar) — her biri
`recordOperand(...)` çağırır.

**Anti-pattern / smell tespit yardımcıları** *(hepsi private)*
- **`detectExceptionAntiPatterns(CatchClause)`** — genel tip yakalama ve yutulan istisna
  kontrolünü yapıp `ExceptionSmell` ekler.
- **`isGenericExceptionType(String)`** — tip adı `Exception`/`Throwable` mı kontrol eder.
- **`isSwallowed(BlockStmt)`** — catch gövdesi boş mu ya da sadece "gürültü" ifadeler mi içeriyor.
- **`isNoiseOnlyStatement(Statement)`** — bir ifadenin sadece `printStackTrace()` ya da
  `System.out/err.print(ln)` çağrısı olup olmadığını kontrol eder.
- **`detectStringConcatInLoop(AssignExpr)`** — döngü içinde `String` tipli bir değişkende `+=`
  veya `x = x + ...` deseni arar.
- **`detectHardcodedLiteral(StringLiteralExpr)`** — string literal'i IP/SQL/URL regex'lerine
  karşı test edip uygun `CodeSmell`'i method veya sınıf seviyesine ekler.
- **`rawTypeSmell(Type, int line)`** — bir tipin, tip argümanı verilmeden kullanılan bilinen bir
  generic-capable JDK tipi (List, Map, ...) olup olmadığını kontrol eder.
- **`markFieldAccessIfMatch(String name)`** — bir isim geçerli sınıfın alanlarından biriyle
  eşleşiyorsa, o metodun erişilen-alanlar kümesine ekler.

**Genel yardımcılar** *(hepsi private/static)*
`addDecisionPoint()`, `recordOperator(String)`, `recordOperand(String)`, `withNesting(Runnable)`,
`withLoop(Runnable)` (nesting + loop derinliğini birlikte yönetir), `isElseIf(IfStmt)`,
`countCodeLines(int,int)`, `countStatements(BlockStmt)`, `signatureOf(CallableDeclaration<?>)`,
`countExceptionSmells(...)`, `countCodeSmells(...)`, `countHardcoded(...)`, `isHardcoded(...)`,
`mergeInto(Map,Map)`, `truncate(String)`, `round2(double)`, `beginLine(Node)`, `endLine(Node)`.

**İç yardımcı sınıflar (private static)**
- **`MethodContext`** — tek bir metod gezimi sırasında biriken geçici durum: `cyclomatic`,
  `depth`, `maxDepth`, `loopDepth`, `operators`, `operands`, `localTypes`, `accessedFields`,
  `exceptionSmells`, `codeSmells`.
- **`TypeContext`** — tek bir sınıf gezimi sırasında biriken durum: `name`, `kind`, `fieldNames`,
  `methods`, birleştirilmiş `operators`/`operands`, `methodFieldAccessSets`,
  `classLevelCodeSmells`.

---

## İki Aşamalı Lazy Parsing (Cache) Sınıfları

### `HashService.java`
SHA-256 tabanlı hash üretimini `java.security.MessageDigest` ile yapan, harici bağımlılık
gerektirmeyen yardımcı sınıf (statik, instance edilemez).
- **`hashFile(Path file)`** — dosyanın ham byte içeriğinin SHA-256 hex hash'i (**Stage 1**).
- **`semanticCompilationUnitHash(CompilationUnit cu)`** — dosya AST özeti (**Stage 1b**, kozmetik drift).
- **`normalizedMethodHash(CallableDeclaration<?> declaration)`** — metod gövdesi özeti (**Stage 2**).
- **`sha256Hex(byte[] data)`** *(private)* — `MessageDigest("SHA-256")` ile hash alıp hex string'e
  çevirir.

### `AnalyzerState.java`
`analyzer-state.json`'ın bellekteki karşılığı olan basit POJO model (Gson ile serileştirilir).
- **`AnalyzerState`** — `cacheIdentity`, `lastScanTimestamp`, `files`, `modules` (modül fingerprint).
- **`FileState`** — `fileHash`, `semanticFileHash`, `cachedFileMetric`, `methods`.
- **`MethodState`** — `methodHash`, `lastCalculatedRiskScore` (enterprise `riskScore` özeti).

Fonksiyon içermez, salt veri taşır.

### `StatePersistenceManager.java`
`analyzer-state.json` dosyasının Gson ile okunup yazılmasından sorumlu sınıf.
- **`load(Path stateFile)`** — dosya yoksa veya bozuksa boş bir `AnalyzerState` döndürür
  (hata durumunda konsola `[WARN]` yazıp çalışmaya devam eder, programı durdurmaz).
- **`save(Path stateFile, AnalyzerState state)`** — `lastScanTimestamp`'i günceller, gerekli
  klasörleri oluşturur, JSON'u UTF-8 olarak yazar; yazma hatası olursa `[WARN]` loglar.

### `IncrementalAnalysisEngine.java`
`ProjectAnalyzer`'ın her dosya için çağırdığı, iki aşamalı cache mantığının orkestrasyonunu yapan
sınıf.
- **`checkFileCache(Path absoluteFile, String relativePath)`** *(Stage 1)* — dosyanın hash'ini
  alır, önceki state'te aynı yol+hash var mı bakar; varsa önceki `FileMetric`'i, yoksa `null`
  taşıyan bir `CacheCheck` kaydı döndürür. İçindeki **`CacheCheck.canSkipParsing()`** metodu bu
  dosyanın parse edilmesine gerek olup olmadığını söyler.
- **`buildFileMetric(String relativePath, CompilationUnit cu, String fileHash)`** *(Stage 2)* —
  bu dosya için önceki metod hash'lerini (`previousMethodsByKey`) toplar, `ComplexityVisitor`'ı bu
  haritayla başlatıp AST'yi gezdirir, ardından dosya seviyesi toplamları (WMC, Halstead, risk,
  granular skor, exception/code-smell sayıları) hesaplayıp nihai `FileMetric`'i üretir.
- **`previousMethodsByKey(String relativePath)`** *(private)* — önceki taramanın
  `cachedFileMetric`'inden `"SınıfAdı#imza"` → `MethodMetric` haritası çıkarır.
- **`round2(double)`** *(private)* — 2 ondalığa yuvarlama.

---

## Orkestrasyon ve Giriş Noktası

### `ProjectAnalyzer.java`
Tüm taramayı yöneten üst düzey sınıf: dosya keşfi, cache kontrolü, özet/hotspot derlemesi.
- **`ProjectAnalyzer(Charset sourceCharset, int topN, Path stateFile, boolean ignoreCache)`** —
  JavaParser'ı `LanguageLevel.JAVA_6` ile yapılandırır.
- **`analyze(Path root)`** — ana akış: state'i yükler, dosyaları tarar, her dosya için Stage-1
  kontrolünü yapar (geçerse `reusedCopy()`, geçmezse parse edip `buildFileMetric`), süreyi ölçer,
  yeni state'i `persistState` ile kaydeder, konsola özet basar, `Summary`/`CacheStatistics`/
  `RiskHotspot` listelerini derleyip nihai `AnalysisReport`'u döndürür.
- **`persistState(List<FileMetric> files)`** *(private)* — taranan tüm dosyalardan yeni bir
  `AnalyzerState` inşa edip `StatePersistenceManager.save` ile diske yazar.
- **`printCacheSummary(CacheStatistics)`** *(private static)* — "Total Files: 500 | Skipped via
  File Hash: 480 | ..." formatındaki performans özetini stderr'e basar.
- **`collectJavaFiles(...)`** *(private)* — `Files.walkFileTree` ile `.git`, `target` vb.
  klasörleri atlayarak tüm `.java` dosyalarını toplar.
- **`relativePath(...)`**, **`describe(List<Problem>)`** *(private static)* — yol/hata metni
  yardımcıları.
- **`buildSummary(...)`** *(private)* — tüm dosya/sınıf/metod verilerinden proje geneli
  `Summary`'i (ağırlıklı ortalamalar, God Method/Class sayıları, LCOM3/coupling ortalamaları)
  hesaplar.
- **`buildHotspots(List<FileMetric>)`** *(private)* — metodları `riskScore`'a göre sıralayıp `--top` kadarını seçer.
- **`Counters`** *(private static iç sınıf)* — tarama sırasında biriken sayaçlar (kaç dosya
  atlandı/yeniden analiz edildi, kaç metod atlandı/yeniden analiz edildi).

### `Java6CodeAnalyzerMain.java`
Konsol giriş noktası.
- **`main(String[] args)`** — `run(args)`'ı çağırıp exit code ile çıkar.
- **`run(String[] args)`** — argümanları `Options.parse` ile ayrıştırır, yol geçerliliğini
  kontrol eder, `ProjectAnalyzer.analyze`'ı çalıştırır, JSON'u yazar, özet log satırını basar.
- **`writeJson(AnalysisReport, Options)`** *(private static)* — Gson ile (pretty veya compact)
  JSON üretip `--output` dosyasına veya stdout'a yazar.
- **`printUsage()`** *(private static)* — yardım metnini basar.
- **`Options`** *(private record)* — CLI argümanlarının ayrıştırılmış hali (`path`, `output`,
  `top`, `charset`, `compact`, `stateFile`, `fresh`, `help`).
  - **`parse(String[] args)`** — `--path=`, `--output=`, `--top=`, `--encoding=`, `--compact`,
    `--state=`, `--fresh`, `--help` bayraklarını okur.
  - **`value(String arg)`**, **`parseTop(String raw)`** *(private static)* — değer ayrıştırma
    yardımcıları.

---

## Uçtan Uca Akış Özeti

1. **CLI** argümanları okur, `ProjectAnalyzer`'ı kurar.
2. **`ProjectAnalyzer.analyze`** önceki `analyzer-state.json`'ı yükler, dosya ağacını tarar.
3. **Modül fingerprint** aynıysa modüldeki tüm dosyalar state’ten yüklenir.
4. **Stage 1** byte hash aynıysa dosya state’ten; farklıysa parse.
5. **Stage 1b** `semanticFileHash` aynıysa kozmetik — analiz atlanır; farklıysa **`ComplexityVisitor`** +
   **`SupplementalMetrics`** ve **enterprise-java v3** `RiskCalculator`:
   - **Stage 2** metod hash ile değişmeyen metodlar reuse.
   - Aynı gezinti sırasında exception anti-pattern'leri, Java 6 code smell'leri, alan erişimleri
     (LCOM3 için) ve Halstead operatör/operand sayaçları da toplanır.
   - Sınıf tamamlanınca `TypeDependencyAnalyzer` (Ce) ve `CohesionAnalyzer` (LCOM3) devreye girer.
5. **`ProjectAnalyzer`** tüm dosyaları toplayıp proje geneli `Summary`, `CacheStatistics` ve
   `RiskHotspot` listesini derler, yeni durumu `StatePersistenceManager` ile
   `analyzer-state.json`'a yazar.
6. **`Java6CodeAnalyzerMain`** nihai `AnalysisReport`'u JSON olarak stdout'a veya `--output`
   dosyasına yazar ve konsola özet performans/risk bilgisini basar.
