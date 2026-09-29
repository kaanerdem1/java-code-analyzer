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
| Kaynak (XML) | `/Users/kaanerdem/Desktop/java-code-analyzer/mock-modules/target/pmd.xml` |
| Java dosyası | 12 |
| Metod (benzersiz) | 40 |
| PMD uyarı satırı | 116 (≈ 3 kural × metod) |

## Metod listesi (dallanma CC’ye göre azalan)

| Dallanma (CC) | Bilişsel | NPath | Sınıf | Metod | Dosya | Satır |
|-------------:|---------:|------:|-------|-------|-------|------:|
| 31 | 92 | 612 | `ReportAggregator` | `buildExecutiveSummary` | `mock-modules/module-reporting/src/main/java/com/mock/reporting/ReportAggregator.java` | 9 |
| 18 | 25 | 720 | `CancellationPolicyService` | `computeRefundPercent` | `mock-modules/module-orders/src/main/java/com/mock/orders/CancellationPolicyService.java` | 7 |
| 16 | 28 | 340 | `BillingService` | `calculateWithTaxAndTier` | `mock-modules/module-billing/src/main/java/com/mock/billing/BillingService.java` | 20 |
| 15 | 31 | 1024 | `AuthFacade` | `authorizeAction` | `mock-modules/module-auth/src/main/java/com/mock/auth/AuthFacade.java` | 48 |
| 13 | 25 | 339 | `DimensionMerger` | `mergeRows` | `mock-modules/module-etl/src/main/java/com/mock/etl/DimensionMerger.java` | 10 |
| 13 | 14 | 93 | `ReportAggregator` | `rankRowsByMetric` | `mock-modules/module-reporting/src/main/java/com/mock/reporting/ReportAggregator.java` | 91 |
| 9 | 15 | 19 | `OrderFulfillmentService` | `countShippableLines` | `mock-modules/module-orders/src/main/java/com/mock/orders/OrderFulfillmentService.java` | 9 |
| 8 | 18 | 13 | `InvoiceRepository` | `reconcileDuplicates` | `mock-modules/module-billing/src/main/java/com/mock/billing/InvoiceRepository.java` | 25 |
| 7 | 10 | 9 | `StagingLoader` | `loadRows` | `mock-modules/module-etl/src/main/java/com/mock/etl/StagingLoader.java` | 10 |
| 7 | 2 | 8 | `StagingLoader` | `ingestLine` | `mock-modules/module-etl/src/main/java/com/mock/etl/StagingLoader.java` | 33 |
| 6 | 5 | 16 | `PriceRuleEngine` | `applyDiscount` | `mock-modules/module-catalog/src/main/java/com/mock/catalog/PriceRuleEngine.java` | 5 |
| 6 | 5 | 24 | `LegacyBridgeService` | `translatePayload` | `mock-modules/module-integration/src/main/java/com/mock/integration/LegacyBridgeService.java` | 11 |
| 6 | 7 | 18 | `OrderFulfillmentService` | `pickWarehouse` | `mock-modules/module-orders/src/main/java/com/mock/orders/OrderFulfillmentService.java` | 28 |
| 6 | 7 | 14 | `OrderFulfillmentService` | `splitByWeight` | `mock-modules/module-orders/src/main/java/com/mock/orders/OrderFulfillmentService.java` | 47 |
| 5 | 4 | 8 | `CatalogService` | `resolveDisplayName` | `mock-modules/module-catalog/src/main/java/com/mock/catalog/CatalogService.java` | 11 |
| 5 | 4 | 16 | `CatalogService` | `categoryRank` | `mock-modules/module-catalog/src/main/java/com/mock/catalog/CatalogService.java` | 26 |
| 5 | 5 | 8 | `StagingLoader` | `loadDim` | `mock-modules/module-etl/src/main/java/com/mock/etl/StagingLoader.java` | 50 |
| 4 | 3 | 8 | `AuthFacade` | `passwordStrength` | `mock-modules/module-auth/src/main/java/com/mock/auth/AuthFacade.java` | 30 |
| 4 | 3 | 6 | `DimensionMerger` | `hasChanged` | `mock-modules/module-etl/src/main/java/com/mock/etl/DimensionMerger.java` | 66 |
| 4 | 3 | 6 | `StagingLoader` | `loadFact` | `mock-modules/module-etl/src/main/java/com/mock/etl/StagingLoader.java` | 60 |
| 4 | 3 | 6 | `StagingLoader` | `handleBadRow` | `mock-modules/module-etl/src/main/java/com/mock/etl/StagingLoader.java` | 76 |
| 4 | 4 | 4 | `LegacyBridgeService` | `mapOracle` | `mock-modules/module-integration/src/main/java/com/mock/integration/LegacyBridgeService.java` | 36 |
| 4 | 3 | 6 | `ReportAggregator` | `parseMetric` | `mock-modules/module-reporting/src/main/java/com/mock/reporting/ReportAggregator.java` | 118 |
| 3 | 3 | 3 | `AuthFacade` | `hasRole` | `mock-modules/module-auth/src/main/java/com/mock/auth/AuthFacade.java` | 16 |
| 3 | 2 | 3 | `AuthFacade` | `maskEmail` | `mock-modules/module-auth/src/main/java/com/mock/auth/AuthFacade.java` | 23 |
| 3 | 2 | 3 | `InvoiceRepository` | `save` | `mock-modules/module-billing/src/main/java/com/mock/billing/InvoiceRepository.java` | 10 |
| 3 | 3 | 3 | `InvoiceRepository` | `findById` | `mock-modules/module-billing/src/main/java/com/mock/billing/InvoiceRepository.java` | 16 |
| 3 | 3 | 3 | `DimensionMerger` | `extractKey` | `mock-modules/module-etl/src/main/java/com/mock/etl/DimensionMerger.java` | 57 |
| 3 | 3 | 3 | `LegacyBridgeService` | `mapMainframe` | `mock-modules/module-integration/src/main/java/com/mock/integration/LegacyBridgeService.java` | 45 |
| 2 | 1 | 2 | `TokenService` | `issueToken` | `mock-modules/module-auth/src/main/java/com/mock/auth/TokenService.java` | 5 |
| 2 | 1 | 2 | `BillingService` | `calculateLineTotal` | `mock-modules/module-billing/src/main/java/com/mock/billing/BillingService.java` | 9 |
| 2 | 1 | 2 | `StagingLoader` | `loadStage` | `mock-modules/module-etl/src/main/java/com/mock/etl/StagingLoader.java` | 72 |
| 2 | 1 | 2 | `LegacyBridgeService` | `mapSap` | `mock-modules/module-integration/src/main/java/com/mock/integration/LegacyBridgeService.java` | 27 |
| 1 | 1 | 2 | `AuthFacade` | `login` | `mock-modules/module-auth/src/main/java/com/mock/auth/AuthFacade.java` | 8 |
| 1 | — | 1 | `AuthFacade` | `logout` | `mock-modules/module-auth/src/main/java/com/mock/auth/AuthFacade.java` | 12 |
| 1 | — | 1 | `AuthFacade` | `isSessionExpired` | `mock-modules/module-auth/src/main/java/com/mock/auth/AuthFacade.java` | 44 |
| 1 | 1 | 1 | `TokenService` | `validateToken` | `mock-modules/module-auth/src/main/java/com/mock/auth/TokenService.java` | 12 |
| 1 | — | 1 | `BillingService` | `isOverdue` | `mock-modules/module-billing/src/main/java/com/mock/billing/BillingService.java` | 16 |
| 1 | — | 1 | `CatalogService` | `register` | `mock-modules/module-catalog/src/main/java/com/mock/catalog/CatalogService.java` | 42 |
| 1 | 1 | 1 | `StagingLoader` | `isFatal` | `mock-modules/module-etl/src/main/java/com/mock/etl/StagingLoader.java` | 83 |

