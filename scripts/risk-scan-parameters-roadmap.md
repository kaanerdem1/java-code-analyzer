# Servis–metod–sınıf taraması: yeni parametre araştırması

## Sıra (senin dediğin gibi)

1. **Ne ölçeceğiz?** — taramaya eklenecek boyutlar + anlamları (bu belge)  
2. **Eşikler ve ağırlıklar** — profil (`enterprise-java` vb.) **kesinleşsin**  
3. **`--risk-profile`** — YAML → `RiskCalculator` (puanlama bağlantısı)  
4. **Golden test** — mock-modules + gerçek modül

Profile’ı (2→3) eşikler oturmadan bağlamak, yanlış alarm oranını kodda mühürler. Doğru sıra bu.

---

## Bugün taramada ne var?

| Seviye | Toplanan | Risk formülünde? |
|--------|----------|------------------|
| Metod | CC, codeLines, maxNestingDepth, parameterCount | Evet (4 girdi) |
| Metod | logicalStatements, physicalLines | **Hayır** (zaten sayılıyor) |
| Metod | godMethod (heuristik) | Ayrı bayrak |
| Sınıf | WMC, LOC, metod listesi, max/avg metod skoru | Sınıf skoru |
| Proje | p95, KRİTİK/KLOC, LOC payı | Özet KPI |

---

## Literatür / araçlardan alınabilecek fikirler

