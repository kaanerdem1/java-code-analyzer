# PMD karmaşıklık raporu

Bu rapor **PMD** aracının üç karmaşıklık kuralını çalıştırır. Eşikler düşük tutulduğu için neredeyse **her metod** listede yer alır.

## Metrikler ne anlama geliyor?

1. **Dallanma karmaşıklığı (CC — Cyclomatic Complexity)**  
   Metodda kaç bağımsız yürütme yolu var? (if, döngü, case, `&&` / `||` …)  
   *Yüksek CC = daha fazla test senaryosu gerekir.*

2. **Bilişsel karmaşıklık (Cognitive Complexity)**  
   Kodu **insanın okuma zorluğu**. İç içe yapı ve `else if` zincirleri daha çok puan artırır.  
   *Aynı CC’ye sahip iki metoddan biri cognitive olarak daha ağır olabilir.*

3. **NPath karmaşıklığı**  
   Metodun **olası yürütme yolu sayısının** üst sınırı (kombinatorik).  
   *Çok yüksek NPath = patlama riski; büyük metodlarda astronomik çıkabilir.*

## Özet

| Alan | Değer |
|------|-------|
| Kaynak (XML) | `/Users/kaanerdem/Desktop/java-code-analyzer/target/pmd-scan/pmd.xml` |
| Java dosyası | 59 |
| Metod (benzersiz) | 335 |
| PMD uyarı satırı | 1003 (≈ 3 kural × metod) |

## Metod listesi (dallanma CC’ye göre azalan)

