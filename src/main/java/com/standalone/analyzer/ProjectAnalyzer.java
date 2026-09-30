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
import java.util.Comparator;
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

    /** Deep expression trees (generated SQL/strings) need more stack than the default ~1 MB. */
    private static final long WORKER_STACK_BYTES = 64L << 20;
    private static final AtomicInteger WORKER_ID = new AtomicInteger();

    private final RiskCalculator riskCalculator;
    private final ParserConfiguration parserConfiguration;
    private final int topN;
    private final ParserConfiguration.LanguageLevel languageLevel;
    private final ScanOptions scanOptions;

    public ProjectAnalyzer(Charset sourceCharset, int topN, ParserConfiguration.LanguageLevel languageLevel) {
        this(sourceCharset, topN, languageLevel, ScanOptions.defaults(), new RiskCalculator());
    }

    public ProjectAnalyzer(Charset sourceCharset, int topN, ParserConfiguration.LanguageLevel languageLevel,
                           ScanOptions scanOptions) {
        this(sourceCharset, topN, languageLevel, scanOptions, new RiskCalculator());
    }

    public ProjectAnalyzer(Charset sourceCharset, int topN, ParserConfiguration.LanguageLevel languageLevel,
                           ScanOptions scanOptions, RiskCalculator riskCalculator) {
        this.riskCalculator = riskCalculator;
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
        return analyze(root, new ScanRunContext());
    }

    public AnalysisReport analyze(Path root, ScanRunContext runContext) throws IOException {
        long started = System.nanoTime();
        ScanRunContext run = runContext != null ? runContext : new ScanRunContext();
        run.phase(ScanPhase.DISCOVERY);
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

        ModuleRootIndex moduleRoots = ModuleRootIndex.forScanRoot(absoluteRoot);
        ScanAccumulator summaryAccumulator =
                scanOptions.reportDetail() == ReportDetail.SUMMARY
                        ? new ScanAccumulator(topN, moduleRoots) : null;
        run.phase(ScanPhase.PARSING);
        List<FileMetric> files = parseAll(sources, base, errors, summaryAccumulator, run);
        Collections.sort(files, (a, b) -> a.path().compareTo(b.path()));
        errors.sort(Comparator.comparing(FileError::file));
        run.phase(ScanPhase.REPORT_ASSEMBLY);

        int filesParsed = files.size();
        int filesFailed = errors.size();
        Summary summary = summaryAccumulator != null
                ? summaryAccumulator.toSummary(sources.size(), filesParsed, filesFailed)
                : buildSummary(sources.size(), files, errors, moduleRoots);
        List<RiskHotspot> hotspots = summaryAccumulator != null
                ? summaryAccumulator.topHotspots()
                : buildHotspots(files);
        List<FileMetric> reportFiles = files;
        long elapsedMs = (System.nanoTime() - started) / 1_000_000L;
        System.err.println("[STANDALONE] Parse wall time: " + elapsedMs + " ms");

        AnalysisReport report = new AnalysisReport(TOOL_NAME, Instant.now().toString(), absoluteRoot.toString(),
                languageLevel.name(), riskCalculator.buildRiskModel(), summary, hotspots, reportFiles, errors, null);
        ScanDiagnostics diagnostics = ScanDiagnostics.from(report, run, null);
        run.phase(ScanPhase.COMPLETE);
        return new AnalysisReport(TOOL_NAME, report.generatedAt(), report.analyzedPath(),
                report.parserLanguageLevel(), report.riskModel(), report.summary(), report.topRiskyMethods(),
                report.files(), report.errors(), diagnostics);
    }

    private List<FileMetric> parseAll(List<Path> sources, Path base, List<FileError> errors,
                                      ScanAccumulator summaryAccumulator, ScanRunContext run) {
        if (sources.isEmpty()) {
            return new ArrayList<>();
        }
        if (sources.size() == 1 || scanOptions.workers() == 1) {
            return parseSequential(sources, base, errors, summaryAccumulator, run);
        }
        return parseParallel(sources, base, errors, summaryAccumulator, run);
    }

    private List<FileMetric> parseSequential(List<Path> sources, Path base, List<FileError> errors,
                                             ScanAccumulator summaryAccumulator, ScanRunContext run) {
        List<FileMetric> files = new ArrayList<>();
        int processed = 0;
        for (Path source : sources) {
            parseOne(source, base, files, errors, summaryAccumulator, run);
            logProgress(++processed, sources.size());
        }
        return files;
    }

    private List<FileMetric> parseParallel(List<Path> sources, Path base, List<FileError> errors,
                                           ScanAccumulator summaryAccumulator, ScanRunContext run) {
        List<FileMetric> files = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger processed = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(scanOptions.workers(), ProjectAnalyzer::newWorkerThread);
        try {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (Path source : sources) {
                tasks.add(() -> {
                    parseOne(source, base, files, errors, summaryAccumulator, run);
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

    private void parseOne(Path source, Path base, List<FileMetric> files, List<FileError> errors,
                          ScanAccumulator summaryAccumulator, ScanRunContext run) {
        String relative = relativePath(base, source);
        run.parsingFile(relative);
        try {
            SourceParser.Outcome outcome = SourceParser.parse(source, sourceCharset(), languageLevel);
            ParseResult<CompilationUnit> result = outcome.result();
            if (result.isSuccessful() && result.getResult().isPresent()) {
                files.add(buildFileMetric(relative, result.getResult().get(), summaryAccumulator));
            } else {
                String detail = describe(result.getProblems());
                if (outcome.attempted().size() > 1) {
                    detail = detail + " (tried language levels: "
                            + outcome.attempted().stream().map(Enum::name).collect(Collectors.joining(", ")) + ")";
                }
                errors.add(fileError(relative, "Parse error: " + detail));
            }
        } catch (IOException e) {
            errors.add(fileError(relative, "I/O error: " + e.getMessage()));
        } catch (ParseProblemException e) {
            errors.add(fileError(relative, "Parse error: " + e.getMessage()));
        } catch (StackOverflowError e) {
            errors.add(fileError(relative, "Analysis aborted: expression nesting too deep (stack overflow)"));
        } catch (OutOfMemoryError e) {
            errors.add(fileError(relative, "Analysis aborted: out of memory while analyzing file"));
        } catch (RuntimeException e) {
            errors.add(fileError(relative, "Unexpected error: " + e));
        } finally {
            run.clearParsingFile();
            run.fileFinished();
        }
    }

    private static FileError fileError(String relative, String message) {
        return new FileError(relative, message, ScanErrorClassifier.classify(message));
    }

    private void logProgress(int processed, int total) {
        if (processed % scanOptions.progressEvery() == 0 || processed == total) {
            System.err.println("[STANDALONE] " + processed + "/" + total + " files processed");
        }
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
        int[] skippedIgnoredDirs = {0};
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (!dir.equals(root)) {
                    if (shouldSkipAsBuildOutputDirectory(root, dir, scan.applyDefaultIgnoredDirectories())) {
                        skippedIgnoredDirs[0]++;
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    Path probe = dir.resolve("__analyzer_probe__.java");
                    if (!filter.accept(root, probe)) {
                        skippedIgnoredDirs[0]++;
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                }
                return FileVisitResult.CONTINUE;
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
        if (skippedIgnoredDirs[0] > 0) {
            System.err.println("[STANDALONE] Skipped " + skippedIgnoredDirs[0]
                    + " directory subtree(s) (build outputs / exclude globs)");
        }
        return new FileDiscovery(result, skipped[0]);
    }

    /**
     * Skip {@code target/build/dist/...} trees but not package segments such as
     * {@code src/main/java/com/acme/build/util}.
     */
    static boolean shouldSkipAsBuildOutputDirectory(Path scanRoot, Path dir, boolean applyDefaultIgnores) {
        if (!applyDefaultIgnores) {
            return false;
        }
        Path name = dir.getFileName();
        if (name == null || !IGNORED_DIRECTORIES.contains(name.toString())) {
            return false;
        }
        String relative = scanRoot.relativize(dir).toString().replace('\\', '/');
        if (isUnderJavaSourceTree(relative)) {
            return false;
        }
        Path parent = dir.getParent();
        return parent != null && isModuleBoundaryDirectory(scanRoot, parent);
    }

    static boolean isModuleBoundaryDirectory(Path scanRoot, Path directory) {
        if (directory.equals(scanRoot)) {
            return true;
        }
        return hasBuildMarker(directory)
                || Files.isDirectory(directory.resolve("src/main/java"))
                || Files.isDirectory(directory.resolve("src"));
    }

    private static boolean hasBuildMarker(Path directory) {
        return Files.isRegularFile(directory.resolve("pom.xml"))
                || Files.isRegularFile(directory.resolve("build.gradle"))
                || Files.isRegularFile(directory.resolve("build.gradle.kts"));
    }

    private Charset sourceCharset() {
        Charset encoding = parserConfiguration.getCharacterEncoding();
        return encoding != null ? encoding : java.nio.charset.StandardCharsets.UTF_8;
    }

    static boolean isUnderJavaSourceTree(String relativeUnixPath) {
        return relativeUnixPath.contains("/src/main/java/")
                || relativeUnixPath.contains("/src/test/java/")
                || relativeUnixPath.startsWith("src/main/java/")
                || relativeUnixPath.startsWith("src/test/java/");
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

    private FileMetric buildFileMetric(String relativePath, CompilationUnit cu, ScanAccumulator summaryAccumulator) {
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

        if (summaryAccumulator != null) {
            summaryAccumulator.ingestFile(relativePath, packageName, classes, loc);
            return slimFileMetric(relativePath, packageName, physicalLines, loc, classes.size(), methods.size(),
                    wmc, maxCc, risk);
        }

        return new FileMetric(relativePath, packageName, physicalLines, loc, classes.size(), methods.size(),
                wmc, maxCc, risk.score(), risk.level(), risk.factors(), classes);
    }

    private static FileMetric slimFileMetric(String relativePath, String packageName, int physicalLines, int loc,
                                             int classCount, int methodCount, int wmc, int maxCc,
                                             RiskCalculator.Assessment risk) {
        return new FileMetric(relativePath, packageName, physicalLines, loc, classCount, methodCount,
                wmc, maxCc, risk.score(), risk.level(), risk.factors(), List.of());
    }

    // ------------------------------------------------------------------ report-level aggregation

    private Summary buildSummary(int scanned, List<FileMetric> files, List<FileError> errors,
                                 ModuleRootIndex moduleRoots) {
        ProjectSummaryStats stats = new ProjectSummaryStats();
        int classCount = 0;
        int totalLoc = 0;
        for (FileMetric file : files) {
            totalLoc += file.codeLines();
            classCount += file.classCount();
            String moduleRoot = moduleRoots.moduleRoot(file.path());
            for (ClassMetric type : file.classes()) {
                for (MethodMetric method : type.methods()) {
                    stats.addMethod(moduleRoot, method);
                }
            }
        }
        return stats.toSummary(scanned, files.size(), errors.size(), classCount, totalLoc);
    }

    private List<RiskHotspot> buildHotspots(List<FileMetric> files) {
        List<RiskHotspot> hotspots = new ArrayList<>();
        for (FileMetric file : files) {
            for (ClassMetric type : file.classes()) {
                for (MethodMetric m : type.methods()) {
                    hotspots.add(new RiskHotspot(file.path(), file.packageName(), type.name(), m.signature(),
                            m.startLine(), m.riskScore(), m.riskLevel(), m.cyclomaticComplexity(), m.codeLines(),
                            m.maxNestingDepth(), m.parameterCount(), m.cognitiveComplexity(),
                            m.outboundDistinctCalls(), RiskBreakdownUtil.dominantDriver(m.riskBreakdown()),
                            m.riskFactors()));
                }
            }
        }
        hotspots.sort((a, b) -> {
            int byScore = Double.compare(b.riskScore(), a.riskScore());
            return byScore != 0 ? byScore : Integer.compare(b.cyclomaticComplexity(), a.cyclomaticComplexity());
        });
        return hotspots.size() > topN ? new ArrayList<>(hotspots.subList(0, topN)) : hotspots;
    }

    private static Thread newWorkerThread(Runnable task) {
        int id = WORKER_ID.incrementAndGet();
        return new Thread(null, task, "standalone-parser-" + id, WORKER_STACK_BYTES);
    }
}
