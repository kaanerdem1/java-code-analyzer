# Katalog sorguları — parser / PMD / `method_uid`

PostgreSQL `env.method_risk_analyze` tablosu için hazır sorgular.  
Genel kurulum: [catalog-db-tr.md](./catalog-db-tr.md).

## `method_uid` (kısa)

Metodu parser ve PMD satırları arasında eşleştiren anahtar:

```text
relativePath#className#methodSignature#startLine
```

Örnek:

```text
module-auth/src/main/java/com/mock/auth/AuthFacade.java#AuthFacade#login(String, String)#8
```

Her satır **tek pipeline**: `is_parser=true` **veya** `is_pmd=true` (ikisi birden olamaz).

---

## Son koşunun `scan_run_id` değeri

```sql
SELECT scan_run_id, MAX(created_at) AS loaded_at, COUNT(*) AS rows
FROM env.method_risk_analyze
GROUP BY scan_run_id
ORDER BY loaded_at DESC
LIMIT 5;
```

Aşağıdaki sorgularda `'YOUR-SCAN-RUN-UUID'::uuid` yerine kendi UUID’nizi koyun.

---

## Tüm parser kayıtları

```sql
SELECT method_uid, file_path, class_name, method_signature,
       start_line, end_line, ncss_loc, risk_score, risk_level
FROM env.method_risk_analyze
WHERE scan_run_id = 'YOUR-SCAN-RUN-UUID'::uuid
  AND is_parser = TRUE
ORDER BY risk_score DESC NULLS LAST, method_uid;
```

---

## Tüm PMD kayıtları

```sql
SELECT method_uid, file_path, class_name, method_signature,
       start_line, end_line, ncss_loc, risk_score, risk_level
FROM env.method_risk_analyze
WHERE scan_run_id = 'YOUR-SCAN-RUN-UUID'::uuid
  AND is_pmd = TRUE
ORDER BY risk_score DESC NULLS LAST, method_uid;
```

---

## Yalnız parser’da olan metodlar (PMD satırı yok)

Aynı koşuda sadece `run-analyze` yüklendiyse veya PMD o metodu üretmediyse:

```sql
SELECT p.method_uid, p.file_path, p.class_name, p.method_signature,
       p.risk_score AS parser_score, p.risk_level AS parser_level
FROM env.method_risk_analyze p
WHERE p.scan_run_id = 'YOUR-SCAN-RUN-UUID'::uuid
  AND p.is_parser = TRUE
  AND NOT EXISTS (
    SELECT 1
    FROM env.method_risk_analyze m
    WHERE m.scan_run_id = p.scan_run_id
      AND m.method_uid = p.method_uid
      AND m.is_pmd = TRUE
  )
ORDER BY p.risk_score DESC NULLS LAST;
```

---

## Yalnız PMD’de olan metodlar (parser satırı yok)

```sql
SELECT m.method_uid, m.file_path, m.class_name, m.method_signature,
       m.risk_score AS pmd_score, m.risk_level AS pmd_level
FROM env.method_risk_analyze m
WHERE m.scan_run_id = 'YOUR-SCAN-RUN-UUID'::uuid
  AND m.is_pmd = TRUE
  AND NOT EXISTS (
    SELECT 1
    FROM env.method_risk_analyze p
    WHERE p.scan_run_id = m.scan_run_id
      AND p.method_uid = m.method_uid
      AND p.is_parser = TRUE
  )
ORDER BY m.risk_score DESC NULLS LAST;
```

---

## Parser + PMD yan yana (ikisi de var)

```sql
SELECT p.method_uid,
       p.file_path,
       p.class_name,
       p.method_signature,
       p.risk_score   AS parser_score,
       p.risk_level   AS parser_level,
       m.risk_score   AS pmd_score,
       m.risk_level   AS pmd_level,
       ABS(p.risk_score - m.risk_score) AS score_delta
FROM env.method_risk_analyze p
JOIN env.method_risk_analyze m
  ON m.scan_run_id = p.scan_run_id
 AND m.method_uid = p.method_uid
WHERE p.scan_run_id = 'YOUR-SCAN-RUN-UUID'::uuid
  AND p.is_parser = TRUE
  AND m.is_pmd = TRUE
ORDER BY GREATEST(p.risk_score, m.risk_score) DESC NULLS LAST;
```

---

## Özet: pipeline kapsamı (tek koşu)

```sql
SELECT scan_run_id,
       COUNT(*) FILTER (WHERE is_parser) AS parser_rows,
       COUNT(*) FILTER (WHERE is_pmd)    AS pmd_rows,
       COUNT(DISTINCT method_uid)        AS distinct_methods,
       COUNT(DISTINCT method_uid) FILTER (
         WHERE is_parser
           AND EXISTS (
             SELECT 1 FROM env.method_risk_analyze x
             WHERE x.scan_run_id = env.method_risk_analyze.scan_run_id
               AND x.method_uid = env.method_risk_analyze.method_uid
               AND x.is_pmd
           )
       ) AS methods_with_both_pipelines
FROM env.method_risk_analyze
WHERE scan_run_id = 'YOUR-SCAN-RUN-UUID'::uuid
GROUP BY scan_run_id;
```

---

## Risk JSON (`risk_inputs_json`)

```sql
SELECT method_uid, is_parser, is_pmd, risk_score, risk_level,
       jsonb_pretty(risk_inputs_json) AS risk_inputs
FROM env.method_risk_analyze
WHERE scan_run_id = 'YOUR-SCAN-RUN-UUID'::uuid
  AND method_uid LIKE '%AuthFacade%login%'
LIMIT 5;
```

---

## Hash kolonları (duplicate debug)

```sql
-- Aynı gövde hash (birebir adayları)
SELECT scan_run_id, body_hash, COUNT(*) AS n,
       array_agg(method_uid ORDER BY method_uid) AS uids
FROM env.method_risk_analyze
WHERE scan_run_id = 'YOUR-SCAN-RUN-UUID'::uuid
  AND body_hash IS NOT NULL
GROUP BY scan_run_id, body_hash
HAVING COUNT(*) > 1;

-- Yapısal hash (yapısal klon adayları)
SELECT scan_run_id, structural_hash, COUNT(*) AS n
FROM env.method_risk_analyze
WHERE scan_run_id = 'YOUR-SCAN-RUN-UUID'::uuid
  AND structural_hash IS NOT NULL
GROUP BY scan_run_id, structural_hash
HAVING COUNT(*) > 1;
```

Asıl gruplar: `env.method_similarity` (`members_json` içinde `method_uid`).
