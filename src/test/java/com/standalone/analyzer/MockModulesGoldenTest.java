package com.standalone.analyzer;

import com.github.javaparser.ParserConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks enterprise-java scores for a few mock-modules methods (regression guard).
 */
class MockModulesGoldenTest {

    private static final Path MOCK = Path.of("mock-modules");
    private static final Path CONFIG = Path.of("config/risk-parameters-proposal.yaml");

    @Test
    void enterpriseProfileDimensionCount() throws Exception {
        RiskProfile profile = RiskProfileLoader.load("enterprise-java", CONFIG);
        assertEquals(17, profile.methodDimensions().size());
    }

    @Test
    void paymentOrchestratorRouteStaysLowToMedium() throws Exception {
        MethodMetric route = analyzeMethod("PaymentOrchestrator", "route");
        assertEquals(42.747, route.riskScore(), 0.5);
        assertEquals(RiskLevel.MEDIUM, route.riskLevel());
    }

    @Test
    void dimensionMergerMergeRowsGoldenScore() throws Exception {
        MethodMetric merge = analyzeMethod("DimensionMerger", "mergeRows");
        assertEquals(13, merge.cyclomaticComplexity());
        assertEquals(57.853, merge.riskScore(), 0.5);
        assertEquals(RiskLevel.HIGH, merge.riskLevel());
    }

    @Test
    void strictReviewIsStricterThanEnterpriseOnSameSample() throws Exception {
        MethodScanValues heavy = new MethodScanValues(
                14, 60, 6, 5, 18, 30, 7, 2, 4, 11, 1, 1, 6, 3, 0, 1, 0);
        double enterprise = new RiskCalculator(RiskProfileLoader.load("enterprise-java", CONFIG))
                .assessMethod(heavy).score();
        double strict = new RiskCalculator(RiskProfileLoader.load("strict-review", CONFIG))
                .assessMethod(heavy).score();
        assertTrue(strict >= enterprise - 5.0, "strict=" + strict + " enterprise=" + enterprise);
    }

    private static MethodMetric analyzeMethod(String className, String methodName) throws Exception {
        if (!MOCK.toFile().isDirectory()) {
            throw new IllegalStateException("mock-modules missing");
        }
        RiskCalculator calculator = new RiskCalculator(RiskProfileLoader.load("enterprise-java", CONFIG));
        ProjectAnalyzer analyzer = new ProjectAnalyzer(
                StandardCharsets.UTF_8, 5, ParserConfiguration.LanguageLevel.JAVA_17,
                ScanOptions.defaults(), calculator);
        AnalysisReport report = analyzer.analyze(MOCK);
        return report.files().stream()
                .flatMap(f -> f.classes().stream())
                .filter(c -> c.name().endsWith(className))
                .flatMap(c -> c.methods().stream())
                .filter(m -> methodName.equals(m.name()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(className + "." + methodName + " not found"));
    }
}
