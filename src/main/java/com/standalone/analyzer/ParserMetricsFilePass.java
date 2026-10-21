package com.standalone.analyzer;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.standalone.analyzer.AnalysisReport.FileError;
import com.standalone.analyzer.AnalysisReport.FileMetric;
import com.standalone.analyzer.RiskCalculator.Assessment;
import com.standalone.analyzer.ScanOptions.ReportDetail;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/** JavaParser + {@link FileMetricsBuilder}; PMD CC/cog/NCSS/NPath hizasi acik. */
final class ParserMetricsFilePass {

    private ParserMetricsFilePass() {
    }

    static List<FileMetric> scan(List<Path> sources, Path base, Charset charset,
                                 ParserConfiguration.LanguageLevel languageLevel, ScanOptions scanOptions,
                                 RiskCalculator riskCalculator, List<FileError> errors, String logPrefix)
            throws IOException {
        if (sources.isEmpty()) {
            return List.of();
        }
        List<FileMetric> files = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger processed = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(scanOptions.workers());
        try {
            List<Callable<Void>> tasks = new ArrayList<>();
            for (Path source : sources) {
                tasks.add(() -> {
                    parseOne(source, base, charset, languageLevel, scanOptions, riskCalculator, files, errors);
                    int n = processed.incrementAndGet();
                    if (n % scanOptions.progressEvery() == 0 || n == sources.size()) {
                        System.err.println(logPrefix + n + "/" + sources.size() + " files");
                    }
                    return null;
                });
            }
            for (Future<Void> future : pool.invokeAll(tasks)) {
                future.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Parser metrics pass interrupted", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Parser metrics pass failed", e.getCause());
        } finally {
            pool.shutdown();
        }
        List<FileMetric> sorted = files.stream().sorted(Comparator.comparing(FileMetric::path)).collect(Collectors.toList());
        if (scanOptions.reportDetail() == ReportDetail.SUMMARY) {
            return sorted.stream().map(ParserMetricsFilePass::summaryStub).collect(Collectors.toList());
        }
        return sorted;
    }

    private static void parseOne(Path source, Path base, Charset charset,
                                 ParserConfiguration.LanguageLevel languageLevel, ScanOptions scanOptions,
                                 RiskCalculator riskCalculator, List<FileMetric> files,
                                 List<FileError> errors) {
        String relative = JavaSourceDiscovery.relativePath(base, source);
        try {
            String text = SourceEncoding.read(source, charset);
            SourceParser.Outcome outcome = SourceParser.parse(source, charset, languageLevel);
            ParseResult<CompilationUnit> result = outcome.result();
            if (result.isSuccessful() && result.getResult().isPresent()) {
                CompilationUnit cu = result.getResult().get();
                String fileHash = HashService.hashFile(source);
                FileMetric full = FileMetricsBuilder.build(relative, text, outcome.usedLevel(), cu, riskCalculator,
                        Map.of(), null, fileHash, false, scanOptions.pmdMetricAlignment());
                if (scanOptions.detectDuplicates()) {
                    full = ParserDuplicateHashEnrichment.enrich(source, relative, text, full, charset,
                            languageLevel).get(0);
                }
                files.add(full);
            } else {
                errors.add(new FileError(relative, "Parse error"));
            }
        } catch (Exception e) {
            errors.add(new FileError(relative, e.getMessage()));
        }
    }

    private static FileMetric summaryStub(FileMetric metric) {
        return FileMetricsBuilder.buildSummaryStub(metric.path(), metric.packageName(), metric.physicalLines(),
                metric.codeLines(), metric.classCount(), metric.methodCount(), metric.totalCyclomaticComplexity(),
                metric.maxCyclomaticComplexity(),
                new Assessment(metric.riskScore(), metric.riskLevel(), metric.riskFactors(), null),
                metric.fileHash());
    }
}
