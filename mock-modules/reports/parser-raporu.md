# Parser analiz raporu

Bu rapor **Java kaynak kodunu** tarayıp her **metod** için karmaşıklık ve **teknik risk** özetler.

## Kısa sözlük

- **Dallanma karmaşıklığı (CC):** Metod içinde kaç farklı karar/yol var (if, else, for, while, catch, `&&`, `||` vb.). Yüksek = test etmesi zor.
- **Kod satırı:** Metod gövdesindeki gerçek kod satırları (boş satır ve sadece yorum sayılmaz).
- **İç içe derinlik:** Blokların iç içe kaç kat olduğu (if içinde for içinde if…).
- **Risk skoru (0–1):** CC, satır sayısı, iç içe yapı ve parametre sayısından üretilen birleşik puan. **0’a yakın = sakin**, **1’e yakın = dikkat**.
- **Risk seviyesi:** DÜŞÜK / ORTA / YÜKSEK / KRİTİK (skora göre bant).
- **Dev metod:** Çok uzun veya çok karmaşık metod uyarısı (heuristik).

## Tarama özeti

| Alan | Değer |
|------|-------|
| Tarih | 2026-09-29T06:51:55.234380Z |
| Taranan klasör | `/Users/kaanerdem/Desktop/java-code-analyzer/mock-modules` |
| Java sürümü (parse) | JAVA_17 |
| Dosya (tarandı / okundu / hata) | 49 / 49 / 0 |
| Sınıf / metod sayısı | 49 / 254 |
| Toplam kod satırı | 3863 |
| Ortalama / en yüksek dallanma (CC) | 7.150 / 31 |
| Dev metod sayısı | 1 |
| **Proje risk skoru** | **0.364 (ORTA)** |

### Metodların risk dağılımı

| DÜŞÜK | ORTA | YÜKSEK | KRİTİK |
|-------|------|--------|--------|
| 174 | 40 | 39 | 1 |

## En riskli metodlar

| Sıra | Risk skoru | Seviye | Sınıf | Metod imzası | Dallanma (CC) | Kod satırı | İç içe | Parametre | Başlangıç satırı |
|-----:|-----------:|--------|-------|--------------|-------------:|-----------:|------:|----------:|-----------------:|
| 1 | 0.902 | KRİTİK | `ReportAggregator` | `buildExecutiveSummary(List, String, int, boolean)` | 31 | 81 | 8 | 4 | 8 |
| 2 | 0.640 | YÜKSEK | `AuthFacade` | `authorizeAction(String, String, String, List, boolean)` | 18 | 33 | 5 | 5 | 48 |
| 3 | 0.638 | YÜKSEK | `BillingService` | `calculateWithTaxAndTier(List<String>, List<Integer>, String, boolean, String)` | 16 | 36 | 5 | 5 | 20 |
| 4 | 0.619 | YÜKSEK | `CancellationPolicyService` | `computeRefundPercent(String, Date, Date, String, boolean, int)` | 18 | 42 | 3 | 6 | 7 |
| 5 | 0.614 | YÜKSEK | `InvoiceRepository` | `reconcileDuplicates(String, boolean)` | 8 | 21 | 5 | 2 | 25 |
| 6 | 0.611 | YÜKSEK | `LoadService001` | `nested001(int, int, int)` | 6 | 13 | 5 | 3 | 86 |
| 7 | 0.611 | YÜKSEK | `LoadService002` | `nested002(int, int, int)` | 6 | 13 | 5 | 3 | 86 |
| 8 | 0.611 | YÜKSEK | `LoadService003` | `nested003(int, int, int)` | 6 | 13 | 5 | 3 | 86 |
| 9 | 0.611 | YÜKSEK | `LoadService004` | `nested004(int, int, int)` | 6 | 13 | 5 | 3 | 86 |
| 10 | 0.611 | YÜKSEK | `LoadService005` | `nested005(int, int, int)` | 6 | 13 | 5 | 3 | 86 |
| 11 | 0.611 | YÜKSEK | `LoadService006` | `nested006(int, int, int)` | 6 | 13 | 5 | 3 | 86 |
| 12 | 0.611 | YÜKSEK | `LoadService007` | `nested007(int, int, int)` | 6 | 13 | 5 | 3 | 86 |
| 13 | 0.611 | YÜKSEK | `LoadService008` | `nested008(int, int, int)` | 6 | 13 | 5 | 3 | 86 |
| 14 | 0.611 | YÜKSEK | `LoadService009` | `nested009(int, int, int)` | 6 | 13 | 5 | 3 | 86 |
| 15 | 0.611 | YÜKSEK | `LoadService010` | `nested010(int, int, int)` | 6 | 13 | 5 | 3 | 86 |

