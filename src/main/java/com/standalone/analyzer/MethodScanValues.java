package com.standalone.analyzer;

/** Raw method counters passed into {@link RiskCalculator#assessMethod(MethodScanValues)}. */
public record MethodScanValues(
        int cyclomaticComplexity,
        int codeLines,
        int maxNestingDepth,
        int parameterCount,
        int cognitiveComplexity,
        int logicalStatements,
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
        int maxBooleanOperatorsInCondition) {

    static MethodScanValues zeros(int cyclomatic, int codeLines, int nesting, int parameters) {
        return new MethodScanValues(cyclomatic, codeLines, nesting, parameters,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }
}
