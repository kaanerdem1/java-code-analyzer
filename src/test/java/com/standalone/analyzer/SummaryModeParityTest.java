package com.standalone.analyzer;

import com.github.javaparser.ParserConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SummaryModeParityTest {

    @Test
    void summaryMatchesFullAggregatesOnMockModules() throws Exception {
        Path root = Path.of("mock-modules").toAbsolutePath().normalize();
        ParserConfiguration.LanguageLevel level = ParserConfiguration.LanguageLevel.JAVA_17;

        ProjectAnalyzer fullAnalyzer = new ProjectAnalyzer(StandardCharsets.UTF_8, 20, level,
                new ScanOptions(1, 500, java.util.List.of(), java.util.List.of(), ScanOptions.ReportDetail.FULL, true,
                        null, false, true, 50, true, 0, false, true));
        ProjectAnalyzer summaryAnalyzer = new ProjectAnalyzer(StandardCharsets.UTF_8, 20, level,
                new ScanOptions(1, 500, java.util.List.of(), java.util.List.of(), ScanOptions.ReportDetail.SUMMARY, true,
                        null, false, true, 50, true, 0, false, true));

        AnalysisReport full = fullAnalyzer.analyze(root);
        AnalysisReport summary = summaryAnalyzer.analyze(root);

        assertEquals(full.summary().methodCount(), summary.summary().methodCount());
        assertEquals(full.summary().classCount(), summary.summary().classCount());
        assertEquals(full.summary().totalCodeLines(), summary.summary().totalCodeLines());
        assertEquals(full.summary().maxCyclomaticComplexity(), summary.summary().maxCyclomaticComplexity());
        assertEquals(full.summary().projectRiskScore(), summary.summary().projectRiskScore(), 0.001);
        assertEquals(full.summary().methodRiskDistribution(), summary.summary().methodRiskDistribution());
        assertEquals(full.summary().methodRiskScoreP95(), summary.summary().methodRiskScoreP95(), 0.001);
        assertEquals(full.summary().highPlusCriticalLocRatio(), summary.summary().highPlusCriticalLocRatio(), 0.001);
    }

    @Test
    void pmdScanSummaryMatchesFullAggregatesOnMockModules() throws Exception {
        Path root = Path.of("mock-modules").toAbsolutePath().normalize();
        ParserConfiguration.LanguageLevel level = ParserConfiguration.LanguageLevel.JAVA_17;
        RiskCalculator calculator = new RiskCalculator(
                RiskProfileLoader.load("enterprise-java", Path.of("config/risk-parameters-proposal.yaml")));

        ScanOptions fullOpts = new ScanOptions(1, 500, java.util.List.of(), java.util.List.of(),
                ScanOptions.ReportDetail.FULL, true, null, false, true, 50, true, 0, false, true);
        ScanOptions summaryOpts = new ScanOptions(1, 500, java.util.List.of(), java.util.List.of(),
                ScanOptions.ReportDetail.SUMMARY, true, null, false, true, 50, true, 0, false, true);

        PmdProjectScanner fullScanner = new PmdProjectScanner(StandardCharsets.UTF_8, level, fullOpts, 20, calculator);
        PmdProjectScanner summaryScanner =
                new PmdProjectScanner(StandardCharsets.UTF_8, level, summaryOpts, 20, calculator);

        AnalysisReport full = fullScanner.scan(root);
        AnalysisReport summary = summaryScanner.scan(root);

        assertEquals(full.summary().methodCount(), summary.summary().methodCount());
        assertEquals(full.summary().projectRiskScore(), summary.summary().projectRiskScore(), 0.001);
        assertEquals(full.summary().methodRiskScoreP95(), summary.summary().methodRiskScoreP95(), 0.001);
    }
}
