package com.standalone.analyzer;

/** File-level or run-level failure bucket for diagnostics. */
enum ScanErrorCategory {
    PARSE_SYNTAX,
    PARSE_LANGUAGE_LEVEL,
    IO,
    STACK_OVERFLOW,
    OUT_OF_MEMORY,
    ACCESS,
    UNEXPECTED;

    String labelTr() {
        return switch (this) {
            case PARSE_SYNTAX -> "sözdizimi (parse)";
            case PARSE_LANGUAGE_LEVEL -> "Java dil seviyesi";
            case IO -> "dosya I/O";
            case STACK_OVERFLOW -> "ifade derinliği (stack)";
            case OUT_OF_MEMORY -> "bellek";
            case ACCESS -> "erişim";
            case UNEXPECTED -> "beklenmeyen";
        };
    }
}
