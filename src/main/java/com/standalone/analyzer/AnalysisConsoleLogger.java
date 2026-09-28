package com.standalone.analyzer;

import com.standalone.analyzer.AnalysisReport.ClassMetric;
import com.standalone.analyzer.AnalysisReport.FileMetric;
import com.standalone.analyzer.AnalysisReport.RiskHotspot;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;

/** Human-readable stderr logs for comparing with external tools (e.g. PMD). */
final class AnalysisConsoleLogger {

    private static final String TAG = "[STANDALONE]";

    private AnalysisConsoleLogger() {
    }

    static void logRunHeader(PathLabel path, boolean verbose) {
        PrintStream err = System.err;
        err.println(TAG + " ========================================");
        err.println(TAG + " Standalone Java complexity & risk scan");
        err.println(TAG + " Target: " + path.value());
        err.println(TAG + " Risk model: v1 (0.0–1.0 composite score)");
        err.println(TAG + " Weights: CC=40% LOC=20% Nesting=25% Params=15%");
        err.println(TAG + " Formula: final = min(bandCap, max(subScores) + 0.15 * weightedBlend)");
        err.println(TAG + " Thresholds — CC: 10/15/30 | LOC: 50/100/200 | Nest: 3/4/8 | Params: 5/7/12");
        err.println(TAG + " Verbose method listing: " + (verbose ? "ON" : "OFF (use --verbose)"));
        err.println(TAG + " JSON schema: riskBreakdown per method when score is computed");
        err.println(TAG + " ========================================");
    }

    static void logSummary(AnalysisReport report, String jsonOutput) {
        PrintStream err = System.err;
        AnalysisReport.Summary s = report.summary();
        err.println(TAG + " --- Summary ---");
        err.println(TAG + " Files: scanned=" + s.filesScanned() + " parsed=" + s.filesParsed()
                + " failed=" + s.filesFailed());
        err.println(TAG + " Methods=" + s.methodCount() + " classes=" + s.classCount()
                + " totalCodeLines=" + s.totalCodeLines());
        err.println(TAG + " CC: avg=" + s.averageCyclomaticComplexity() + " max=" + s.maxCyclomaticComplexity());
        err.println(TAG + " God methods=" + s.godMethodCount());
        err.println(TAG + " Project risk: score=" + s.projectRiskScore() + " level=" + s.projectRiskLevel());
        err.println(TAG + " Distribution: LOW=" + s.methodRiskDistribution().get(RiskLevel.LOW)
                + " MEDIUM=" + s.methodRiskDistribution().get(RiskLevel.MEDIUM)
                + " HIGH=" + s.methodRiskDistribution().get(RiskLevel.HIGH)
                + " CRITICAL=" + s.methodRiskDistribution().get(RiskLevel.CRITICAL));
        if (jsonOutput != null) {
            err.println(TAG + " JSON report: " + jsonOutput);
        }
    }

    static void logHotspots(List<RiskHotspot> hotspots, int limit) {
        PrintStream err = System.err;
        err.println(TAG + " --- Top risk hotspots (max " + limit + ") ---");
        int n = Math.min(limit, hotspots.size());
        for (int i = 0; i < n; i++) {
            RiskHotspot h = hotspots.get(i);
            err.printf(Locale.US, TAG + " #%d score=%.3f %s %s.%s line=%d%n",
                    i + 1, h.riskScore(), h.riskLevel(), h.className(), h.method(), h.startLine());
            err.printf(TAG + "     raw: CC=%d LOC=%d nest=%d params=%d%n",
                    h.cyclomaticComplexity(), h.codeLines(), h.maxNestingDepth(), h.parameterCount());
            if (!h.riskFactors().isEmpty()) {
                err.println(TAG + "     factors: " + String.join("; ", h.riskFactors()));
            }
        }
        if (hotspots.isEmpty()) {
            err.println(TAG + " (none)");
        }
    }

    static void logVerboseMethods(List<FileMetric> files) {
        PrintStream err = System.err;
        err.println(TAG + " --- All methods (verbose) ---");
        for (FileMetric file : files) {
            for (ClassMetric type : file.classes()) {
                for (MethodMetric m : type.methods()) {
                    RiskBreakdown b = m.riskBreakdown();
                    err.printf(Locale.US, TAG + " %s | %s.%s | score=%.3f %s | CC=%d LOC=%d nest=%d params=%d%n",
                            file.path(), type.name(), m.signature(), m.riskScore(), m.riskLevel(),
                            m.cyclomaticComplexity(), m.codeLines(), m.maxNestingDepth(), m.parameterCount());
                    if (b != null) {
                        err.printf(Locale.US, TAG + "     sub: cc=%.3f loc=%.3f nest=%.3f par=%.3f dom=%.3f blend=%.3f bonus=%.3f final=%.3f%n",
                                b.ccSubScore(), b.locSubScore(), b.nestingSubScore(), b.paramsSubScore(),
                                b.dominantSubScore(), b.weightedBlend(), b.compoundBonus(), b.finalScore());
                    }
                    if (m.godMethod()) {
                        err.println(TAG + "     ** godMethod heuristic **");
                    }
                }
            }
        }
    }

    static void logParseErrors(List<AnalysisReport.FileError> errors) {
        if (errors.isEmpty()) {
            return;
        }
        PrintStream err = System.err;
        err.println(TAG + " --- Parse / analysis errors ---");
        for (AnalysisReport.FileError e : errors) {
            err.println(TAG + " " + e.file() + ": " + e.message());
        }
    }

    record PathLabel(String value) {
    }
}
