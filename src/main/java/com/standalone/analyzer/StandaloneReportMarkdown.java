package com.standalone.analyzer;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.standalone.analyzer.AnalysisReport.ClassMetric;
import com.standalone.analyzer.AnalysisReport.FileMetric;
import com.standalone.analyzer.AnalysisReport.RiskHotspot;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Türkçe, okunabilir standalone (parser) Markdown raporu. */
final class StandaloneReportMarkdown {

    private StandaloneReportMarkdown() {
    }

    static String render(AnalysisReport report) {
        StringWriter out = new StringWriter();
        try {
            write(report, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return out.toString();
    }

    static void write(AnalysisReport report, Writer out) throws IOException {
        ModuleRootIndex moduleRoots = ModuleRootIndex.forScanRoot(Path.of(report.analyzedPath()));
        AncestorIndex ancestors = new AncestorIndex();
        writeHeader(out, report.tool(), report.generatedAt(), report.analyzedPath(), report.parserLanguageLevel(),
                report.riskModel(), report.summary());
        writeHotspotSection(out, moduleRoots, report.topRiskyMethods(), ancestors);
        writeMethodTableFromMetrics(out, moduleRoots, report.files(), ancestors);
        writeAncestorAppendix(out, ancestors);
        writeErrors(out, report.errors());
        writeFooter(out);
    }

    /** JSON'u belleğe komple almadan Markdown üretir (dosya dosya {@code files} dizisi). */
    static void renderFromJson(Reader json, Writer out) throws IOException {
        JsonReader reader = new JsonReader(json);
        JsonMeta meta = new JsonMeta();
        meta.ancestors = new AncestorIndex();
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "tool" -> meta.tool = reader.nextString();
                case "generatedAt" -> meta.generatedAt = reader.nextString();
                case "analyzedPath" -> meta.analyzedPath = reader.nextString();
                case "parserLanguageLevel" -> meta.parserLanguageLevel = reader.nextString();
                case "riskModel" -> meta.riskModel = readRiskModelJson(reader);
                case "summary" -> meta.summary = readSummaryJson(reader);
                case "topRiskyMethods" -> meta.hotspots = readHotspotsJson(reader);
                case "files" -> {
                    meta.filesSectionSeen = true;
                    flushHeader(out, meta);
                    streamMethodTableFromFilesJson(reader, out, moduleRoots(meta.analyzedPath), meta.ancestors);
                }
                case "errors" -> meta.errors = readErrorsJson(reader);
                default -> skipValue(reader);
            }
        }
        reader.endObject();
        flushHeader(out, meta);
        if (!meta.filesSectionSeen) {
            writeMethodTableIntro(out, true);
        }
        writeAncestorAppendix(out, meta.ancestors);
        writeErrors(out, meta.errors);
        writeFooter(out);
    }

    private static void flushHeader(Writer out, JsonMeta meta) throws IOException {
        if (meta.headerWritten || meta.summary == null) {
            return;
        }
        writeHeader(out, meta.tool, meta.generatedAt, meta.analyzedPath, meta.parserLanguageLevel,
                meta.riskModel, meta.summary);
        writeHotspotSection(out, moduleRoots(meta.analyzedPath), meta.hotspots, meta.ancestors);
        meta.headerWritten = true;
    }

    private static ModuleRootIndex moduleRoots(String analyzedPath) {
        if (analyzedPath == null || analyzedPath.isBlank()) {
            return ModuleRootIndex.forScanRoot(Path.of("."));
        }
        return ModuleRootIndex.forScanRoot(Path.of(analyzedPath));
    }

