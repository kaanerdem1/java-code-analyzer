-- =============================================================================
-- Java Code Analyzer — katalog depolama (PostgreSQL)
-- Metod risk kayıtları (parser / PMD) ve benzerlik grupları.
-- Çalıştırma: psql -f scripts/catalog-schema.sql
-- Tablolar: env şeması (CREATE SCHEMA IF NOT EXISTS env).
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS env;

-- Eski tablo adları (bir kez, isteğe bağlı):
-- ALTER TABLE IF EXISTS env.method_scan RENAME TO method_risk_analyze;
-- ALTER TABLE IF EXISTS env.duplicate_group RENAME TO method_similarity;

-- -----------------------------------------------------------------------------
-- env.method_risk_analyze
-- Bir tarama koşusunda (scan_run_id) tek bir metodun tek pipeline çıktısı.
-- Aynı metod hem parser hem PMD ile taranmışsa iki satır: biri is_parser,
-- diğeri is_pmd (method_uid aynı kalır).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS env.method_risk_analyze (
    id                  BIGSERIAL PRIMARY KEY,
    scan_run_id         UUID NOT NULL,
    method_uid          TEXT NOT NULL,
    file_path           TEXT NOT NULL,
    class_name          TEXT NOT NULL,
    method_signature    TEXT NOT NULL,
    start_line          INT NOT NULL,
    end_line            INT NOT NULL,
    is_parser           BOOLEAN NOT NULL DEFAULT FALSE,
    is_pmd              BOOLEAN NOT NULL DEFAULT FALSE,
    ncss_loc            INT NOT NULL,
    risk_score          NUMERIC(5, 2),
    risk_level          TEXT,
    risk_inputs_json    JSONB NOT NULL DEFAULT '{}',
    body_hash           TEXT,
    structural_hash     TEXT,
    java_method_id      BIGINT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT method_risk_analyze_one_pipeline CHECK (
        (is_parser::int + is_pmd::int) = 1
    ),
    CONSTRAINT method_risk_analyze_line_range CHECK (end_line >= start_line),
    CONSTRAINT method_risk_analyze_ncss_positive CHECK (ncss_loc >= 0),
    UNIQUE (scan_run_id, method_uid, is_parser, is_pmd)
);

COMMENT ON TABLE env.method_risk_analyze IS
    'Metod bazlı tarama sonucu: risk skoru, seviye ve skoru oluşturan girdiler. '
    'Her satır tek kaynaktan gelir (JavaParser veya PMD-native); karşılaştırma için '
    'aynı scan_run_id ve method_uid ile iki satır birleştirilir.';

COMMENT ON COLUMN env.method_risk_analyze.id IS
    'Surrogate primary key.';

COMMENT ON COLUMN env.method_risk_analyze.scan_run_id IS
    'Tarama koşusunun kimliği (UUID). Aynı repo/commit/tarih veya uygulama tarafından '
    'üretilen run id; ayrı scan_run tablosu zorunlu değil.';

COMMENT ON COLUMN env.method_risk_analyze.method_uid IS
    'Metodun taramalar arası stabil anahtarı. Önerilen biçim: '
    'relativePath#fullyQualifiedClass#methodSignature#startLine '
    '(start_line imza çakışmalarını ayırır).';

COMMENT ON COLUMN env.method_risk_analyze.file_path IS
    'Proje köküne göre göreli .java dosya yolu (POSIX / ile).';

COMMENT ON COLUMN env.method_risk_analyze.class_name IS
    'Basit veya tam nitelikli sınıf adı (rapor ile aynı convention).';

COMMENT ON COLUMN env.method_risk_analyze.method_signature IS
    'Metod imzası metni (parametre tipleri dahil); duplicate ve UI etiketi için.';

COMMENT ON COLUMN env.method_risk_analyze.start_line IS
    'Metod gövdesinin başlangıç satır numarası (1 tabanlı, dosyada).';

COMMENT ON COLUMN env.method_risk_analyze.end_line IS
    'Metod gövdesinin bitiş satır numarası (1 tabanlı, dahil).';

COMMENT ON COLUMN env.method_risk_analyze.is_parser IS
    'TRUE ise satır JavaParser hattından (run-analyze / enterprise-java profili).';

