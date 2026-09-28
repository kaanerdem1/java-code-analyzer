package com.standalone.analyzer;

/**
 * Risk bands on the normalised 0.0 - 1.0 score scale.
 * LOW [0.0, 0.3) - MEDIUM [0.3, 0.5) - HIGH [0.5, 0.8) - CRITICAL [0.8, 1.0]
 */
public enum RiskLevel {
    LOW(0.0, 0.3),
    MEDIUM(0.3, 0.5),
    HIGH(0.5, 0.8),
    CRITICAL(0.8, 1.0);

    private final double lowerBound;
    private final double upperBound;

    RiskLevel(double lowerBound, double upperBound) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public static RiskLevel fromScore(double score) {
        if (score >= CRITICAL.lowerBound) return CRITICAL;
        if (score >= HIGH.lowerBound) return HIGH;
        if (score >= MEDIUM.lowerBound) return MEDIUM;
        return LOW;
    }

    /** Highest score (3 decimal precision) that still belongs to this band. */
    public double maxScore() {
        return this == CRITICAL ? upperBound : upperBound - 0.001;
    }
}