    private static void writeHeader(Writer out, String tool, String generatedAt, String analyzedPath,
                                    String parserLevel, AnalysisReport.RiskModel riskModel,
                                    AnalysisReport.Summary s) throws IOException {
        out.write("# Parser analiz raporu\n\n");
        out.write("Bu rapor **Java kaynak kodunu** tarayıp her **metod** için karmaşıklık ve **teknik risk** özetler.\n\n");
        out.write("Tablolarda **M1**, **M2** … kodları ve kısa metod adı görünür; tam yol ");
        out.write("(modül › dosya › sınıf › metod) rapor sonunda **Metod ata zincirleri** bölümündedir.\n\n");
        if (riskModel != null) {
            out.write(describeRiskModelTr(riskModel) + "\n\n");
        }
        out.write("## Bu tablolarda ne görüyorsun?\n\n");
        out.write("### Sayılar (metodun kendisi)\n\n");
        out.write("- **Dallanma (CC):** Kaç farklı karar yolu var — `if`, `for`, `catch`, `&&` vb.\n");
        out.write("- **Kod satırı (LOC):** Metod gövdesindeki gerçek kod satırı (boş ve yorum hariç).\n");
        out.write("- **İç içe:** Blokların en derin katmanı (if içinde for içinde if…).\n");
        out.write("- **Okunabilirlik:** Kodu kafada takip etme zorluğu; iç içe yapılar daha ağır sayılır.\n");
        out.write("- **Dış çağrı:** Metodun başka kaç farklı işe dokunduğu (repo çağrısı, `save`, `write` …). ");
        out.write("Orchestrator veya kalın yardımcı metodlarda yükselir.\n");
        out.write("- **Parametre:** Metod imzasındaki parametre adedi.\n\n");
        out.write("### Risk skoru ve seviye\n\n");
        out.write("- **Risk (0–100):** Her ölçü 0–100 alt skor üretir; **en kötü ölçü** ile **tüm ölçülerin ");
        out.write("ağırlıklı ortalaması** birleştirilir (profil katsayıları YAML’da). Skor **3 ondalık** ");
        out.write("(ör. 68,374).\n");
        out.write("- **80+:** KRİTİK bandı; 100 tavan değil, üst sınır — ayırt için ince farklar korunur.\n");
        out.write("- **Neden (dominant):** Skoru en çok hangi ölçü şişirdi — örneğin “dış çağrı” veya “dallanma”.\n");
        out.write("- **Dev metod:** Aşırı uzun veya aşırı karmaşık metod uyarısı.\n\n");
        out.write("DÜŞÜK riskli metodlar aşağıdaki büyük tabloda **kasıtlı olarak yok** (okunabilirlik). ");
        out.write("Lambda, catch kalitesi, switch vb. ek ayrıntılar **JSON raporunda** (`--output=…json`).\n\n");

        out.write("## Tarama özeti\n\n");
        out.write("| Alan | Değer |\n|------|-------|\n");
        out.write("| Tarih | " + nullSafe(generatedAt) + " |\n");
        out.write("| Taranan klasör | `" + nullSafe(analyzedPath) + "` |\n");
        out.write("| Java sürümü (parse) | " + nullSafe(parserLevel) + " |\n");
        out.write("| Dosya (tarandı / okundu / hata) | "
                + s.filesScanned() + " / " + s.filesParsed() + " / " + s.filesFailed() + " |\n");
        out.write("| Sınıf / metod sayısı | " + s.classCount() + " / " + s.methodCount() + " |\n");
        out.write("| Toplam kod satırı | " + s.totalCodeLines() + " |\n");
        out.write("| Ortalama / en yüksek dallanma (CC) | "
                + fmt(s.averageCyclomaticComplexity()) + " / " + s.maxCyclomaticComplexity() + " |\n");
        out.write("| Dev metod sayısı | " + s.godMethodCount() + " |\n");
        out.write("| **Proje risk skoru (LOC ort.)** | **" + fmt(s.projectRiskScore())
                + " (" + levelTr(s.projectRiskLevel()) + ")** |\n");
        out.write("| Metod risk p95 / p99 | " + fmt(s.methodRiskScoreP95()) + " / "
                + fmt(s.methodRiskScoreP99()) + " |\n");
        out.write("| YÜKSEK+KRİTİK metod LOC payı | " + fmt(s.highPlusCriticalLocRatio()) + " |\n");
        out.write("| KRİTİK metod / KLOC | " + fmt(s.criticalMethodsPerKloc()) + " |\n\n");

        Map<RiskLevel, Long> dist = s.methodRiskDistribution();
        out.write("### Metodların risk dağılımı\n\n");
        out.write("| DÜŞÜK | ORTA | YÜKSEK | KRİTİK |\n|-------|------|--------|--------|\n");
        out.write("| " + dist.get(RiskLevel.LOW) + " | " + dist.get(RiskLevel.MEDIUM) + " | "
                + dist.get(RiskLevel.HIGH) + " | " + dist.get(RiskLevel.CRITICAL) + " |\n\n");
        if (tool != null && !tool.isBlank()) {
            out.write("<!-- tool: " + tool + " -->\n\n");
        }
    }

