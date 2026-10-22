# PMD taraması — üç araç

| Script | Motor | Çıktı | Ne için? |
|--------|--------|--------|----------|
| **`run-analyze.sh`** | JavaParser + risk YAML | Parser JSON/MD | Üretim risk katalogu, incremental |
| **`run-pmd-analyze.sh`** | **Yalnızca PMD** (ruleset + JavaMetrics + PMD AST istatistik + ihlal→risk). JavaParser yok | JSON/MD + `-violations.xml` | PMD tabanlı katalog |
| **`run-analyze.sh`** | JavaParser (tam katalog) | parser JSON/MD | Referans / incremental |
| **`SCAN_PIPELINE=parser-hybrid`** | PMD ruleset + JavaParser metrik (parity test) | hibrit | Karşılaştırma |
| **`run-compare-pipelines.sh`** | Parser vs PMD-native | `pipeline-compare-*.json` | İki yöntemi karşılaştır |
| **`run-classic-pmd.sh`** | PMD **`PmdAnalysis` + ruleset XML** | **`pmd.xml`** + Türkçe violation MD | **Klasik PMD** — eşik ihlali, regresyon |

## İki farklı “PMD” yolu (özet)

| Yol | Ne çalışır | Çıktı |
|-----|------------|--------|
| **`run-classic-pmd.sh` → `ClassicPmdMain`** | Gerçek **ruleset violation** motoru (`config/pmd-ruleset-complexity.xml`) | Standart **pmd.xml** |
| **`run-pmd-analyze.sh` → `PmdScanMain`** | **Aşama 1:** klasik PMD ruleset → `-violations.xml`. **Aşama 2:** parser metrik, risk, duplicate | Tek JSON/MD: ihlaller + risk tablosu |

**Aşama 2** (parser) ruleset’teki 3 kuraldan fazlasını üretir: tam parametre seti JavaParser + risk YAML’dan gelir. Aşama 1 yalnızca klasik PMD ihlal listesidir.

## Ruleset

| Dosya | Amaç |
|-------|------|
| **`config/pmd-ruleset-complexity.xml`** | Üretim: karmaşıklık + design/errorprone/bestpractices (~20 kural, parser sinyallerine yakın) |
| **`config/pmd-ruleset-complexity-mock.xml`** | Demo: CC/cog/NPath eşiği 1 → mock-modules’te çok ihlal satırı |

Örnek üretim kuralları: `CyclomaticComplexity`, `CognitiveComplexity`, `NPathComplexity`, `NcssCount`, `ExcessiveParameterList`, `AvoidDeeplyNestedIfStmts`, `GodClass`, `TooManyMethods`, `LawOfDemeter`, `EmptyCatchBlock`, `AvoidCatchingGenericException`, `AvoidPrintStackTrace`, `LooseCoupling`, …

Mock ruleset: `PMD_RULESET=config/pmd-ruleset-complexity-mock.xml ./scripts/run-classic-pmd.sh mock-modules`

**Eklemek / çıkarmak (Maven ruleset):** XML’de `<rule ref="…">`. **`PmdScanMain` / parser metrikleri:** `ComplexityVisitor`, `SupplementalMetrics`, `config/risk-parameters-proposal.yaml` — yeni risk boyutu burada tanımlanır; PMD taraması parser ile otomatik hizalanır (`PmdParserMetricsParityTest`).

**PMD’den gelen 4 sayı:** cyclomatic, cognitive, NCSS, NPath — PMD’nin resmi sayımı. Geri kalan sütunlar (LOC, nesting, catch, Halstead, raw type, …) JavaParser ziyaretçisinden gelir; PMD ruleset’te karşılığı yoktur.

## Parser ile aynı kalite — nereye ekleme yapılır?

