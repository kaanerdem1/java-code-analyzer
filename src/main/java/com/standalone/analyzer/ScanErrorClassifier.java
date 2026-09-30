package com.standalone.analyzer;

import java.util.Locale;

final class ScanErrorClassifier {

    private ScanErrorClassifier() {
    }

    static ScanErrorCategory classify(String message) {
        if (message == null || message.isBlank()) {
            return ScanErrorCategory.UNEXPECTED;
        }
        String lower = message.toLowerCase(Locale.ROOT);
        if (lower.contains("stack overflow") || lower.contains("nesting too deep")) {
            return ScanErrorCategory.STACK_OVERFLOW;
        }
        if (lower.contains("out of memory")) {
            return ScanErrorCategory.OUT_OF_MEMORY;
        }
        if (lower.contains("cannot access") || lower.contains("not readable")) {
            return ScanErrorCategory.ACCESS;
        }
        if (lower.contains("i/o error") || lower.contains("io error")) {
            return ScanErrorCategory.IO;
        }
        if (lower.contains("tried language levels")) {
            return ScanErrorCategory.PARSE_LANGUAGE_LEVEL;
        }
        if (lower.contains("parse error") || lower.contains("unexpected token")
                || lower.contains("problem") || lower.contains("syntax")) {
            return ScanErrorCategory.PARSE_SYNTAX;
        }
        return ScanErrorCategory.UNEXPECTED;
    }
}
