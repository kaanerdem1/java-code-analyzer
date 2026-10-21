# CCRM analyzer JSON — yüzdelik kalibrasyon (2026-10)

Kaynak: 8 modül, **12.228 metod** (Desktop `raporlar/CCRM/*.json`, o koşuda v1 skor; ham metrikler geçerli).

| Metrik | p50 | p90 | p95 | max | >0 % |
|--------|-----|-----|-----|-----|------|
| branching (CC) | 1 | 4 | 10 | 199 | 100 |
| length (LOC) | 4 | 24 | 45 | 558 | 100 |
| nesting | 0 | 2 | 4 | 9 | 34.6 |
| parameters | 1 | 3 | 4 | 8 | 77.2 |
| cognitive | 0 | 5 | 18 | 230 | 34.6 |
| logicalStatements | 1 | 14 | 32 | 330 | 99.3 |
| lambdaCount | 0 | 0 | 0 | 1 | 0.0 |
| switchCases | 0 | 0 | 0 | 24 | 0.0 |
| catch / try nest | 0 | 0 | 1 | 7 | ~9.5 |

Koku (metod sayısı): hardcoded **2765** (22.6%), rawType **827**, generic catch **351**, swallowed **26**, concat **6**.

`enterprise-java` profilinde yapılan ayar özeti → `config/risk-parameters-proposal.yaml` `policy_tr` (2026-10 satırı).
