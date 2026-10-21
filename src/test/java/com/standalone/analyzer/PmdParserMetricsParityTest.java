package com.standalone.analyzer;

import com.github.javaparser.ParserConfiguration;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ToIntFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Parser ile {@link PmdProjectScanner} aynı metrik ve risk çıktısı üretmeli. */
class PmdParserMetricsParityTest {

    private static final Path MOCK = Path.of("mock-modules");

    private static final Map<String, ToIntFunction<MethodScanValues>> SCAN_FIELDS = Map.ofEntries(
            Map.entry("cyclomaticComplexity", MethodScanValues::cyclomaticComplexity),
            Map.entry("codeLines", MethodScanValues::codeLines),
            Map.entry("maxNestingDepth", MethodScanValues::maxNestingDepth),
            Map.entry("parameterCount", MethodScanValues::parameterCount),
            Map.entry("cognitiveComplexity", MethodScanValues::cognitiveComplexity),
            Map.entry("logicalStatements", MethodScanValues::logicalStatements),
            Map.entry("exitPoints", MethodScanValues::exitPoints),
            Map.entry("lambdaCount", MethodScanValues::lambdaCount),
            Map.entry("switchCases", MethodScanValues::switchCases),
            Map.entry("maxTryNestingDepth", MethodScanValues::maxTryNestingDepth),
            Map.entry("localVariableCount", MethodScanValues::localVariableCount),
            Map.entry("maxMethodCallChainLength", MethodScanValues::maxMethodCallChainLength),
            Map.entry("catchClauses", MethodScanValues::catchClauses),
            Map.entry("emptyCatchBlocks", MethodScanValues::emptyCatchBlocks),
            Map.entry("catchExceptionOrThrowable", MethodScanValues::catchExceptionOrThrowable),
            Map.entry("catchWithOnlyPrintStackTrace", MethodScanValues::catchWithOnlyPrintStackTrace),
            Map.entry("primitiveObsessionIndex", MethodScanValues::primitiveObsessionIndex),
            Map.entry("maxBooleanOperatorsInCondition", MethodScanValues::maxBooleanOperatorsInCondition),
            Map.entry("halsteadDifficultyRounded", MethodScanValues::halsteadDifficultyRounded),
            Map.entry("halsteadEffortRounded", MethodScanValues::halsteadEffortRounded),
            Map.entry("rawTypeUsage", MethodScanValues::rawTypeUsage),
            Map.entry("stringConcatInLoop", MethodScanValues::stringConcatInLoop),
            Map.entry("hardcodedLiteralCount", MethodScanValues::hardcodedLiteralCount),
            Map.entry("swallowedExceptionSmells", MethodScanValues::swallowedExceptionSmells),
            Map.entry("genericExceptionSmells", MethodScanValues::genericExceptionSmells));

    @Test
    void fullMethodMetricsMatchOnMockModules() throws Exception {
        if (!MOCK.toFile().isDirectory()) {
            return;
        }
        ScanOptions options = ScanOptions.defaults();
        RiskCalculator calculator = new RiskCalculator(
                RiskProfileLoader.load("enterprise-java", Path.of("config/risk-parameters-proposal.yaml")));

        ProjectAnalyzer parser = new ProjectAnalyzer(
                StandardCharsets.UTF_8, 50, ParserConfiguration.LanguageLevel.JAVA_17, options, calculator);
        AnalysisReport parserReport = parser.analyze(MOCK);

        PmdProjectScanner pmdScanner = new PmdProjectScanner(
                StandardCharsets.UTF_8, ParserConfiguration.LanguageLevel.JAVA_17, options, 50, calculator,
                null, null, false);
        AnalysisReport pmdReport = pmdScanner.scan(MOCK);

        Map<String, MethodMetric> parserByKey = index(parserReport);
        Map<String, MethodMetric> pmdByKey = index(pmdReport);

        assertEquals(parserByKey.size(), pmdByKey.size(), "method count");
        int mismatches = 0;
        StringBuilder detail = new StringBuilder();
        for (Map.Entry<String, MethodMetric> entry : parserByKey.entrySet()) {
            MethodMetric p = entry.getValue();
            MethodMetric m = pmdByKey.get(entry.getKey());
            assertTrue(m != null, "missing in PMD scan: " + entry.getKey());
            MethodScanValues ps = MethodScanValues.fromMetric(p);
            MethodScanValues ms = MethodScanValues.fromMetric(m);
            for (Map.Entry<String, ToIntFunction<MethodScanValues>> field : SCAN_FIELDS.entrySet()) {
                int pv = field.getValue().applyAsInt(ps);
                int mv = field.getValue().applyAsInt(ms);
                if (pv != mv) {
                    mismatches++;
                    detail.append(entry.getKey()).append(' ').append(field.getKey()).append('=').append(pv)
                            .append(" vs ").append(mv).append('\n');
                }
            }
            if (p.npathComplexity() != m.npathComplexity() || p.ncssCount() != m.ncssCount()) {
                mismatches++;
                detail.append(entry.getKey()).append(" npath=").append(p.npathComplexity())
                        .append('/').append(m.npathComplexity()).append(" ncss=").append(p.ncssCount())
                        .append('/').append(m.ncssCount()).append('\n');
            }
            if (Double.compare(p.riskScore(), m.riskScore()) != 0 || p.riskLevel() != m.riskLevel()) {
                mismatches++;
                detail.append(entry.getKey()).append(" risk ").append(p.riskScore()).append('/')
                        .append(m.riskScore()).append(' ').append(p.riskLevel()).append('/')
                        .append(m.riskLevel()).append('\n');
            }
        }
        assertEquals(0, mismatches, "metric mismatches:\n" + detail);
    }

    private static Map<String, MethodMetric> index(AnalysisReport report) {
        Map<String, MethodMetric> map = new HashMap<>();
        for (AnalysisReport.FileMetric file : report.files()) {
            for (AnalysisReport.ClassMetric type : file.classes()) {
                for (MethodMetric method : type.methods()) {
                    map.put(file.path() + "|" + type.name() + "|" + method.name(), method);
                }
            }
        }
        return map;
    }
}