COMMENT ON COLUMN env.method_risk_analyze.is_pmd IS
    'TRUE ise satır PMD-native hattından (run-pmd-analyze / enterprise-java-pmd). '
    'is_parser ile aynı satırda TRUE olamaz (CHECK).';

COMMENT ON COLUMN env.method_risk_analyze.ncss_loc IS
    'Metod kod satır sayısı (LOC): yorum ve boş satırlar hariç. Risk skoru, rapor '
    '"Satır" sütunu ve benzerlik paydası için tek kaynak; parser ve PMD aynı tanımı '
    'hedefler (NCSS / token tabanlı kod satırı).';

COMMENT ON COLUMN env.method_risk_analyze.risk_score IS
    'Birleşik metod risk skoru (0–100), aktif YAML risk profiline göre.';

COMMENT ON COLUMN env.method_risk_analyze.risk_level IS
    'Skor bandı: LOW, MEDIUM, HIGH, CRITICAL (profil eşikleri ile uyumlu).';

COMMENT ON COLUMN env.method_risk_analyze.risk_inputs_json IS
    'Risk seviyesini belirleyen tüm girdilerin yapılandırılmış kaydı. '
    'Örnek anahtarlar: profile, metrics (CC, cognitive, codeLines, …), '
    'riskBreakdown (alt skorlar), riskFactors (metin listesi), '
    'pmd.rulesetPath, pmd.violationsInMethod (LooseCoupling vb. kural satırları), '
    'pmd.ruleHits (kural adı → adet), parserOnly (yalnız parser sinyalleri). '
    'PMD ruleset''teki kurallar ayrı tabloya yazılmaz; rapor sütunları bu JSON''dan türetilir.';

COMMENT ON COLUMN env.method_risk_analyze.body_hash IS
    'Normalize edilmiş gövde metni hash''i; birebir duplicate (BIREBIR_METIN) eşleşmesi.';

COMMENT ON COLUMN env.method_risk_analyze.structural_hash IS
    'Yapısal imza hash''i; yapısal klon (YAPISAL_KLON) eşleşmesi.';

COMMENT ON COLUMN env.method_risk_analyze.java_method_id IS
    'Opsiyonel FK: env.java_method(id). Kaynak FQCN + metod adı ile envanter JAR kataloğuna bağlanır; '
    'servis kapsamı için service_definition join''i java_method üzerinden yapılır.';

COMMENT ON COLUMN env.method_risk_analyze.created_at IS
    'Satırın veritabanına yazıldığı zaman (UTC).';

CREATE INDEX IF NOT EXISTS idx_method_risk_analyze_run_uid
    ON env.method_risk_analyze (scan_run_id, method_uid);

CREATE INDEX IF NOT EXISTS idx_method_risk_analyze_run_file
    ON env.method_risk_analyze (scan_run_id, file_path);

CREATE INDEX IF NOT EXISTS idx_method_risk_analyze_run_level
    ON env.method_risk_analyze (scan_run_id, risk_level);

CREATE INDEX IF NOT EXISTS idx_method_risk_analyze_java_method
    ON env.method_risk_analyze (java_method_id)
    WHERE java_method_id IS NOT NULL;

COMMENT ON INDEX env.idx_method_risk_analyze_run_uid IS
    'Aynı koşuda parser vs PMD satırlarını method_uid ile eşleştirme.';

COMMENT ON INDEX env.idx_method_risk_analyze_run_file IS
    'Dosya bazlı katalog ve filtreleme.';

COMMENT ON INDEX env.idx_method_risk_analyze_run_level IS
    'Hotspot / seviye dağılımı sorguları.';

-- -----------------------------------------------------------------------------
-- env.method_similarity
-- Bir taramada 2+ metodun aynı/ benzer kod parçası paylaştığı grup.
-- similarity_percent < 50 olan gruplar uygulama katmanında yazılmamalı.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS env.method_similarity (
    id                   BIGSERIAL PRIMARY KEY,
    scan_run_id          UUID NOT NULL,
    group_key            TEXT NOT NULL,
    is_parser            BOOLEAN NOT NULL DEFAULT FALSE,
    is_pmd               BOOLEAN NOT NULL DEFAULT FALSE,
    similarity_type      TEXT NOT NULL,
    similarity_percent   SMALLINT NOT NULL,
    shared_ncss_lines    INT NOT NULL,
    members_json         JSONB NOT NULL,
    meta_json            JSONB NOT NULL DEFAULT '{}',
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT method_similarity_one_pipeline CHECK ((is_parser::int + is_pmd::int) = 1),
    CONSTRAINT method_similarity_percent_floor CHECK (similarity_percent >= 50),
    CONSTRAINT method_similarity_shared_lines CHECK (shared_ncss_lines >= 0)
);