## Tüm metodlar (risk skoruna göre)

Alt skorlar 0–1 arası; hangi boyut riski artırdıysa o yüksek çıkar.

| Risk skoru | Seviye | Dosya | Metod | Dallanma | Kod satırı | İç içe | Dev metod? | Katkı: dallanma | satır | iç içe | parametre |
|-----------:|--------|-------|-------|----------:|-----------:|-------:|:----------:|---------------:|------:|---------:|-----------:|
| 0.902 | KRİTİK | `module-reporting/src/main/java/com/mock/reporting/ReportAggregator.java` | `buildExecutiveSummary(List, String, int, boolean)` | 31 | 81 | 8 | evet | 0.807 | 0.418 | 0.790 | 0.232 |
| 0.640 | YÜKSEK | `module-auth/src/main/java/com/mock/auth/AuthFacade.java` | `authorizeAction(String, String, String, List, boolean)` | 18 | 33 | 5 | hayır | 0.558 | 0.191 | 0.573 | 0.290 |
| 0.638 | YÜKSEK | `module-billing/src/main/java/com/mock/billing/BillingService.java` | `calculateWithTaxAndTier(List<String>, List<Integer>, String, boolean, String)` | 16 | 36 | 5 | hayır | 0.519 | 0.209 | 0.573 | 0.290 |
| 0.619 | YÜKSEK | `module-orders/src/main/java/com/mock/orders/CancellationPolicyService.java` | `computeRefundPercent(String, Date, Date, String, boolean, int)` | 18 | 42 | 3 | hayır | 0.558 | 0.244 | 0.290 | 0.395 |
| 0.614 | YÜKSEK | `module-billing/src/main/java/com/mock/billing/InvoiceRepository.java` | `reconcileDuplicates(String, boolean)` | 8 | 21 | 5 | hayır | 0.232 | 0.122 | 0.573 | 0.116 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService001.java` | `nested001(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService002.java` | `nested002(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService003.java` | `nested003(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService004.java` | `nested004(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService005.java` | `nested005(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService006.java` | `nested006(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService007.java` | `nested007(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService008.java` | `nested008(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService009.java` | `nested009(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService010.java` | `nested010(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService011.java` | `nested011(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService012.java` | `nested012(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService013.java` | `nested013(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService014.java` | `nested014(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService015.java` | `nested015(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService016.java` | `nested016(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService017.java` | `nested017(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService018.java` | `nested018(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService019.java` | `nested019(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService020.java` | `nested020(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService021.java` | `nested021(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService022.java` | `nested022(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService023.java` | `nested023(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService024.java` | `nested024(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService025.java` | `nested025(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService026.java` | `nested026(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService027.java` | `nested027(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService028.java` | `nested028(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService029.java` | `nested029(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService030.java` | `nested030(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService031.java` | `nested031(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService032.java` | `nested032(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService033.java` | `nested033(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService034.java` | `nested034(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.611 | YÜKSEK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService035.java` | `nested035(int, int, int)` | 6 | 13 | 5 | hayır | 0.174 | 0.075 | 0.573 | 0.174 |
| 0.499 | ORTA | `module-etl/src/main/java/com/mock/etl/DimensionMerger.java` | `mergeRows(List, List, String, boolean)` | 13 | 46 | 4 | hayır | 0.414 | 0.267 | 0.490 | 0.232 |
| 0.499 | ORTA | `module-fraud/src/main/java/com/mock/fraud/FraudRuleEngine.java` | `scoreTransaction(Map<String,Object>, List<String>, String, boolean, int)` | 15 | 25 | 4 | hayır | 0.490 | 0.145 | 0.490 | 0.290 |
| 0.499 | ORTA | `module-orders/src/main/java/com/mock/orders/OrderFulfillmentService.java` | `countShippableLines(List<String>, List<Integer>, List<Boolean>)` | 9 | 18 | 4 | hayır | 0.261 | 0.104 | 0.490 | 0.174 |
| 0.455 | ORTA | `module-reporting/src/main/java/com/mock/reporting/ReportAggregator.java` | `rankRowsByMetric(List, String, boolean)` | 13 | 27 | 2 | hayır | 0.414 | 0.157 | 0.193 | 0.174 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService001.java` | `evaluate001(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService002.java` | `evaluate002(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService003.java` | `evaluate003(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService004.java` | `evaluate004(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService005.java` | `evaluate005(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService006.java` | `evaluate006(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService007.java` | `evaluate007(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService008.java` | `evaluate008(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService009.java` | `evaluate009(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService010.java` | `evaluate010(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService011.java` | `evaluate011(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService012.java` | `evaluate012(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService013.java` | `evaluate013(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService014.java` | `evaluate014(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService015.java` | `evaluate015(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService016.java` | `evaluate016(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService017.java` | `evaluate017(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService018.java` | `evaluate018(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService019.java` | `evaluate019(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService020.java` | `evaluate020(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService021.java` | `evaluate021(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService022.java` | `evaluate022(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService023.java` | `evaluate023(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService024.java` | `evaluate024(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService025.java` | `evaluate025(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService026.java` | `evaluate026(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService027.java` | `evaluate027(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService028.java` | `evaluate028(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService029.java` | `evaluate029(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService030.java` | `evaluate030(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService031.java` | `evaluate031(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService032.java` | `evaluate032(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService033.java` | `evaluate033(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService034.java` | `evaluate034(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.418 | ORTA | `module-loadtest/src/main/java/com/mock/loadtest/LoadService035.java` | `evaluate035(int, String, List<String>, boolean)` | 12 | 20 | 3 | hayır | 0.376 | 0.116 | 0.290 | 0.232 |
| 0.373 | ORTA | `module-payments/src/main/java/com/mock/payments/PaymentOrchestrator.java` | `route(List<String>, double, String, boolean)` | 11 | 11 | 2 | hayır | 0.338 | 0.064 | 0.193 | 0.232 |
| 0.299 | DÜŞÜK | `module-etl/src/main/java/com/mock/etl/StagingLoader.java` | `loadRows(String, String)` | 6 | 22 | 3 | hayır | 0.174 | 0.128 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService001.java` | `merge001(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService001.java` | `rate001(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService002.java` | `merge002(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService002.java` | `rate002(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService003.java` | `merge003(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService003.java` | `rate003(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService004.java` | `merge004(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService004.java` | `rate004(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService005.java` | `merge005(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService005.java` | `rate005(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService006.java` | `merge006(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService006.java` | `rate006(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService007.java` | `merge007(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService007.java` | `rate007(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService008.java` | `merge008(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService008.java` | `rate008(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService009.java` | `merge009(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService009.java` | `rate009(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService010.java` | `merge010(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService010.java` | `rate010(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService011.java` | `merge011(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService011.java` | `rate011(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService012.java` | `merge012(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService012.java` | `rate012(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService013.java` | `merge013(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService013.java` | `rate013(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService014.java` | `merge014(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService014.java` | `rate014(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService015.java` | `merge015(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService015.java` | `rate015(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService016.java` | `merge016(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService016.java` | `rate016(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService017.java` | `merge017(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService017.java` | `rate017(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService018.java` | `merge018(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService018.java` | `rate018(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService019.java` | `merge019(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService019.java` | `rate019(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService020.java` | `merge020(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService020.java` | `rate020(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService021.java` | `merge021(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService021.java` | `rate021(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService022.java` | `merge022(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService022.java` | `rate022(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService023.java` | `merge023(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService023.java` | `rate023(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService024.java` | `merge024(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService024.java` | `rate024(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService025.java` | `merge025(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService025.java` | `rate025(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService026.java` | `merge026(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService026.java` | `rate026(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService027.java` | `merge027(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService027.java` | `rate027(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService028.java` | `merge028(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService028.java` | `rate028(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService029.java` | `merge029(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService029.java` | `rate029(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService030.java` | `merge030(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService030.java` | `rate030(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService031.java` | `merge031(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService031.java` | `rate031(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService032.java` | `merge032(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService032.java` | `rate032(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService033.java` | `merge033(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService033.java` | `rate033(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService034.java` | `merge034(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService034.java` | `rate034(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService035.java` | `merge035(Map<String,Object>, Map<String,Object>)` | 7 | 13 | 3 | hayır | 0.203 | 0.075 | 0.290 | 0.116 |
| 0.299 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService035.java` | `rate035(double, String, int)` | 6 | 13 | 3 | hayır | 0.174 | 0.075 | 0.290 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService001.java` | `validate001(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService002.java` | `validate002(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService003.java` | `validate003(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService004.java` | `validate004(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService005.java` | `validate005(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService006.java` | `validate006(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService007.java` | `validate007(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService008.java` | `validate008(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService009.java` | `validate009(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService010.java` | `validate010(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService011.java` | `validate011(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService012.java` | `validate012(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService013.java` | `validate013(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService014.java` | `validate014(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService015.java` | `validate015(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService016.java` | `validate016(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService017.java` | `validate017(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService018.java` | `validate018(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService019.java` | `validate019(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService020.java` | `validate020(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService021.java` | `validate021(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService022.java` | `validate022(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService023.java` | `validate023(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService024.java` | `validate024(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService025.java` | `validate025(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService026.java` | `validate026(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService027.java` | `validate027(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService028.java` | `validate028(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService029.java` | `validate029(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService030.java` | `validate030(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService031.java` | `validate031(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService032.java` | `validate032(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService033.java` | `validate033(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService034.java` | `validate034(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.228 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService035.java` | `validate035(String, int, boolean)` | 7 | 9 | 2 | hayır | 0.203 | 0.052 | 0.193 | 0.174 |
| 0.224 | DÜŞÜK | `module-fraud/src/main/java/com/mock/fraud/FraudRuleEngine.java` | `blockIfHighRisk(int, String, boolean)` | 7 | 9 | 1 | hayır | 0.203 | 0.052 | 0.097 | 0.174 |
| 0.217 | DÜŞÜK | `module-orders/src/main/java/com/mock/orders/OrderFulfillmentService.java` | `pickWarehouse(String, int)` | 6 | 18 | 2 | hayır | 0.174 | 0.104 | 0.193 | 0.116 |
| 0.217 | DÜŞÜK | `module-orders/src/main/java/com/mock/orders/OrderFulfillmentService.java` | `splitByWeight(List<String>, int)` | 6 | 17 | 2 | hayır | 0.174 | 0.099 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService001.java` | `batch001(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService002.java` | `batch002(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService003.java` | `batch003(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService004.java` | `batch004(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService005.java` | `batch005(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService006.java` | `batch006(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService007.java` | `batch007(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService008.java` | `batch008(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService009.java` | `batch009(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService010.java` | `batch010(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService011.java` | `batch011(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService012.java` | `batch012(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService013.java` | `batch013(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService014.java` | `batch014(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService015.java` | `batch015(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService016.java` | `batch016(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService017.java` | `batch017(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService018.java` | `batch018(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService019.java` | `batch019(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService020.java` | `batch020(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService021.java` | `batch021(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService022.java` | `batch022(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService023.java` | `batch023(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService024.java` | `batch024(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService025.java` | `batch025(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService026.java` | `batch026(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService027.java` | `batch027(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService028.java` | `batch028(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService029.java` | `batch029(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService030.java` | `batch030(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService031.java` | `batch031(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService032.java` | `batch032(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService033.java` | `batch033(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService034.java` | `batch034(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.216 | DÜŞÜK | `module-loadtest/src/main/java/com/mock/loadtest/LoadService035.java` | `batch035(List<String>, int)` | 6 | 16 | 2 | hayır | 0.174 | 0.093 | 0.193 | 0.116 |
| 0.213 | DÜŞÜK | `module-payments/src/main/java/com/mock/payments/PaymentOrchestrator.java` | `reconcile(List<Integer>, List<Integer>)` | 5 | 9 | 2 | hayır | 0.145 | 0.052 | 0.193 | 0.116 |
| 0.212 | DÜŞÜK | `module-etl/src/main/java/com/mock/etl/StagingLoader.java` | `loadDim(String[])` | 5 | 9 | 2 | hayır | 0.145 | 0.052 | 0.193 | 0.058 |
| 0.210 | DÜŞÜK | `module-integration/src/main/java/com/mock/integration/LegacyBridgeService.java` | `mapOracle(Map<String,Object>)` | 4 | 8 | 2 | hayır | 0.116 | 0.046 | 0.193 | 0.058 |
| 0.209 | DÜŞÜK | `module-billing/src/main/java/com/mock/billing/InvoiceRepository.java` | `findById(String)` | 3 | 8 | 2 | hayır | 0.087 | 0.046 | 0.193 | 0.058 |
| 0.209 | DÜŞÜK | `module-integration/src/main/java/com/mock/integration/LegacyBridgeService.java` | `mapMainframe(Map<String,Object>)` | 3 | 11 | 2 | hayır | 0.087 | 0.064 | 0.193 | 0.058 |
| 0.195 | DÜŞÜK | `module-catalog/src/main/java/com/mock/catalog/PriceRuleEngine.java` | `applyDiscount(double, String, boolean)` | 6 | 17 | 1 | hayır | 0.174 | 0.099 | 0.097 | 0.174 |
| 0.193 | DÜŞÜK | `module-integration/src/main/java/com/mock/integration/LegacyBridgeService.java` | `translatePayload(String, Map<String,Object>)` | 6 | 15 | 1 | hayır | 0.174 | 0.087 | 0.097 | 0.116 |
| 0.163 | DÜŞÜK | `module-etl/src/main/java/com/mock/etl/StagingLoader.java` | `ingestLine(String, String)` | 5 | 16 | 1 | hayır | 0.145 | 0.093 | 0.097 | 0.116 |
| 0.162 | DÜŞÜK | `module-catalog/src/main/java/com/mock/catalog/CatalogService.java` | `resolveDisplayName(String, String)` | 5 | 14 | 1 | hayır | 0.145 | 0.081 | 0.097 | 0.116 |
| 0.161 | DÜŞÜK | `module-catalog/src/main/java/com/mock/catalog/CatalogService.java` | `categoryRank(String)` | 5 | 15 | 1 | hayır | 0.145 | 0.087 | 0.097 | 0.058 |
| 0.131 | DÜŞÜK | `module-etl/src/main/java/com/mock/etl/DimensionMerger.java` | `hasChanged(Object, Object)` | 4 | 9 | 1 | hayır | 0.116 | 0.052 | 0.097 | 0.116 |
| 0.131 | DÜŞÜK | `module-reporting/src/main/java/com/mock/reporting/ReportAggregator.java` | `parseMetric(Object, int)` | 4 | 10 | 1 | hayır | 0.116 | 0.058 | 0.097 | 0.116 |
| 0.130 | DÜŞÜK | `module-auth/src/main/java/com/mock/auth/AuthFacade.java` | `hasRole(String, String)` | 4 | 6 | 1 | hayır | 0.116 | 0.035 | 0.097 | 0.116 |
| 0.130 | DÜŞÜK | `module-auth/src/main/java/com/mock/auth/AuthFacade.java` | `passwordStrength(String)` | 4 | 13 | 1 | hayır | 0.116 | 0.075 | 0.097 | 0.058 |
| 0.130 | DÜŞÜK | `module-etl/src/main/java/com/mock/etl/StagingLoader.java` | `loadFact(String[])` | 4 | 11 | 1 | hayır | 0.116 | 0.064 | 0.097 | 0.058 |
| 0.130 | DÜŞÜK | `module-etl/src/main/java/com/mock/etl/StagingLoader.java` | `handleBadRow(String, String)` | 4 | 6 | 1 | hayır | 0.116 | 0.035 | 0.097 | 0.116 |
| 0.129 | DÜŞÜK | `module-etl/src/main/java/com/mock/etl/DimensionMerger.java` | `extractKey(Object, String)` | 3 | 8 | 1 | hayır | 0.087 | 0.046 | 0.097 | 0.116 |
| 0.127 | DÜŞÜK | `module-billing/src/main/java/com/mock/billing/BillingService.java` | `calculateLineTotal(int, BigDecimal)` | 2 | 6 | 1 | hayır | 0.058 | 0.035 | 0.097 | 0.116 |
| 0.124 | DÜŞÜK | `module-auth/src/main/java/com/mock/auth/AuthFacade.java` | `login(String, String)` | 3 | 3 | 0 | hayır | 0.087 | 0.017 | 0.000 | 0.116 |
| 0.121 | DÜŞÜK | `module-auth/src/main/java/com/mock/auth/AuthFacade.java` | `isSessionExpired(long, long)` | 1 | 3 | 0 | hayır | 0.029 | 0.017 | 0.000 | 0.116 |
| 0.121 | DÜŞÜK | `module-billing/src/main/java/com/mock/billing/BillingService.java` | `isOverdue(long, long)` | 1 | 3 | 0 | hayır | 0.029 | 0.017 | 0.000 | 0.116 |
| 0.121 | DÜŞÜK | `module-catalog/src/main/java/com/mock/catalog/CatalogService.java` | `register(String, String)` | 1 | 3 | 0 | hayır | 0.029 | 0.017 | 0.000 | 0.116 |
| 0.108 | DÜŞÜK | `module-auth/src/main/java/com/mock/auth/AuthFacade.java` | `maskEmail(String)` | 3 | 6 | 1 | hayır | 0.087 | 0.035 | 0.097 | 0.058 |
| 0.108 | DÜŞÜK | `module-billing/src/main/java/com/mock/billing/InvoiceRepository.java` | `save(String)` | 3 | 5 | 1 | hayır | 0.087 | 0.029 | 0.097 | 0.058 |
| 0.106 | DÜŞÜK | `module-auth/src/main/java/com/mock/auth/TokenService.java` | `issueToken(String)` | 2 | 6 | 1 | hayır | 0.058 | 0.035 | 0.097 | 0.058 |
| 0.106 | DÜŞÜK | `module-integration/src/main/java/com/mock/integration/LegacyBridgeService.java` | `mapSap(Map<String,Object>)` | 2 | 8 | 1 | hayır | 0.058 | 0.046 | 0.097 | 0.058 |
| 0.063 | DÜŞÜK | `module-auth/src/main/java/com/mock/auth/TokenService.java` | `validateToken(String)` | 2 | 3 | 0 | hayır | 0.058 | 0.017 | 0.000 | 0.058 |
| 0.063 | DÜŞÜK | `module-etl/src/main/java/com/mock/etl/StagingLoader.java` | `loadStage(String[])` | 2 | 3 | 0 | hayır | 0.058 | 0.017 | 0.000 | 0.058 |
| 0.063 | DÜŞÜK | `module-etl/src/main/java/com/mock/etl/StagingLoader.java` | `isFatal(RuntimeException)` | 2 | 3 | 0 | hayır | 0.058 | 0.017 | 0.000 | 0.058 |
| 0.062 | DÜŞÜK | `module-auth/src/main/java/com/mock/auth/AuthFacade.java` | `logout(String)` | 1 | 3 | 0 | hayır | 0.029 | 0.017 | 0.000 | 0.058 |

---
*PMD ile karşılaştırmak için `pmd-raporu.md` dosyasına bakın; CC sayıları yakın olmalı, risk skoru ise sadece bu parser aracına özeldir.*
