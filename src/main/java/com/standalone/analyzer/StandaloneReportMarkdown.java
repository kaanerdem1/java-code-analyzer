package com.standalone.analyzer;

import com.standalone.analyzer.AnalysisReport.ClassMetric;
import com.standalone.analyzer.AnalysisReport.FileMetric;
import com.standalone.analyzer.AnalysisReport.RiskHotspot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Türkçe, okunabilir standalone (parser) Markdown raporu. */
final class StandaloneReportMarkdown {

    private StandaloneReportMarkdown() {
    }

    static String render(AnalysisReport report) {
        StringBuilder md = new StringBuilder();
        AnalysisReport.Summary s = report.summary();

        md.append("# Parser analiz raporu\n\n");
        md.append("Bu rapor **Java kaynak kodunu** tarayıp her **metod** için karmaşıklık ve **teknik risk** özetler.\n\n");

        md.append("Her metod satırında **ata zinciri** vardır: `modül › dosya › paket.Sınıf › metod(...)` — en alttaki satır metod, üsttekiler yeri kaybetmemeniz içindir.\n\n");

        md.append("## Kısa sözlük\n\n");
        md.append("- **Dallanma karmaşıklığı (CC):** Metod içinde kaç farklı karar/yol var ");
        md.append("(if, else, for, while, catch, `&&`, `||` vb.). Yüksek = test etmesi zor.\n");
        md.append("- **Kod satırı:** Metod gövdesindeki gerçek kod satırları (boş satır ve sadece yorum sayılmaz).\n");
        md.append("- **İç içe derinlik:** Blokların iç içe kaç kat olduğu (if içinde for içinde if…).\n");
        md.append("- **Risk skoru (0–1):** CC, satır sayısı, iç içe yapı ve parametre sayısından üretilen ");
        md.append("birleşik puan. **0’a yakın = sakin**, **1’e yakın = dikkat**.\n");
        md.append("- **Risk seviyesi:** DÜŞÜK / ORTA / YÜKSEK / KRİTİK (skora göre bant).\n");
        md.append("- **Dev metod:** Çok uzun veya çok karmaşık metod uyarısı (heuristik).\n\n");

        md.append("## Tarama özeti\n\n");
        md.append("| Alan | Değer |\n|------|-------|\n");
        md.append("| Tarih | ").append(report.generatedAt()).append(" |\n");
        md.append("| Taranan klasör | `").append(report.analyzedPath()).append("` |\n");
        md.append("| Java sürümü (parse) | ").append(report.parserLanguageLevel()).append(" |\n");
        md.append("| Dosya (tarandı / okundu / hata) | ")
                .append(s.filesScanned()).append(" / ").append(s.filesParsed()).append(" / ")
                .append(s.filesFailed()).append(" |\n");
        md.append("| Sınıf / metod sayısı | ").append(s.classCount()).append(" / ").append(s.methodCount()).append(" |\n");
        md.append("| Toplam kod satırı | ").append(s.totalCodeLines()).append(" |\n");
        md.append("| Ortalama / en yüksek dallanma (CC) | ")
                .append(fmt(s.averageCyclomaticComplexity())).append(" / ")
                .append(s.maxCyclomaticComplexity()).append(" |\n");
        md.append("| Dev metod sayısı | ").append(s.godMethodCount()).append(" |\n");
        md.append("| **Proje risk skoru** | **").append(fmt(s.projectRiskScore()))
                .append(" (").append(levelTr(s.projectRiskLevel())).append(")** |\n\n");

        Map<RiskLevel, Long> dist = s.methodRiskDistribution();
        md.append("### Metodların risk dağılımı\n\n");
        md.append("| DÜŞÜK | ORTA | YÜKSEK | KRİTİK |\n|-------|------|--------|--------|\n");
        md.append("| ").append(dist.get(RiskLevel.LOW)).append(" | ")
                .append(dist.get(RiskLevel.MEDIUM)).append(" | ")
                .append(dist.get(RiskLevel.HIGH)).append(" | ")
                .append(dist.get(RiskLevel.CRITICAL)).append(" |\n\n");

        md.append("## En riskli metodlar\n\n");
        md.append("| Sıra | Ata zinciri (modül › … › metod) | Risk | Seviye | CC | LOC | İç içe | Satır |\n");
        md.append("|-----:|----------------------------------|-----:|--------|---:|----:|-------:|------:|\n");
        int i = 1;
        for (RiskHotspot h : report.topRiskyMethods()) {
            String chain = h.ancestorPath() == null || h.ancestorPath().isEmpty()
                    ? h.file() + " › " + h.className() + " › " + h.method()
                    : MethodHierarchy.breadcrumb(h.ancestorPath());
            md.append("| ").append(i++).append(" | `").append(escapeCell(chain)).append("` | ")
                    .append(fmt(h.riskScore())).append(" | ")
                    .append(levelTr(h.riskLevel())).append(" | ")
                    .append(h.cyclomaticComplexity())
                    .append(" | ").append(h.codeLines()).append(" | ").append(h.maxNestingDepth())
                    .append(" | ").append(h.startLine()).append(" |\n");
        }
        md.append("\n");

        List<MethodRow> rows = flattenMethods(report.files());
        rows.sort(Comparator.comparingDouble(MethodRow::score).reversed());

        md.append("## Tüm metodlar (risk skoruna göre)\n\n");
        if (rows.isEmpty()) {
            md.append("*Özet mod (`--detail=summary`): metod listesi JSON'da kısaltıldı; yukarıdaki hotspot tablosuna bakın.*\n\n");
        } else {
        md.append("Alt skorlar 0–1 arası; hangi boyut riski artırdıysa o yüksek çıkar.\n\n");
        md.append("| Risk | Seviye | Ata zinciri | CC | LOC | İç içe | Dev? | Katkılar (d/s/i/p) |\n");
        md.append("|-----:|--------|-------------|---:|----:|-------:|:----:|-------------------|\n");
        for (MethodRow r : rows) {
            RiskBreakdown b = r.breakdown();
            String subs = b == null ? "—"
                    : fmt(b.ccSubScore()) + " / " + fmt(b.locSubScore()) + " / "
                    + fmt(b.nestingSubScore()) + " / " + fmt(b.paramsSubScore());
            md.append("| ").append(fmt(r.score())).append(" | ").append(levelTr(r.level())).append(" | `")
                    .append(escapeCell(r.ancestorLabel())).append("` | ")
                    .append(r.cc()).append(" | ").append(r.loc()).append(" | ").append(r.nest()).append(" | ")
                    .append(r.god() ? "evet" : "hayır").append(" | ").append(subs).append(" |\n");
        }
        md.append("\n");
        }

        if (!report.errors().isEmpty()) {
            md.append("## Okunamayan dosyalar\n\n");
            for (AnalysisReport.FileError e : report.errors()) {
                md.append("- `").append(e.file()).append("`: ").append(e.message()).append("\n");
            }
            md.append("\n");
        }

        md.append("---\n");
        md.append("*PMD ile karşılaştırmak için `pmd-raporu.md` dosyasına bakın; CC sayıları yakın olmalı, ");
        md.append("risk skoru ise sadece bu parser aracına özeldir.*\n");
        return md.toString();
    }

