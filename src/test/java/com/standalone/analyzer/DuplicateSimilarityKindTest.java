package com.standalone.analyzer;

import com.standalone.analyzer.AnalysisReport.DuplicateMember;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DuplicateSimilarityKindTest {

    @Test
    void cpdTierByOverlapPercent() {
        List<DuplicateMember> wide = List.of(
                new DuplicateMember("A.java", "A", "m()", 1, 100),
                new DuplicateMember("B.java", "B", "m()", 1, 100));
        assertEquals(DuplicateSimilarityKind.BENZER_COK_YUKSEK.code(),
                DuplicateSimilarityKind.classifyCpd(90, wide));
        assertEquals(DuplicateSimilarityKind.BENZER_YUKSEK.code(),
                DuplicateSimilarityKind.classifyCpd(80, wide));
        assertEquals(DuplicateSimilarityKind.BENZER_ORTA.code(),
                DuplicateSimilarityKind.classifyCpd(65, wide));
        assertEquals(DuplicateSimilarityKind.BENZER_KISMI.code(),
                DuplicateSimilarityKind.classifyCpd(45, wide));
    }

    @Test
    void cpdPercentUsesMethodSpanNotChunkOnly() {
        List<DuplicateMember> method100 = List.of(
                new DuplicateMember("A.java", "A", "m()", 1, 100),
                new DuplicateMember("B.java", "B", "m()", 1, 100));
        assertEquals(45, DuplicateSimilarityKind.lineOverlapPercent(45, method100));
        assertEquals(DuplicateSimilarityKind.BENZER_KISMI.code(),
                DuplicateSimilarityKind.classifyCpd(45, method100));
    }

    @Test
    void legacyCodesMapToLabels() {
        assertEquals("Birebir aynı metin", DuplicateSimilarityKind.labelTrFor("EXACT_TEXT"));
        assertEquals("Orta benzerlik", DuplicateSimilarityKind.labelTrFor("BLOK_ORTA"));
    }
}
