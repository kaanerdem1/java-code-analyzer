package com.standalone.analyzer;

import java.io.IOException;
import java.nio.file.Path;

/** CLI wiring for {@link RiskProfile} / {@link RiskCalculator}. */
public final class RiskProfileResolver {

    private RiskProfileResolver() {
    }

    public static RiskCalculator create(String profileName, Path configPath) {
        if (profileName == null || profileName.isBlank()) {
            return new RiskCalculator();
        }
        Path resolved = configPath != null ? configPath : RiskProfileLoader.defaultConfigPath();
        try {
            RiskProfile profile = RiskProfileLoader.load(profileName.trim(), resolved);
            return new RiskCalculator(profile);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Failed to load risk profile '" + profileName + "': " + e.getMessage(), e);
        }
    }
}
