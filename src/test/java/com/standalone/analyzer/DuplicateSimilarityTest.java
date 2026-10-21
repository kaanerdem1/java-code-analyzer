package com.standalone.analyzer;

import com.standalone.analyzer.AnalysisReport.DuplicateMember;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DuplicateSimilarityTest {

    @Test
    void exactTextIs100() {
        List<DuplicateMember> members = List.of(
                new DuplicateMember("A.java", "Left", "copy()", 1, 50),
                new DuplicateMember("B.java", "Right", "copy()", 10, 59));
        assertEquals(100, DuplicateSimilarity.percent("EXACT_TEXT", 50, members));
        assertEquals(95, DuplicateSimilarity.percent("EXACT_STRUCTURE", 50, members));
        assertEquals(83, DuplicateSimilarity.percent("NEAR_MISS", 50, List.of(
                new DuplicateMember("A.java", "Left", "copy()", 1, 60),
                new DuplicateMember("B.java", "Right", "paste()", 1, 60))));
    }

    @Test
    void headlineNamesMethods() {
        String headline = DuplicateSimilarity.pairHeadline(List.of(
                new DuplicateMember("a/Foo.java", "Foo", "route(List, boolean)", 8, 60),
                new DuplicateMember("b/Bar.java", "Bar", "dispatch(List, boolean)", 12, 70)));
        assertTrue(headline.contains("Foo.route"));
        assertTrue(headline.contains("Bar.dispatch"));
        assertTrue(headline.contains("şu metoda benziyor"));
    }
}
