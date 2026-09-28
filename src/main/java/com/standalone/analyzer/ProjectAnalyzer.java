package com.standalone.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import com.standalone.analyzer.AnalysisReport.ClassMetric;
import com.standalone.analyzer.AnalysisReport.FileError;
import com.standalone.analyzer.AnalysisReport.FileMetric;
import com.standalone.analyzer.AnalysisReport.RiskHotspot;
import com.standalone.analyzer.AnalysisReport.Summary;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Scans a directory tree, parses .java files and assembles the {@link AnalysisReport}. */
public final class ProjectAnalyzer {

    private static final String TOOL_NAME = "Standalone Java Code Analyzer";
    private static final Set<String> IGNORED_DIRECTORIES =
            Set.of(".git", ".svn", ".hg", ".idea", "target", "node_modules");

    private final RiskCalculator riskCalculator = new RiskCalculator();
    private final JavaParser parser;
    private final int topN;
    private final ParserConfiguration.LanguageLevel languageLevel;

    public ProjectAnalyzer(Charset sourceCharset, int topN, ParserConfiguration.LanguageLevel languageLevel) {
        this.languageLevel = languageLevel;
        ParserConfiguration configuration = new ParserConfiguration()
                .setLanguageLevel(languageLevel)
                .setCharacterEncoding(sourceCharset)
                .setStoreTokens(true)
                .setAttributeComments(false);
        this.parser = new JavaParser(configuration);
        this.topN = topN;
    }

    public AnalysisReport analyze(Path root) throws IOException {
        Path absoluteRoot = root.toAbsolutePath().normalize();
        Path base = Files.isDirectory(absoluteRoot) ? absoluteRoot : absoluteRoot.getParent();

        List<FileError> errors = new ArrayList<>();
        List<Path> sources = collectJavaFiles(absoluteRoot, base, errors);
        System.err.println("[STANDALONE] " + sources.size() + " .java file(s) found, parsing as "
                + languageLevel.name() + "...");

        List<FileMetric> files = new ArrayList<>();
        int processed = 0;
        for (Path source : sources) {
            String relative = relativePath(base, source);
            try {
                ParseResult<CompilationUnit> result = parser.parse(source);
                if (result.isSuccessful() && result.getResult().isPresent()) {
                    files.add(buildFileMetric(relative, result.getResult().get()));
                } else {
                    errors.add(new FileError(relative, "Parse error: " + describe(result.getProblems())));
                }
            } catch (IOException e) {
                errors.add(new FileError(relative, "I/O error: " + e.getMessage()));
            } catch (ParseProblemException e) {
                errors.add(new FileError(relative, "Parse error: " + e.getMessage()));
            } catch (StackOverflowError e) {
                errors.add(new FileError(relative, "Analysis aborted: expression nesting too deep (stack overflow)"));
            } catch (RuntimeException e) {
                errors.add(new FileError(relative, "Unexpected error: " + e));
            }
            if (++processed % 200 == 0) {
                System.err.println("[STANDALONE] " + processed + "/" + sources.size() + " files processed");
            }
        }

        Summary summary = buildSummary(sources.size(), files, errors);
        return new AnalysisReport(TOOL_NAME, Instant.now().toString(), absoluteRoot.toString(),
                languageLevel.name(), buildRiskModel(), summary, buildHotspots(files),
                files, errors);
    }

    // ------------------------------------------------------------------ file discovery

