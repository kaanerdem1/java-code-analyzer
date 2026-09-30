# Risk parametreleri araştırması (v2 öncesi)

Bu belge, standalone analyzer’ın metod risk skorunda kullanılan **eşikler ve ağırlıklar** için literatür/araç karşılaştırması ve **bizim kullanım senaryomuza** (Java monorepo, servis/ETL, katalog/lineage, Türkçe rapor) uyarlanmış öneridir. Kod değişikliği yok; uygulanacak set: `[config/risk-parameters-proposal.yaml](../config/risk-parameters-proposal.yaml)`.

**Sade anlatım (servis metodu örnekleri, adım adım skor):** `[scripts/risk-model-plain-language.md](risk-model-plain-language.md)`

## 1. Ne ölçüyoruz?


| Boyut     | Araçtaki karşılık                   | Not                                          |
| --------- | ----------------------------------- | -------------------------------------------- |
| Dallanma  | Cyclomatic complexity (CC)          | PMD/Sonar/Checkstyle ile kıyaslanabilir      |
| Uzunluk   | Metod `codeLines` (yorum/boş hariç) | PMD NCSS’e yakın; fiziksel satır değil       |
| İç içe    | `maxNestingDepth`                   | Sonar **cognitive** nesting’e kısmi karşılık |
| Parametre | `parameterCount`                    | Uzun parametre listesi / God API             |


**Cognitive complexity** (Sonar S3776, varsayılan **15**) ayrı metrik; şu an CC + nesting ile dolaylı yakalanıyor. v2’de isteğe bağlı eklenebilir.

## 2. Sektörde yaygın eşikler (özet)



### Cyclomatic complexity (metod)


| Kaynak                             | Öneri                                                |
| ---------------------------------- | ---------------------------------------------------- |
| McCabe / NIST 500-235              | **10** (klasik); süreç güçlüyse **15**               |
| Sonar `MethodCyclomaticComplexity` | **10**                                               |
| MISRA / NASA güvenlik              | **15** üstü inceleme                                 |
| JetBrains / ReSharper varsayılan   | **20** uyarı                                         |
| Carnegie Mellon bandları           | 1–10 düşük, 11–20 orta, 21–50 yüksek, 50+ çok yüksek |


**Pratik gate:** çoğu ekip **10–15** arasında durur; enterprise Java için **10 / 15 / 25** üçlüsü hem Sonar hem MISRA ile uyumlu.

### Cognitive / nesting / parametre


| Metrik                            | Yaygın gate                             |
| --------------------------------- | --------------------------------------- |
| Sonar cognitive (metod)           | **15**                                  |
| Parametre (RuboCop, Code Climate) | **4–5**                                 |
| PMD eski metod uzunluğu           | **100** satır (deprecated; NCSS tercih) |




### Proje / sınıf


| Metrik                                | Örnek                     |
| ------------------------------------- | ------------------------- |
| Sınıf WMC (big-code-analysis default) | **60** (percentile-türev) |
| NPath (PMD)                           | **200** (ayrı metrik)     |




## 3. Mevcut v1 modelimiz

- **Alt skor → 0–1** parçalı doğrusal (MEDIUM/HIGH/CRITICAL eşikleri).
- **Metod skoru** = `min(bant tavanı, max(alt skorlar) + 0.15 × ağırlıklı blend)`.
- **Ağırlıklar** (0.40 / 0.20 / 0.25 / 0.15) fiilen yalnızca **%15 blend**’i etkiler; baskın boyut `max()` ile belirlenir (doc madde 11 ile uyumlu).



### v1 eşikleri vs mock-modules (49 dosya, 254 metod)


| Metrik  | p50 | p90 | p95 | max | v1 MED eşiği | Gözlem                                        |
| ------- | --- | --- | --- | --- | ------------ | --------------------------------------------- |
| CC      | 6   | 12  | 12  | 31  | 10           | MED/HIGH anlamlı                              |
| LOC     | 13  | 20  | 20  | 81  | 50           | LOC boyutu **neredeyse hiç devreye girmiyor** |
| Nesting | —   | 5   | 5   | 8   | HIGH=4       | İç içe yapı sık uyarı                         |
| Params  | —   | —   | 4   | 6   | MED=5        | Az tetiklenir                                 |


Örnek özet (enterprise profili kalibrasyonu): `projectRiskScore≈0.36`, metod risk **p95≈0.61** (düzeltilmiş histogram), YÜKSEK+KRİTİK LOC payı ≈ %19.

## 4. Bize uygun öneri (varsayılan profil: `enterprise-java`)

Amaç: **kısa servis metodları**nda gereksiz alarm azaltmak; **ETL/legacy** tarzı derin dallanmayı kaçırmamak; PMD/Sonar ile aynı dilde konuşmak.


