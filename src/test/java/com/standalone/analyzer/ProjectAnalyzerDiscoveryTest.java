package com.standalone.analyzer;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectAnalyzerDiscoveryTest {

    @Test
    void skipsTargetButNotPackageNamedBuild() {
        Path root = Path.of("/repo");
        assertTrue(ProjectAnalyzer.shouldSkipAsBuildOutputDirectory(root, root.resolve("target")));
        assertFalse(ProjectAnalyzer.shouldSkipAsBuildOutputDirectory(
                root, root.resolve("src/main/java/com/acme/build")));
    }
}
