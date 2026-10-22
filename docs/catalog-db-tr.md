# PostgreSQL katalog (`env` şeması)

Tarama bittiğinde metod risk kayıtları ve benzerlik grupları otomatik yazılır — **`CATALOG_JDBC_URL` tanımlıysa**.

## Kurulum (bir kez)

```bash
psql -h HOST -U USER -d DBNAME -f scripts/catalog-schema.sql
```

## Bağlantı (Seçenek A — JDBC)

```bash
cp .env.catalog.example .env.catalog
# .env.catalog içini düzenleyin
```

Örnek `.env.catalog`:

```bash
CATALOG_JDBC_URL=jdbc:postgresql://localhost:5432/postgres
PGUSER=postgres
PGPASSWORD=secret
CATALOG_SCHEMA=env
```

Tarama:

```bash
./scripts/run-analyze.sh mock-modules      # is_parser=true satırları
./scripts/run-pmd-analyze.sh mock-modules  # is_pmd=true satırları
```

Konsolda: `[CATALOG] PostgreSQL: N satır yazıldı | scan_run_id=...`

### Parser + PMD aynı koşu

İlk taramadan sonra logdaki `scan_run_id` veya:

```bash
export CATALOG_SCAN_RUN=$(uuidgen | tr '[:upper:]' '[:lower:]')
./scripts/run-analyze.sh /path/to/proje
./scripts/run-pmd-analyze.sh /path/to/proje
```

### Başka makine / başka DB

Aynı `.env.catalog` (veya ortam değişkenleri) o makinede; DB sunucusunda `pg_hba` + gerekirse `sslmode=require`:

```bash
CATALOG_JDBC_URL=jdbc:postgresql://10.0.0.5:5432/analyzer_catalog?sslmode=require
PGUSER=catalog_writer
PGPASSWORD=...
```

## Kapatma

- `CATALOG_JDBC_URL` unset → DB yazımı yok  
- veya `--no-catalog-write`

## Sorgular

- Parser / PMD / `method_uid`: [catalog-queries-parser-pmd-tr.md](./catalog-queries-parser-pmd-tr.md)
- Envanter (`java_method`, servisler) bağlantısı: [catalog-env-inventory-link-tr.md](./catalog-env-inventory-link-tr.md)

```bash
psql -d inventory_db -f scripts/catalog-queries-examples.sql
psql -d inventory_db -f scripts/catalog-inventory-link.sql
```

Tek metod risk JSON:

```sql
SELECT jsonb_pretty(risk_inputs_json)
FROM env.method_risk_analyze
WHERE scan_run_id = '...'::uuid AND method_uid LIKE '%Foo%bar%';
```

## Manuel yükleme (JSON zaten varsa)

```bash
./scripts/run-catalog-load-env.sh mock-modules
```