COMMENT ON TABLE env.method_similarity IS
    'Tekrarlayan / benzer kod grupları. Grup başına bir satır; üye metodlar members_json '
    'içinde. Parser hash tabanlı ve PMD CPD sonuçları ayrı satırlarda (is_parser / is_pmd). '
    'Rapor ve kataloga yalnızca similarity_percent >= 50 olan gruplar alınır.';

COMMENT ON COLUMN env.method_similarity.id IS
    'Surrogate primary key.';

COMMENT ON COLUMN env.method_similarity.scan_run_id IS
    'method_risk_analyze.scan_run_id ile aynı tarama koşusu.';

COMMENT ON COLUMN env.method_similarity.group_key IS
    'Rapor/JSON groupId ile uyumlu okunabilir anahtar (ör. BENZER-3, BIREBIR-1).';

COMMENT ON COLUMN env.method_similarity.is_parser IS
    'TRUE ise grup JavaParser duplicate motorundan (bodyHash / structuralHash).';

COMMENT ON COLUMN env.method_similarity.is_pmd IS
    'TRUE ise grup PMD CPD (veya PMD-native duplicate pass) kaynaklı.';

COMMENT ON COLUMN env.method_similarity.similarity_type IS
    'Benzerlik sınıfı: BIREBIR_METIN, YAPISAL_KLON, BENZER_COK_YUKSEK, BENZER_YUKSEK, '
    'BENZER_ORTA, BENZER_KISMI (eski TOKEN_* / BLOK_* kodları raporda eşlenir).';

COMMENT ON COLUMN env.method_similarity.similarity_percent IS
    'Tek ölçekte benzerlik yüzdesi (1–100). Birebir %100, yapısal klon %95; CPD satır '
    'örtüşmesi method_risk_analyze.ncss_loc tanımıyla uyumlu payda kullanır. '
    'CHECK: en az %50; altı kayda ve rapora girmez.';

COMMENT ON COLUMN env.method_similarity.shared_ncss_lines IS
    'Ortak kod satırı sayısı (yorum/boş hariç, ncss_loc ile aynı LOC tanımı). '
    'Minimum eşik (ör. 40) uygulama tarafında gruplama öncesi uygulanır.';

COMMENT ON COLUMN env.method_similarity.members_json IS
    'Grup üyeleri dizisi. Her eleman: method_uid, file_path, class_name, method, '
    'start_line, end_line (method_risk_analyze.method_uid ile join edilebilir).';

COMMENT ON COLUMN env.method_similarity.meta_json IS
    'Motor detayları: engine (hash|cpd), cpdMatchUnits, formula, percentByPipeline '
    '(parser vs pmd karşılaştırması), matchedBodyHash vb.';

COMMENT ON COLUMN env.method_similarity.created_at IS
    'Satırın veritabanına yazıldığı zaman (UTC).';

CREATE INDEX IF NOT EXISTS idx_method_similarity_run
    ON env.method_similarity (scan_run_id);

CREATE INDEX IF NOT EXISTS idx_method_similarity_run_type
    ON env.method_similarity (scan_run_id, similarity_type);

COMMENT ON INDEX env.idx_method_similarity_run IS
    'Tarama koşusuna göre tüm benzerlik grupları.';

COMMENT ON INDEX env.idx_method_similarity_run_type IS
    'Türe göre özet (çok yüksek / yüksek / …) sorguları.';

-- Mevcut DB: kolon + FK (env.java_method zaten var — inventory_db)
ALTER TABLE env.method_risk_analyze
    ADD COLUMN IF NOT EXISTS java_method_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_method_risk_analyze_java_method'
    ) THEN
        ALTER TABLE env.method_risk_analyze
            ADD CONSTRAINT fk_method_risk_analyze_java_method
            FOREIGN KEY (java_method_id) REFERENCES env.java_method (id)
            ON DELETE SET NULL;
    END IF;
END $$;
