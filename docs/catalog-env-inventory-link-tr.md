# `env` envanter şeması ↔ analyzer katalog (2 tablo)

`inventory_db` içindeki **`env`** şeması (~46 tablo) bankacılık **servis envanteri**, JAR bytecode katalogu, süreç/ekran haritası ve analyzer çıktılarını bir arada tutar.  
Analyzer’ın yazdığı iki tablo:

| Tablo | Rol |
|--------|-----|
| `env.method_risk_analyze` | Metod riski (parser **veya** PMD), `risk_inputs_json`, hash’ler |
| `env.method_similarity` | Benzerlik grupları (`members_json`, `meta_json`) |

Parser/PMD sorguları: [catalog-queries-parser-pmd-tr.md](./catalog-queries-parser-pmd-tr.md).

---

## Envanterde metodlar nerede?

### `env.java_class` + `env.java_method` (asıl metod envanteri)

Bytecode / JAR taramasından gelir (~21k sınıf, ~293k metod).

| Kolon | Anlam |
|--------|--------|
| `java_class.id` | Sınıf PK |
| `java_class.fqcn` | Tam nitelikli sınıf adı |
| `java_class.artifact_id` → `env.artifact` | Hangi JAR/modül |
| `java_class.source_file` | **Şu an dolu değil** (0 kayıt) |
| `java_method.id` | Metod PK — **call graph ve servis bağlantısının merkezi** |
| `java_method.class_id` | → `java_class` |
| `java_method.name` | Metod adı (`get`, `save`, …) |
| `java_method.descriptor` | JVM imzası `(L...;)L...;` |
| `java_method.line_start` / `line_end` | **Şu an dolu değil** |
| `java_method.service_definition_id` | → `env.service_definition` (**~17k** metod — “gerçek servis” adayı) |

Unique: `(class_id, name, descriptor)` → aynı sınıfta overload ayırımı descriptor ile.

### `env.service_definition` (iş servisleri)

Katalog UI / test kapsamı için anlamlı servis satırları: `service_name`, `package_name`, `class_name`, `method_name`.  
`java_method.service_definition_id` dolu olanlar **envanter metodu ↔ servis tanımı** bağlı.

### `env.scan_run` (bigint) ≠ analyzer `scan_run_id` (UUID)

| | Envanter `env.scan_run` | Analyzer `method_risk_analyze.scan_run_id` |
|--|-------------------------|---------------------------------------------|
| Tip | `bigint` (sequence) | `UUID` |
| Anlam | JAR bytecode tarama koşusu (`jar_scope`, `status`, …) | Tek `run-analyze` / `run-pmd-analyze` koşusu |
| Birleştirme | **Doğrudan FK yok**; ileride köprü tablo veya `analyzed_path` / `artifact.path` ile eşleme |

### Diğer tablolar (kısa)

- **`call_edge`**: `caller_id` / `callee_id` → `java_method.id` (statik çağrı grafiği).
- **`process_*`, `screen_*`, `service_*`**: Süreç/ekran ↔ `service_definition`.
- **`exception_scan_*`**: Kaynak taraması koşuları; analyzer UUID koşusu ile aynı değil.
- Eski adlar: `method_scan`, `duplicate_group` — analyzer artık `method_risk_analyze` / `method_similarity` kullanır.

---

## Analyzer ile envanter nasıl eşleşir?

### `method_uid` (analyzer)

```text
file_path#class_name#method_signature#start_line
```

- `class_name`: çoğu raporda **kısa sınıf adı** (`AuthFacade`), FQCN değil.
- `method_signature`: kaynak tarzı (`login(String, String)`), JVM `descriptor` değil.
- `start_line`: kaynak satırı — envanterde karşılığı yok (line kolonları boş).

### Eşleme anahtarı (pratik)

1. **FQCN türet**: `file_path` içinde `src/main/java/.../Foo.java` → `....Foo`  
   (`class_name` zaten noktalı ise doğrudan FQCN kabul edilir).
