package com.standalone.analyzer;

import java.util.List;

/**
 * Metrics of a single method or constructor.
 *
 * @param physicalLines     total line span of the declaration (method length)
 * @param codeLines         net lines of code (blank and comment-only lines excluded)
 * @param logicalStatements number of statements (blocks excluded)
 * @param outboundDistinctCalls distinct outbound call keys (FOUT / ATFD proxy)
 */
public record MethodMetric(
        String name,
        String kind,
        String signature,
        int startLine,
        int endLine,
        int cyclomaticComplexity,
        int physicalLines,
        int codeLines,
        int logicalStatements,
        int cognitiveComplexity,
        int exitPoints,
        int catchClauses,
        int switchCases,
        int outboundDistinctCalls,
        int lambdaCount,
        int maxTryNestingDepth,
        int localVariableCount,
        int maxMethodCallChainLength,
        int emptyCatchBlocks,
        int catchExceptionOrThrowable,
        int catchWithOnlyPrintStackTrace,
        int primitiveObsessionIndex,
        int maxBooleanOperatorsInCondition,
        int maxNestingDepth,
        int parameterCount,
        boolean godMethod,
        double riskScore,
        RiskLevel riskLevel,
        List<String> riskFactors,
        RiskBreakdown riskBreakdown,
        HalsteadMetrics halstead,
        List<ExceptionSmell> exceptionSmells,
        List<CodeSmell> codeSmells,
        String methodHash,
        boolean analysisReused,
        List<String> accessedFieldNames) {

    public MethodMetric {
        halstead = halstead == null ? HalsteadMetrics.EMPTY : halstead;
        exceptionSmells = exceptionSmells == null ? List.of() : List.copyOf(exceptionSmells);
        codeSmells = codeSmells == null ? List.of() : List.copyOf(codeSmells);
        methodHash = methodHash == null ? "" : methodHash;
        accessedFieldNames = accessedFieldNames == null ? List.of() : List.copyOf(accessedFieldNames);
    }

    /** Legacy smell/Halstead alanları boş; {@link SupplementalMetrics} doldurur. */
    static MethodMetric withoutLegacyExtensions(
            String name, String kind, String signature, int startLine, int endLine,
            int cyclomaticComplexity, int physicalLines, int codeLines, int logicalStatements,
            int cognitiveComplexity, int exitPoints, int catchClauses, int switchCases,
            int outboundDistinctCalls, int lambdaCount, int maxTryNestingDepth, int localVariableCount,
            int maxMethodCallChainLength, int emptyCatchBlocks, int catchExceptionOrThrowable,
            int catchWithOnlyPrintStackTrace, int primitiveObsessionIndex, int maxBooleanOperatorsInCondition,
            int maxNestingDepth, int parameterCount, boolean godMethod, double riskScore, RiskLevel riskLevel,
            List<String> riskFactors, RiskBreakdown riskBreakdown) {
        return new MethodMetric(name, kind, signature, startLine, endLine, cyclomaticComplexity, physicalLines,
                codeLines, logicalStatements, cognitiveComplexity, exitPoints, catchClauses, switchCases,
                outboundDistinctCalls, lambdaCount, maxTryNestingDepth, localVariableCount, maxMethodCallChainLength,
                emptyCatchBlocks, catchExceptionOrThrowable, catchWithOnlyPrintStackTrace, primitiveObsessionIndex,
                maxBooleanOperatorsInCondition, maxNestingDepth, parameterCount, godMethod, riskScore, riskLevel,
                riskFactors, riskBreakdown, HalsteadMetrics.EMPTY, List.of(), List.of(), "", false, List.of());
    }
}
