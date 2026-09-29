package com.standalone.analyzer;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;

/** Relative path glob include/exclude (monorepo filtreleri). */
final class PathGlobFilter {

    private final List<PathMatcher> includes;
    private final List<PathMatcher> excludes;

    PathGlobFilter(FileSystem fs, List<String> includeGlobs, List<String> excludeGlobs) {
        this.includes = compile(fs, includeGlobs);
        this.excludes = compile(fs, excludeGlobs);
    }

    boolean accept(Path scanRoot, Path file) {
        String relative = scanRoot.relativize(file).toString().replace('\\', '/');
        Path relativePath = Path.of(relative);
        for (PathMatcher exclude : excludes) {
            if (exclude.matches(relativePath)) {
                return false;
            }
        }
        if (includes.isEmpty()) {
            return true;
        }
        for (PathMatcher include : includes) {
            if (include.matches(relativePath)) {
                return true;
            }
        }
        return false;
    }

    private static List<PathMatcher> compile(FileSystem fs, List<String> globs) {
        List<PathMatcher> matchers = new ArrayList<>();
        for (String glob : globs) {
            String pattern = glob.contains("*") || glob.contains("?") ? "glob:" + glob : "glob:**/" + glob + "/**";
            matchers.add(fs.getPathMatcher(pattern));
        }
        return matchers;
    }
}
