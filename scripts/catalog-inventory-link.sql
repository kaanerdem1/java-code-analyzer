-- Envanter (java_method / service_definition) ↔ method_risk_analyze bağlantısı
-- Önkoşul: scripts/catalog-schema.sql (java_method_id kolonu)
-- psql -d inventory_db -f scripts/catalog-inventory-link.sql

-- Mevcut satırlarda java_method_id doldur (FQCN + metod adı + overload kuralı — loader ile aynı mantık)
-- Not: PostgreSQL tarafında basit eşleme; belirsiz overload'da NULL kalır.

CREATE OR REPLACE FUNCTION env.catalog_fqcn_from_path(file_path text, class_name text)
RETURNS text
LANGUAGE sql
IMMUTABLE
AS $$
  SELECT CASE
    WHEN class_name LIKE '%.%' THEN trim(class_name)
    WHEN file_path ~ 'src/(main|test|integrationTest)/java/.+\.java$' THEN
      replace(
        regexp_replace(
          regexp_replace(file_path, '^.*src/(main|test|integrationTest)/java/', ''),
          '\.java$', ''
        ),
        '/', '.'
      )
    WHEN file_path ~ '/[^/]+\.java$' THEN
      regexp_replace(file_path, '.*/([^/]+)\.java$', '\1')
    ELSE trim(class_name)
  END
$$;

CREATE OR REPLACE FUNCTION env.catalog_method_name_from_sig(method_signature text)
RETURNS text
LANGUAGE sql
IMMUTABLE
AS $$
  SELECT trim(split_part(coalesce(method_signature, ''), '(', 1))
$$;

-- Tek aday varsa bağla
UPDATE env.method_risk_analyze m
SET java_method_id = sub.jm_id
FROM (
  SELECT m2.id AS risk_id,
         (
           SELECT jm.id
           FROM env.java_method jm
           JOIN env.java_class jc ON jc.id = jm.class_id
           WHERE jc.fqcn = env.catalog_fqcn_from_path(m2.file_path, m2.class_name)
             AND jm.name = env.catalog_method_name_from_sig(m2.method_signature)
           ORDER BY jm.id
           LIMIT 1
         ) AS jm_id,
         (
           SELECT COUNT(*)
           FROM env.java_method jm
           JOIN env.java_class jc ON jc.id = jm.class_id
           WHERE jc.fqcn = env.catalog_fqcn_from_path(m2.file_path, m2.class_name)
             AND jm.name = env.catalog_method_name_from_sig(m2.method_signature)
         ) AS cand_count
  FROM env.method_risk_analyze m2
  WHERE m2.java_method_id IS NULL
) sub
WHERE m.id = sub.risk_id
  AND sub.cand_count = 1
  AND sub.jm_id IS NOT NULL;

CREATE OR REPLACE VIEW env.v_method_risk_with_service AS
SELECT
  m.id,
  m.scan_run_id,
  m.method_uid,
  m.file_path,
  m.class_name,
  m.method_signature,
  m.start_line,
  m.end_line,
  m.is_parser,
  m.is_pmd,
  m.ncss_loc,
  m.risk_score,
  m.risk_level,
  m.risk_inputs_json,
  m.body_hash,
  m.structural_hash,
  m.java_method_id,
  jc.fqcn,
  jm.name AS inventory_method_name,
  jm.descriptor AS inventory_descriptor,
  sd.id AS service_definition_id,
  sd.service_name,
  sd.package_name AS service_package,
  sd.class_name AS service_class_name,
  sd.method_name AS service_method_name
FROM env.method_risk_analyze m
LEFT JOIN env.java_method jm ON jm.id = m.java_method_id
LEFT JOIN env.java_class jc ON jc.id = jm.class_id
LEFT JOIN env.service_definition sd ON sd.id = jm.service_definition_id;

COMMENT ON VIEW env.v_method_risk_with_service IS
  'Analyzer risk satırı + envanter java_method + opsiyonel service_definition.';