    private static String levelTr(RiskLevel level) {
        return switch (level) {
            case LOW -> "DÜŞÜK";
            case MEDIUM -> "ORTA";
            case HIGH -> "YÜKSEK";
            case CRITICAL -> "KRİTİK";
        };
    }

    private static List<MethodRow> flattenMethods(List<FileMetric> files) {
        List<MethodRow> rows = new ArrayList<>();
        for (FileMetric file : files) {
            for (ClassMetric type : file.classes()) {
                for (MethodMetric m : type.methods()) {
                    String label = m.ancestorPath() == null || m.ancestorPath().isEmpty()
                            ? file.path() + " › " + m.signature()
                            : MethodHierarchy.breadcrumb(m.ancestorPath());
                    rows.add(new MethodRow(label, m.riskScore(), m.riskLevel(),
                            m.cyclomaticComplexity(), m.codeLines(), m.maxNestingDepth(), m.godMethod(),
                            m.riskBreakdown()));
                }
            }
        }
        return rows;
    }

    private static String fmt(double v) {
        return String.format(Locale.US, "%.3f", v);
    }

    private static String escapeCell(String s) {
        return s.replace("|", "\\|");
    }

    private record MethodRow(
            String ancestorLabel, double score, RiskLevel level,
            int cc, int loc, int nest, boolean god, RiskBreakdown breakdown) {
    }
}
