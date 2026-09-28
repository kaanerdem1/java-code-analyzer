package com.standalone.analyzer;

/**
 * Transparent numeric decomposition of {@link RiskCalculator#assessMethod(int, int, int, int)}.
 *
 * <p>All sub-scores are on 0.0–1.0. Raw counts are included so reports can be compared with PMD.
 */
public record RiskBreakdown(
        int cyclomaticComplexity,
        int codeLines,
        int maxNestingDepth,
        int parameterCount,
        double ccSubScore,
        double locSubScore,
        double nestingSubScore,
        double paramsSubScore,
        double dominantSubScore,
        double weightedBlend,
        double compoundBonus,
        double finalScore) {
}