2. **Metod adı**: `method_signature` içinde `(` öncesi.
3. **`java_method`**: `java_class.fqcn = fqcn` AND `java_method.name = metod_adı`.
4. **Overload**: birden fazla satır varsa imzadaki parametre sayısı ile daralt (JVM descriptor veya `param_count`).

Mock-modules taraması envanterde **karşılık bulmaz** (farklı kod tabanı). Kurumsal **kaynak ağacı** aynı FQCN’leri JAR envanterinde üretiyorsa bağlantı dolar.

### `method_similarity`

Üyeler `members_json` içinde `method_uid` taşır. Envantere:

- `members_json` → `method_uid` parse → `method_risk_analyze` → `java_method_id`, veya
- aynı FQCN + metod adı kuralı ile doğrudan `java_method`.

---

## Uygulanan bağlantı: `java_method_id`

`method_risk_analyze` tablosuna opsiyonel FK:

```text
java_method_id → env.java_method(id)
```

- Yükleme sırasında (`CatalogEnvLoader`) envanter tabloları varsa otomatik çözülür.
- Mevcut satırlar için: `scripts/catalog-inventory-link.sql` (backfill + view).

### Servis + risk bir arada (view)

```sql
SELECT *
FROM env.v_method_risk_with_service
WHERE scan_run_id = 'YOUR-SCAN-RUN-UUID'::uuid
ORDER BY risk_score DESC NULLS LAST
LIMIT 20;
```

(View tanımı `catalog-inventory-link.sql` içinde.)

### Elle join (servis adı bilinen metod)

```sql
SELECT m.method_uid, m.risk_score, m.risk_level,
       sd.service_name, sd.package_name, sd.class_name, sd.method_name,
       jm.id AS java_method_id, jc.fqcn
FROM env.method_risk_analyze m
JOIN env.java_method jm ON jm.id = m.java_method_id
JOIN env.java_class jc ON jc.id = jm.class_id
LEFT JOIN env.service_definition sd ON sd.id = jm.service_definition_id
WHERE m.scan_run_id = 'YOUR-SCAN-RUN-UUID'::uuid
  AND sd.service_name IS NOT NULL;
```

### Call graph’e geçiş (etki / lineage)

Riskli bir servis metodundan çağrılan metodlar:

```sql
SELECT ce.*, callee.name AS callee_name, jc2.fqcn AS callee_fqcn
FROM env.method_risk_analyze m
JOIN env.call_edge ce ON ce.caller_id = m.java_method_id
JOIN env.java_method callee ON callee.id = ce.callee_id
JOIN env.java_class jc2 ON jc2.id = callee.class_id
WHERE m.scan_run_id = 'YOUR-SCAN-RUN-UUID'::uuid
  AND m.java_method_id IS NOT NULL
LIMIT 100;
```

(`call_edge` envanter taramasından; analyzer koşusu ile aynı commit garantisi yok — dokümante edilmeli.)

---

## Kurulum sırası (`inventory_db`)

```bash
# Analyzer tabloları (java_method_id kolonu dahil)
psql -d inventory_db -f scripts/catalog-schema.sql

# View + mevcut satırlarda java_method_id backfill
psql -d inventory_db -f scripts/catalog-inventory-link.sql
```

Sonra normal tarama (`CATALOG_JDBC_URL=.../inventory_db`).

---

## Sınırlar / sonraki adımlar

| Konu | Durum |
|------|--------|
| `source_file` / satır numarası envanterde | Boş → satır bazlı join yok |
| `scan_run` (bigint) ↔ UUID | Köprü yok; isteğe bağlı `analyzer_scan_run` tablosu |
| Mock-modules | `java_method_id` genelde NULL |
| Kurumsal kaynak + aynı FQCN JAR | `java_method_id` dolar; servisli metodlarda `service_definition` join |
| `method_similarity.members_json` | İleride `java_method_id` zenginleştirmesi |
