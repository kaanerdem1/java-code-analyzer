package com.standalone.analyzer;

import com.github.javaparser.ParserConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PmdNativeProjectScannerTest {

    @Test
    void producesMethodsAndRiskWithoutJavaParserPipeline() throws Exception {
        Path root = Path.of("mock-modules");
        if (!root.toFile().isDirectory()) {
            return;
        }
        RiskCalculator calculator = new RiskCalculator(
                RiskProfileLoader.load("enterprise-java-pmd", Path.of("config/risk-parameters-proposal.yaml")));
        PmdNativeProjectScanner scanner = new PmdNativeProjectScanner(
                StandardCharsets.UTF_8, ParserConfiguration.LanguageLevel.JAVA_17, ScanOptions.defaults(), 20,
                calculator, Path.of("config/pmd-ruleset-complexity.xml"), null, true);
        AnalysisReport report = scanner.scan(root);
        assertNotNull(report.pmdRulesetPhase());
        assertTrue(report.pmdRulesetPhase().violationCount() > 0);
        assertTrue(report.summary().methodCount() > 0);
        assertTrue(report.tool().contains("PMD-only"));
        boolean anyRisk = report.files().stream()
                .flatMap(f -> f.classes().stream())
                .flatMap(c -> c.methods().stream())
                .anyMatch(m -> m.riskScore() > 0);
        assertTrue(anyRisk, "expected non-zero risk from PMD-derived metrics");
        boolean anyBodyHash = report.files().stream()
                .flatMap(f -> f.classes().stream())
                .flatMap(c -> c.methods().stream())
                .anyMatch(m -> m.bodyHash() != null && !m.bodyHash().isEmpty());
        assertTrue(anyBodyHash, "duplicate pass should attach parser bodyHash");
    }
}
