package com.standalone.analyzer;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnterpriseJavaPmdProfileTest {

    @Test
    void loadsAndExcludesParserOnlyDimensionsFromGroups() throws Exception {
        RiskProfile profile = RiskProfileLoader.load("enterprise-java-pmd", Path.of("config/risk-parameters-proposal.yaml"));
        assertEquals("enterprise-java-pmd", profile.profileId());
        assertTrue(profile.methodDimensionGroups().size() >= 4);
        boolean hasPrimitive = profile.methodDimensions().stream()
                .anyMatch(d -> "primitiveObsessionIndex".equals(d.id()));
        assertTrue(hasPrimitive, "PMD profile scores primitive obsession via ruleset proxy");
        boolean hasCallChain = profile.methodDimensions().stream()
                .anyMatch(d -> "maxMethodCallChainLength".equals(d.id()));
        assertFalse(hasCallChain, "PMD profile should not score parser-only call-chain dimension");
    }
}
