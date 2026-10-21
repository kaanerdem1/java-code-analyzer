package com.standalone.analyzer;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** PMD metrik taraması için parser-benzeri Türkçe Markdown (risk skoru yok). */
final class PmdScanReportMarkdown {

    private PmdScanReportMarkdown() {
    }

    static void write(AnalysisReport report, Writer out) throws IOException {
        StringBuilder md = new StringBuilder();
        md.append("# PMD metrik raporu (parser formatına yakın)\n\n");
        md.append("Kaynak: PMD `JavaMetrics` — CC, cognitive, NCSS, NPath. **Risk skoru hesaplanmaz.**\n\n");
        md.append("| Alan | Değer |\n|------|-------|\n");
        md.append("| Araç | ").append(report.tool()).append(" |\n");
        md.append("| Taranan yol | `").append(report.analyzedPath()).append("` |\n");
        md.append("| Dil seviyesi | ").append(report.parserLanguageLevel()).append(" |\n");
        AnalysisReport.Summary s = report.summary();
        md.append("| Dosya (parse) | ").append(s.filesParsed()).append(" |\n");
        md.append("| Metod | ").append(s.methodCount()).append(" |\n");
        md.append("| Ort. CC | ").append(s.averageCyclomaticComplexity()).append(" |\n");
        md.append("| Max CC | ").append(s.maxCyclomaticComplexity()).append(" |\n\n");

        md.append("## En yüksek CC (hotspot)\n\n");
        md.append("| CC | Cog | NPath | NCSS | Sınıf | Metod | Dosya | Satır |\n");
        md.append("|----:|----:|------:|-----:|-------|-------|-------|------:|\n");
        for (AnalysisReport.RiskHotspot h : report.topRiskyMethods()) {
            MethodMetric m = findMethod(report, h);
            md.append("| ").append(h.cyclomaticComplexity());
            md.append(" | ").append(h.cognitiveComplexity());
            if (m != null) {
                md.append(" | ").append(m.npathComplexity());
                md.append(" | ").append(m.ncssCount());
            } else {
                md.append(" | — | —");
            }
            md.append(" | `").append(h.className()).append("` | `").append(h.method()).append("` | `");
            md.append(h.file()).append("` | ").append(h.startLine()).append(" |\n");
        }
        md.append("\n");

        if (report.files().stream().anyMatch(f -> !f.classes().isEmpty())) {
            md.append("## Metodlar (CC azalan, ilk 50)\n\n");
            List<Row> rows = flatten(report);
            rows.sort(Comparator.comparingInt(Row::cc).reversed());
            int limit = Math.min(50, rows.size());
            md.append("| CC | Cog | NPath | NCSS | Sınıf | Metod | Dosya |\n");
            md.append("|----:|----:|------:|-----:|-------|-------|-------|\n");
            for (int i = 0; i < limit; i++) {
                Row r = rows.get(i);
                md.append("| ").append(r.cc()).append(" | ").append(r.cog());
                md.append(" | ").append(r.npath()).append(" | ").append(r.ncss());
                md.append(" | `").append(r.className()).append("` | `").append(r.method()).append("` | `");
                md.append(r.file()).append("` |\n");
            }
        }
        md.append("\n---\n*Risk skoru ve duplicate için parser taraması (`run-analyze.sh`) kullanın.*\n");
        out.write(md.toString());
    }

    private record Row(int cc, int cog, int npath, int ncss, String className, String method, String file) {
    }

    private static List<Row> flatten(AnalysisReport report) {
        List<Row> rows = new ArrayList<>();
        for (AnalysisReport.FileMetric file : report.files()) {
            for (AnalysisReport.ClassMetric type : file.classes()) {
                for (MethodMetric m : type.methods()) {
                    rows.add(new Row(m.cyclomaticComplexity(), m.cognitiveComplexity(), m.npathComplexity(),
                            m.ncssCount(), type.name(), m.name(), file.path()));
                }
            }
        }
        return rows;
    }

    private static MethodMetric findMethod(AnalysisReport report, AnalysisReport.RiskHotspot h) {
        for (AnalysisReport.FileMetric file : report.files()) {
            if (!file.path().equals(h.file())) {
                continue;
            }
            for (AnalysisReport.ClassMetric type : file.classes()) {
                if (!type.name().equals(h.className())) {
                    continue;
                }
                for (MethodMetric m : type.methods()) {
                    if (m.name().equals(h.method()) && m.startLine() == h.startLine()) {
                        return m;
                    }
                }
            }
        }
        return null;
    }
}