| Dallanma (CC) | Bilişsel | NPath | Sınıf | Metod | Dosya | Satır |
|-------------:|---------:|------:|-------|-------|-------|------:|
| 31 | 92 | 612 | `ReportAggregator` | `buildExecutiveSummary` | `mock-modules/module-reporting/src/main/java/com/mock/reporting/ReportAggregator.java` | 9 |
| 18 | 25 | 720 | `CancellationPolicyService` | `computeRefundPercent` | `mock-modules/module-orders/src/main/java/com/mock/orders/CancellationPolicyService.java` | 7 |
| 17 | 16 | 39 | `Java6CodeAnalyzerMain$Options` | `parse` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/Java6CodeAnalyzerMain.java` | 150 |
| 16 | 28 | 340 | `BillingService` | `calculateWithTaxAndTier` | `mock-modules/module-billing/src/main/java/com/mock/billing/BillingService.java` | 20 |
| 15 | 31 | 1024 | `AuthFacade` | `authorizeAction` | `mock-modules/module-auth/src/main/java/com/mock/auth/AuthFacade.java` | 48 |
| 15 | 22 | 180 | `FraudRuleEngine` | `scoreTransaction` | `mock-modules/module-fraud/src/main/java/com/mock/fraud/FraudRuleEngine.java` | 9 |
| 13 | 25 | 339 | `DimensionMerger` | `mergeRows` | `mock-modules/module-etl/src/main/java/com/mock/etl/DimensionMerger.java` | 10 |
| 13 | 14 | 93 | `ReportAggregator` | `rankRowsByMetric` | `mock-modules/module-reporting/src/main/java/com/mock/reporting/ReportAggregator.java` | 91 |
| 12 | 16 | 22 | `LoadService001` | `evaluate001` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService001.java` | 10 |
| 12 | 16 | 22 | `LoadService002` | `evaluate002` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService002.java` | 10 |
| 12 | 16 | 22 | `LoadService003` | `evaluate003` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService003.java` | 10 |
| 12 | 16 | 22 | `LoadService004` | `evaluate004` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService004.java` | 10 |
| 12 | 16 | 22 | `LoadService005` | `evaluate005` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService005.java` | 10 |
| 12 | 16 | 22 | `LoadService006` | `evaluate006` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService006.java` | 10 |
| 12 | 16 | 22 | `LoadService007` | `evaluate007` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService007.java` | 10 |
| 12 | 16 | 22 | `LoadService008` | `evaluate008` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService008.java` | 10 |
| 12 | 16 | 22 | `LoadService009` | `evaluate009` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService009.java` | 10 |
| 12 | 16 | 22 | `LoadService010` | `evaluate010` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService010.java` | 10 |
| 12 | 16 | 22 | `LoadService011` | `evaluate011` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService011.java` | 10 |
| 12 | 16 | 22 | `LoadService012` | `evaluate012` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService012.java` | 10 |
| 12 | 16 | 22 | `LoadService013` | `evaluate013` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService013.java` | 10 |
| 12 | 16 | 22 | `LoadService014` | `evaluate014` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService014.java` | 10 |
| 12 | 16 | 22 | `LoadService015` | `evaluate015` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService015.java` | 10 |
| 12 | 16 | 22 | `LoadService016` | `evaluate016` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService016.java` | 10 |
| 12 | 16 | 22 | `LoadService017` | `evaluate017` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService017.java` | 10 |
| 12 | 16 | 22 | `LoadService018` | `evaluate018` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService018.java` | 10 |
| 12 | 16 | 22 | `LoadService019` | `evaluate019` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService019.java` | 10 |
| 12 | 16 | 22 | `LoadService020` | `evaluate020` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService020.java` | 10 |
| 12 | 16 | 22 | `LoadService021` | `evaluate021` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService021.java` | 10 |
| 12 | 16 | 22 | `LoadService022` | `evaluate022` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService022.java` | 10 |
| 12 | 16 | 22 | `LoadService023` | `evaluate023` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService023.java` | 10 |
| 12 | 16 | 22 | `LoadService024` | `evaluate024` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService024.java` | 10 |
| 12 | 16 | 22 | `LoadService025` | `evaluate025` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService025.java` | 10 |
| 12 | 16 | 22 | `LoadService026` | `evaluate026` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService026.java` | 10 |
| 12 | 16 | 22 | `LoadService027` | `evaluate027` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService027.java` | 10 |
| 12 | 16 | 22 | `LoadService028` | `evaluate028` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService028.java` | 10 |
| 12 | 16 | 22 | `LoadService029` | `evaluate029` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService029.java` | 10 |
| 12 | 16 | 22 | `LoadService030` | `evaluate030` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService030.java` | 10 |
| 12 | 16 | 22 | `LoadService031` | `evaluate031` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService031.java` | 10 |
| 12 | 16 | 22 | `LoadService032` | `evaluate032` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService032.java` | 10 |
| 12 | 16 | 22 | `LoadService033` | `evaluate033` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService033.java` | 10 |
| 12 | 16 | 22 | `LoadService034` | `evaluate034` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService034.java` | 10 |
| 12 | 16 | 22 | `LoadService035` | `evaluate035` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService035.java` | 10 |
| 11 | 14 | 165 | `PaymentOrchestrator` | `route` | `mock-modules/module-payments/src/main/java/com/mock/payments/PaymentOrchestrator.java` | 7 |
| 10 | 16 | 30 | `ProjectAnalyzer` | `analyze` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ProjectAnalyzer.java` | 56 |
| 9 | 15 | 19 | `OrderFulfillmentService` | `countShippableLines` | `mock-modules/module-orders/src/main/java/com/mock/orders/OrderFulfillmentService.java` | 9 |
| 9 | 8 | 56 | `Java6CodeAnalyzerMain` | `run` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/Java6CodeAnalyzerMain.java` | 39 |
| 9 | 6 | 28 | `ReadableReportMain` | `run` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ReadableReportMain.java` | 31 |
| 8 | 18 | 13 | `InvoiceRepository` | `reconcileDuplicates` | `mock-modules/module-billing/src/main/java/com/mock/billing/InvoiceRepository.java` | 25 |
| 8 | 13 | 40 | `ProjectAnalyzer` | `buildSummary` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ProjectAnalyzer.java` | 166 |
| 7 | 10 | 9 | `StagingLoader` | `loadRows` | `mock-modules/module-etl/src/main/java/com/mock/etl/StagingLoader.java` | 10 |
| 7 | 2 | 8 | `StagingLoader` | `ingestLine` | `mock-modules/module-etl/src/main/java/com/mock/etl/StagingLoader.java` | 33 |
| 7 | 6 | 36 | `FraudRuleEngine` | `blockIfHighRisk` | `mock-modules/module-fraud/src/main/java/com/mock/fraud/FraudRuleEngine.java` | 35 |
| 7 | 9 | 15 | `LoadService001` | `merge001` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService001.java` | 31 |
| 7 | 9 | 15 | `LoadService002` | `merge002` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService002.java` | 31 |
| 7 | 9 | 15 | `LoadService003` | `merge003` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService003.java` | 31 |
| 7 | 9 | 15 | `LoadService004` | `merge004` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService004.java` | 31 |
| 7 | 9 | 15 | `LoadService005` | `merge005` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService005.java` | 31 |
| 7 | 9 | 15 | `LoadService006` | `merge006` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService006.java` | 31 |
| 7 | 9 | 15 | `LoadService007` | `merge007` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService007.java` | 31 |
| 7 | 9 | 15 | `LoadService008` | `merge008` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService008.java` | 31 |
| 7 | 9 | 15 | `LoadService009` | `merge009` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService009.java` | 31 |
| 7 | 9 | 15 | `LoadService010` | `merge010` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService010.java` | 31 |
| 7 | 9 | 15 | `LoadService011` | `merge011` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService011.java` | 31 |
| 7 | 9 | 15 | `LoadService012` | `merge012` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService012.java` | 31 |
| 7 | 9 | 15 | `LoadService013` | `merge013` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService013.java` | 31 |
| 7 | 9 | 15 | `LoadService014` | `merge014` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService014.java` | 31 |
| 7 | 9 | 15 | `LoadService015` | `merge015` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService015.java` | 31 |
| 7 | 9 | 15 | `LoadService016` | `merge016` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService016.java` | 31 |
| 7 | 9 | 15 | `LoadService017` | `merge017` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService017.java` | 31 |
| 7 | 9 | 15 | `LoadService018` | `merge018` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService018.java` | 31 |
| 7 | 9 | 15 | `LoadService019` | `merge019` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService019.java` | 31 |
| 7 | 9 | 15 | `LoadService020` | `merge020` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService020.java` | 31 |
| 7 | 9 | 15 | `LoadService021` | `merge021` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService021.java` | 31 |
| 7 | 9 | 15 | `LoadService022` | `merge022` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService022.java` | 31 |
| 7 | 9 | 15 | `LoadService023` | `merge023` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService023.java` | 31 |
| 7 | 9 | 15 | `LoadService024` | `merge024` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService024.java` | 31 |
| 7 | 9 | 15 | `LoadService025` | `merge025` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService025.java` | 31 |
| 7 | 9 | 15 | `LoadService026` | `merge026` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService026.java` | 31 |
| 7 | 9 | 15 | `LoadService027` | `merge027` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService027.java` | 31 |
| 7 | 9 | 15 | `LoadService028` | `merge028` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService028.java` | 31 |
| 7 | 9 | 15 | `LoadService029` | `merge029` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService029.java` | 31 |
| 7 | 9 | 15 | `LoadService030` | `merge030` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService030.java` | 31 |
| 7 | 9 | 15 | `LoadService031` | `merge031` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService031.java` | 31 |
| 7 | 9 | 15 | `LoadService032` | `merge032` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService032.java` | 31 |
| 7 | 9 | 15 | `LoadService033` | `merge033` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService033.java` | 31 |
| 7 | 9 | 15 | `LoadService034` | `merge034` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService034.java` | 31 |
| 7 | 9 | 15 | `LoadService035` | `merge035` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService035.java` | 31 |
| 7 | 6 | 27 | `PmdXmlMarkdown$MethodAgg` | `apply` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/PmdXmlMarkdown.java` | 160 |
| 7 | 9 | 30 | `StandaloneReportMarkdown` | `render` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/StandaloneReportMarkdown.java` | 19 |
| 6 | 5 | 16 | `PriceRuleEngine` | `applyDiscount` | `mock-modules/module-catalog/src/main/java/com/mock/catalog/PriceRuleEngine.java` | 5 |
| 6 | 5 | 24 | `LegacyBridgeService` | `translatePayload` | `mock-modules/module-integration/src/main/java/com/mock/integration/LegacyBridgeService.java` | 11 |
| 6 | 7 | 18 | `LoadService001` | `validate001` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService001.java` | 45 |
| 6 | 7 | 14 | `LoadService001` | `batch001` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService001.java` | 55 |
| 6 | 9 | 10 | `LoadService001` | `rate001` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService001.java` | 72 |
| 6 | 15 | 6 | `LoadService001` | `nested001` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService001.java` | 86 |
| 6 | 7 | 18 | `LoadService002` | `validate002` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService002.java` | 45 |
| 6 | 7 | 14 | `LoadService002` | `batch002` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService002.java` | 55 |
| 6 | 9 | 10 | `LoadService002` | `rate002` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService002.java` | 72 |
| 6 | 15 | 6 | `LoadService002` | `nested002` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService002.java` | 86 |
| 6 | 7 | 18 | `LoadService003` | `validate003` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService003.java` | 45 |
| 6 | 7 | 14 | `LoadService003` | `batch003` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService003.java` | 55 |
| 6 | 9 | 10 | `LoadService003` | `rate003` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService003.java` | 72 |
| 6 | 15 | 6 | `LoadService003` | `nested003` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService003.java` | 86 |
| 6 | 7 | 18 | `LoadService004` | `validate004` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService004.java` | 45 |
| 6 | 7 | 14 | `LoadService004` | `batch004` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService004.java` | 55 |
| 6 | 9 | 10 | `LoadService004` | `rate004` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService004.java` | 72 |
| 6 | 15 | 6 | `LoadService004` | `nested004` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService004.java` | 86 |
| 6 | 7 | 18 | `LoadService005` | `validate005` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService005.java` | 45 |
| 6 | 7 | 14 | `LoadService005` | `batch005` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService005.java` | 55 |
| 6 | 9 | 10 | `LoadService005` | `rate005` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService005.java` | 72 |
| 6 | 15 | 6 | `LoadService005` | `nested005` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService005.java` | 86 |
| 6 | 7 | 18 | `LoadService006` | `validate006` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService006.java` | 45 |
| 6 | 7 | 14 | `LoadService006` | `batch006` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService006.java` | 55 |
| 6 | 9 | 10 | `LoadService006` | `rate006` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService006.java` | 72 |
| 6 | 15 | 6 | `LoadService006` | `nested006` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService006.java` | 86 |
| 6 | 7 | 18 | `LoadService007` | `validate007` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService007.java` | 45 |
| 6 | 7 | 14 | `LoadService007` | `batch007` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService007.java` | 55 |
| 6 | 9 | 10 | `LoadService007` | `rate007` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService007.java` | 72 |
| 6 | 15 | 6 | `LoadService007` | `nested007` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService007.java` | 86 |
| 6 | 7 | 18 | `LoadService008` | `validate008` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService008.java` | 45 |
| 6 | 7 | 14 | `LoadService008` | `batch008` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService008.java` | 55 |
| 6 | 9 | 10 | `LoadService008` | `rate008` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService008.java` | 72 |
| 6 | 15 | 6 | `LoadService008` | `nested008` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService008.java` | 86 |
| 6 | 7 | 18 | `LoadService009` | `validate009` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService009.java` | 45 |
| 6 | 7 | 14 | `LoadService009` | `batch009` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService009.java` | 55 |
| 6 | 9 | 10 | `LoadService009` | `rate009` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService009.java` | 72 |
| 6 | 15 | 6 | `LoadService009` | `nested009` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService009.java` | 86 |
| 6 | 7 | 18 | `LoadService010` | `validate010` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService010.java` | 45 |
| 6 | 7 | 14 | `LoadService010` | `batch010` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService010.java` | 55 |
| 6 | 9 | 10 | `LoadService010` | `rate010` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService010.java` | 72 |
| 6 | 15 | 6 | `LoadService010` | `nested010` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService010.java` | 86 |
| 6 | 7 | 18 | `LoadService011` | `validate011` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService011.java` | 45 |
| 6 | 7 | 14 | `LoadService011` | `batch011` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService011.java` | 55 |
| 6 | 9 | 10 | `LoadService011` | `rate011` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService011.java` | 72 |
| 6 | 15 | 6 | `LoadService011` | `nested011` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService011.java` | 86 |
| 6 | 7 | 18 | `LoadService012` | `validate012` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService012.java` | 45 |
| 6 | 7 | 14 | `LoadService012` | `batch012` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService012.java` | 55 |
| 6 | 9 | 10 | `LoadService012` | `rate012` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService012.java` | 72 |
| 6 | 15 | 6 | `LoadService012` | `nested012` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService012.java` | 86 |
| 6 | 7 | 18 | `LoadService013` | `validate013` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService013.java` | 45 |
| 6 | 7 | 14 | `LoadService013` | `batch013` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService013.java` | 55 |
| 6 | 9 | 10 | `LoadService013` | `rate013` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService013.java` | 72 |
| 6 | 15 | 6 | `LoadService013` | `nested013` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService013.java` | 86 |
| 6 | 7 | 18 | `LoadService014` | `validate014` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService014.java` | 45 |
| 6 | 7 | 14 | `LoadService014` | `batch014` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService014.java` | 55 |
| 6 | 9 | 10 | `LoadService014` | `rate014` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService014.java` | 72 |
| 6 | 15 | 6 | `LoadService014` | `nested014` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService014.java` | 86 |
| 6 | 7 | 18 | `LoadService015` | `validate015` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService015.java` | 45 |
| 6 | 7 | 14 | `LoadService015` | `batch015` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService015.java` | 55 |
| 6 | 9 | 10 | `LoadService015` | `rate015` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService015.java` | 72 |
| 6 | 15 | 6 | `LoadService015` | `nested015` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService015.java` | 86 |
| 6 | 7 | 18 | `LoadService016` | `validate016` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService016.java` | 45 |
| 6 | 7 | 14 | `LoadService016` | `batch016` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService016.java` | 55 |
| 6 | 9 | 10 | `LoadService016` | `rate016` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService016.java` | 72 |
| 6 | 15 | 6 | `LoadService016` | `nested016` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService016.java` | 86 |
| 6 | 7 | 18 | `LoadService017` | `validate017` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService017.java` | 45 |
| 6 | 7 | 14 | `LoadService017` | `batch017` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService017.java` | 55 |
| 6 | 9 | 10 | `LoadService017` | `rate017` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService017.java` | 72 |
| 6 | 15 | 6 | `LoadService017` | `nested017` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService017.java` | 86 |
| 6 | 7 | 18 | `LoadService018` | `validate018` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService018.java` | 45 |
| 6 | 7 | 14 | `LoadService018` | `batch018` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService018.java` | 55 |
| 6 | 9 | 10 | `LoadService018` | `rate018` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService018.java` | 72 |
| 6 | 15 | 6 | `LoadService018` | `nested018` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService018.java` | 86 |
| 6 | 7 | 18 | `LoadService019` | `validate019` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService019.java` | 45 |
| 6 | 7 | 14 | `LoadService019` | `batch019` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService019.java` | 55 |
| 6 | 9 | 10 | `LoadService019` | `rate019` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService019.java` | 72 |
| 6 | 15 | 6 | `LoadService019` | `nested019` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService019.java` | 86 |
| 6 | 7 | 18 | `LoadService020` | `validate020` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService020.java` | 45 |
| 6 | 7 | 14 | `LoadService020` | `batch020` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService020.java` | 55 |
| 6 | 9 | 10 | `LoadService020` | `rate020` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService020.java` | 72 |
| 6 | 15 | 6 | `LoadService020` | `nested020` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService020.java` | 86 |
| 6 | 7 | 18 | `LoadService021` | `validate021` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService021.java` | 45 |
| 6 | 7 | 14 | `LoadService021` | `batch021` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService021.java` | 55 |
| 6 | 9 | 10 | `LoadService021` | `rate021` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService021.java` | 72 |
| 6 | 15 | 6 | `LoadService021` | `nested021` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService021.java` | 86 |
| 6 | 7 | 18 | `LoadService022` | `validate022` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService022.java` | 45 |
| 6 | 7 | 14 | `LoadService022` | `batch022` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService022.java` | 55 |
| 6 | 9 | 10 | `LoadService022` | `rate022` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService022.java` | 72 |
| 6 | 15 | 6 | `LoadService022` | `nested022` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService022.java` | 86 |
| 6 | 7 | 18 | `LoadService023` | `validate023` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService023.java` | 45 |
| 6 | 7 | 14 | `LoadService023` | `batch023` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService023.java` | 55 |
| 6 | 9 | 10 | `LoadService023` | `rate023` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService023.java` | 72 |
| 6 | 15 | 6 | `LoadService023` | `nested023` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService023.java` | 86 |
| 6 | 7 | 18 | `LoadService024` | `validate024` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService024.java` | 45 |
| 6 | 7 | 14 | `LoadService024` | `batch024` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService024.java` | 55 |
| 6 | 9 | 10 | `LoadService024` | `rate024` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService024.java` | 72 |
| 6 | 15 | 6 | `LoadService024` | `nested024` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService024.java` | 86 |
| 6 | 7 | 18 | `LoadService025` | `validate025` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService025.java` | 45 |
| 6 | 7 | 14 | `LoadService025` | `batch025` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService025.java` | 55 |
| 6 | 9 | 10 | `LoadService025` | `rate025` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService025.java` | 72 |
| 6 | 15 | 6 | `LoadService025` | `nested025` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService025.java` | 86 |
| 6 | 7 | 18 | `LoadService026` | `validate026` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService026.java` | 45 |
| 6 | 7 | 14 | `LoadService026` | `batch026` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService026.java` | 55 |
| 6 | 9 | 10 | `LoadService026` | `rate026` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService026.java` | 72 |
| 6 | 15 | 6 | `LoadService026` | `nested026` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService026.java` | 86 |
| 6 | 7 | 18 | `LoadService027` | `validate027` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService027.java` | 45 |
| 6 | 7 | 14 | `LoadService027` | `batch027` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService027.java` | 55 |
| 6 | 9 | 10 | `LoadService027` | `rate027` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService027.java` | 72 |
| 6 | 15 | 6 | `LoadService027` | `nested027` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService027.java` | 86 |
| 6 | 7 | 18 | `LoadService028` | `validate028` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService028.java` | 45 |
| 6 | 7 | 14 | `LoadService028` | `batch028` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService028.java` | 55 |
| 6 | 9 | 10 | `LoadService028` | `rate028` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService028.java` | 72 |
| 6 | 15 | 6 | `LoadService028` | `nested028` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService028.java` | 86 |
| 6 | 7 | 18 | `LoadService029` | `validate029` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService029.java` | 45 |
| 6 | 7 | 14 | `LoadService029` | `batch029` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService029.java` | 55 |
| 6 | 9 | 10 | `LoadService029` | `rate029` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService029.java` | 72 |
| 6 | 15 | 6 | `LoadService029` | `nested029` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService029.java` | 86 |
| 6 | 7 | 18 | `LoadService030` | `validate030` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService030.java` | 45 |
| 6 | 7 | 14 | `LoadService030` | `batch030` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService030.java` | 55 |
| 6 | 9 | 10 | `LoadService030` | `rate030` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService030.java` | 72 |
| 6 | 15 | 6 | `LoadService030` | `nested030` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService030.java` | 86 |
| 6 | 7 | 18 | `LoadService031` | `validate031` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService031.java` | 45 |
| 6 | 7 | 14 | `LoadService031` | `batch031` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService031.java` | 55 |
| 6 | 9 | 10 | `LoadService031` | `rate031` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService031.java` | 72 |
| 6 | 15 | 6 | `LoadService031` | `nested031` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService031.java` | 86 |
| 6 | 7 | 18 | `LoadService032` | `validate032` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService032.java` | 45 |
| 6 | 7 | 14 | `LoadService032` | `batch032` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService032.java` | 55 |
| 6 | 9 | 10 | `LoadService032` | `rate032` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService032.java` | 72 |
| 6 | 15 | 6 | `LoadService032` | `nested032` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService032.java` | 86 |
| 6 | 7 | 18 | `LoadService033` | `validate033` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService033.java` | 45 |
| 6 | 7 | 14 | `LoadService033` | `batch033` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService033.java` | 55 |
| 6 | 9 | 10 | `LoadService033` | `rate033` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService033.java` | 72 |
| 6 | 15 | 6 | `LoadService033` | `nested033` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService033.java` | 86 |
| 6 | 7 | 18 | `LoadService034` | `validate034` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService034.java` | 45 |
| 6 | 7 | 14 | `LoadService034` | `batch034` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService034.java` | 55 |
| 6 | 9 | 10 | `LoadService034` | `rate034` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService034.java` | 72 |
| 6 | 15 | 6 | `LoadService034` | `nested034` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService034.java` | 86 |
| 6 | 7 | 18 | `LoadService035` | `validate035` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService035.java` | 45 |
| 6 | 7 | 14 | `LoadService035` | `batch035` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService035.java` | 55 |
| 6 | 9 | 10 | `LoadService035` | `rate035` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService035.java` | 72 |
| 6 | 15 | 6 | `LoadService035` | `nested035` | `mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService035.java` | 86 |
| 6 | 7 | 18 | `OrderFulfillmentService` | `pickWarehouse` | `mock-modules/module-orders/src/main/java/com/mock/orders/OrderFulfillmentService.java` | 28 |
| 6 | 7 | 14 | `OrderFulfillmentService` | `splitByWeight` | `mock-modules/module-orders/src/main/java/com/mock/orders/OrderFulfillmentService.java` | 47 |
| 6 | 14 | 7 | `AnalysisConsoleLogger` | `logVerboseMethods` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/AnalysisConsoleLogger.java` | 72 |
| 6 | 4 | 12 | `LanguageLevelOption` | `parse` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/LanguageLevelOption.java` | 13 |
| 5 | 4 | 8 | `CatalogService` | `resolveDisplayName` | `mock-modules/module-catalog/src/main/java/com/mock/catalog/CatalogService.java` | 11 |
| 5 | 4 | 16 | `CatalogService` | `categoryRank` | `mock-modules/module-catalog/src/main/java/com/mock/catalog/CatalogService.java` | 26 |
| 5 | 5 | 8 | `StagingLoader` | `loadDim` | `mock-modules/module-etl/src/main/java/com/mock/etl/StagingLoader.java` | 50 |
| 5 | 5 | 9 | `PaymentOrchestrator` | `reconcile` | `mock-modules/module-payments/src/main/java/com/mock/payments/PaymentOrchestrator.java` | 19 |
| 5 | 2 | 3 | `Java6CodeAnalyzerMain$Options` | `parseTop` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/Java6CodeAnalyzerMain.java` | 198 |
| 5 | 9 | 16 | `ProjectAnalyzer` | `buildHotspots` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ProjectAnalyzer.java` | 219 |
| 5 | 4 | 16 | `RiskCalculator` | `subScore` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/RiskCalculator.java` | 136 |
| 5 | — | 4 | `StandaloneReportMarkdown` | `levelTr` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/StandaloneReportMarkdown.java` | 112 |
| 4 | 3 | 8 | `AuthFacade` | `passwordStrength` | `mock-modules/module-auth/src/main/java/com/mock/auth/AuthFacade.java` | 30 |
| 4 | 3 | 6 | `DimensionMerger` | `hasChanged` | `mock-modules/module-etl/src/main/java/com/mock/etl/DimensionMerger.java` | 66 |
| 4 | 3 | 6 | `StagingLoader` | `loadFact` | `mock-modules/module-etl/src/main/java/com/mock/etl/StagingLoader.java` | 60 |
| 4 | 3 | 6 | `StagingLoader` | `handleBadRow` | `mock-modules/module-etl/src/main/java/com/mock/etl/StagingLoader.java` | 76 |
| 4 | 4 | 4 | `LegacyBridgeService` | `mapOracle` | `mock-modules/module-integration/src/main/java/com/mock/integration/LegacyBridgeService.java` | 36 |
| 4 | 3 | 6 | `ReportAggregator` | `parseMetric` | `mock-modules/module-reporting/src/main/java/com/mock/reporting/ReportAggregator.java` | 118 |
| 4 | 4 | 6 | `AnalysisConsoleLogger` | `logHotspots` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/AnalysisConsoleLogger.java` | 53 |
| 4 | 5 | 6 | `Java6CodeAnalyzerMain` | `writeJson` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/Java6CodeAnalyzerMain.java` | 104 |
| 4 | 3 | 8 | `RiskLevel` | `fromScore` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/RiskLevel.java` | 21 |
| 4 | 6 | 4 | `StandaloneReportMarkdown` | `flattenMethods` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/StandaloneReportMarkdown.java` | 121 |
| 3 | 3 | 3 | `AuthFacade` | `hasRole` | `mock-modules/module-auth/src/main/java/com/mock/auth/AuthFacade.java` | 16 |
| 3 | 2 | 3 | `AuthFacade` | `maskEmail` | `mock-modules/module-auth/src/main/java/com/mock/auth/AuthFacade.java` | 23 |
| 3 | 2 | 3 | `InvoiceRepository` | `save` | `mock-modules/module-billing/src/main/java/com/mock/billing/InvoiceRepository.java` | 10 |
| 3 | 3 | 3 | `InvoiceRepository` | `findById` | `mock-modules/module-billing/src/main/java/com/mock/billing/InvoiceRepository.java` | 16 |
| 3 | 3 | 3 | `DimensionMerger` | `extractKey` | `mock-modules/module-etl/src/main/java/com/mock/etl/DimensionMerger.java` | 57 |
| 3 | 3 | 3 | `LegacyBridgeService` | `mapMainframe` | `mock-modules/module-integration/src/main/java/com/mock/integration/LegacyBridgeService.java` | 45 |
| 3 | 2 | 4 | `AnalysisConsoleLogger` | `logParseErrors` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/AnalysisConsoleLogger.java` | 95 |
| 3 | 2 | 3 | `ComplexityVisitor` | `visit` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ComplexityVisitor.java` | 89 |
| 3 | 2 | 6 | `ComplexityVisitor` | `withNesting` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ComplexityVisitor.java` | 261 |
| 3 | 2 | 3 | `ComplexityVisitor` | `countCodeLines` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ComplexityVisitor.java` | 285 |
| 3 | 2 | 4 | `Java6CodeAnalyzerMain` | `writeMarkdownIfRequested` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/Java6CodeAnalyzerMain.java` | 92 |
| 3 | 1 | 2 | `Java6CodeAnalyzerMain$Options` | `value` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/Java6CodeAnalyzerMain.java` | 190 |
| 3 | 7 | 12 | `PmdXmlMarkdown` | `render` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/PmdXmlMarkdown.java` | 33 |
| 3 | 3 | 3 | `PmdXmlMarkdown` | `collectRows` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/PmdXmlMarkdown.java` | 89 |
| 3 | 1 | 3 | `PmdXmlMarkdown` | `parse` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/PmdXmlMarkdown.java` | 119 |
| 3 | 2 | 3 | `ProjectAnalyzer` | `relativePath` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ProjectAnalyzer.java` | 129 |
| 3 | 2 | 3 | `RiskCalculator` | `describe` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/RiskCalculator.java` | 144 |
| 2 | 1 | 2 | `TokenService` | `issueToken` | `mock-modules/module-auth/src/main/java/com/mock/auth/TokenService.java` | 5 |
| 2 | 1 | 2 | `BillingService` | `calculateLineTotal` | `mock-modules/module-billing/src/main/java/com/mock/billing/BillingService.java` | 9 |
| 2 | 1 | 2 | `StagingLoader` | `loadStage` | `mock-modules/module-etl/src/main/java/com/mock/etl/StagingLoader.java` | 72 |
| 2 | 1 | 2 | `LegacyBridgeService` | `mapSap` | `mock-modules/module-integration/src/main/java/com/mock/integration/LegacyBridgeService.java` | 27 |
| 2 | 1 | 2 | `AnalysisConsoleLogger` | `logRunHeader` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/AnalysisConsoleLogger.java` | 19 |
| 2 | 1 | 2 | `AnalysisConsoleLogger` | `logSummary` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/AnalysisConsoleLogger.java` | 33 |
| 2 | 1 | 4 | `ComplexityVisitor` | `analyseType` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ComplexityVisitor.java` | 98 |
| 2 | 1 | 2 | `ComplexityVisitor` | `buildClassMetric` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ComplexityVisitor.java` | 113 |
| 2 | 1 | 4 | `ComplexityVisitor` | `analyseCallable` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ComplexityVisitor.java` | 143 |
| 2 | 1 | 2 | `ComplexityVisitor` | `addDecisionPoint` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ComplexityVisitor.java` | 254 |
| 2 | 1 | 2 | `ComplexityVisitor` | `typeLabel` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ComplexityVisitor.java` | 307 |
| 2 | 1 | 2 | `PmdXmlMarkdown` | `aggregate` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/PmdXmlMarkdown.java` | 110 |
| 2 | 1 | 2 | `PmdXmlMarkdown` | `shortenPath` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/PmdXmlMarkdown.java` | 133 |
| 2 | 1 | 2 | `PmdXmlMarkdown` | `dash` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/PmdXmlMarkdown.java` | 138 |
| 2 | 1 | 2 | `PmdXmlMarkdown$MethodAgg` | `sortCc` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/PmdXmlMarkdown.java` | 174 |
| 2 | 7 | 12 | `ProjectAnalyzer` | `collectJavaFiles` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ProjectAnalyzer.java` | 98 |
| 2 | 1 | 2 | `ProjectAnalyzer` | `describe` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ProjectAnalyzer.java` | 134 |
| 2 | 1 | 2 | `ReadableReportMain` | `standalone` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ReadableReportMain.java` | 53 |
| 2 | 1 | 2 | `ReadableReportMain` | `pmd` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ReadableReportMain.java` | 62 |
| 2 | 1 | 2 | `ReadableReportMain` | `write` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ReadableReportMain.java` | 75 |
| 2 | 1 | 2 | `ReadableReportMain` | `defaultMd` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ReadableReportMain.java` | 84 |
| 2 | 1 | 2 | `RiskCalculator` | `assessAggregate` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/RiskCalculator.java` | 104 |
| 2 | 1 | 2 | `RiskLevel` | `maxScore` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/RiskLevel.java` | 29 |
| 1 | 1 | 2 | `AuthFacade` | `login` | `mock-modules/module-auth/src/main/java/com/mock/auth/AuthFacade.java` | 8 |
| 1 | — | 1 | `AuthFacade` | `logout` | `mock-modules/module-auth/src/main/java/com/mock/auth/AuthFacade.java` | 12 |
| 1 | — | 1 | `AuthFacade` | `isSessionExpired` | `mock-modules/module-auth/src/main/java/com/mock/auth/AuthFacade.java` | 44 |
| 1 | 1 | 1 | `TokenService` | `validateToken` | `mock-modules/module-auth/src/main/java/com/mock/auth/TokenService.java` | 12 |
| 1 | — | 1 | `BillingService` | `isOverdue` | `mock-modules/module-billing/src/main/java/com/mock/billing/BillingService.java` | 16 |
| 1 | — | 1 | `CatalogService` | `register` | `mock-modules/module-catalog/src/main/java/com/mock/catalog/CatalogService.java` | 42 |
| 1 | 1 | 1 | `StagingLoader` | `isFatal` | `mock-modules/module-etl/src/main/java/com/mock/etl/StagingLoader.java` | 83 |
| 1 | — | 1 | `AnalysisConsoleLogger` | `AnalysisConsoleLogger` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/AnalysisConsoleLogger.java` | 16 |
| 1 | — | 1 | `ComplexityVisitor` | `ComplexityVisitor` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ComplexityVisitor.java` | 61 |
| 1 | 5 | 3 | `ComplexityVisitor` | `computeCodeLines` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ComplexityVisitor.java` | 67 |
| 1 | — | 1 | `ComplexityVisitor` | `getClassMetrics` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ComplexityVisitor.java` | 80 |
| 1 | — | 1 | `ComplexityVisitor` | `isElseIf` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ComplexityVisitor.java` | 276 |
| 1 | — | 1 | `ComplexityVisitor` | `countStatements` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ComplexityVisitor.java` | 293 |
| 1 | 2 | 2 | `ComplexityVisitor` | `signatureOf` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ComplexityVisitor.java` | 299 |
| 1 | — | 1 | `ComplexityVisitor` | `beginLine` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ComplexityVisitor.java` | 314 |
| 1 | — | 1 | `ComplexityVisitor` | `endLine` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ComplexityVisitor.java` | 318 |
| 1 | — | 1 | `ComplexityVisitor$TypeContext` | `TypeContext` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ComplexityVisitor.java` | 333 |
| 1 | — | 1 | `Java6CodeAnalyzerMain` | `Java6CodeAnalyzerMain` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/Java6CodeAnalyzerMain.java` | 32 |
| 1 | — | 1 | `Java6CodeAnalyzerMain` | `main` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/Java6CodeAnalyzerMain.java` | 35 |
| 1 | — | 1 | `Java6CodeAnalyzerMain` | `printUsage` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/Java6CodeAnalyzerMain.java` | 126 |
| 1 | — | 1 | `LanguageLevelOption` | `LanguageLevelOption` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/LanguageLevelOption.java` | 10 |
| 1 | — | 1 | `PmdXmlMarkdown` | `PmdXmlMarkdown` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/PmdXmlMarkdown.java` | 30 |
| 1 | — | 1 | `PmdXmlMarkdown` | `text` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/PmdXmlMarkdown.java` | 129 |
| 1 | — | 1 | `PmdXmlMarkdown$MethodAgg` | `MethodAgg` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/PmdXmlMarkdown.java` | 154 |
| 1 | — | 1 | `ProjectAnalyzer` | `ProjectAnalyzer` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ProjectAnalyzer.java` | 45 |
| 1 | — | 1 | `ProjectAnalyzer` | `buildFileMetric` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ProjectAnalyzer.java` | 143 |
| 1 | — | 1 | `ProjectAnalyzer` | `buildRiskModel` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ProjectAnalyzer.java` | 207 |
| 1 | — | 1 | `ReadableReportMain` | `ReadableReportMain` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ReadableReportMain.java` | 24 |
| 1 | — | 1 | `ReadableReportMain` | `main` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ReadableReportMain.java` | 27 |
| 1 | — | 1 | `ReadableReportMain` | `loadJson` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ReadableReportMain.java` | 70 |
| 1 | — | 1 | `ReadableReportMain` | `printUsage` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/ReadableReportMain.java` | 89 |
| 1 | — | 1 | `RiskCalculator$Scope` | `Scope` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/RiskCalculator.java` | 55 |
| 1 | — | 1 | `RiskCalculator$Assessment` | `Assessment` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/RiskCalculator.java` | 67 |
| 1 | — | 1 | `RiskCalculator` | `assessMethod` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/RiskCalculator.java` | 72 |
| 1 | 3 | 4 | `RiskCalculator` | `isGodMethod` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/RiskCalculator.java` | 97 |
| 1 | — | 1 | `RiskCalculator` | `compose` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/RiskCalculator.java` | 126 |
| 1 | — | 1 | `RiskCalculator` | `round3` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/RiskCalculator.java` | 154 |
| 1 | — | 1 | `RiskLevel` | `RiskLevel` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/RiskLevel.java` | 16 |
| 1 | — | 1 | `StandaloneReportMarkdown` | `StandaloneReportMarkdown` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/StandaloneReportMarkdown.java` | 16 |
| 1 | — | 1 | `StandaloneReportMarkdown` | `fmt` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/StandaloneReportMarkdown.java` | 135 |
| 1 | — | 1 | `StandaloneReportMarkdown` | `escapeCell` | `/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/StandaloneReportMarkdown.java` | 139 |

## En yüksek dallanma (CC) — ilk 10

- **ReportAggregator.buildExecutiveSummary** → CC 31, bilişsel 92, NPath 612 (`mock-modules/module-reporting/src/main/java/com/mock/reporting/ReportAggregator.java`)
- **CancellationPolicyService.computeRefundPercent** → CC 18, bilişsel 25, NPath 720 (`mock-modules/module-orders/src/main/java/com/mock/orders/CancellationPolicyService.java`)
- **Java6CodeAnalyzerMain$Options.parse** → CC 17, bilişsel 16, NPath 39 (`/Users/kaanerdem/Desktop/java-code-analyzer/src/main/java/com/standalone/analyzer/Java6CodeAnalyzerMain.java`)
- **BillingService.calculateWithTaxAndTier** → CC 16, bilişsel 28, NPath 340 (`mock-modules/module-billing/src/main/java/com/mock/billing/BillingService.java`)
- **AuthFacade.authorizeAction** → CC 15, bilişsel 31, NPath 1024 (`mock-modules/module-auth/src/main/java/com/mock/auth/AuthFacade.java`)
- **FraudRuleEngine.scoreTransaction** → CC 15, bilişsel 22, NPath 180 (`mock-modules/module-fraud/src/main/java/com/mock/fraud/FraudRuleEngine.java`)
- **DimensionMerger.mergeRows** → CC 13, bilişsel 25, NPath 339 (`mock-modules/module-etl/src/main/java/com/mock/etl/DimensionMerger.java`)
- **ReportAggregator.rankRowsByMetric** → CC 13, bilişsel 14, NPath 93 (`mock-modules/module-reporting/src/main/java/com/mock/reporting/ReportAggregator.java`)
- **LoadService001.evaluate001** → CC 12, bilişsel 16, NPath 22 (`mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService001.java`)
- **LoadService002.evaluate002** → CC 12, bilişsel 16, NPath 22 (`mock-modules/module-loadtest/src/main/java/com/mock/loadtest/LoadService002.java`)

---
*Parser tarafındaki risk skoru için `parser-raporu.md` dosyasına bakın. CC değerleri genelde yakın olur; cognitive ve NPath yalnızca PMD’de vardır.*
