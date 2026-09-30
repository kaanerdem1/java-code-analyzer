package com.standalone.analyzer;

import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/** Gson {@code toJson} yerine doğrudan {@link Writer}'a yazar; dev JSON için ek String kopyası oluşturmaz. */
final class AnalysisReportJsonWriter {

    private AnalysisReportJsonWriter() {
    }

    static void write(AnalysisReport report, Writer out, boolean compact) throws IOException {
        JsonWriter writer = new JsonWriter(out);
        if (!compact) {
            writer.setIndent("  ");
        }
        writeReport(writer, report);
        writer.flush();
    }

    private static void writeReport(JsonWriter w, AnalysisReport report) throws IOException {
        w.beginObject();
        w.name("tool").value(report.tool());
        w.name("generatedAt").value(report.generatedAt());
        w.name("analyzedPath").value(report.analyzedPath());
        w.name("parserLanguageLevel").value(report.parserLanguageLevel());
        writeRiskModel(w, report.riskModel());
        writeSummary(w, report.summary());
        writeHotspots(w, report.topRiskyMethods());
        writeFiles(w, report.files());
        writeErrors(w, report.errors());
        w.endObject();
    }

    private static void writeRiskModel(JsonWriter w, AnalysisReport.RiskModel model) throws IOException {
        w.name("riskModel");
        w.beginObject();
        w.name("version").value(model.version());
        w.name("formula").value(model.formula());
        w.name("weights");
        w.beginObject();
        for (Map.Entry<String, Double> e : model.weights().entrySet()) {
            w.name(e.getKey()).value(e.getValue());
        }
        w.endObject();
        w.name("thresholds");
        w.beginObject();
        for (Map.Entry<String, List<Integer>> e : model.thresholds().entrySet()) {
            w.name(e.getKey());
            w.beginArray();
            for (int v : e.getValue()) {
                w.value(v);
            }
            w.endArray();
        }
        w.endObject();
        w.endObject();
    }

    private static void writeSummary(JsonWriter w, AnalysisReport.Summary s) throws IOException {
        w.name("summary");
        w.beginObject();
        w.name("filesScanned").value(s.filesScanned());
        w.name("filesParsed").value(s.filesParsed());
        w.name("filesFailed").value(s.filesFailed());
        w.name("classCount").value(s.classCount());
        w.name("methodCount").value(s.methodCount());
        w.name("totalCodeLines").value(s.totalCodeLines());
        w.name("averageCyclomaticComplexity").value(s.averageCyclomaticComplexity());
        w.name("maxCyclomaticComplexity").value(s.maxCyclomaticComplexity());
        w.name("godMethodCount").value(s.godMethodCount());
        w.name("projectRiskScore").value(s.projectRiskScore());
        w.name("projectRiskLevel").value(s.projectRiskLevel().name());
        w.name("highPlusCriticalLocRatio").value(s.highPlusCriticalLocRatio());
        w.name("criticalMethodsPerKloc").value(s.criticalMethodsPerKloc());
        w.name("methodRiskScoreP95").value(s.methodRiskScoreP95());
        w.name("methodRiskScoreP99").value(s.methodRiskScoreP99());
        w.name("methodRiskDistribution");
        w.beginObject();
        for (RiskLevel level : RiskLevel.values()) {
            w.name(level.name()).value(s.methodRiskDistribution().get(level));
        }
        w.endObject();
        w.endObject();
    }

    private static void writeHotspots(JsonWriter w, List<AnalysisReport.RiskHotspot> hotspots) throws IOException {
        w.name("topRiskyMethods");
        w.beginArray();
        for (AnalysisReport.RiskHotspot h : hotspots) {
            w.beginObject();
            w.name("file").value(h.file());
            w.name("packageName").value(h.packageName());
            w.name("className").value(h.className());
            w.name("method").value(h.method());
            w.name("startLine").value(h.startLine());
            w.name("riskScore").value(h.riskScore());
            w.name("riskLevel").value(h.riskLevel().name());
            w.name("cyclomaticComplexity").value(h.cyclomaticComplexity());
            w.name("codeLines").value(h.codeLines());
            w.name("maxNestingDepth").value(h.maxNestingDepth());
            w.name("parameterCount").value(h.parameterCount());
            w.name("cognitiveComplexity").value(h.cognitiveComplexity());
            w.name("outboundDistinctCalls").value(h.outboundDistinctCalls());
            w.name("dominantDriver").value(h.dominantDriver());
            writeStringList(w, "riskFactors", h.riskFactors());
            w.endObject();
        }
        w.endArray();
    }

