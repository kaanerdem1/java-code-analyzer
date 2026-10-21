# mock-modules tarama metrikleri — yüzdelikler

Otomatik üretim: `ScanMetricsCalibration` — 2026-10-20

Kök: `/Users/kaanerdem/Developer/java-code-analyzer`

Özet: 76 dosya, 141 sınıf, 656 metod, 9305 kod satırı.

## Metod

| Metrik | n | p50 | p90 | p95 | max |
|--------|---|-----|-----|-----|-----|
| cyclomaticComplexity | 656 | 2 | 7 | 10 | 36 |
| codeLines | 656 | 8 | 25 | 37 | 105 |
| maxNestingDepth | 656 | 1 | 2 | 3 | 8 |
| parameterCount | 656 | 1 | 3 | 5 | 33 |
| cognitiveComplexity | 656 | 1 | 7 | 12 | 94 |
| logicalStatements | 656 | 4 | 17 | 27 | 92 |
| exitPoints | 656 | 1 | 3 | 4 | 11 |
| catchClauses | 656 | 0 | 0 | 0 | 5 |
| switchCases | 656 | 0 | 0 | 0 | 29 |
| outboundDistinctCalls | 656 | 0 | 4 | 6 | 25 |
| primitiveObsessionIndex | 656 | 2 | 12 | 15 | 65 |
| maxBooleanOperatorsInCondition | 656 | 0 | 1 | 1 | 6 |
| lambdaCount | 656 | 0 | 1 | 1 | 4 |
| maxTryNestingDepth | 656 | 0 | 0 | 1 | 2 |
| localVariableCount | 656 | 0 | 5 | 8 | 32 |
| maxMethodCallChainLength | 656 | 1 | 3 | 4 | 15 |
| emptyCatchBlocks | 656 | 0 | 0 | 0 | 1 |
| catchExceptionOrThrowable | 656 | 0 | 0 | 0 | 1 |
| catchWithOnlyPrintStackTrace | 656 | 0 | 0 | 0 | 1 |

## Sınıf

| Metrik | n | p50 | p90 | p95 | max |
|--------|---|-----|-----|-----|-----|
| methodCount | 141 | 2 | 10 | 15 | 62 |
| codeLines (class) | 141 | 19 | 121 | 230 | 950 |
| weightedMethodComplexity (WMC) | 141 | 6 | 37 | 59 | 285 |
| publicMethodCount | 141 | 0 | 3 | 4 | 36 |
| efferentCouplingProxy | 141 | 1 | 9 | 14 | 51 |

## Önerilen YAML eşikleri (dikkat=p50, yüksek=p90, kritik=p95)

```yaml
      branching:
        thresholds: { dikkat: 2, yuksek: 7, kritik: 10 }
      length:
        thresholds: { dikkat: 8, yuksek: 25, kritik: 37 }
      nesting:
        thresholds: { dikkat: 1, yuksek: 2, kritik: 3 }
      parameters:
        thresholds: { dikkat: 1, yuksek: 3, kritik: 5 }
      cognitive:
        thresholds: { dikkat: 1, yuksek: 7, kritik: 12 }
      logicalStatements:
        thresholds: { dikkat: 4, yuksek: 17, kritik: 27 }
      exitPoints:
        thresholds: { dikkat: 1, yuksek: 3, kritik: 4 }
      catchClauses:
        thresholds: { dikkat: 0, yuksek: 0, kritik: 0 }
      switchCases:
        thresholds: { dikkat: 0, yuksek: 0, kritik: 0 }
      outboundDistinctCalls:
        thresholds: { dikkat: 0, yuksek: 4, kritik: 6 }
      primitiveObsessionIndex:
        thresholds: { dikkat: 2, yuksek: 12, kritik: 15 }
      maxBooleanOperatorsInCondition:
        thresholds: { dikkat: 0, yuksek: 1, kritik: 1 }
      lambdaCount:
        thresholds: { dikkat: 0, yuksek: 1, kritik: 1 }
      maxTryNestingDepth:
        thresholds: { dikkat: 0, yuksek: 0, kritik: 1 }
      localVariableCount:
        thresholds: { dikkat: 0, yuksek: 5, kritik: 8 }
      maxMethodCallChainLength:
        thresholds: { dikkat: 1, yuksek: 3, kritik: 4 }
      emptyCatchBlocks:
        thresholds: { dikkat: 0, yuksek: 0, kritik: 0 }
      catchExceptionOrThrowable:
        thresholds: { dikkat: 0, yuksek: 0, kritik: 0 }
      catchWithOnlyPrintStackTrace:
        thresholds: { dikkat: 0, yuksek: 0, kritik: 0 }
```

Not: FOUT artık import-aware + JDK/same-type filtreli; eşikleri bu tabloya göre güncelle.
