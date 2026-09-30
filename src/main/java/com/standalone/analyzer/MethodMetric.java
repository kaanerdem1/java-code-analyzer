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
        int maxNestingDepth,
        int parameterCount,
        boolean godMethod,
        double riskScore,
        RiskLevel riskLevel,
        List<String> riskFactors,
        RiskBreakdown riskBreakdown) {
}