    private List<Path> collectJavaFiles(Path root, Path base, List<FileError> errors) throws IOException {
        if (Files.isRegularFile(root)) {
            return List.of(root);
        }
        List<Path> result = new ArrayList<>();
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                Path name = dir.getFileName();
                boolean ignored = !dir.equals(root) && name != null && IGNORED_DIRECTORIES.contains(name.toString());
                return ignored ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (attrs.isRegularFile() && file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".java")) {
                    result.add(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                errors.add(new FileError(relativePath(base, file), "Cannot access: " + exc.getMessage()));
                return FileVisitResult.CONTINUE;
            }
        });
        Collections.sort(result);
        return result;
    }

    private static String relativePath(Path base, Path file) {
        Path relative = base != null && file.startsWith(base) ? base.relativize(file) : file;
        return relative.toString().replace('\\', '/');
    }

    private static String describe(List<Problem> problems) {
        if (problems.isEmpty()) {
            return "unknown parser failure";
        }
        return problems.stream().limit(3).map(Problem::getVerboseMessage).collect(Collectors.joining(" | "));
    }

    // ------------------------------------------------------------------ per-file assembly

    private FileMetric buildFileMetric(String relativePath, CompilationUnit cu) {
        BitSet codeLines = ComplexityVisitor.computeCodeLines(cu);
        ComplexityVisitor visitor = new ComplexityVisitor(riskCalculator, codeLines);
        cu.accept(visitor, null);

        List<ClassMetric> classes = visitor.getClassMetrics();
        List<MethodMetric> methods = classes.stream().flatMap(c -> c.methods().stream()).toList();
        int wmc = methods.stream().mapToInt(MethodMetric::cyclomaticComplexity).sum();
        int maxCc = methods.stream().mapToInt(MethodMetric::cyclomaticComplexity).max().orElse(0);
        int loc = codeLines.cardinality();

        RiskCalculator.Assessment risk =
                riskCalculator.assessAggregate(methods, loc, wmc, RiskCalculator.Scope.FILE);

        String packageName = cu.getPackageDeclaration().map(p -> p.getNameAsString()).orElse("");
        int physicalLines = cu.getRange().map(r -> r.end.line).orElse(0);

        return new FileMetric(relativePath, packageName, physicalLines, loc, classes.size(), methods.size(),
                wmc, maxCc, risk.score(), risk.level(), risk.factors(), classes);
    }

    // ------------------------------------------------------------------ report-level aggregation

    private Summary buildSummary(int scanned, List<FileMetric> files, List<FileError> errors) {
        Map<RiskLevel, Long> distribution = new EnumMap<>(RiskLevel.class);
        for (RiskLevel level : RiskLevel.values()) {
            distribution.put(level, 0L);
        }

        int classCount = 0;
        int methodCount = 0;
        int godMethods = 0;
        int maxCc = 0;
        int totalLoc = 0;
        long totalCc = 0;
        double weightedScore = 0;
        long weight = 0;

        for (FileMetric file : files) {
            totalLoc += file.codeLines();
            classCount += file.classCount();
            for (ClassMetric type : file.classes()) {
                for (MethodMetric method : type.methods()) {
                    methodCount++;
                    totalCc += method.cyclomaticComplexity();
                    maxCc = Math.max(maxCc, method.cyclomaticComplexity());
                    distribution.merge(method.riskLevel(), 1L, Long::sum);
                    if (method.godMethod()) {
                        godMethods++;
                    }
                    long methodWeight = Math.max(1, method.codeLines());
                    weightedScore += method.riskScore() * methodWeight;
                    weight += methodWeight;
                }
            }
        }

        double avgCc = methodCount == 0 ? 0.0 : Math.round(totalCc * 100.0 / methodCount) / 100.0;
        double projectScore = weight == 0 ? 0.0 : RiskCalculator.round3(weightedScore / weight);

        return new Summary(scanned, files.size(), scanned - files.size(), classCount, methodCount, totalLoc,
                avgCc, maxCc, godMethods, projectScore, RiskLevel.fromScore(projectScore), distribution);
    }

    private static AnalysisReport.RiskModel buildRiskModel() {
        return new AnalysisReport.RiskModel(
                "v1",
                "finalScore = min(bandCap, max(cc,loc,nesting,paramsSubScore) + 0.15 * weightedBlend)",
                Map.of("cyclomatic", 0.40, "codeLines", 0.20, "nesting", 0.25, "parameters", 0.15),
                Map.of(
                        "cyclomatic", List.of(RiskCalculator.CC_MEDIUM, RiskCalculator.CC_HIGH, RiskCalculator.CC_CRITICAL),
                        "codeLines", List.of(RiskCalculator.LOC_MEDIUM, RiskCalculator.LOC_HIGH, RiskCalculator.LOC_CRITICAL),
                        "nesting", List.of(RiskCalculator.NESTING_MEDIUM, RiskCalculator.NESTING_HIGH, RiskCalculator.NESTING_CRITICAL),
                        "parameters", List.of(RiskCalculator.PARAMS_MEDIUM, RiskCalculator.PARAMS_HIGH, RiskCalculator.PARAMS_CRITICAL)));
    }

    private List<RiskHotspot> buildHotspots(List<FileMetric> files) {
        List<RiskHotspot> hotspots = new ArrayList<>();
        for (FileMetric file : files) {
            for (ClassMetric type : file.classes()) {
                for (MethodMetric m : type.methods()) {
                    hotspots.add(new RiskHotspot(file.path(), type.name(), m.signature(), m.startLine(),
                            m.riskScore(), m.riskLevel(), m.cyclomaticComplexity(), m.codeLines(),
                            m.maxNestingDepth(), m.parameterCount(), m.riskFactors()));
                }
            }
        }
        hotspots.sort((a, b) -> {
            int byScore = Double.compare(b.riskScore(), a.riskScore());
            return byScore != 0 ? byScore : Integer.compare(b.cyclomaticComplexity(), a.cyclomaticComplexity());
        });
        return hotspots.size() > topN ? new ArrayList<>(hotspots.subList(0, topN)) : hotspots;
    }
}
