package com.standalone.analyzer;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RiskProfileLoaderTest {

    @Test
    void loadsEnterpriseJavaProfile() throws Exception {
        Path config = Path.of("config/risk-parameters-proposal.yaml");
        RiskProfile profile = RiskProfileLoader.load("enterprise-java", config);
        assertTrue(profile.modelVersion().startsWith("v3"));
        assertEquals(25, profile.methodDimensions().size());
        assertEquals(6, profile.classDimensions().size());
        assertTrue(profile.methodDimensions().stream().anyMatch(d -> "cognitive".equals(d.id())));
    }

    @Test
    void defaultCalculatorLoadsEnterpriseProfile() {
        RiskCalculator calculator = new RiskCalculator();
        assertEquals(RiskProfileLoader.DEFAULT_PROFILE_ID, calculator.profile().profileId());
        assertTrue(calculator.profile().modelVersion().startsWith("v3"));
    }
}