    private static void writeFiles(JsonWriter w, List<AnalysisReport.FileMetric> files) throws IOException {
        w.name("files");
        w.beginArray();
        for (AnalysisReport.FileMetric file : files) {
            w.beginObject();
            w.name("path").value(file.path());
            w.name("packageName").value(file.packageName());
            w.name("physicalLines").value(file.physicalLines());
            w.name("codeLines").value(file.codeLines());
            w.name("classCount").value(file.classCount());
            w.name("methodCount").value(file.methodCount());
            w.name("totalCyclomaticComplexity").value(file.totalCyclomaticComplexity());
            w.name("maxCyclomaticComplexity").value(file.maxCyclomaticComplexity());
            w.name("riskScore").value(file.riskScore());
            w.name("riskLevel").value(file.riskLevel().name());
            writeStringList(w, "riskFactors", file.riskFactors());
            w.name("classes");
            w.beginArray();
            for (AnalysisReport.ClassMetric type : file.classes()) {
                w.beginObject();
                w.name("name").value(type.name());
                w.name("kind").value(type.kind());
                w.name("startLine").value(type.startLine());
                w.name("endLine").value(type.endLine());
                w.name("methodCount").value(type.methodCount());
                w.name("publicMethodCount").value(type.publicMethodCount());
                w.name("codeLines").value(type.codeLines());
                w.name("efferentCouplingProxy").value(type.efferentCouplingProxy());
                w.name("weightedMethodComplexity").value(type.weightedMethodComplexity());
                w.name("maxMethodComplexity").value(type.maxMethodComplexity());
                w.name("averageMethodComplexity").value(type.averageMethodComplexity());
                w.name("riskScore").value(type.riskScore());
                w.name("riskLevel").value(type.riskLevel().name());
                writeStringList(w, "riskFactors", type.riskFactors());
                w.name("methods");
                w.beginArray();
                for (MethodMetric m : type.methods()) {
                    writeMethod(w, m);
                }
                w.endArray();
                w.endObject();
            }
            w.endArray();
            w.endObject();
        }
        w.endArray();
    }

    private static void writeMethod(JsonWriter w, MethodMetric m) throws IOException {
        w.beginObject();
        w.name("name").value(m.name());
        w.name("kind").value(m.kind());
        w.name("signature").value(m.signature());
        w.name("startLine").value(m.startLine());
        w.name("endLine").value(m.endLine());
        w.name("cyclomaticComplexity").value(m.cyclomaticComplexity());
        w.name("physicalLines").value(m.physicalLines());
        w.name("codeLines").value(m.codeLines());
        w.name("logicalStatements").value(m.logicalStatements());
        w.name("cognitiveComplexity").value(m.cognitiveComplexity());
        w.name("exitPoints").value(m.exitPoints());
        w.name("catchClauses").value(m.catchClauses());
        w.name("switchCases").value(m.switchCases());
        w.name("outboundDistinctCalls").value(m.outboundDistinctCalls());
        w.name("lambdaCount").value(m.lambdaCount());
        w.name("maxTryNestingDepth").value(m.maxTryNestingDepth());
        w.name("localVariableCount").value(m.localVariableCount());
        w.name("maxMethodCallChainLength").value(m.maxMethodCallChainLength());
        w.name("emptyCatchBlocks").value(m.emptyCatchBlocks());
        w.name("catchExceptionOrThrowable").value(m.catchExceptionOrThrowable());
        w.name("catchWithOnlyPrintStackTrace").value(m.catchWithOnlyPrintStackTrace());
        w.name("maxNestingDepth").value(m.maxNestingDepth());
        w.name("parameterCount").value(m.parameterCount());
        w.name("godMethod").value(m.godMethod());
        w.name("riskScore").value(m.riskScore());
        w.name("riskLevel").value(m.riskLevel().name());
        writeStringList(w, "riskFactors", m.riskFactors());
        writeBreakdown(w, m.riskBreakdown());
        w.endObject();
    }

    private static void writeBreakdown(JsonWriter w, RiskBreakdown b) throws IOException {
        w.name("riskBreakdown");
        w.beginObject();
        w.name("cyclomaticComplexity").value(b.cyclomaticComplexity());
        w.name("codeLines").value(b.codeLines());
        w.name("maxNestingDepth").value(b.maxNestingDepth());
        w.name("parameterCount").value(b.parameterCount());
        w.name("ccSubScore").value(b.ccSubScore());
        w.name("locSubScore").value(b.locSubScore());
        w.name("nestingSubScore").value(b.nestingSubScore());
        w.name("paramsSubScore").value(b.paramsSubScore());
        w.name("cognitiveSubScore").value(b.cognitiveSubScore());
        w.name("outboundSubScore").value(b.outboundSubScore());
        w.name("exitSubScore").value(b.exitSubScore());
        w.name("extraSubScores");
        w.beginObject();
        for (Map.Entry<String, Double> entry : b.extraSubScores().entrySet()) {
            w.name(entry.getKey()).value(entry.getValue());
        }
        w.endObject();
        w.name("dominantSubScore").value(b.dominantSubScore());
        w.name("weightedBlend").value(b.weightedBlend());
        w.name("compoundBonus").value(b.compoundBonus());
        w.name("finalScore").value(b.finalScore());
        w.endObject();
    }

    private static void writeErrors(JsonWriter w, List<AnalysisReport.FileError> errors) throws IOException {
        w.name("errors");
        w.beginArray();
        for (AnalysisReport.FileError e : errors) {
            w.beginObject();
            w.name("file").value(e.file());
            w.name("message").value(e.message());
            w.endObject();
        }
        w.endArray();
    }

    private static void writeStringList(JsonWriter w, String name, List<String> values) throws IOException {
        w.name(name);
        w.beginArray();
        for (String v : values) {
            w.value(v);
        }
        w.endArray();
    }
}