| Boyut   | v1 (şimdi)     | Önerilen MED / HIGH / CRIT | Gerekçe                                |
| ------- | -------------- | -------------------------- | -------------------------------------- |
| CC      | 10 / 15 / 30   | **10 / 15 / 25**           | Sonar+MISRA; 30 yerine 25 “böl” bandı  |
| LOC     | 50 / 100 / 200 | **25 / 55 / 120**          | mock p90≈20; god-method hâlâ yakalanır |
| Nesting | 3 / 4 / 8      | **3 / 5 / 7**              | p95=5; HIGH=4 fazla gürültü            |
| Params  | 5 / 7 / 12     | **4 / 6 / 8**              | API şişkinliği erken yakalanır         |


**v2 için hedef ağırlıklar** (blend/dominant yeniden tasarımında): CC **0.38**, nesting **0.32**, LOC **0.18**, params **0.12** — DWH/servis kodunda kontrol akışı + iç içe yapı öncelikli.

Ek profiller YAML’da: `strict-review` (PR gate), `generated-tolerant` (üretilmiş kod).

## 5. Proje seviyesi (dashboard)

Büyük repoda LOC-ağırlıklı `projectRiskScore` alçalır; bu **normal**.


| KPI                          | Ne işe yarar                      |
| ---------------------------- | --------------------------------- |
| `methodRiskScoreP95` / `p99` | Kuyruk riski — “en kötü %5 metod” |
| `highPlusCriticalLocRatio`   | Riskli metodların kod hacmi payı  |
| `criticalMethodsPerKloc`     | KRİTİK yoğunluğu                  |


**Önerilen okuma:** operasyonel özet = **p95 + LOC payı**; tek skor = `projectRiskScore`.

## 6. PMD ile hizalama

Repoda `config/pmd-ruleset-complexity.xml` karşılaştırma için eşikleri **1**’e çekiyor (tüm metodlar violation). Üretim PMD seti için:

- CyclomaticComplexity **methodReportLevel=10**
- CognitiveComplexity **reportLevel=15** (referans; analyzer’da ayrı metrik yok)

Standalone JSON’daki CC, PMD cyclomatic ile **yakın** olmalı; risk skoru **yalnızca bu araca özel**.

## 7. v2’ye geçerken yapılacaklar

1. `config/risk-parameters-proposal.yaml` → runtime config + `--risk-profile`.
2. Skor birleşimini sadeleştir veya ağırlıkları **dominant skora** taşı (README/JSON `riskModel` tutarlı olsun).
3. Golden test: mock-modules’te birkaç bilinen metod CC + skor + seviye.
4. Gerçek DWH/modül reposunda percentile ile profil doğrulama (mock ETL ağırlıklı ama küçük).

## 8. mock-modules kalibrasyon tablosu (Faz 5 girdisi)

Yenileme: `./scripts/calibrate-scan-metrics.sh` → [`scripts/mock-modules-scan-percentiles.md`](mock-modules-scan-percentiles.md).

Son koşu (254 metod): CC p90=12, cognitive p90=15, `outboundDistinctCalls` p90=9, exit p90=6, nesting p90=5 — v1 dörtlüsüyle uyumlu. Mock’ta `lambdaCount` ve catch kalite sayaçları neredeyse sıfır; eşikler gerçek repoda doğrulanmalı. Sınıf `efferentCouplingProxy` mock’ta düz (p90=5); Ce eşiği için gerçek modül şart.

**Skor (2026-09):** `--risk-profile=enterprise-java` ile **17 metod boyutu** skora girer (JSON `riskBreakdown` + `extraSubScores`). Profil yoksa legacy v1 (4 boyut).

---