## En yüksek dallanma (CC) — ilk 10

- **ReportAggregator.buildExecutiveSummary** → CC 31, bilişsel 92, NPath 612 (`mock-modules/module-reporting/src/main/java/com/mock/reporting/ReportAggregator.java`)
- **CancellationPolicyService.computeRefundPercent** → CC 18, bilişsel 25, NPath 720 (`mock-modules/module-orders/src/main/java/com/mock/orders/CancellationPolicyService.java`)
- **BillingService.calculateWithTaxAndTier** → CC 16, bilişsel 28, NPath 340 (`mock-modules/module-billing/src/main/java/com/mock/billing/BillingService.java`)
- **AuthFacade.authorizeAction** → CC 15, bilişsel 31, NPath 1024 (`mock-modules/module-auth/src/main/java/com/mock/auth/AuthFacade.java`)
- **DimensionMerger.mergeRows** → CC 13, bilişsel 25, NPath 339 (`mock-modules/module-etl/src/main/java/com/mock/etl/DimensionMerger.java`)
- **ReportAggregator.rankRowsByMetric** → CC 13, bilişsel 14, NPath 93 (`mock-modules/module-reporting/src/main/java/com/mock/reporting/ReportAggregator.java`)
- **OrderFulfillmentService.countShippableLines** → CC 9, bilişsel 15, NPath 19 (`mock-modules/module-orders/src/main/java/com/mock/orders/OrderFulfillmentService.java`)
- **InvoiceRepository.reconcileDuplicates** → CC 8, bilişsel 18, NPath 13 (`mock-modules/module-billing/src/main/java/com/mock/billing/InvoiceRepository.java`)
- **StagingLoader.loadRows** → CC 7, bilişsel 10, NPath 9 (`mock-modules/module-etl/src/main/java/com/mock/etl/StagingLoader.java`)
- **StagingLoader.ingestLine** → CC 7, bilişsel 2, NPath 8 (`mock-modules/module-etl/src/main/java/com/mock/etl/StagingLoader.java`)

---
*Parser tarafındaki risk skoru için `parser-raporu.md` dosyasına bakın. CC değerleri genelde yakın olur; cognitive ve NPath yalnızca PMD’de vardır.*