    private static void writeHotspotSection(Writer out, ModuleRootIndex moduleRoots,
                                            List<RiskHotspot> hotspots, AncestorIndex ancestors)
            throws IOException {
        out.write("## En riskli metodlar\n\n");
        out.write("| Sıra | Konum | Risk | Seviye | Dallanma | Satır | İç içe | Okunabilirlik | Dış çağrı | Nedeni | Kaynak satırı |\n");
        out.write("|-----:|-------|-----:|--------|--------:|------:|-------:|--------------:|----------:|--------|-------------:|\n");
        int i = 1;
        for (RiskHotspot h : hotspots) {
            String chain = MethodHierarchy.breadcrumb(MethodHierarchy.ancestorPath(
                    moduleRoots, h.file(), h.packageName(), h.className(), h.method()));
            String compact = MethodHierarchy.compactMethodLabel(h.packageName(), h.className(), h.method());
            String ref = ancestors.register(chain);
            out.write("| " + i++ + " | " + formatTableLocation(ref, compact) + " | "
                    + fmt(h.riskScore()) + " | " + levelTr(h.riskLevel()) + " | "
                    + h.cyclomaticComplexity() + " | " + h.codeLines() + " | " + h.maxNestingDepth()
                    + " | " + h.cognitiveComplexity() + " | " + h.outboundDistinctCalls() + " | "
                    + escapeCell(RiskBreakdownUtil.driverLabelTr(h.dominantDriver())) + " | "
                    + h.startLine() + " |\n");
        }
        out.write("\n");
    }

    private static void writeMethodTableFromMetrics(Writer out, ModuleRootIndex moduleRoots,
                                                    List<FileMetric> files, AncestorIndex ancestors)
            throws IOException {
        List<MethodRow> rows = collectMediumPlusRows(moduleRoots, files, ancestors);
        rows.sort(Comparator.comparingDouble(MethodRow::score).reversed());
        writeMethodTableIntro(out, rows.isEmpty());
        for (MethodRow r : rows) {
            writeMethodRow(out, r);
        }
        if (!rows.isEmpty()) {
            out.write("\n");
        }
    }

    private static void writeMethodTableIntro(Writer out, boolean empty) throws IOException {
        out.write("## Metodlar (ORTA ve üzeri risk, skora göre)\n\n");
        if (empty) {
            out.write("*Özet mod (`--detail=summary`) veya bu eşikte metod yok; yukarıdaki hotspot tablosuna bakın.*\n\n");
            out.write("*DÜŞÜK riskli metodlar tabloda gösterilmez (bellek/okunabilirlik); tam liste için `--detail=full` JSON kullanın.*\n\n");
        } else {
            out.write("Sütunlar: **Okunabilirlik** ve **Dış çağrı** ham sayılar; **Nedeni** skoru en çok neyin ");
            out.write("artırdığı; son sütun her ölçünün 0–1 alt puanı (dallanma / uzunluk / iç içe / parametre / ");
            out.write("okunabilirlik / dış çağrı).\n\n");
            out.write("| Risk | Seviye | Konum | Dallanma | Satır | İç içe | Okunabilirlik | Dış çağrı | Nedeni | Dev? | Alt puanlar (6 ölçü) |\n");
            out.write("|-----:|--------|-------|--------:|------:|-------:|--------------:|----------:|--------|:----:|---------------------|\n");
        }
    }

    private static void writeMethodRow(Writer out, MethodRow r) throws IOException {
        RiskBreakdown b = r.breakdown();
        String subs = b == null ? "—"
                : fmt(b.ccSubScore()) + " / " + fmt(b.locSubScore()) + " / "
                + fmt(b.nestingSubScore()) + " / " + fmt(b.paramsSubScore());
        String cogSub = b == null ? "—" : fmt(b.cognitiveSubScore());
        String foutSub = b == null ? "—" : fmt(b.outboundSubScore());
        String subsExtended = subs + " / " + cogSub + " / " + foutSub;
        out.write("| " + fmt(r.score()) + " | " + levelTr(r.level()) + " | "
                + formatTableLocation(r.ref(), r.compactLabel()) + " | "
                + r.cc() + " | " + r.loc() + " | " + r.nest() + " | "
                + r.cogCount() + " | " + r.foutCount() + " | "
                + escapeCell(RiskBreakdownUtil.driverLabelTr(r.dominantDriver())) + " | "
                + (r.god() ? "evet" : "hayır") + " | " + subsExtended + " |\n");
    }

