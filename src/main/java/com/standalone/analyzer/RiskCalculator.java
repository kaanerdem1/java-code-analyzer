package com.standalone.analyzer;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns raw metrics into a normalised 0.0 - 1.0 technical risk score.
 *
 * <p>Algorithm:
 * <ol>
 *   <li>Every metric is mapped to a sub-score with a piecewise-linear curve anchored on its
 *       MEDIUM / HIGH / CRITICAL thresholds (value &gt; threshold moves it into the next band).</li>
 *   <li>The worst sub-score dominates the result (one critical smell cannot be averaged away).</li>
 *   <li>A small compound bonus (weighted mean of all sub-scores) separates methods that are bad in
 *       several dimensions from methods that are bad in one; the bonus never leaves the band.</li>
 * </ol>
 * Class and file scores follow the same principle using their worst method, average method risk,
 * total size and WMC (sum of cyclomatic complexities).
 */
public final class RiskCalculator {

    // Thresholds: value > MEDIUM/HIGH/CRITICAL enters the respective band.
    public static final int CC_MEDIUM = 10;
    public static final int CC_HIGH = 15;
    public static final int CC_CRITICAL = 30;

    public static final int LOC_MEDIUM = 50;
    public static final int LOC_HIGH = 100;
    public static final int LOC_CRITICAL = 200;

    public static final int NESTING_MEDIUM = 3;
    public static final int NESTING_HIGH = 4;
    public static final int NESTING_CRITICAL = 8;

    public static final int PARAMS_MEDIUM = 5;
    public static final int PARAMS_HIGH = 7;
    public static final int PARAMS_CRITICAL = 12;

    private static final double WEIGHT_CC = 0.40;
    private static final double WEIGHT_LOC = 0.20;
    private static final double WEIGHT_NESTING = 0.25;
    private static final double WEIGHT_PARAMS = 0.15;

    private static final double COMPOUND_FACTOR = 0.15;
    private static final double WORST_METHOD_INFLUENCE = 0.65;

    /** Size thresholds for aggregate (class / file) assessment. */
    public enum Scope {
        CLASS(200, 500, 1000, 30, 50, 100),
        FILE(400, 800, 1500, 60, 100, 200);

        private final int locMedium, locHigh, locCritical;
        private final int wmcMedium, wmcHigh, wmcCritical;

        Scope(int locMedium, int locHigh, int locCritical, int wmcMedium, int wmcHigh, int wmcCritical) {
            this.locMedium = locMedium;
            this.locHigh = locHigh;
            this.locCritical = locCritical;
            this.wmcMedium = wmcMedium;
            this.wmcHigh = wmcHigh;
            this.wmcCritical = wmcCritical;
        }
    }

    public record Assessment(double score, RiskLevel level, List<String> factors, RiskBreakdown breakdown) {

        Assessment(double score, RiskLevel level, List<String> factors) {
            this(score, level, factors, null);
        }
    }

    public Assessment assessMethod(int cyclomatic, int codeLines, int nesting, int parameters) {
        double sCc = subScore(cyclomatic, CC_MEDIUM, CC_HIGH, CC_CRITICAL);
        double sLoc = subScore(codeLines, LOC_MEDIUM, LOC_HIGH, LOC_CRITICAL);
        double sNesting = subScore(nesting, NESTING_MEDIUM, NESTING_HIGH, NESTING_CRITICAL);
        double sParams = subScore(parameters, PARAMS_MEDIUM, PARAMS_HIGH, PARAMS_CRITICAL);

        double dominant = Math.max(Math.max(sCc, sLoc), Math.max(sNesting, sParams));
        double blend = WEIGHT_CC * sCc + WEIGHT_LOC * sLoc + WEIGHT_NESTING * sNesting + WEIGHT_PARAMS * sParams;

        List<String> factors = new ArrayList<>();
        describe(factors, "Cyclomatic complexity", cyclomatic, sCc, CC_HIGH, CC_CRITICAL);
        describe(factors, "Lines of code", codeLines, sLoc, LOC_HIGH, LOC_CRITICAL);
        describe(factors, "Nesting depth", nesting, sNesting, NESTING_HIGH, NESTING_CRITICAL);
        describe(factors, "Parameter count", parameters, sParams, PARAMS_HIGH, PARAMS_CRITICAL);

        double compoundBonus = round3(COMPOUND_FACTOR * blend);
        Assessment assessment = compose(dominant, blend, factors);
        RiskBreakdown breakdown = new RiskBreakdown(
                cyclomatic, codeLines, nesting, parameters,
                round3(sCc), round3(sLoc), round3(sNesting), round3(sParams),
                round3(dominant), round3(blend), compoundBonus, assessment.score());
        return new Assessment(assessment.score(), assessment.level(), assessment.factors(), breakdown);
    }

