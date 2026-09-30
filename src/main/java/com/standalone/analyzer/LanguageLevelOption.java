package com.standalone.analyzer;

import com.github.javaparser.ParserConfiguration;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/** CLI value for {@link ParserConfiguration.LanguageLevel}. */
final class LanguageLevelOption {

    static final ParserConfiguration.LanguageLevel DEFAULT = ParserConfiguration.LanguageLevel.JAVA_17;

    private LanguageLevelOption() {
    }

    static ParserConfiguration.LanguageLevel parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEFAULT;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        try {
            if (normalized.matches("JAVA_[\\d_]+")) {
                return ParserConfiguration.LanguageLevel.valueOf(normalized);
            }
            if (normalized.matches("\\d+")) {
                return ParserConfiguration.LanguageLevel.valueOf("JAVA_" + normalized);
            }
        } catch (IllegalArgumentException ignored) {
            // fall through with supported list
        }
        throw new IllegalArgumentException(
                "Unknown --language-level: " + raw + ". Supported: " + supportedLevelsHint());
    }

    static String supportedLevelsHint() {
        return Arrays.stream(ParserConfiguration.LanguageLevel.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}
