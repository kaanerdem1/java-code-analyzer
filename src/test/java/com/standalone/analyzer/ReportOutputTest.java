package com.standalone.analyzer;

import com.github.javaparser.ParserConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportOutputTest {

    @TempDir
    Path temp;

    @Test
    void jsonStreamOmitsHierarchyFields() throws Exception {
        Path root = Path.of("mock-modules").toAbsolutePath().normalize();
        var analyzer = new ProjectAnalyzer(StandardCharsets.UTF_8, 5, ParserConfiguration.LanguageLevel.JAVA_17,
                new ScanOptions(1, 500, java.util.List.of(), java.util.List.of(), ScanOptions.ReportDetail.FULL, true,
                        null, false));
        AnalysisReport report = analyzer.analyze(root);

        StringWriter jsonOut = new StringWriter();
        AnalysisReportJsonWriter.write(report, jsonOut, true);
        String json = jsonOut.toString();
        assertFalse(json.contains("ancestorPath"));
        assertTrue(json.contains("moduleSummaries"));
        assertTrue(json.contains("\"methods\""));
    }

    @Test
    void markdownFromJsonIncludesAllRiskLevels() throws Exception {
        Path root = Path.of("mock-modules").toAbsolutePath().normalize();
        Path jsonFile = temp.resolve("report.json");
        var analyzer = new ProjectAnalyzer(StandardCharsets.UTF_8, 5, ParserConfiguration.LanguageLevel.JAVA_17,
                new ScanOptions(1, 500, java.util.List.of(), java.util.List.of(), ScanOptions.ReportDetail.FULL, true,
                        null, false));
        AnalysisReport report = analyzer.analyze(root);
        try (var w = Files.newBufferedWriter(jsonFile, StandardCharsets.UTF_8)) {
            AnalysisReportJsonWriter.write(report, w, false);
        }

        StringWriter md = new StringWriter();
        try (var r = Files.newBufferedReader(jsonFile, StandardCharsets.UTF_8)) {
            StandaloneReportMarkdown.renderFromJson(r, md);
        }
        String markdown = md.toString();
        assertTrue(markdown.contains("Metodlar (risk skoruna göre)"));
        assertTrue(markdown.contains("| DÜŞÜK |"));
        assertFalse(markdown.contains("En riskli metodlar"));
    }
}
