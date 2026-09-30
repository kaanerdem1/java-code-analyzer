package com.standalone.analyzer;

/** Method risk scores use a 0–100 scale with millisecond-style decimals (e.g. 68.374). */
final class RiskScoreScale {

    static final double MAX = 100.0;

    private RiskScoreScale() {
    }

    static double toDisplayScale(double unitIntervalSubScore) {
        return unitIntervalSubScore * MAX;
    }

    static double clamp(double score) {
        if (score <= 0) {
            return 0;
        }
        return Math.min(MAX, score);
    }
}
