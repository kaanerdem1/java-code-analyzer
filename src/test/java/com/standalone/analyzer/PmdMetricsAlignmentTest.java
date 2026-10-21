package com.standalone.analyzer;

import com.github.javaparser.ParserConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PmdMetricsAlignmentTest {

    private static final String SOURCE = """
            package demo;
            public class Sample {
                public int pick(int x) {
                    if (x > 0) {
                        return 1;
                    } else if (x < 0) {
                        return -1;
                    }
                    return 0;
                }
            }
            """;

    @Test
    void pmdExtractReportsCyclomaticForMethod() {
        Map<PmdMetricsAligner.MethodKey, PmdMetricsAligner.PmdMethodMetrics> metrics =
                PmdMetricsAligner.extract(SOURCE, "Sample.java", ParserConfiguration.LanguageLevel.JAVA_17);
        PmdMetricsAligner.PmdMethodMetrics pick = metrics.get(
                new PmdMetricsAligner.MethodKey("Sample", "pick", 3));
        assertTrue(pick != null, () -> "keys: " + metrics.keySet());
        assertEquals(3, pick.cyclomaticComplexity());
        assertTrue(pick.cognitiveComplexity() >= 1);
        assertTrue(pick.ncssCount() >= 1);
        assertTrue(pick.npathComplexity() >= 1);
    }
}