    private static List<MethodRow> collectMediumPlusRows(ModuleRootIndex moduleRoots, List<FileMetric> files,
                                                       AncestorIndex ancestors) {
        List<MethodRow> rows = new ArrayList<>();
        for (FileMetric file : files) {
            if (file.classes().isEmpty()) {
                continue;
            }
            for (ClassMetric type : file.classes()) {
                for (MethodMetric m : type.methods()) {
                    if (!includeInMarkdownTable(m.riskLevel())) {
                        continue;
                    }
                    String chain = MethodHierarchy.breadcrumb(MethodHierarchy.ancestorPath(
                            moduleRoots, file.path(), file.packageName(), type.name(), m.signature()));
                    String ref = ancestors.register(chain);
                    String compact = MethodHierarchy.compactMethodLabel(
                            file.packageName(), type.name(), m.signature());
                    rows.add(new MethodRow(ref, compact, m.riskScore(), m.riskLevel(),
                            m.cyclomaticComplexity(), m.codeLines(), m.maxNestingDepth(),
                            m.cognitiveComplexity(), m.outboundDistinctCalls(),
                            RiskBreakdownUtil.dominantDriver(m.riskBreakdown()), m.godMethod(),
                            m.riskBreakdown()));
                }
            }
        }
        return rows;
    }

