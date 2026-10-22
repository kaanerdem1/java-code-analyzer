# DB bağlantısı

Repo kökü: `java-code-analyzer/`

---

## İlk kurulum (bir kez)

1. **Pull alınacak**  
   `git pull`

2. **JAR**  
   `mvn package`

3. **Katalog tabloları (PostgreSQL)**  
   ```bash
   psql -h HOST -U USER -d inventory_db -f scripts/catalog-schema.sql
   ```

4. **Envanter view + eşleme**  
   ```bash
   psql -h HOST -U USER -d inventory_db -f scripts/catalog-inventory-link.sql
   ```

5. **DB bağlantı dosyası** (repoda `.env` yok)  
   ```bash
   cp .env.catalog.example .env.catalog
   ```  
   Doldur: `CATALOG_JDBC_URL`, `PGUSER`, `PGPASSWORD`, `CATALOG_SCHEMA=env`

---

## Her tarama

6. **Koşu UUID** (parser + PMD aynı grupta) — **macOS/Linux: satırı olduğu gibi kopyala-yapıştır; elle UUID yazma.**  
   ```bash
   export CATALOG_SCAN_RUN=$(uuidgen | tr '[:upper:]' '[:lower:]')
   ```  
   **Sebep:** Parser ve PMD ayrı script; export yoksa her biri farklı `scan_run_id` üretir. Bu komut tek UUID üretip aynı terminale yazar; adım 7–8 aynı grupta DB’ye gider. Export etmezsen tarama yine çalışır, sadece iki hat birleşik karşılaştırılamaz.

7. **Parser** (isteğe bağlı; ikisini de çalıştırabilirsiniz)  
   `./scripts/run-analyze.sh /path/to/kaynak`  
   → `.env.catalog` içindeki DB’ye yazar: `env.method_risk_analyze` (`is_parser=true`), varsa `env.method_similarity`.

8. **PMD** (aynı terminal; adım 6 yaptıysanız aynı `scan_run_id`)  
   `./scripts/run-pmd-analyze.sh /path/to/kaynak`  
   → Aynı DB’ye yazar: `env.method_risk_analyze` (`is_pmd=true`), varsa benzerlik grupları.

Konsol: `[CATALOG] PostgreSQL: … satır yazıldı | scan_run_id=...` — bu UUID’yi aşağıdaki sorgularda kullanın.  
Raporlar ayrıca `analysis-output/` klasörüne gider (DB’den bağımsız).

---

## Kontrol sorguları (pgAdmin / psql)

**Son koşuların UUID’si:**

```sql
SELECT scan_run_id, MAX(created_at) AS yukleme, COUNT(*) AS satir
FROM env.method_risk_analyze
GROUP BY scan_run_id
ORDER BY yukleme DESC
LIMIT 5;
```

**Bu koşuda kaç satır, kaçı envantere bağlı** (`'...'` yerine konsoldaki UUID):

```sql
SELECT COUNT(*) AS satir,
       COUNT(java_method_id) AS envantere_bagli
FROM env.method_risk_analyze
WHERE scan_run_id = '...'::uuid;
```

**Risk + servis adı** (kurumsal kaynak + dolu envanter):

```sql
SELECT service_name, fqcn, method_uid, risk_score, risk_level, java_method_id
FROM env.v_method_risk_with_service
WHERE scan_run_id = '...'::uuid
  AND java_method_id IS NOT NULL
ORDER BY risk_score DESC NULLS LAST
LIMIT 20;
```

**Parser vs PMD yan yana** (adım 6 ile aynı koşu):

```sql
SELECT p.method_uid, p.class_name, p.method_signature,
       p.risk_score AS parser_score, m.risk_score AS pmd_score
FROM env.method_risk_analyze p
JOIN env.method_risk_analyze m
  ON m.scan_run_id = p.scan_run_id AND m.method_uid = p.method_uid
WHERE p.scan_run_id = '...'::uuid
  AND p.is_parser = TRUE AND m.is_pmd = TRUE
ORDER BY GREATEST(p.risk_score, m.risk_score) DESC NULLS LAST
LIMIT 20;
```

**Sadece parser satırları:**

```sql
SELECT method_uid, file_path, risk_score, risk_level
FROM env.method_risk_analyze
WHERE scan_run_id = '...'::uuid AND is_parser = TRUE
ORDER BY risk_score DESC NULLS LAST
LIMIT 50;
```

**Sadece PMD satırları:**

```sql
SELECT method_uid, file_path, risk_score, risk_level
FROM env.method_risk_analyze
WHERE scan_run_id = '...'::uuid AND is_pmd = TRUE
ORDER BY risk_score DESC NULLS LAST
LIMIT 50;
```

---

## Not

- `CATALOG_SCHEMA=env` → PostgreSQL **şema adı**, dosya değil.
- DB yazmasın: `.env.catalog` yok veya `--no-catalog-write`.
- Detay sorgu: [catalog-queries-parser-pmd-tr.md](./catalog-queries-parser-pmd-tr.md)
