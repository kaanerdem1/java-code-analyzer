package com.standalone.analyzer;

import java.util.List;
import java.util.Map;

/** Root DTO serialised to JSON. */
public record AnalysisReport(
        String tool,
        String generatedAt,
        String analyzedPath,
        String parserLanguageLevel,
        RiskModel riskModel,
        Summary summary,
        List<RiskHotspot> topRiskyMethods,
        List<FileMetric> files,
        List<FileError> errors) {

    /** Documents the composite score so JSON can be compared with PMD rule output. */
    public record RiskModel(
            String version,
            String formula,
            Map<String, Double> weights,
            Map<String, List<Integer>> thresholds) {
    }

    public record Summary(
            int filesScanned,
            int filesParsed,
            int filesFailed,
            int classCount,
            int methodCount,
            int totalCodeLines,
            double averageCyclomaticComplexity,
            int maxCyclomaticComplexity,
            int godMethodCount,
            double projectRiskScore,
            RiskLevel projectRiskLevel,
            Map<RiskLevel, Long> methodRiskDistribution) {
    }

    public record FileMetric(
            String path,
            String packageName,
            int physicalLines,
            int codeLines,
            int classCount,
            int methodCount,
            int totalCyclomaticComplexity,
            int maxCyclomaticComplexity,
            double riskScore,
            RiskLevel riskLevel,
            List<String> riskFactors,
            List<ClassMetric> classes) {
    }

    public record ClassMetric(
            String name,
            String kind,
            int startLine,
            int endLine,
            int methodCount,
            int codeLines,
            int weightedMethodComplexity,
            int maxMethodComplexity,
            double averageMethodComplexity,
            double riskScore,
            RiskLevel riskLevel,
            List<String> riskFactors,
            List<MethodMetric> methods) {
    }

    public record RiskHotspot(
            String file,
            String className,
            String method,
            int startLine,
            double riskScore,
            RiskLevel riskLevel,
            int cyclomaticComplexity,
            int codeLines,
            int maxNestingDepth,
            int parameterCount,
            List<String> riskFactors) {
    }

    public record FileError(String file, String message) {
    }
}
