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
import com.standalone.analyzer.ScanOptions.ReportDetail;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/** Scans a directory tree, parses .java files and assembles the {@link AnalysisReport}. */
public final class ProjectAnalyzer {

    private static final String TOOL_NAME = "Standalone Java Code Analyzer";
    private static final Set<String> IGNORED_DIRECTORIES = Set.of(
            ".git", ".svn", ".hg", ".idea", "target", "node_modules",
            "build", "out", "bin", "dist", ".gradle", "coverage",
            "generated", "generated-sources", "generated-test-sources",
            "__generated__", ".mvn", "vendor", "tmp");

    private final RiskCalculator riskCalculator = new RiskCalculator();
    private final ParserConfiguration parserConfiguration;
    private final int topN;
    private final ParserConfiguration.LanguageLevel languageLevel;
    private final ScanOptions scanOptions;

    public ProjectAnalyzer(Charset sourceCharset, int topN, ParserConfiguration.LanguageLevel languageLevel) {
        this(sourceCharset, topN, languageLevel, ScanOptions.defaults());
    }

    public ProjectAnalyzer(Charset sourceCharset, int topN, ParserConfiguration.LanguageLevel languageLevel,
                           ScanOptions scanOptions) {
        this.languageLevel = languageLevel;
        this.scanOptions = scanOptions;
        this.parserConfiguration = new ParserConfiguration()
                .setLanguageLevel(languageLevel)
                .setCharacterEncoding(sourceCharset)
                .setStoreTokens(true)
                .setAttributeComments(false);
        this.topN = topN;
    }

    public AnalysisReport analyze(Path root) throws IOException {
        long started = System.nanoTime();
        Path absoluteRoot = root.toAbsolutePath().normalize();
        Path base = Files.isDirectory(absoluteRoot) ? absoluteRoot : absoluteRoot.getParent();

        List<FileError> errors = Collections.synchronizedList(new ArrayList<>());
        FileDiscovery discovery = collectJavaFiles(absoluteRoot, base, errors, scanOptions);
        List<Path> sources = discovery.paths();

        System.err.println("[STANDALONE] " + sources.size() + " .java file(s) queued"
                + (discovery.skippedByFilter() > 0
                ? " (" + discovery.skippedByFilter() + " skipped by include/exclude)" : "")
                + ", parsing as " + languageLevel.name()
                + ", workers=" + scanOptions.workers() + ", detail=" + scanOptions.reportDetail() + "...");

        List<FileMetric> files = parseAll(sources, base, errors);
        Collections.sort(files, (a, b) -> a.path().compareTo(b.path()));

        Summary summary = buildSummary(sources.size(), files, errors);
        List<RiskHotspot> hotspots = buildHotspots(files);
        List<FileMetric> reportFiles =
                scanOptions.reportDetail() == ReportDetail.SUMMARY ? stripDetail(files) : files;
        long elapsedMs = (System.nanoTime() - started) / 1_000_000L;
        System.err.println("[STANDALONE] Parse wall time: " + elapsedMs + " ms");

        return new AnalysisReport(TOOL_NAME, Instant.now().toString(), absoluteRoot.toString(),
                languageLevel.name(), buildRiskModel(), summary, hotspots, reportFiles, errors);
    }

    private List<FileMetric> parseAll(List<Path> sources, Path base, List<FileError> errors) {
        if (sources.isEmpty()) {
            return new ArrayList<>();
        }
        if (sources.size() == 1 || scanOptions.workers() == 1) {
            return parseSequential(sources, base, errors);
        }
        return parseParallel(sources, base, errors);
    }

    private List<FileMetric> parseSequential(List<Path> sources, Path base, List<FileError> errors) {
        List<FileMetric> files = new ArrayList<>();
        JavaParser parser = new JavaParser(parserConfiguration);
        int processed = 0;
        for (Path source : sources) {
            parseOne(source, base, parser, files, errors);
            logProgress(++processed, sources.size());
        }
        return files;
    }

