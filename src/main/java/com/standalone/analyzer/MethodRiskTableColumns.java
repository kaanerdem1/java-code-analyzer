package com.standalone.analyzer;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Markdown metod tablosu: enterprise-java v3 risk profilindeki ham girdiler (legacy/Halstead/koku dahil).
 */
final class MethodRiskTableColumns {

    record Column(String id, String header, String legendTr, ToIntFunction<MethodScanValues> value) {
    }

    /** Sıra {@code config/risk-parameters-proposal.yaml} blend_weights ile uyumlu. */
    static final List<Column> ENTERPRISE_V3 = List.of(
            new Column("branching", "CC", "Dallanma (siklomatik) — if/for/catch/&& …", MethodScanValues::cyclomaticComplexity),
            new Column("length", "Satır", "Kod satırı (LOC, yorum/boş hariç)", MethodScanValues::codeLines),
            new Column("nesting", "İçi", "Maksimum iç içe blok derinliği", MethodScanValues::maxNestingDepth),
            new Column("parameters", "Param", "Parametre sayısı", MethodScanValues::parameterCount),
            new Column("cognitive", "Cog", "Cognitive (okunabilirlik) karmaşıklığı", MethodScanValues::cognitiveComplexity),
            new Column("exitPoints", "Çıkış", "return / throw çıkış noktası sayısı", MethodScanValues::exitPoints),
            new Column("logicalStatements", "İfade", "Mantıksal ifade (statement) sayısı", MethodScanValues::logicalStatements),
            new Column("lambdaCount", "λ", "Lambda ifadesi sayısı", MethodScanValues::lambdaCount),
            new Column("switchCases", "Switch", "Switch kolu sayısı", MethodScanValues::switchCases),
            new Column("maxTryNestingDepth", "Try", "İç içe try derinliği", MethodScanValues::maxTryNestingDepth),
            new Column("localVariableCount", "Yerel", "Yerel değişken sayısı", MethodScanValues::localVariableCount),
            new Column("maxMethodCallChainLength", "Zincir", "Peş peşe metod çağrı zinciri uzunluğu",
                    MethodScanValues::maxMethodCallChainLength),
            new Column("catchClauses", "Catch", "catch bloğu sayısı", MethodScanValues::catchClauses),
            new Column("emptyCatchBlocks", "∅Catch", "Boş catch bloğu", MethodScanValues::emptyCatchBlocks),
            new Column("catchExceptionOrThrowable", "ExcCatch", "Exception/Throwable geniş catch",
                    MethodScanValues::catchExceptionOrThrowable),
            new Column("catchWithOnlyPrintStackTrace", "PST", "Yalnızca printStackTrace içeren catch",
                    MethodScanValues::catchWithOnlyPrintStackTrace),
            new Column("primitiveObsessionIndex", "Prim", "Primitive obsession indeksi", MethodScanValues::primitiveObsessionIndex),
            new Column("maxBooleanOperatorsInCondition", "&&‖", "Tek koşuldaki max && / || sayısı",
                    MethodScanValues::maxBooleanOperatorsInCondition),
            new Column("halsteadDifficulty", "H.Dif", "Halstead zorluk (yuvarlak)", MethodScanValues::halsteadDifficultyRounded),
            new Column("halsteadEffort", "H.Efor", "Halstead effort (yuvarlak)", MethodScanValues::halsteadEffortRounded),
            new Column("rawTypeUsage", "Raw", "Ham (raw) tip kullanımı", MethodScanValues::rawTypeUsage),
            new Column("stringConcatInLoop", "Concat", "Döngüde string birleştirme", MethodScanValues::stringConcatInLoop),
            new Column("hardcodedLiteralCount", "Sabit", "Gömülü sabit (IP/SQL/URL vb.)", MethodScanValues::hardcodedLiteralCount),
            new Column("swallowedExceptionSmells", "YutExc", "Yutulan exception kokusu", MethodScanValues::swallowedExceptionSmells),
            new Column("genericExceptionSmells", "GenExc", "Generic exception catch kokusu",
                    MethodScanValues::genericExceptionSmells));

    private MethodRiskTableColumns() {
    }

    static void writeScoreInputLegend(Writer out) throws IOException {
        out.write("**Skor girdileri (enterprise-java v3):** Dokümantasyondaki legacy/Halstead/koku katmanının ");
        out.write("ham sayıları — her biri YAML eşiklerine göre 0–100 alt skora çevrilip nihai **Risk** ");
        out.write("sütununa girer (~25 metod boyutu). Sınıf düzeyi (LCOM3, Ce, god-class) JSON sınıf kaydında.\n\n");
        for (Column c : ENTERPRISE_V3) {
            out.write("- **" + c.header() + "** (`" + c.id() + "`): " + c.legendTr() + "\n");
        }
        out.write("- **FOUT†** (`outboundDistinctCalls`): Dış çağrı çeşitliliği — **katalog**; v3 metod skoruna ");
        out.write("**girmez** (YAML policy).\n\n");
    }

    static void writeMetricHeaderRow(Writer out) throws IOException {
        StringBuilder headers = new StringBuilder("| Risk | Seviye | Kod | Metod | Dosya |");
        StringBuilder sep = new StringBuilder("|-----:|--------|:---:|-------|-------|");
        for (Column c : ENTERPRISE_V3) {
            headers.append(' ').append(c.header()).append(" |");
            sep.append("---:|");
        }
        headers.append(" FOUT† | Nedeni | Dev? |");
        sep.append("------:|--------|:----:|");
        out.write(headers + "\n");
        out.write(sep + "\n");
    }

    static void writeMetricCells(Writer out, MethodScanValues scan, int foutCatalog) throws IOException {
        for (Column c : ENTERPRISE_V3) {
            out.write(" " + c.value().applyAsInt(scan) + " |");
        }
        out.write(" " + foutCatalog + " |");
    }
}
