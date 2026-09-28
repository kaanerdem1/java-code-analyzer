package com.standalone.analyzer;

import com.github.javaparser.ParserConfiguration;

/** CLI value for {@link ParserConfiguration.LanguageLevel}. */
final class LanguageLevelOption {

    static final ParserConfiguration.LanguageLevel DEFAULT = ParserConfiguration.LanguageLevel.JAVA_17;

    private LanguageLevelOption() {
    }

    static ParserConfiguration.LanguageLevel parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT;
        }
        String normalized = raw.trim().toUpperCase();
        if (normalized.matches("JAVA_\\d+")) {
            return ParserConfiguration.LanguageLevel.valueOf(normalized);
        }
        if (normalized.matches("\\d+")) {
            return ParserConfiguration.LanguageLevel.valueOf("JAVA_" + normalized);
        }
        throw new IllegalArgumentException(
                "Unknown --language-level: " + raw + " (ör. JAVA_17, 17, JAVA_6)");
    }
}
