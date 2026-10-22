-- Çalışır örnekler (en son scan_run_id otomatik). psql -d postgres -f scripts/catalog-queries-examples.sql

WITH last_run AS (
    SELECT scan_run_id
    FROM env.method_risk_analyze
    ORDER BY created_at DESC
    LIMIT 1
)
SELECT scan_run_id,
       COUNT(*) FILTER (WHERE is_parser) AS parser_methods,
       COUNT(*) FILTER (WHERE is_pmd) AS pmd_methods
FROM env.method_risk_analyze
WHERE scan_run_id = (SELECT scan_run_id FROM last_run)
GROUP BY scan_run_id;

WITH last_run AS (SELECT scan_run_id FROM env.method_risk_analyze ORDER BY created_at DESC LIMIT 1)
SELECT method_uid,
       is_parser,
       risk_score,
       risk_level,
       jsonb_pretty(risk_inputs_json) AS risk_inputs
FROM env.method_risk_analyze
WHERE scan_run_id = (SELECT scan_run_id FROM last_run)
  AND method_uid LIKE '%ReportAggregator%buildExecutiveSummary%'
ORDER BY is_parser DESC;

WITH last_run AS (SELECT scan_run_id FROM env.method_risk_analyze ORDER BY created_at DESC LIMIT 1)
SELECT method_uid,
       is_parser,
       risk_inputs_json -> 'riskBreakdown' ->> 'finalScore' AS breakdown_final,
       risk_inputs_json -> 'metrics' -> 'cyclomaticComplexity' AS cc,
       risk_inputs_json -> 'riskFactors' AS risk_factors
FROM env.method_risk_analyze
WHERE scan_run_id = (SELECT scan_run_id FROM last_run)
  AND risk_level IN ('HIGH', 'CRITICAL')
ORDER BY risk_score DESC
LIMIT 5;

WITH last_run AS (SELECT scan_run_id FROM env.method_risk_analyze ORDER BY created_at DESC LIMIT 1)
SELECT p.method_uid,
       p.risk_score AS parser_score,
       m.risk_score AS pmd_score,
       p.risk_inputs_json -> 'riskBreakdown' AS parser_breakdown,
       m.risk_inputs_json -> 'riskBreakdown' AS pmd_breakdown
FROM env.method_risk_analyze p
JOIN env.method_risk_analyze m
  ON p.scan_run_id = m.scan_run_id AND p.method_uid = m.method_uid
WHERE p.scan_run_id = (SELECT scan_run_id FROM last_run)
  AND p.is_parser AND m.is_pmd
ORDER BY p.risk_score DESC
LIMIT 10;