    private List<FileMetric> parseParallel(List<Path> sources, Path base, List<FileError> errors) {
        List<FileMetric> files = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger processed = new AtomicInteger();
        ThreadLocal<JavaParser> parsers =
                ThreadLocal.withInitial(() -> new JavaParser(parserConfiguration));

        ExecutorService pool = Executors.newFixedThreadPool(scanOptions.workers());
        try {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (Path source : sources) {
                tasks.add(() -> {
                    parseOne(source, base, parsers.get(), files, errors);
                    logProgress(processed.incrementAndGet(), sources.size());
                    return null;
                });
            }
            try {
                List<Future<Void>> futures = pool.invokeAll(tasks);
                for (Future<Void> future : futures) {
                    future.get();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Parallel parse interrupted", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Parallel parse failed", e.getCause());
            }
        } finally {
            pool.shutdown();
        }
        return new ArrayList<>(files);
    }

    private void parseOne(Path source, Path base, JavaParser parser, List<FileMetric> files, List<FileError> errors) {
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
    }

    private void logProgress(int processed, int total) {
        if (processed % scanOptions.progressEvery() == 0 || processed == total) {
            System.err.println("[STANDALONE] " + processed + "/" + total + " files processed");
        }
    }

    private static List<FileMetric> stripDetail(List<FileMetric> files) {
        List<FileMetric> slim = new ArrayList<>(files.size());
        for (FileMetric file : files) {
            slim.add(new FileMetric(file.path(), file.packageName(), file.physicalLines(), file.codeLines(),
                    file.classCount(), file.methodCount(), file.totalCyclomaticComplexity(), file.maxCyclomaticComplexity(),
                    file.riskScore(), file.riskLevel(), file.riskFactors(), List.of()));
        }
        return slim;
    }

    // ------------------------------------------------------------------ file discovery

    private record FileDiscovery(List<Path> paths, int skippedByFilter) {
    }

    private FileDiscovery collectJavaFiles(Path root, Path base, List<FileError> errors, ScanOptions scan)
            throws IOException {
        if (Files.isRegularFile(root)) {
            return new FileDiscovery(List.of(root), 0);
        }
        FileSystem fs = root.getFileSystem();
        PathGlobFilter filter = new PathGlobFilter(fs, scan.includeGlobs(), scan.excludeGlobs());
        List<Path> result = new ArrayList<>();
        int[] skipped = {0};
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
                    if (filter.accept(root, file)) {
                        result.add(file);
                    } else {
                        skipped[0]++;
                    }
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
        return new FileDiscovery(result, skipped[0]);
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

        List<ClassMetric> withHierarchy = attachHierarchy(relativePath, packageName, classes);
        return new FileMetric(relativePath, packageName, physicalLines, loc, withHierarchy.size(), methods.size(),
                wmc, maxCc, risk.score(), risk.level(), risk.factors(), withHierarchy);
    }

    private static List<ClassMetric> attachHierarchy(String relativePath, String packageName, List<ClassMetric> classes) {
        List<ClassMetric> enriched = new ArrayList<>(classes.size());
        for (ClassMetric type : classes) {
            List<MethodMetric> methods = new ArrayList<>(type.methods().size());
            for (MethodMetric method : type.methods()) {
                List<String> ancestors = MethodHierarchy.ancestorPath(
                        relativePath, packageName, type.name(), method.signature());
                methods.add(new MethodMetric(
                        method.name(), method.kind(), method.signature(), method.startLine(), method.endLine(),
                        method.cyclomaticComplexity(), method.physicalLines(), method.codeLines(),
                        method.logicalStatements(), method.maxNestingDepth(), method.parameterCount(),
                        method.godMethod(), method.riskScore(), method.riskLevel(), method.riskFactors(),
                        method.riskBreakdown(), MethodHierarchy.moduleRoot(relativePath), ancestors));
            }
            enriched.add(new ClassMetric(type.name(), type.kind(), type.startLine(), type.endLine(), type.methodCount(),
                    type.codeLines(), type.weightedMethodComplexity(), type.maxMethodComplexity(),
                    type.averageMethodComplexity(), type.riskScore(), type.riskLevel(), type.riskFactors(), methods));
        }
        return enriched;
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

        return new Summary(scanned, files.size(), scanned - files.size(), classCount, methodCount,
                totalLoc, avgCc, maxCc, godMethods, projectScore, RiskLevel.fromScore(projectScore), distribution);
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
                            m.maxNestingDepth(), m.parameterCount(), m.riskFactors(), m.moduleRoot(),
                            m.ancestorPath()));
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
