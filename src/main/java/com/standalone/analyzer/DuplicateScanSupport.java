package com.standalone.analyzer;

import com.standalone.analyzer.AnalysisReport.DuplicateGroup;
import com.standalone.analyzer.AnalysisReport.DuplicateStatistics;
import com.standalone.analyzer.AnalysisReport.FileMetric;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Parser ve PMD taramalarında ortak duplicate (exact + PMD CPD) mantığı. */
final class DuplicateScanSupport {

    private static final DuplicateDetectionEngine ENGINE = new DuplicateDetectionEngine();

    private DuplicateScanSupport() {
    }

    static List<DuplicateGroup> detectGroups(Path absoluteRoot, ScanOptions scanOptions, List<FileMetric> files) {
        if (!scanOptions.detectDuplicates()) {
            return List.of();
        }
        List<DuplicateGroup> groups = new ArrayList<>();
        groups.addAll(ENGINE.detectExactDuplicates(files));
        if (shouldRunNearMiss(scanOptions, files.size())) {
            try {
                groups.addAll(ENGINE.detectNearMissDuplicates(
                        absoluteRoot, scanOptions.minDuplicateTokens(), files));
            } catch (RuntimeException e) {
                System.err.println("[DUPLICATE] Near-miss (PMD CPD) failed, exact groups kept: " + e.getMessage());
            }
        }
        return groups.stream()
                .filter(g -> g != null && g.duplicatedLines() >= DuplicateDetectionEngine.MIN_DUPLICATED_LINES
                        && g.members() != null && g.members().size() >= 2)
                .toList();
    }

    static DuplicateStatistics summarise(List<DuplicateGroup> groups) {
        int exactGroups = 0;
        int exactMethods = 0;
        int nearGroups = 0;
        int nearMethods = 0;
        for (DuplicateGroup group : groups) {
            if ("NEAR_MISS".equals(group.similarityType())) {
                nearGroups++;
                nearMethods += group.members().size();
            } else {
                exactGroups++;
                exactMethods += group.members().size();
            }
        }
        return new DuplicateStatistics(exactGroups, exactMethods, nearGroups, nearMethods);
    }

    static void logSummary(DuplicateStatistics stats) {
        if (stats.exactGroups() + stats.nearMissGroups() == 0) {
            return;
        }
        System.err.println("[DUPLICATE] " + stats.exactGroups() + " exact group(s) (" + stats.methodsInExactGroups()
                + " methods) | " + stats.nearMissGroups() + " near-miss group(s) ("
                + stats.methodsInNearMissGroups() + " methods)");
    }

    private static boolean shouldRunNearMiss(ScanOptions scanOptions, int parsedFileCount) {
        if (!scanOptions.detectNearMissDuplicates()) {
            return false;
        }
        int threshold = scanOptions.nearDuplicateAutoSkipMinFiles();
        return threshold <= 0 || parsedFileCount < threshold || scanOptions.forceNearMissDuplicates();
    }
}
