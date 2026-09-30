package com.standalone.analyzer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Aggregated run outcome for console, Markdown, and JSON. */
record ScanDiagnostics(
        String completionStatus,
        String lastPhaseTr,
        String fatalPhaseTr,
        String fatalMessage,
        String lastFileAttempted,
        double parseFailureRatio,
        Map<String, Integer> errorsByCategory,
        List<String> recommendationsTr) {

    static ScanDiagnostics from(AnalysisReport report, ScanRunContext context, Integer exitCode) {
        AnalysisReport.Summary s = report.summary();
        Map<ScanErrorCategory, Integer> byCat = new EnumMap<>(ScanErrorCategory.class);
        for (AnalysisReport.FileError e : report.errors()) {
            ScanErrorCategory cat = e.category();
            byCat.merge(cat, 1, Integer::sum);
        }
        Map<String, Integer> named = new LinkedHashMap<>();
        byCat.entrySet().stream()
                .sorted(Map.Entry.<ScanErrorCategory, Integer>comparingByValue().reversed())
                .forEach(e -> named.put(e.getKey().name(), e.getValue()));

        double ratio = s.filesScanned() > 0 ? (double) s.filesFailed() / s.filesScanned() : 0.0;
        List<String> tips = buildRecommendations(named, ratio, report.parserLanguageLevel(), context, exitCode);

        String status;
        String fatalTr = null;
        String fatalMsg = null;
        if (context != null && context.fatal()) {
            status = "FAILED_FATAL";
            fatalTr = context.fatalPhase() != null ? context.fatalPhase().labelTr() : null;
            fatalMsg = context.fatalMessage();
        } else if (s.filesFailed() > 0) {
            status = "COMPLETED_WITH_FILE_ERRORS";
        } else {
            status = "COMPLETED_OK";
        }

        ScanPhase lastPhase = context != null ? context.phase() : ScanPhase.COMPLETE;
        String lastFile = context != null ? context.currentFile() : null;

        return new ScanDiagnostics(
                status,
                lastPhase.labelTr(),
                fatalTr,
                fatalMsg,
                lastFile,
                ratio,
                Map.copyOf(named),
                List.copyOf(tips));
    }

    private static List<String> buildRecommendations(
            Map<String, Integer> byCat,
            double ratio,
            String languageLevel,
            ScanRunContext context,
            Integer exitCode) {
        List<String> tips = new ArrayList<>();
        if (context != null && context.fatal()) {
            tips.add("Süreç fatal hata ile kesildi; aşama: "
                    + (context.fatalPhase() != null ? context.fatalPhase().labelTr() : "?")
                    + (context.currentFile() != null ? "; son işlenen dosya: `" + context.currentFile() + "`" : "")
                    + ".");
        }
        int syntax = byCat.getOrDefault(ScanErrorCategory.PARSE_SYNTAX.name(), 0);
        int lang = byCat.getOrDefault(ScanErrorCategory.PARSE_LANGUAGE_LEVEL.name(), 0);
        if (syntax + lang > 0) {
            tips.add("Parse hataları için `docs/scan-error-management.md` (unexpected token senaryoları).");
        }
        if (lang > 0) {
            tips.add("Karışık Java sürümleri: dosya başına dil fallback var; yine de "
                    + "LANGUAGE_LEVEL=" + languageLevel + " yetersiz kalabilir — JAVA_21 deneyin veya modülü ayrı tarayın.");
        }
        if (ratio > 0.1 && ratio <= 1.0) {
            tips.add(String.format(Locale.US,
                    "Parse hata oranı %.1f%% — kökü src/main/java'ya daraltın, EXCLUDE_GLOBS ile target/build/generated hariç tutun.",
                    ratio * 100));
        }
        if (exitCode != null && exitCode == ScanExitEvaluator.EXIT_PARSE_FAILURE_RATIO) {
            tips.add("CI exit 2: --max-failure-ratio aşıldı; rapor yine üretildi, eşiği gevşetin veya kaynakları düzeltin.");
        }
        if (exitCode != null && exitCode == ScanExitEvaluator.EXIT_RISK_GATE) {
            tips.add("CI exit 3: --fail-on-risk tetiklendi; parse başarısından bağımsız risk gate.");
        }
        if (tips.isEmpty()) {
            tips.add("Kritik dosya hatası yok; özet ve hotspot tablolarını kullanabilirsiniz.");
        }
        return tips;
    }
}
