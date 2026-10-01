package com.standalone.analyzer;

/** Persisted cache is valid only when profile, model version, and schema match. */
final class AnalyzerCacheIdentity {

    /** Bump when scoring or state shape changes (auto-invalidates old analyzer-state.json). */
    static final int SCHEMA_VERSION = 4;

    private AnalyzerCacheIdentity() {
    }

    static String current(RiskCalculator calculator) {
        RiskProfile profile = calculator.profile();
        return "schema=" + SCHEMA_VERSION
                + ";profile=" + profile.profileId()
                + ";model=" + profile.modelVersion();
    }

    static boolean matches(AnalyzerState state, RiskCalculator calculator) {
        if (state == null || state.cacheIdentity == null || state.cacheIdentity.isBlank()) {
            return false;
        }
        return state.cacheIdentity.equals(current(calculator));
    }
}