**Asıl yol:** yeni sinyali **`ComplexityVisitor` / `SupplementalMetrics`** ve **`config/risk-parameters-proposal.yaml`** (profil ağırlıkları + eşikler) içine eklemek. `run-pmd-analyze.sh` aynı `FileMetricsBuilder` + `RiskCalculator` hattını kullandığı için **`PmdScanMain` otomatik miras alır**; ayrı PMD ruleset kopyası gerekmez.

**İsteğe bağlı referans (skor üretmez):**

- Maven **`pmd-ruleset-complexity.xml`** veya `pmd.xml` — “PMD de bu metodu işaretledi mi?” regresyon / CI kapısı.
- Kurallar eklenebilir: GodClass, ExcessiveMethodLength, LawOfDemeter, … — parser skoruna bağlamadan ikinci görüş.

**`PmdScanMain`:** önce **gerçek PMD ruleset** (`ClassicPmdRulesetScanner`), sonra parser zenginleştirmesi. Yalnızca ihlal istiyorsanız `run-classic-pmd.sh`. Yalnızca risk/duplicate (parity): `--skip-pmd-ruleset`.

## Duplicate (benzerlik)

**PMD-native** (`run-pmd-analyze` varsayılan): önce PMD ruleset; risk katalogu parser + PMD sayım hizası (Halstead/nesting dahil — parser ile aynı skor, `PmdNativeParserRiskParityTest`). Duplicate:

1. **BIREBIR_METIN / YAPISAL_KLON** — risk sonrası JavaParser yalnızca `bodyHash` / `structuralHash` (`ParserDuplicateHashEnrichment`)
2. **BENZER_*** — **PMD CPD**; yüzde (ortak satır ÷ metod aralığı) ile çok yüksek / yüksek / orta / kısmi

**Parser-hybrid:** metrik+risk de JavaParser; duplicate aynı motor.

Kapatmak: parser’da `--no-duplicates` / `--no-near-duplicates`; PMD taramasında ileride aynı bayraklar eklenebilir (şu an duplicate açık).

## Risk skoru

**Sıra (run-pmd-analyze):** Aşama 1 PMD biter → **sonra** aşama 2 parser → **en sonda** duplicate/CPD. Risk skoru ve benzerlik grupları **PMD bittikten sonra** üretilir; fakat girdi **violations.xml değil**, aynı `.java` dosyalarının **JavaParser AST** çıktısıdır.

| Çıktı | Kaynak |
|--------|--------|
| `-violations.xml`, MD “Aşama 1” tablosu | PMD `RuleViolation` listesi |
| Metod tablosu, `riskScore`, `riskBreakdown` | `ComplexityVisitor` + YAML (**PMD XML okunmaz**) |
| CC/cog/NCSS/NPath (4 alan) | İsteğe bağlı `PmdMetricsAligner` (PMD metrics API, violation listesinden bağımsız) |
| `duplicateGroups` | Aşama 2 sonu: parser hash + PMD CPD |

Neden iki parse? PMD XML yalnızca kural ihlali satırı taşır; enterprise skor ~25 boyut + Halstead + sınıf metrikleri ister. Tek motor hedefi mümkün ama bugün risk motoru JavaParser’a bağlı; PMD aşama 1 **kapı/regresyon**, aşama 2 **katalog**.

**PMD script** varsayılan profil: **`enterprise-java-pmd`** (FOUT/call-chain yok; primitiveObsession ruleset proxy; Halstead AST ×1.52). **Parser** varsayılan: **`enterprise-java`**. `mock-modules`: parser ~53 HIGH, PMD ~51 HIGH (≈2 puan fark).

**Karmaşıklık ruleset** (`config/pmd-ruleset-complexity.xml`): CC/cog/NPath/NCSS, GodClass, SwitchDensity, ExcessiveImports/PublicCount, LawOfDemeter, ConfusingTernary, IdenticalCatchBranches, PrimitiveWrapperInstantiation, vb. — ihlaller `PmdViolationMethodEnricher` ile risk boyutlarına yansır.
