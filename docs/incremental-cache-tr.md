# Incremental cache (Türkçe)

`analyzer-state.json` ile artımlı tarama. Script varsayılan: `--state=analysis-output/analyzer-state.json`.

## Katmanlar (sırayla)

1. **Modül toplu skip** — En yakın `pom.xml` / `build.gradle` kökü = modül. Modüldeki tüm dosyaların `yol|byteHash` listesinden parmak izi; önceki tarama ile aynıysa modüldeki **tüm dosyalar parse edilmeden** state’ten yüklenir.
2. **Dosya byte hash (Stage 1)** — Tek dosyanın ham SHA-256 özeti aynıysa parse yok.
3. **Parse + AST dosya özeti (Stage 1b)** — Byte değiştiyse JavaParser çalışır. `semanticFileHash` (yorum/boşluk temizlenmiş AST metni) önceki ile aynıysa **risk analizi atlanır**, metrikler state’ten alınır; sadece byte hash güncellenir.
4. **Metod hash (Stage 2)** — Kod gerçekten değiştiyse dosya analiz edilir; değişmeyen metodlar `methodHash` ile reuse.

## Ne “kozmetik” sayılır?

Yorum, boşluk, format — **kod anlamı aynı**. Parse olur (AST için), skor motoru **çalışmaz**.

## Otomatik cache invalidation

State içinde `cacheIdentity` (schema + risk profili + model sürümü). Uyuşmazsa eski state okunmaz.

## Ortam değişkenleri (script)

| Değişken | Anlam |
|----------|--------|
| `NO_STATE=1` | Cache kapalı |
| `FRESH=1` | Bu koşuda state okunmasın |

## Rapor alanları

- **Markdown:** `## Incremental cache`, `## Son taramaya göre değişiklikler` (kozmetik sütunu)
- **JSON:** `cacheStatistics`, `incrementalChanges.filesCosmeticOnly`

## State dosyası

`analysis-output/analyzer-state.json` — dosya `fileHash`, `semanticFileHash`, `cachedFileMetric`, modül `fingerprint`.
