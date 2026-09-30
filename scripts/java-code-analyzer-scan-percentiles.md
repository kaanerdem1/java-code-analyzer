# mock-modules tarama metrikleri — yüzdelikler

Otomatik üretim: `ScanMetricsCalibration` — 2026-09-30

Kök: `/Users/kaanerdem/Desktop/java-code-analyzer`

Özet: 89 dosya, 124 sınıf, 611 metod, 8657 kod satırı.

## Metod

| Metrik | n | p50 | p90 | p95 | max |
|--------|---|-----|-----|-----|-----|
| cyclomaticComplexity | 611 | 4 | 9 | 12 | 31 |
| codeLines | 611 | 12 | 20 | 27 | 81 |
| maxNestingDepth | 611 | 1 | 3 | 5 | 8 |
| parameterCount | 611 | 2 | 4 | 4 | 16 |
| cognitiveComplexity | 611 | 3 | 15 | 15 | 94 |
| logicalStatements | 611 | 8 | 21 | 21 | 63 |
| exitPoints | 611 | 1 | 5 | 6 | 11 |
| catchClauses | 611 | 0 | 0 | 0 | 5 |
| switchCases | 611 | 0 | 0 | 3 | 19 |
| outboundDistinctCalls | 611 | 0 | 3 | 5 | 25 |
| primitiveObsessionIndex | 611 | 4 | 12 | 12 | 30 |
| maxBooleanOperatorsInCondition | 611 | 0 | 1 | 1 | 6 |
| lambdaCount | 611 | 0 | 0 | 1 | 4 |
| maxTryNestingDepth | 611 | 0 | 0 | 0 | 2 |
| localVariableCount | 611 | 1 | 4 | 5 | 23 |
| maxMethodCallChainLength | 611 | 1 | 3 | 3 | 15 |
| emptyCatchBlocks | 611 | 0 | 0 | 0 | 1 |
| catchExceptionOrThrowable | 611 | 0 | 0 | 0 | 1 |
| catchWithOnlyPrintStackTrace | 611 | 0 | 0 | 0 | 0 |

## Sınıf

| Metrik | n | p50 | p90 | p95 | max |
|--------|---|-----|-----|-----|-----|
| methodCount | 124 | 4 | 7 | 11 | 58 |
| codeLines (class) | 124 | 51 | 97 | 122 | 618 |
| weightedMethodComplexity (WMC) | 124 | 17 | 44 | 45 | 182 |
| publicMethodCount | 124 | 1 | 6 | 6 | 31 |
| efferentCouplingProxy | 124 | 1 | 7 | 12 | 47 |

## Önerilen YAML eşikleri (dikkat=p50, yüksek=p90, kritik=p95)

```yaml
      branching:
        thresholds: { dikkat: 4, yuksek: 9, kritik: 12 }
      length:
        thresholds: { dikkat: 12, yuksek: 20, kritik: 27 }
      nesting:
        thresholds: { dikkat: 1, yuksek: 3, kritik: 5 }
      parameters:
        thresholds: { dikkat: 2, yuksek: 4, kritik: 4 }
      cognitive:
        thresholds: { dikkat: 3, yuksek: 15, kritik: 15 }
      logicalStatements:
        thresholds: { dikkat: 8, yuksek: 21, kritik: 21 }
      exitPoints:
        thresholds: { dikkat: 1, yuksek: 5, kritik: 6 }
      catchClauses:
        thresholds: { dikkat: 0, yuksek: 0, kritik: 0 }
      switchCases:
        thresholds: { dikkat: 0, yuksek: 0, kritik: 3 }
      outboundDistinctCalls:
        thresholds: { dikkat: 0, yuksek: 3, kritik: 5 }
      primitiveObsessionIndex:
        thresholds: { dikkat: 4, yuksek: 12, kritik: 12 }
      maxBooleanOperatorsInCondition:
        thresholds: { dikkat: 0, yuksek: 1, kritik: 1 }
      lambdaCount:
        thresholds: { dikkat: 0, yuksek: 0, kritik: 1 }
      maxTryNestingDepth:
        thresholds: { dikkat: 0, yuksek: 0, kritik: 0 }
      localVariableCount:
        thresholds: { dikkat: 1, yuksek: 4, kritik: 5 }
      maxMethodCallChainLength:
        thresholds: { dikkat: 1, yuksek: 3, kritik: 3 }
      emptyCatchBlocks:
        thresholds: { dikkat: 0, yuksek: 0, kritik: 0 }
      catchExceptionOrThrowable:
        thresholds: { dikkat: 0, yuksek: 0, kritik: 0 }
      catchWithOnlyPrintStackTrace:
        thresholds: { dikkat: 0, yuksek: 0, kritik: 0 }
```

Not: FOUT artık import-aware + JDK/same-type filtreli; eşikleri bu tabloya göre güncelle.