    private static void streamMethodTableFromFilesJson(JsonReader reader, Writer out,
                                                       ModuleRootIndex moduleRoots, AncestorIndex ancestors)
            throws IOException {
        List<MethodRow> rows = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            readFileMetricsJson(reader, rows, moduleRoots, ancestors);
        }
        reader.endArray();
        rows.sort(Comparator.comparingDouble(MethodRow::score).reversed());
        writeMethodTableIntro(out, rows.isEmpty());
        for (MethodRow r : rows) {
            writeMethodRow(out, r);
        }
        if (!rows.isEmpty()) {
            out.write("\n");
        }
    }

    private static void readFileMetricsJson(JsonReader reader, List<MethodRow> rows,
                                            ModuleRootIndex moduleRoots, AncestorIndex ancestors)
            throws IOException {
        String path = "";
        String packageName = "";
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "path" -> path = reader.nextString();
                case "packageName" -> packageName = reader.nextString();
                case "classes" -> readClassesJson(reader, path, packageName, rows, moduleRoots, ancestors);
                default -> skipValue(reader);
            }
        }
        reader.endObject();
    }

    private static void readClassesJson(JsonReader reader, String path, String packageName,
                                        List<MethodRow> rows, ModuleRootIndex moduleRoots,
                                        AncestorIndex ancestors) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            String className = "";
            reader.beginObject();
            while (reader.hasNext()) {
                switch (reader.nextName()) {
                    case "name" -> className = reader.nextString();
                    case "methods" -> readMethodsJson(reader, path, packageName, className, rows, moduleRoots,
                            ancestors);
                    default -> skipValue(reader);
                }
            }
            reader.endObject();
        }
        reader.endArray();
    }

    private static void readMethodsJson(JsonReader reader, String path, String packageName, String className,
                                        List<MethodRow> rows, ModuleRootIndex moduleRoots,
                                        AncestorIndex ancestors) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            MethodJson m = readMethodJson(reader);
            if (!includeInMarkdownTable(m.level)) {
                continue;
            }
            String chain = MethodHierarchy.breadcrumb(MethodHierarchy.ancestorPath(
                    moduleRoots, path, packageName, className, m.signature));
            String ref = ancestors.register(chain);
            String compact = MethodHierarchy.compactMethodLabel(packageName, className, m.signature);
            String driver = RiskBreakdownUtil.dominantDriver(m.breakdown);
            rows.add(new MethodRow(ref, compact, m.score, m.level, m.cc, m.loc, m.nest,
                    m.cogCount, m.foutCount, driver, m.god, m.breakdown));
        }
        reader.endArray();
    }

    private static MethodJson readMethodJson(JsonReader reader) throws IOException {
        MethodJson m = new MethodJson();
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "signature" -> m.signature = reader.nextString();
                case "cyclomaticComplexity" -> m.cc = reader.nextInt();
                case "codeLines" -> m.loc = reader.nextInt();
                case "maxNestingDepth" -> m.nest = reader.nextInt();
                case "cognitiveComplexity" -> m.cogCount = reader.nextInt();
                case "outboundDistinctCalls" -> m.foutCount = reader.nextInt();
                case "godMethod" -> m.god = reader.nextBoolean();
                case "riskScore" -> m.score = reader.nextDouble();
                case "riskLevel" -> m.level = RiskLevel.valueOf(reader.nextString());
                case "riskBreakdown" -> m.breakdown = readBreakdownJson(reader);
                default -> skipValue(reader);
            }
        }
        reader.endObject();
        return m;
    }

    private static RiskBreakdown readBreakdownJson(JsonReader reader) throws IOException {
        int cc = 0;
        int loc = 0;
        int nest = 0;
        int params = 0;
        double ccSub = 0;
        double locSub = 0;
        double nestSub = 0;
        double paramsSub = 0;
        double cognitiveSub = 0;
        double outboundSub = 0;
        double exitSub = 0;
        Map<String, Double> extraSubScores = Map.of();
        double dominant = 0;
        double blend = 0;
        double bonus = 0;
        double finalScore = 0;
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "cyclomaticComplexity" -> cc = reader.nextInt();
                case "codeLines" -> loc = reader.nextInt();
                case "maxNestingDepth" -> nest = reader.nextInt();
                case "parameterCount" -> params = reader.nextInt();
                case "ccSubScore" -> ccSub = reader.nextDouble();
                case "locSubScore" -> locSub = reader.nextDouble();
                case "nestingSubScore" -> nestSub = reader.nextDouble();
                case "paramsSubScore" -> paramsSub = reader.nextDouble();
                case "cognitiveSubScore" -> cognitiveSub = reader.nextDouble();
                case "outboundSubScore" -> outboundSub = reader.nextDouble();
                case "exitSubScore" -> exitSub = reader.nextDouble();
                case "extraSubScores" -> extraSubScores = readDoubleMap(reader);
                case "dominantSubScore" -> dominant = reader.nextDouble();
                case "weightedBlend" -> blend = reader.nextDouble();
                case "compoundBonus" -> bonus = reader.nextDouble();
                case "finalScore" -> finalScore = reader.nextDouble();
                default -> skipValue(reader);
            }
        }
        reader.endObject();
        return new RiskBreakdown(cc, loc, nest, params, ccSub, locSub, nestSub, paramsSub,
                cognitiveSub, outboundSub, exitSub, extraSubScores, dominant, blend, bonus, finalScore);
    }

    private static Map<String, Double> readDoubleMap(JsonReader reader) throws IOException {
        Map<String, Double> map = new java.util.LinkedHashMap<>();
        reader.beginObject();
        while (reader.hasNext()) {
            map.put(reader.nextName(), reader.nextDouble());
        }
        reader.endObject();
        return map;
    }

    private static AnalysisReport.Summary readSummaryJson(JsonReader reader) throws IOException {
        int filesScanned = 0;
        int filesParsed = 0;
        int filesFailed = 0;
        int classCount = 0;
        int methodCount = 0;
        int totalCodeLines = 0;
        double avgCc = 0;
        int maxCc = 0;
        int godMethods = 0;
        double projectScore = 0;
        RiskLevel projectLevel = RiskLevel.LOW;
        double highLocRatio = 0;
        double criticalPerKloc = 0;
        double p95 = 0;
        double p99 = 0;
        Map<RiskLevel, Long> dist = new EnumMap<>(RiskLevel.class);
        for (RiskLevel level : RiskLevel.values()) {
            dist.put(level, 0L);
        }
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "filesScanned" -> filesScanned = reader.nextInt();
                case "filesParsed" -> filesParsed = reader.nextInt();
                case "filesFailed" -> filesFailed = reader.nextInt();
                case "classCount" -> classCount = reader.nextInt();
                case "methodCount" -> methodCount = reader.nextInt();
                case "totalCodeLines" -> totalCodeLines = reader.nextInt();
                case "averageCyclomaticComplexity" -> avgCc = reader.nextDouble();
                case "maxCyclomaticComplexity" -> maxCc = reader.nextInt();
                case "godMethodCount" -> godMethods = reader.nextInt();
                case "projectRiskScore" -> projectScore = reader.nextDouble();
                case "projectRiskLevel" -> projectLevel = RiskLevel.valueOf(reader.nextString());
                case "highPlusCriticalLocRatio" -> highLocRatio = reader.nextDouble();
                case "criticalMethodsPerKloc" -> criticalPerKloc = reader.nextDouble();
                case "methodRiskScoreP95" -> p95 = reader.nextDouble();
                case "methodRiskScoreP99" -> p99 = reader.nextDouble();
                case "methodRiskDistribution" -> readDistributionJson(reader, dist);
                default -> skipValue(reader);
            }
        }
        reader.endObject();
        return new AnalysisReport.Summary(filesScanned, filesParsed, filesFailed, classCount, methodCount,
                totalCodeLines, avgCc, maxCc, godMethods, projectScore, projectLevel, dist,
                highLocRatio, criticalPerKloc, p95, p99);
    }

    private static void readDistributionJson(JsonReader reader, Map<RiskLevel, Long> dist) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            RiskLevel level = RiskLevel.valueOf(reader.nextName());
            dist.put(level, reader.nextLong());
        }
        reader.endObject();
    }

    private static List<RiskHotspot> readHotspotsJson(JsonReader reader) throws IOException {
        List<RiskHotspot> hotspots = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            String file = "";
            String packageName = "";
            String className = "";
            String method = "";
            int startLine = 0;
            double riskScore = 0;
            RiskLevel riskLevel = RiskLevel.LOW;
            int cc = 0;
            int loc = 0;
            int nest = 0;
            int params = 0;
            int cog = 0;
            int fout = 0;
            String dominantDriver = "—";
            List<String> factors = List.of();
            reader.beginObject();
            while (reader.hasNext()) {
                switch (reader.nextName()) {
                    case "file" -> file = reader.nextString();
                    case "packageName" -> packageName = reader.nextString();
                    case "className" -> className = reader.nextString();
                    case "method" -> method = reader.nextString();
                    case "startLine" -> startLine = reader.nextInt();
                    case "riskScore" -> riskScore = reader.nextDouble();
                    case "riskLevel" -> riskLevel = RiskLevel.valueOf(reader.nextString());
                    case "cyclomaticComplexity" -> cc = reader.nextInt();
                    case "codeLines" -> loc = reader.nextInt();
                    case "maxNestingDepth" -> nest = reader.nextInt();
                    case "parameterCount" -> params = reader.nextInt();
                    case "cognitiveComplexity" -> cog = reader.nextInt();
                    case "outboundDistinctCalls" -> fout = reader.nextInt();
                    case "dominantDriver" -> dominantDriver = reader.nextString();
                    case "riskFactors" -> factors = readStringArray(reader);
                    case "moduleRoot", "ancestorPath" -> skipValue(reader);
                    default -> skipValue(reader);
                }
            }
            reader.endObject();
            hotspots.add(new RiskHotspot(file, packageName, className, method, startLine, riskScore, riskLevel,
                    cc, loc, nest, params, cog, fout, dominantDriver, factors));
        }
        reader.endArray();
        return hotspots;
    }

    private static AnalysisReport.RiskModel readRiskModelJson(JsonReader reader) throws IOException {
        String version = "";
        String formula = "";
        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "version" -> version = reader.nextString();
                case "formula" -> formula = reader.nextString();
                default -> skipValue(reader);
            }
        }
        reader.endObject();
        return new AnalysisReport.RiskModel(version, formula, Map.of(), Map.of());
    }

    private static List<AnalysisReport.FileError> readErrorsJson(JsonReader reader) throws IOException {
        List<AnalysisReport.FileError> errors = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            String file = "";
            String message = "";
            reader.beginObject();
            while (reader.hasNext()) {
                switch (reader.nextName()) {
                    case "file" -> file = reader.nextString();
                    case "message" -> message = reader.nextString();
                    default -> skipValue(reader);
                }
            }
            reader.endObject();
            errors.add(new AnalysisReport.FileError(file, message));
        }
        reader.endArray();
        return errors;
    }

    private static List<String> readStringArray(JsonReader reader) throws IOException {
        List<String> list = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            list.add(reader.nextString());
        }
        reader.endArray();
        return list;
    }

    private static void writeErrors(Writer out, List<AnalysisReport.FileError> errors) throws IOException {
        if (errors == null || errors.isEmpty()) {
            return;
        }
        out.write("## Okunamayan dosyalar\n\n");
        for (AnalysisReport.FileError e : errors) {
            out.write("- `" + e.file() + "`: " + e.message() + "\n");
        }
        out.write("\n");
    }

    private static void writeFooter(Writer out) throws IOException {
        out.write("---\n");
        out.write("*PMD ile karşılaştırmak için `pmd-raporu.md` dosyasına bakın; CC sayıları yakın olmalı, ");
        out.write("risk skoru ise sadece bu parser aracına özeldir.*\n");
    }

    private static boolean includeInMarkdownTable(RiskLevel level) {
        return level != RiskLevel.LOW;
    }

    private static void skipValue(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return;
        }
        reader.skipValue();
    }

    private static String nullSafe(String s) {
        return s == null ? "" : s;
    }

    private static String levelTr(RiskLevel level) {
        return switch (level) {
            case LOW -> "DÜŞÜK";
            case MEDIUM -> "ORTA";
            case HIGH -> "YÜKSEK";
            case CRITICAL -> "KRİTİK";
        };
    }

    private static String fmt(double v) {
        return String.format(Locale.US, "%.3f", v);
    }

    private static String escapeCell(String s) {
        return s.replace("|", "\\|");
    }

    private static String formatTableLocation(String ref, String compactLabel) {
        return "**" + ref + "** · `" + escapeCell(compactLabel) + "`";
    }

    private static void writeAncestorAppendix(Writer out, AncestorIndex ancestors) throws IOException {
        if (ancestors == null || ancestors.isEmpty()) {
            return;
        }
        out.write("## Metod ata zincirleri\n\n");
        out.write("Tablolardaki kodların tam konumu (`modül › dosya › sınıf › metod`):\n\n");
        out.write("| Kod | Tam ata zinciri |\n|-----|-----------------|\n");
        for (Map.Entry<String, String> e : ancestors.entriesInOrder()) {
            out.write("| **" + e.getKey() + "** | `" + escapeCell(e.getValue()) + "` |\n");
        }
        out.write("\n");
    }

    /** Tam breadcrumb → M1, M2 … (ek bölümde listelenir). */
    private static final class AncestorIndex {
        private final LinkedHashMap<String, String> breadcrumbToRef = new LinkedHashMap<>();
        private int next = 1;

        String register(String breadcrumb) {
            return breadcrumbToRef.computeIfAbsent(breadcrumb, k -> "M" + (next++));
        }

        boolean isEmpty() {
            return breadcrumbToRef.isEmpty();
        }

        List<Map.Entry<String, String>> entriesInOrder() {
            List<Map.Entry<String, String>> list = new ArrayList<>(breadcrumbToRef.size());
            for (Map.Entry<String, String> e : breadcrumbToRef.entrySet()) {
                list.add(Map.entry(e.getValue(), e.getKey()));
            }
            return list;
        }
    }

    private static String describeRiskModelTr(AnalysisReport.RiskModel riskModel) {
        if ("v2".equals(riskModel.version())) {
            return "**Puanlama:** enterprise-java (0–100) — 17 ölçü; skor ≈ %58 en kötü boyut + %42 ağırlıklı "
                    + "ortalama + ince ayırıcı; seviye skora göre (30 / 50 / 80 eşikleri).";
        }
        return "**Puanlama:** klasik v1 (0–100) — dört boyut; %65 en kötü + %35 ağırlıklı karışım.";
    }

    private record MethodRow(
            String ref, String compactLabel, double score, RiskLevel level,
            int cc, int loc, int nest, int cogCount, int foutCount, String dominantDriver,
            boolean god, RiskBreakdown breakdown) {
    }

    private static final class MethodJson {
        String signature = "";
        double score;
        RiskLevel level = RiskLevel.LOW;
        int cc;
        int loc;
        int nest;
        int cogCount;
        int foutCount;
        boolean god;
        RiskBreakdown breakdown;
    }

    private static final class JsonMeta {
        String tool;
        String generatedAt;
        String analyzedPath;
        String parserLanguageLevel;
        AnalysisReport.RiskModel riskModel;
        AnalysisReport.Summary summary;
        List<RiskHotspot> hotspots = List.of();
        List<AnalysisReport.FileError> errors = List.of();
        boolean headerWritten;
        boolean filesSectionSeen;
        AncestorIndex ancestors = new AncestorIndex();
    }
}
