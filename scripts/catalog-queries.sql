-- Örnek sorgular — env şeması
-- 1) Son koşunun scan_run_id'sini alın (ilk sorgu).
-- 2) Aşağıdaki sorgularda :SCAN_RUN yerine UUID yapıştırın veya psql'de:
--    \set SCAN_RUN '4808a333-6d07-4a15-81c5-7941a2485f12'
--    sonra sorgularda :'SCAN_RUN'::uuid kullanın.

-- Son koşu kimliği
SELECT scan_run_id, MAX(created_at) AS loaded_at
FROM env.method_risk_analyze
GROUP BY scan_run_id
ORDER BY loaded_at DESC
LIMIT 5;

-- --- Aşağıdakilerde SCAN_RUN UUID'sini değiştirin ---
-- \set SCAN_RUN '4808a333-6d07-4a15-81c5-7941a2485f12'

-- Metod sayısı (parser vs pmd)
-- SELECT scan_run_id,
--        COUNT(*) FILTER (WHERE is_parser) AS parser_methods,
--        COUNT(*) FILTER (WHERE is_pmd) AS pmd_methods
-- FROM env.method_risk_analyze
-- WHERE scan_run_id = :'SCAN_RUN'::uuid
-- GROUP BY scan_run_id;

-- Tek metod: risk JSON (tam risk_inputs_json)
-- SELECT method_uid, is_parser, risk_score, risk_level,
--        jsonb_pretty(risk_inputs_json) AS risk_inputs
-- FROM env.method_risk_analyze
-- WHERE scan_run_id = :'SCAN_RUN'::uuid
--   AND method_uid LIKE '%ReportAggregator%buildExecutiveSummary%'
-- ORDER BY is_parser DESC;

-- Alt skorlar ve faktörler (JSON path)
-- SELECT method_uid,
--        is_parser,
--        risk_inputs_json -> 'metrics' AS metrics,
--        risk_inputs_json -> 'riskBreakdown' AS risk_breakdown,
--        risk_inputs_json -> 'riskFactors' AS risk_factors,
--        risk_inputs_json -> 'pmd' -> 'ruleHits' AS pmd_rule_hits
-- FROM env.method_risk_analyze
-- WHERE scan_run_id = :'SCAN_RUN'::uuid
--   AND method_uid LIKE '%buildExecutiveSummary%';

-- Parser vs PMD aynı metod
-- SELECT p.method_uid,
--        p.risk_score AS parser_score,
--        m.risk_score AS pmd_score,
--        p.ncss_loc AS parser_loc,
--        m.ncss_loc AS pmd_loc
-- FROM env.method_risk_analyze p
-- JOIN env.method_risk_analyze m ON p.scan_run_id = m.scan_run_id AND p.method_uid = m.method_uid
-- WHERE p.scan_run_id = :'SCAN_RUN'::uuid AND p.is_parser AND m.is_pmd
-- ORDER BY ABS(p.risk_score - m.risk_score) DESC
-- LIMIT 15;

-- Benzerlik grupları
-- SELECT * FROM env.method_similarity
-- WHERE scan_run_id = :'SCAN_RUN'::uuid
-- ORDER BY similarity_percent DESC;