    /** God Method heuristic: very large, very complex, or large AND (complex or wide signature). */
    public boolean isGodMethod(int cyclomatic, int codeLines, int parameters) {
        return codeLines > LOC_CRITICAL
                || cyclomatic > CC_CRITICAL
                || (codeLines > LOC_HIGH && (cyclomatic > CC_HIGH || parameters > PARAMS_HIGH));
    }

    /** Aggregate assessment for a class or a whole file. */
    public Assessment assessAggregate(List<MethodMetric> methods, int codeLines, int wmc, Scope scope) {
        double sSize = subScore(codeLines, scope.locMedium, scope.locHigh, scope.locCritical);
        double sWmc = subScore(wmc, scope.wmcMedium, scope.wmcHigh, scope.wmcCritical);
        double worstMethod = methods.stream().mapToDouble(MethodMetric::riskScore).max().orElse(0.0);
        double avgMethod = methods.stream().mapToDouble(MethodMetric::riskScore).average().orElse(0.0);

        double dominant = Math.max(Math.max(sSize, sWmc), WORST_METHOD_INFLUENCE * worstMethod);

        List<String> factors = new ArrayList<>();
        long critical = methods.stream().filter(m -> m.riskLevel() == RiskLevel.CRITICAL).count();
        long high = methods.stream().filter(m -> m.riskLevel() == RiskLevel.HIGH).count();
        if (critical + high > 0) {
            factors.add("Contains " + critical + " critical and " + high + " high-risk method(s)");
        }
        describe(factors, "Code size (lines)", codeLines, sSize, scope.locHigh, scope.locCritical);
        describe(factors, "Total cyclomatic complexity (WMC)", wmc, sWmc, scope.wmcHigh, scope.wmcCritical);

        return compose(dominant, avgMethod, factors);
    }

    // ------------------------------------------------------------------ helpers

    private Assessment compose(double dominant, double blend, List<String> factors) {
        RiskLevel level = RiskLevel.fromScore(dominant);
        double score = Math.min(level.maxScore(), dominant + COMPOUND_FACTOR * blend);
        return new Assessment(round3(score), level, List.copyOf(factors));
    }

    /**
     * Piecewise-linear normalisation. value &lt;= medium stays below 0.30, value &gt; medium is
     * at least 0.30, value &gt; high at least 0.50, value &gt; critical at least 0.80 (up to 1.0).
     */
    static double subScore(int value, int medium, int high, int critical) {
        if (value <= 0) return 0.0;
        if (value <= medium) return 0.29 * value / medium;
        if (value <= high) return 0.30 + 0.19 * (value - medium) / (high - medium);
        if (value <= critical) return 0.50 + 0.29 * (value - high) / (critical - high);
        return 0.80 + 0.20 * Math.min(1.0, (double) (value - critical) / critical);
    }

    private static void describe(List<String> factors, String label, int value, double subScore,
                                 int high, int critical) {
        RiskLevel level = RiskLevel.fromScore(subScore);
        if (level == RiskLevel.CRITICAL) {
            factors.add(label + " " + value + " exceeds critical threshold (>" + critical + ")");
        } else if (level == RiskLevel.HIGH) {
            factors.add(label + " " + value + " exceeds high-risk threshold (>" + high + ")");
        }
    }

    public static double round3(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