*Referanslar: [NIST 500-235 / McCabe](https://www2.rivier.edu/faculty/vriabov/nist235r.pdf), [JetBrains threshold guidance](https://github.com/JetBrains/resharper-cyclomatic-complexity/blob/master/docs/ThresholdGuidance.md), [big-code-analysis thresholds](https://dekobon.github.io/big-code-analysis/recipes/thresholds.html), [Sonar S3776 / CC rules](https://stackoverflow.com/questions/55227125/how-to-set-a-custom-complexity-limits-for-sonar-in-java), [PMD NPath / design rules](https://pmd.github.io/pmd/pmd_rules_java_design.html).*







### Araçlar neye bakıyor?


| Araç                              | Metod seviyesi                                                                                                                                    | Sınıf seviyesi                                                                                                    |
| --------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------- |
| **SonarQube**                     | Cyclomatic, Cognitive complexity, LOC, bug/vulnerability/code smell kuralları, teknik borç (düzeltme süresi tahmini)                              | Duplication, comment density, güvenlik hotspot'ları, maintainability/reliability/security rating                  |
| **PMD**                           | CyclomaticComplexity, CognitiveComplexity, **NPathComplexity**, NcssCount, ExcessiveParameterList, LawOfDemeter                                   | **GodClass** (WMC, ATFD, TCC), TooManyFields, TooManyMethods, DataClass, CouplingBetweenObjects, ExcessiveImports |
| **Checkstyle**                    | BooleanExpressionComplexity, NestedIfDepth, NestedTryDepth, ReturnCount, JavaNCSS, NPath                                                          | ClassFanOutComplexity, ClassDataAbstractionCoupling                                                               |
| **SpotBugs / FindSecBugs**        | Null dereference, kaynak sızıntısı, concurrency hataları, SQL injection, zayıf kripto vb. bug pattern'leri                                        | Aynı, sınıf düzeyinde (mutable static, equals/hashCode uyumsuzluğu)                                               |
| **CodeScene (Code Health)**       | Brain Method, Complex Method, **Bumpy Road**, Deep Nested Complexity, Complex Conditional, Many Conditionals, Excess Arguments, String-heavy args | Brain Class, Low Cohesion, Primitive Obsession, Code Duplication, Large Class                                     |
| **NDepend / Understand**          | Halstead, Maintainability Index, cyclomatic, nesting depth, parametre sayısı                                                                      | CK metrikleri (WMC, DIT, NOC, CBO, RFC, LCOM), abstractness/instability                                           |
| **Fortify / Checkmarx / Semgrep** | Taint analizi (kaynak → sink), hardcoded secret, tehlikeli API kullanımı                                                                          | Aynı                                                                                                              |


Buradan çıkan tablo şu: mevcut parametreleriniz (cyclomatic tipi dallanma sayısı + LOC), araçların baktığı şeylerin sadece bir kısmı.

### Metod seviyesi için eklenebilecek parametreler

**Karmaşıklık ailesi**

- **Cognitive complexity:** İç içe yapıları ağırlıklı sayar. Cyclomatic'ten daha iyi "okunabilirlik" göstergesidir.
- **NPath complexity:** Metod içindeki olası yürütme yollarının sayısı. Cyclomatic doğrusal, NPath çarpımsal büyür. Ardışık `if`'ler cyclomatic'te toplanır, NPath'te çarpılır, bu yüzden 10 ardışık `if` çok daha tehlikeli görünür.
- **Max nesting depth** ve **nested try derinliği.**
- **Bumpy Road:** Bir metod içinde birden fazla ayrı "iç içe blok" bölgesi olması. Metod aslında birkaç işi yapıyor ve bölünmeli demektir. Tespit için metodun gövdesinde derinlik ≥2 olan ayrı bölgeleri sayarsınız.
- **Boolean ifade karmaşıklığı:** Tek koşuldaki operatör sayısı (Checkstyle `BooleanExpressionComplexity`), özellikle `&&` ile `||`'nin parantezsiz karışımı.
- **Return/throw/break/continue sayısı:** Çoklu çıkış noktaları akış takibini zorlaştırır.
- **Brain Method:** Bir metodun aynı anda uzun, karmaşık, derin iç içe ve çok değişkenli olması. Tek tek eşik yerine kombinasyon kuralı olarak yazılabilir.

**Boyut ve okunabilirlik**

- **NCSS** (yorum/boş satır hariç ifade sayısı), LOC yerine daha adil.
- **Halstead metrikleri** (hacim, zorluk, efor) ve **Maintainability Index.**
- **Yerel değişken sayısı**, parametre sayısı, boolean flag parametreleri.
- **Aynı türden parametre dizisi** (`String, String, String, int, int`), sıralama hatasına açık.
- **Magic number/string sayısı**, metod başına string literal yoğunluğu.
- **Yorum yoğunluğu:** Çok karmaşık ama hiç yorum/javadoc'u olmayan metod.
- **Metod içi duplication** (aynı blokların tekrarı).

**Tasarım kokuları (metod düzeyi)**

- **Feature Envy:** Metodun kendi sınıfından çok başka bir sınıfın alanlarına/getter'larına erişmesi.
- **Law of Demeter ihlali:** `a.getB().getC().doX()` tarzı zincirler.
- **Long Parameter List** ve **Data Clump.**
- **Primitive Obsession:** Domain kavramlarının `String/int` ile taşınması.

**Hata yönetimi kalitesi**

- Boş catch, sadece `printStackTrace`, çok geniş `catch (Exception/Throwable)`, exception'ı yutma, `throws Exception`.
- Catch içinde yeni exception fırlatırken `cause`'u kaybetme.
- `finally` içinde `return` veya throw.
- try-with-resources kullanılmayan `Closeable` nesneler.

**Bug pattern'leri (SpotBugs/PMD tarzı, AST ile yakalanabilenler)**

- `return null` (özellikle Collection/Optional döndüren metodda), kontrolsüz `Optional.get()`.
- `equals` ile `==` karışımı, `String` karşılaştırmasında `==`.
- Döngüde string `+=` birleştirme, döngü içinde regex derleme.
- `Thread.sleep`, `System.exit`, `System.out.println` kullanımı.
- Swallow edilen `InterruptedException`.
- `catch` içinde `return` ile hata gizleme.
- Yakalanmayan/kontrolsüz cast, kontrolsüz `List.get(0)`.

**Güvenlik desenleri (basit taint / desen tabanlı)**

- String birleştirmeyle kurulan SQL (`"SELECT ... " + param`), `Runtime.exec`, `ProcessBuilder` içinde kullanıcı girdisi.
- `ObjectInputStream.readObject`, XML parser'da XXE korumasız kullanım.
- Zayıf algoritmalar (MD5, SHA-1, DES, `java.util.Random` ile token üretimi).
- Hardcoded şifre/token/anahtar (değişken adı `password`, `secret`, `apiKey` ve string literal atanması).
- Sertifika doğrulamayı kapatma (`TrustAll`, `HostnameVerifier` her zaman true).
- Loglara hassas veri yazma (`log.info("... " + password)`).

**Eşzamanlılık ve yan etkiler**

- `synchronized` blok/metod, lock kullanımı, `volatile`, `wait/notify`.
- Static mutable alana yazma, paylaşılan koleksiyonun senkronizasyonsuz değiştirilmesi.
- `SimpleDateFormat` gibi thread-safe olmayan nesnelerin alan olarak tutulması.
- Reflection, dinamik sınıf yükleme, `Class.forName`.



### Sınıf seviyesi için parametreler

Sınıf risk puanı, metod puanlarının ortalaması olmamalı. Kendi başına özellikleri var:

- **WMC (Weighted Methods per Class):** Sınıftaki metodların karmaşıklık toplamı.
- **LCOM / TCC (cohesion):** Metodlar aynı alanları mı kullanıyor, yoksa sınıf birbirinden bağımsız iki üç işi mi yapıyor? Düşük cohesion, ayrılması gereken sınıf demektir.
- **God Class:** WMC yüksek + ATFD (başka sınıfların verisine erişim) yüksek + TCC düşük kombinasyonu (PMD kuralı).
- **Alan sayısı, metod sayısı, public metod sayısı**, sınıf LOC'u.
- **CBO / import sayısı:** Sınıfın kullandığı farklı tip sayısı. (Bu fan-out'a benziyor ama sınıfın kendi kodundaki yapısal bakış, çağrı grafiği değil.)
- **Data Class / anemic model:** Sadece getter/setter, hiç davranış yok.
- **Mutable public/static alanlar**, encapsulation ihlalleri.
- **Inheritance derinliği (DIT)** ve override edilen metod oranı.
- **Sınıf içi duplication** ve metodlar arası benzerlik.
- **Bir sınıfta biriken "riskli metod" sayısı ve dağılımı:** Örneğin risk puanı yüksek metodların oranı, en kötü metodun puanı.



### Puanlamada araçların yaptığı ortak şeyler

- **SonarQube:** Her kural ihlaline tahmini bir "düzeltme süresi" atar, toplamı teknik borç yapar ve koda oranlayıp A-E reyting üretir. Yani puan ağırlıklı ihlal toplamıdır.
- **CodeScene:** Her kod kokusu için ceza puanı verir ve ağırlıkları kokunun ciddiyetine göre belirler. Tek bir "kötü" metod tüm dosyayı aşağı çekebilir.
- **Eşikler kademeli:** "15 üstü kötü" gibi ikili değil, "10-15 hafif, 15-25 orta, 25+ ağır" gibi bantlar.
- **Kombinasyon kuralları:** Tek metrik yüksek olabilir ama üç metrik birden yüksekse (uzun + derin + çok koşullu) ceza katlanır. Brain Method tam olarak bu.



### Önerim

Mevcut tasarıma en az eforla ve en çok fayda ile eklenecekler (hepsi JavaParser AST'siyle çıkarılabilir):

1. **Cognitive complexity + NPath + max nesting depth** (cyclomatic ve LOC'un yerini alsın/tamamlasın).
2. **Hata yönetimi kalite kuralları** (boş catch, geniş catch, yutma, kaynak sızıntısı).
3. **Basit güvenlik desenleri** (string-birleştirmeli SQL, hardcoded secret, zayıf kripto).
4. **Bumpy Road / Brain Method** gibi bileşik kural.
5. **Sınıf seviyesinde God Class** (WMC + TCC + alan/metod sayısı).

