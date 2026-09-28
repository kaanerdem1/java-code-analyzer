package com.standalone.analyzer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.javaparser.ParserConfiguration;

/**
 * Entry point.
 *
 * <pre>
 *   java -jar java-code-analyzer.jar --path=/path/to/java6/src [--output=report.json]
 *        [--top=20] [--encoding=UTF-8] [--compact] [--verbose] [--markdown=report.md]
 * </pre>
 * JSON goes to stdout (or --output); progress and diagnostics go to stderr.
 * Exit codes: 0 = success, 1 = invalid arguments or fatal I/O error.
 */
public final class Java6CodeAnalyzerMain {

    private static final int DEFAULT_TOP = 20;

    private Java6CodeAnalyzerMain() {
    }

    public static void main(String[] args) {
        System.exit(run(args));
    }

    static int run(String[] args) {
        Options options;
        try {
            options = Options.parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println("[ERROR] " + e.getMessage());
            printUsage();
            return 1;
        }
        if (options.help()) {
            printUsage();
            return 0;
        }

        try {
            if (!Files.exists(options.path()) || !Files.isReadable(options.path())) {
                System.err.println("[ERROR] Path does not exist or is not readable: " + options.path());
                return 1;
            }

            AnalysisConsoleLogger.logRunHeader(
                    new AnalysisConsoleLogger.PathLabel(options.path().toAbsolutePath().normalize().toString()),
                    options.verbose());

            AnalysisReport report = new ProjectAnalyzer(options.charset(), options.top(), options.languageLevel())
                    .analyze(options.path());
            writeJson(report, options);
            writeMarkdownIfRequested(report, options);

            String jsonPath = options.output() != null
                    ? options.output().toAbsolutePath().normalize().toString()
                    : null;
            AnalysisConsoleLogger.logSummary(report, jsonPath);
            AnalysisConsoleLogger.logHotspots(report.topRiskyMethods(), options.top());
            AnalysisConsoleLogger.logParseErrors(report.errors());
            if (options.verbose()) {
                AnalysisConsoleLogger.logVerboseMethods(report.files());
            }

            AnalysisReport.Summary s = report.summary();
            System.err.println("[STANDALONE] Done: " + s.filesParsed() + " parsed, " + s.filesFailed() + " failed, "
                    + s.methodCount() + " methods, project risk " + s.projectRiskScore()
                    + " (" + s.projectRiskLevel() + ")");
            return 0;
        } catch (IOException e) {
            System.err.println("[ERROR] I/O failure: " + e.getMessage());
            return 1;
        } catch (RuntimeException e) {
            System.err.println("[ERROR] Unexpected failure: " + e);
            return 1;
        }
    }

    private static void writeMarkdownIfRequested(AnalysisReport report, Options options) throws IOException {
        if (options.markdown() == null) {
            return;
        }
        Path parent = options.markdown().toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(options.markdown(), StandaloneReportMarkdown.render(report), StandardCharsets.UTF_8);
        System.err.println("[STANDALONE] Readable Markdown: " + options.markdown().toAbsolutePath().normalize());
    }

    private static void writeJson(AnalysisReport report, Options options) throws IOException {
        GsonBuilder builder = new GsonBuilder().disableHtmlEscaping();
        if (!options.compact()) {
            builder.setPrettyPrinting();
        }
        Gson gson = builder.create();
        String json = gson.toJson(report);

        if (options.output() != null) {
            Path parent = options.output().toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(options.output(), json, StandardCharsets.UTF_8);
            System.err.println("[STANDALONE] Report written to " + options.output().toAbsolutePath());
        } else {
            PrintStream out = new PrintStream(new FileOutputStream(FileDescriptor.out), false, StandardCharsets.UTF_8);
            out.println(json);
            out.flush();
        }
    }

    private static void printUsage() {
        System.err.println("""
                Standalone Java Code Analyzer

                Usage:
                  java -jar java-code-analyzer.jar --path=<dir|file> [options]

                Options:
                  --path=<dir|file>   Source root (required): proje kökü veya src/main/java
                  --language-level=<n>  Parse modu: JAVA_17 (varsayılan), JAVA_11, JAVA_6, 17, 6 ...
                  --output=<file>     Write JSON to file instead of stdout
                  --top=<n>           Number of risk hotspots in the report (default 20)
                  --encoding=<name>   Source encoding (default UTF-8; e.g. ISO-8859-9, windows-1254)
                  --compact           Single-line JSON
                  --verbose           List every method with riskBreakdown on stderr
                  --markdown=<file>   Also write human-readable Markdown report (Türkçe)
                  --help              Show this help
                """);
    }

    private record Options(Path path, Path output, Path markdown, int top, Charset charset,
                           ParserConfiguration.LanguageLevel languageLevel, boolean compact, boolean verbose,
                           boolean help) {

        static Options parse(String[] args) {
            Path path = null;
            Path output = null;
            Path markdown = null;
            int top = DEFAULT_TOP;
            Charset charset = StandardCharsets.UTF_8;
            ParserConfiguration.LanguageLevel languageLevel = LanguageLevelOption.DEFAULT;
            boolean compact = false;
            boolean verbose = false;
            boolean help = false;

            for (String arg : args) {
                if (arg.equals("--help") || arg.equals("-h")) {
                    help = true;
                } else if (arg.equals("--verbose") || arg.equals("-v")) {
                    verbose = true;
                } else if (arg.equals("--compact")) {
                    compact = true;
                } else if (arg.startsWith("--path=")) {
                    path = Paths.get(value(arg));
                } else if (arg.startsWith("--output=")) {
                    output = Paths.get(value(arg));
                } else if (arg.startsWith("--markdown=")) {
                    markdown = Paths.get(value(arg));
                } else if (arg.startsWith("--top=")) {
                    top = parseTop(value(arg));
                } else if (arg.startsWith("--encoding=")) {
                    charset = Charset.forName(value(arg));
                } else if (arg.startsWith("--language-level=")) {
                    languageLevel = LanguageLevelOption.parse(value(arg));
                } else {
                    throw new IllegalArgumentException("Unknown argument: " + arg);
                }
            }
            if (!help && path == null) {
                throw new IllegalArgumentException("Missing required argument --path=<dir>");
            }
            return new Options(path, output, markdown, top, charset, languageLevel, compact, verbose, help);
        }

        private static String value(String arg) {
            String value = arg.substring(arg.indexOf('=') + 1).trim();
            if (value.isEmpty()) {
                throw new IllegalArgumentException("Empty value for " + arg);
            }
            return value;
        }

        private static int parseTop(String raw) {
            try {
                int parsed = Integer.parseInt(raw);
                if (parsed < 1) {
                    throw new IllegalArgumentException("--top must be >= 1");
                }
                return parsed;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("--top must be an integer: " + raw);
            }
        }
    }
}
