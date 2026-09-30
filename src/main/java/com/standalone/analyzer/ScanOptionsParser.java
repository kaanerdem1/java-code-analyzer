package com.standalone.analyzer;

import com.standalone.analyzer.ScanOptions.ReportDetail;

import java.util.ArrayList;
import java.util.List;

final class ScanOptionsParser {

    private ScanOptionsParser() {
    }

    static ScanOptions parse(List<String> includeGlobs, List<String> excludeGlobs, int workers,
                             int progressEvery, ReportDetail reportDetail,
                             boolean applyDefaultIgnoredDirectories) {
        ScanOptions defaults = ScanOptions.defaults();
        int resolvedWorkers = workers > 0 ? workers : defaults.workers();
        int resolvedProgress = progressEvery > 0 ? progressEvery : defaults.progressEvery();
        return new ScanOptions(resolvedWorkers, resolvedProgress, includeGlobs, excludeGlobs, reportDetail,
                applyDefaultIgnoredDirectories);
    }

    static ReportDetail parseDetail(String raw) {
        return switch (raw.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "full" -> ReportDetail.FULL;
            case "summary" -> ReportDetail.SUMMARY;
            default -> throw new IllegalArgumentException("--detail must be full or summary: " + raw);
        };
    }

    static List<String> splitCsv(String raw) {
        List<String> parts = new ArrayList<>();
        for (String piece : raw.split(",")) {
            String trimmed = piece.trim();
            if (!trimmed.isEmpty()) {
                parts.add(trimmed);
            }
        }
        return parts;
    }
}