Kaynaklar: [Sonar metrik tanımları](https://docs.sonarsource.com/sonarqube-server/user-guide/code-metrics/metrics-definition), [Cognitive Complexity (Sonar)](https://www.sonarsource.com/blog/cognitive-complexity-because-testability-understandability/), [PMD JavaMetrics](https://docs.pmd-code.org/apidocs/pmd-java/7.24.0/net/sourceforge/pmd/lang/java/metrics/JavaMetrics.html), [CK metrics](https://github.com/mauricioaniche/ck), [codemetrics4j](https://github.com/codemetrics4j/codemetrics4j), [CodeScene — etki × karmaşıklık](https://codescene.com/blog/prioritize-technical-debt-by-impact/).

### Metod — “servis metodu ne yapıyor?”

| Öneri | Ne ölçer? | Serviste ne demek? | Tipik gate (literatür) | Bizde uygulanabilirlik |
|-------|-----------|--------------------|-------------------------|-------------------------|
| **A. Cognitive complexity** | Okunabilirlik; iç içe her yapı ek puan | `PaymentOrchestrator` içinde 5 kat `if/for` → lasagna; CC düşük olsa bile anlamak zor | Sonar **15** / metod ([S3776](https://stackoverflow.com/questions/55227125/how-to-set-a-custom-complexity-limits-for-sonar-in-java)) | **Yüksek** — JavaParser visitor; CC’den farklı sayım (else-if, nesting increment) |
| **B. logicalStatements** (mevcut alan) | Blok hariç ifade/statement adedi | Uzun ama düz prosedür (çok satır, az dal) | big-code-analysis `nexits` / statement yoğunluğu ile birlikte düşünülür | **Çok yüksek** — zaten var, sadece risk boyutuna açılacak |
| **C. NPath** | Tam yol sayısı (dal çarpımı) | Generated SQL / nested ternary; CC’den daha sert | PMD **200** | **Orta** — maliyetli; tek dosyada stack/perf dikkat |
| **D. return / break / continue (nexits)** | Çıkış noktası sayısı | Erken return çok → akış kovalamak zor | Code Climate **4–5** | **Yüksek** — visitor |
| **E. catch sayısı** | `catch` blokları | Her exception kolu = iş kuralı + test yükü | (çoğu araç CC’ye dahil) | **Yüksek** |
| **F. Operasyon fan-out (FOUT)** | Metodun çağırdığı **farklı tür/metod** sayısı (aynı dosya + basit isim) | Orchestrator: 12 servis/repo çağrısı → coupling, mock zor | PMD ATFD **>3** şüpheli; CK **FAN-OUT** sınıf düzeyinde | **Orta–yüksek** — `MethodCallExpr` + `ObjectCreationExpr`; tam çözümleme yok, **proxy** |
| **G. Lambda / anonim yoğunluğu** | Metod içi lambda sayısı | Stream + callback servisleri | — | **Yüksek** — kısmen var (anonim ayrı metrik) |
| **H. Switch kolu sayısı** | `case` etiketli kol sayısı | Rule engine / status enum | CC’ye girer; ayrı rapor faydalı | **Yüksek** |

**Önerilen metod paketi (v2 tarama):** **A + B + D + E + F** (C isteğe bağlı; H opsiyonel tablo sütunu).

**Sade dil örneği (F):**  
*“Bu metod gövdesinde 8 farklı dış tipe `orderRepo.save`, `kafka.send` … çağrısı var → orchestrator şişkinliği; değişiklikte kırılma yüzeyi geniş.”*

---

### Sınıf — “servis sınıfı sağlıklı mı?”

| Öneri | Ne ölçer? | Serviste ne demek? | Literatür | Uygulanabilirlik |
|-------|-----------|--------------------|-----------|------------------|
| **I. WMC** | Metod CC toplamı | Zaten var | CK, PMD | Var |
| **J. Metod sayısı (NOM)** | public + private metod | God class | PMD TooManyMethods **10**, Code Climate **20** | Var (`methodCount`) |
| **K. CBO / Ce (efferent coupling)** | Sınıfın bağımlı olduğu dış sınıf sayısı | `@Autowired` ile 15 repo | CK **FAN-OUT** | **Orta** — dosya/ proje ikinci geçiş veya Symbol Solver |
| **L. Ca (afferent coupling)** | Bu sınıfa kim referans veriyor | Hotspot servis | CK **FAN-IN** | **Düşük–orta** — tüm repo taraması + indeks (2. pass) |
| **M. TCC / LCOM ( cohesion )** | Metodların ortak alan kullanımı | “Her şeyi yapan” service | TCC **<50%** zayıf cohesion | **Düşük** — alan erişim analizi ağır |
| **N. Public API genişliği** | public metod sayısı | Facade vs internal | — | **Yüksek** |

**Önerilen sınıf paketi (ilk dalga):** **N + J + I** (zaten çoğu var); **K** için “dosya içi + import edilen paket türleri” basit proxy.

---

### Proje / monorepo — “hangi modül risk taşıyor?”

| Öneri | Ne ölçer? | Not |
|-------|-----------|-----|
| Mevcut p95, KRİTİK/KLOC, LOC payı | Kuyruk riski | Var |
| **O. Modül başına p95** | `module-payments` vs `module-etl` | ModuleRootIndex ile grupla |
| **P. Değişim sıklığı (Git)** | Hotspot × churn | CodeScene; **Git gerekir**, ayrı pipeline |
| **Q. Fan-in × complexity** | Merkezi ve karmaşık sınıf | ATD literatürü; 2. pass |

Call-graph lineage **bilinçli olarak ayrı** — tam **Ca/Fin** için proje geneli indeks gerekir; roadmap’te “Phase 2: scan bitince `AnalysisIndex`”.

---

## Cognitive vs CC — neden ikisi?

| | Cyclomatic (CC) | Cognitive |
|--|-----------------|-----------|
| Soru | Kaç **test yolu** var? | Kod **okunur mu**? |
| Servis örneği | Çok `case` → CC yüksek | `else if` zinciri düz → CC orta, cognitive yüksek |
| Gate | Sonar cyclomatic **10**, MISRA **15** | Sonar **15** |

Mock ETL’de **nesting p95=5** iken CC p95=12 — cognitive genelde **nesting’e daha duyarlı**; CC tek başına yetmez dediğimiz durumları yakalar.

---

## Risk skoruna nasıl bağlanır? (kesinleşince)

Her yeni boyut için YAML’da aynı kalıp:

```yaml
dimensions:
  cognitive:
    label_tr: "Okunabilirlik (cognitive)"
    thresholds: { dikkat: 10, yuksek: 15, kritik: 25 }
    when_tr:
      - "Orchestrator iç içe if/for → skor artar"
  outbound_calls:
    label_tr: "Dış çağrı çeşitliliği"
    thresholds: { dikkat: 5, yuksek: 10, kritik: 15 }
```

Skor birleşimi v2’de seçenekli:

- **Dominant-max (v1)** — en kötü boyut + küçük blend  
- **Ağırlıklı top-2** — servis kodunda CC + cognitive + fan-out ağırlıklı  

Profile bağlanmadan önce mock-modules’te **hangi boyutların p90’ı tetiklediğini** ölçüp eşik kesinleştir.

---

## Önerilen uygulama fazları

| Faz | İş | Tarama | Puanlama |
|-----|-----|--------|----------|
| **1** | `logicalStatements`, `returnPoints`, `catchCount`, `switchLabels` visitor | Yeni sayaçlar | Henüz rapor sütunu; risk yok |
| **2** | **Cognitive complexity** visitor (Sonar kurallarına yakın) | Yeni metrik | Eşik taslağı 10/15/25 |
| **3** | **outboundDistinctCalls**, `lambdaCount`, `maxTryNestingDepth`, `localVariableCount`, `maxMethodCallChainLength`, catch kalite sayaçları; `throw` → `exitPoints` | Yeni metrikler | enterprise: fout dikkat 6 / 10 / 15 (kalibrasyon Faz 5) |
| **4** | Sınıf: `publicMethodCount`, **`efferentCouplingProxy` (Ce)** | Sınıf DTO genişler | Sınıf risk güncelle (henüz skor yok) |
| **5** | Eşikleri mock + 1 gerçek repoda kalibre et | — | YAML kesin |
| **6** | `--risk-profile` + çok boyutlu skor | — | RiskCalculator v2 |

---

## Bilinçli dışarıda bırakılanlar

- **Tam fan-in / call-graph lineage** — ayrı ürün  
- **PMD GodClass (TCC + ATFD bileşik)** — TCC/cohesion ağır; sınıf riski WMC + metod sayısı ile devam  
- **Fortify/Semgrep güvenlik desenleri** — ayrı ürün/profil; karmaşıklık JAR’ına karıştırılmıyor  
- **Sonar technical debt (sqale_index)** — kural seti + effort dakika; PMD duplicate  
- **Git churn** — opsiyonel plugin; statik tarama JAR’ına zorunlu değil  
- **NPath** — ilk dalgada yok (maliyet); CC+cognitive çoğu servis senaryosunu karşılar

---

## Sonraki adım

1. ~~Faz **1+2** kodu~~ — **yapıldı:** JSON’da `cognitiveComplexity`, `exitPoints`, `catchClauses`, `switchCases`; `logicalStatements` zaten vardı. **Risk skoruna henüz bağlı değil.**  
2. ~~Faz **3**~~ — **yapıldı:** `outboundDistinctCalls`, `lambdaCount`, `maxTryNestingDepth`, `localVariableCount`, `maxMethodCallChainLength`, `emptyCatchBlocks`, `catchExceptionOrThrowable`, `catchWithOnlyPrintStackTrace`; `throw` `exitPoints`’e dahil. **Risk skoruna henüz bağlı değil.**  
3. ~~Mock-modules yüzdelik tablosu~~ — `scripts/calibrate-scan-metrics.sh` → [`mock-modules-scan-percentiles.md`](mock-modules-scan-percentiles.md)  
4. ~~Faz **4**~~ — **yapıldı:** JSON’da `publicMethodCount`, `efferentCouplingProxy`. **Risk skoruna henüz bağlı değil.**  
5. ~~Tabloya göre `enterprise-java` eşiklerini kilitle~~ — `config/risk-parameters-proposal.yaml` (v2 boyutları profilde)  
6. ~~`--risk-profile` + v2 skor~~ — **yapıldı:** `--risk-profile=enterprise-java`, YAML → `RiskCalculator`; varsayılan = legacy v1  
7. ~~Golden test~~ — `MockModulesGoldenTest`; ~~CI exit~~ — `--fail-on-risk`, `--max-failure-ratio`; ~~17 boyut skora~~ — `enterprise-java` YAML  
8. Gerçek DWH/servis modülünde `calibrate-scan-metrics.sh` ile eşik ince ayarı (mock tek tip olduğu için)

İlgili dosyalar: [`risk-model-plain-language.md`](risk-model-plain-language.md), [`risk-parameters-proposal.yaml`](../config/risk-parameters-proposal.yaml).
