package com.standalone.analyzer;

import com.github.javaparser.ParserConfiguration;
import com.standalone.analyzer.AnalysisReport.ClassMetric;
import com.standalone.analyzer.AnalysisReport.FileMetric;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTConstructorDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTExecutableDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTTypeDeclaration;
import net.sourceforge.pmd.lang.java.metrics.JavaMetrics;
import net.sourceforge.pmd.lang.metrics.Metric;
import net.sourceforge.pmd.lang.metrics.MetricOptions;

import java.util.ArrayList;
import java.util.List;

/** Tek bir kaynak dosyadan PMD metrikleri ile parser-benzeri {@link FileMetric} üretir. */
final class PmdCompilationUnitMetrics {

    private PmdCompilationUnitMetrics() {
    }

    static FileMetric build(String relativePath, String sourceText,
                            ParserConfiguration.LanguageLevel languageLevel) {
        return PmdMetricsAligner.withParseSession(languageLevel, session -> {
            try {
                ASTCompilationUnit unit = session.parse(sourceText, relativePath);
                return buildFromUnit(relativePath, unit);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    private static FileMetric buildFromUnit(String relativePath, ASTCompilationUnit unit) {
        String packageName = unit.getPackageName() == null ? "" : unit.getPackageName();
        MetricOptions options = MetricOptions.emptyOptions();
        List<ClassMetric> classes = new ArrayList<>();
        for (ASTTypeDeclaration type : unit.getTypeDeclarations().toList()) {
            classes.add(buildType(type, packageName, options));
        }
        List<MethodMetric> allMethods = classes.stream().flatMap(c -> c.methods().stream()).toList();
        int wmc = allMethods.stream().mapToInt(MethodMetric::cyclomaticComplexity).sum();
        int maxCc = allMethods.stream().mapToInt(MethodMetric::cyclomaticComplexity).max().orElse(0);
        int loc = allMethods.stream().mapToInt(MethodMetric::ncssCount).sum();
        return new FileMetric(relativePath, packageName, unit.getEndLine(), loc, classes.size(), allMethods.size(),
                wmc, maxCc, 0.0, RiskLevel.LOW, List.of(), classes,
                HalsteadMetrics.EMPTY, 0, 0, 0, 0, 0, "", false, 0);
    }

    private static ClassMetric buildType(ASTTypeDeclaration type, String packageName, MetricOptions options) {
        // Parser JSON ile aynı: sınıf adı basit isim (paket file.packageName'de).
        String fqName = type.getSimpleName();
        List<MethodMetric> methods = new ArrayList<>();
        for (ASTExecutableDeclaration exec : type.getOperations().toList()) {
            methods.add(buildMethod(exec, options));
        }
        int wmc = methods.stream().mapToInt(MethodMetric::cyclomaticComplexity).sum();
        int maxCc = methods.stream().mapToInt(MethodMetric::cyclomaticComplexity).max().orElse(0);
        double avg = methods.isEmpty() ? 0.0 : Math.round(wmc * 100.0 / methods.size()) / 100.0;
        int start = type.getBeginLine();
        int end = type.getEndLine();
        int loc = methods.stream().mapToInt(MethodMetric::ncssCount).sum();
        return new ClassMetric(fqName, typeKind(type), start, end, methods.size(), 0, loc, 0, wmc, maxCc, avg,
                0.0, RiskLevel.LOW, List.of(), methods, HalsteadMetrics.EMPTY, List.of(), 0.0, false,
                0, 0, 0, 0, 0);
    }

    private static MethodMetric buildMethod(ASTExecutableDeclaration exec, MetricOptions options) {
        int cyclo = Metric.compute(JavaMetrics.CYCLO, exec, options);
        int cognitive = Metric.compute(JavaMetrics.COGNITIVE_COMPLEXITY, exec, options);
        int ncss = Metric.compute(JavaMetrics.NCSS, exec, options);
        int npath = PmdMetricsAligner.capNpath(Metric.compute(JavaMetrics.NPATH, exec, options));
        String name = exec.getName();
        String kind = exec instanceof ASTConstructorDeclaration ? "CONSTRUCTOR" : "METHOD";
        String signature = exec.getName() + "(" + exec.getArity() + ")";
        int start = exec.getBeginLine();
        int end = exec.getEndLine();
        int physical = Math.max(0, end - start + 1);
        return MethodMetric.withoutLegacyExtensions(
                name, kind, signature, returnType(exec), start, end,
                cyclo, physical, ncss, 0, cognitive, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                false, 0.0, RiskLevel.LOW, List.of(), null, "", "", npath, ncss);
    }

    private static String returnType(ASTExecutableDeclaration exec) {
        if (exec instanceof ASTMethodDeclaration method) {
            if (method.isVoid()) {
                return "void";
            }
            return method.getResultTypeNode().getTypeMirror().toString();
        }
        return "";
    }

    private static String typeKind(ASTTypeDeclaration type) {
        return switch (type.getXPathNodeName()) {
            case "InterfaceDeclaration" -> "INTERFACE";
            case "EnumDeclaration" -> "ENUM";
            case "RecordDeclaration" -> "RECORD";
            case "AnnotationTypeDeclaration" -> "ANNOTATION";
            default -> "CLASS";
        };
    }
}
