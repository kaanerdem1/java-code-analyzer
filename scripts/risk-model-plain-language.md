# Risk modeli — sade anlatım

Bu metin, **bir servis metodunun** nasıl puanlandığını günlük dille anlatır. Varsayılan öneri profili: **`enterprise-java`** ([`config/risk-parameters-proposal.yaml`](../config/risk-parameters-proposal.yaml)).

**CLI:** `--risk-profile=enterprise-java` → YAML’daki **17 metod boyutu** skora girer. Profil vermezsen **legacy v1** (sadece 4 boyut) kullanılır.

---

## Bir cümlede model

Tarayıcı metodu okur, **birçok sayı** toplar (dallanma, uzunluk, iç içe yapı, parametre, okunabilirlik, dış çağrılar, catch kalitesi vb.). Her biri 0–1 **alt puan** üretir. **En kötü boyut** metodun ana derdidir; diğerleri skora **%15’lik ek karışım** olarak girer. Sonuç **0–1 risk skoru** ve **DÜŞÜK / ORTA / YÜKSEK / KRİTİK** etiketidir.

---

## Adım adım (metod skoru)

1. **Ölç** — `enterprise-java` profilinde skora girenler:
   - **Dallanma (CC), LOC, iç içe derinlik, parametre** (klasik dörtlü)
   - **Cognitive complexity** (okunabilirlik)
   - **Outbound distinct calls** (kaç farklı dış çağrı — orchestrator şişkinliği)
   - **Exit points** (return / throw / break / continue)
   - **Logical statements** (ifade yoğunluğu)
   - **Lambda, switch kolu, try iç içe, yerel değişken, çağrı zinciri (LoD proxy)**
   - **Catch:** kol sayısı, boş catch, geniş `Exception`/`Throwable`, sadece `printStackTrace`

2. **Alt skor (0–1)** — Her boyut için YAML’daki **dikkat / yüksek / kritik** eşikleri.

3. **Ana derdi seç** — En yüksek alt skor = `dominant`.

4. **%15 karışım** — Profildeki `blend_weights` (17 boyut, normalize ağırlıklar).

5. **Rapor** — JSON’da `riskBreakdown`: çekirdek dörtlü + cognitive / fout / exit; diğer boyutlar `extraSubScores` map’inde. Markdown tabloda **cog** ve **fout** sütunları.

---

## Profiller

| Profil | Ne zaman? |
|--------|-----------|
| *(yok)* | Eski v1; CI geçişi için |
| `enterprise-java` | Monorepo servis/ETL; tam v2 |
| `strict-review` | PR gate; daha sıkı eşikler (4 boyut) |
| `generated-tolerant` | Üretilmiş kod; yalnız uç değerler |

---

## CI çıkış kodları

| Kod | Bayrak | Anlam |
|-----|--------|--------|
| 0 | — | OK |
| 1 | — | Argüman / I/O hatası |
| 2 | `--max-failure-ratio=0.1` | Parse hata oranı aşıldı |
| 3 | `--fail-on-risk=HIGH` | Proje veya herhangi bir metod eşik seviyede |

Kalibrasyon tablosu: [`mock-modules-scan-percentiles.md`](mock-modules-scan-percentiles.md) (`./scripts/calibrate-scan-metrics.sh`).

Detaylı eşik gerekçesi: [`risk-parameters-research.md`](risk-parameters-research.md).
