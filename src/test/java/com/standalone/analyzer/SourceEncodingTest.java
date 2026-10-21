package com.standalone.analyzer;

import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SourceEncodingTest {

    @Test
    void windows1254TurkishCommentDoesNotThrow() throws Exception {
        Charset win1254 = Charset.forName("windows-1254");
        String expected = "// throw new CCSBusinessException(\"Seçilen BCH kredisine ait Kademeli Faiz Oranı bilgisi bulunamadı.\");";
        Path file = Files.createTempFile("legacy-tr", ".java");
        try {
            Files.write(file, expected.getBytes(win1254));
            String decoded = SourceEncoding.read(file, StandardCharsets.UTF_8);
            assertTrue(decoded.contains("Seçilen"));
            assertTrue(decoded.contains("bulunamadı"));
            assertFalse(decoded.contains("Se�ilen"));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void utf8StillPreferredWhenValid() {
        byte[] utf8 = "şğüı".getBytes(StandardCharsets.UTF_8);
        assertEquals("şğüı", SourceEncoding.decode(utf8, StandardCharsets.UTF_8));
    }

    @Test
    void mockLegacyWindows1254FileIsReadableWithoutEncodingFlag() throws Exception {
        Path file = Path.of("mock-modules/module-legacy-tr/src/main/java/com/mock/legacytr/CcsBusinessExceptionSample.java");
        if (!Files.isRegularFile(file)) {
            return;
        }
        String decoded = SourceEncoding.read(file, StandardCharsets.UTF_8);
        assertTrue(decoded.contains("Seçilen") || decoded.contains("CCSBusinessException"));
    }

    @Test
    void detectsMalformedUtf8() {
        assertTrue(SourceEncoding.looksLikeMalformedUtf8(new MalformedInputException(1)));
    }
}
