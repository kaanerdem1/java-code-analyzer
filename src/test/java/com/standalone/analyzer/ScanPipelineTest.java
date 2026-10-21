package com.standalone.analyzer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScanPipelineTest {

    @Test
    void parsePipelineAliases() {
        assertEquals(ScanPipeline.PMD_NATIVE, ScanPipeline.parse("pmd-native"));
        assertEquals(ScanPipeline.PMD_NATIVE, ScanPipeline.parse("PMD_NATIVE"));
        assertEquals(ScanPipeline.PARSER_HYBRID, ScanPipeline.parse("parser-hybrid"));
    }
}
