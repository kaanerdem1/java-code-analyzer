package com.standalone.analyzer;

/** High-level stages of a scan run (for fatal error reporting). */
enum ScanPhase {
    START,
    DISCOVERY,
    PARSING,
    REPORT_ASSEMBLY,
    WRITE_JSON,
    WRITE_MARKDOWN,
    EXIT_EVAL,
    COMPLETE;

    String labelTr() {
        return switch (this) {
            case START -> "başlangıç";
            case DISCOVERY -> "dosya keşfi";
            case PARSING -> "parse ve metrik";
            case REPORT_ASSEMBLY -> "rapor özeti";
            case WRITE_JSON -> "JSON yazımı";
            case WRITE_MARKDOWN -> "Markdown yazımı";
            case EXIT_EVAL -> "çıkış kodu değerlendirme";
            case COMPLETE -> "tamamlandı";
        };
    }
}
