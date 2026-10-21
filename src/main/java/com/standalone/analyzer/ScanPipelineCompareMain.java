package com.standalone.analyzer;

import com.github.javaparser.ParserConfiguration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Aynı kökte parser vs PMD-native pipeline risk/metrik farkını JSON yazar.
 *
 * <pre>
 *   java -cp … ScanPipelineCompareMain --path=mock-modules --output=compare.json
 * </pre>
 */
public final class ScanPipelineCompareMain {

    private ScanPipelineCompareMain() {
    }

    public static void main(String[] args) {
        System.exit(run(args));
    }

    static int run(String[] args) {
        try {
            Path path = null;
            Path output = Path.of("analysis-output/pipeline-compare.json");
            for (String arg : args) {
                if (arg.startsWith("--path=")) {
                    path = Paths.get(arg.substring(arg.indexOf('=') + 1).trim());
                } else if (arg.startsWith("--output=")) {
                    output = Paths.get(arg.substring(arg.indexOf('=') + 1).trim());
                } else if (arg.equals("--help")) {
                    System.err.println("Usage: ScanPipelineCompareMain --path=<dir> [--output=compare.json]");
                    return 0;
                }
            }
            if (path == null) {
                System.err.println("Missing --path=");
                return 1;
            }
            ScanOptions options = ScanOptions.defaults();
            Path riskConfig = Path.of("config/risk-parameters-proposal.yaml");
            RiskCalculator parserCalculator = new RiskCalculator(
                    RiskProfileLoader.load("enterprise-java", riskConfig));
            RiskCalculator pmdCalculator = new RiskCalculator(
                    RiskProfileLoader.load("enterprise-java-pmd", riskConfig));
            var level = ParserConfiguration.LanguageLevel.JAVA_17;

            AnalysisReport parserReport = new ProjectAnalyzer(
                    StandardCharsets.UTF_8, 50, level, options, parserCalculator).analyze(path);

            Path ruleset = Path.of("config/pmd-ruleset-complexity.xml");
            AnalysisReport pmdReport = new PmdNativeProjectScanner(
                    StandardCharsets.UTF_8, level, options, 50, pmdCalculator, ruleset, null, true)
                    .scan(path);

            Map<String, MethodRow> parserMethods = index(parserReport, "java-parser");
            Map<String, MethodRow> pmdMethods = index(pmdReport, "pmd-native");

            List<MethodCompare> compares = new ArrayList<>();
            for (Map.Entry<String, MethodRow> e : parserMethods.entrySet()) {
                MethodRow p = e.getValue();
                MethodRow m = pmdMethods.get(e.getKey());
                compares.add(MethodCompare.of(e.getKey(), p, m));
            }
            for (Map.Entry<String, MethodRow> e : pmdMethods.entrySet()) {
                if (!parserMethods.containsKey(e.getKey())) {
                    compares.add(MethodCompare.of(e.getKey(), null, e.getValue()));
                }
            }

            CompareDocument doc = new CompareDocument(
                    path.toAbsolutePath().normalize().toString(),
                    parserReport.tool(),
                    pmdReport.tool(),
                    parserMethods.size(),
                    pmdMethods.size(),
                    compares);

            if (output.getParent() != null) {
                Files.createDirectories(output.getParent());
            }
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.writeString(output, gson.toJson(doc));
            System.err.println("[COMPARE] " + compares.size() + " method row(s) → "
                    + output.toAbsolutePath().normalize());
            long riskMismatch = compares.stream().filter(c -> c.onlyInParser() || c.onlyInPmd()
                    || Math.abs(c.riskDelta()) > 0.01).count();
            System.err.println("[COMPARE] Methods with risk delta or missing: " + riskMismatch);
            return 0;
        } catch (Exception e) {
            System.err.println("[FATAL] " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    private static Map<String, MethodRow> index(AnalysisReport report, String pipeline) {
        Map<String, MethodRow> map = new HashMap<>();
        for (AnalysisReport.FileMetric file : report.files()) {
            for (AnalysisReport.ClassMetric type : file.classes()) {
                for (MethodMetric method : type.methods()) {
                    String key = file.path() + "|" + type.name() + "|" + method.name();
                    map.put(key, new MethodRow(pipeline, method.riskScore(), method.cyclomaticComplexity(),
                            method.cognitiveComplexity(), method.codeLines(), method.ncssCount()));
                }
            }
        }
        return map;
    }

    record MethodRow(String pipeline, double riskScore, int cc, int cognitive, int codeLines, int ncss) {
    }

    record MethodCompare(
            String methodKey,
            boolean onlyInParser,
            boolean onlyInPmd,
            double parserRisk,
            double pmdRisk,
            double riskDelta,
            int parserCc,
            int pmdCc,
            int ccDelta) {

        static MethodCompare of(String key, MethodRow parser, MethodRow pmd) {
            boolean onlyP = parser == null;
            boolean onlyM = pmd == null;
            double pr = parser == null ? 0 : parser.riskScore();
            double mr = pmd == null ? 0 : pmd.riskScore();
            int pcc = parser == null ? 0 : parser.cc();
            int mcc = pmd == null ? 0 : pmd.cc();
            return new MethodCompare(key, onlyP, onlyM, pr, mr, mr - pr, pcc, mcc, mcc - pcc);
        }
    }

    record CompareDocument(
            String analyzedPath,
            String parserTool,
            String pmdTool,
            int parserMethodCount,
            int pmdMethodCount,
            List<MethodCompare> methods) {
    }
}
