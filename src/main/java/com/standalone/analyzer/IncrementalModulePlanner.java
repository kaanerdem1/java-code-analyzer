package com.standalone.analyzer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Modül (pom.xml / Gradle kökü) bazında byte-hash parmak izi: modülde hiçbir dosya byte
 * değişmediyse tüm modül dosyaları parse edilmeden state'ten yüklenir.
 */
final class IncrementalModulePlanner {

    private IncrementalModulePlanner() {
    }

    static IncrementalScanContext prepare(
            List<Path> sources,
            Path base,
            ModuleRootIndex moduleRoots,
            AnalyzerState previousState) throws IOException {
        Map<String, String> byteHashes = new LinkedHashMap<>();
        Map<String, List<String>> filesByModule = new TreeMap<>();

        for (Path source : sources) {
            String relative = relativePath(base, source);
            String byteHash = HashService.hashFile(source);
            byteHashes.put(relative, byteHash);
            String module = moduleRoots.moduleRoot(relative);
            filesByModule.computeIfAbsent(module, k -> new ArrayList<>()).add(relative);
        }

        Map<String, String> moduleFingerprints = new LinkedHashMap<>();
        Set<String> bulkReuse = new HashSet<>();
        int unchangedModules = 0;
        Map<String, AnalyzerState.ModuleState> prevModules =
                previousState.modules != null ? previousState.modules : Map.of();

        for (Map.Entry<String, List<String>> entry : filesByModule.entrySet()) {
            String moduleRoot = entry.getKey();
            List<String> paths = entry.getValue();
            String fingerprint = ModuleFingerprint.from(paths, byteHashes);
            moduleFingerprints.put(moduleRoot, fingerprint);

            AnalyzerState.ModuleState prev = prevModules.get(moduleRoot);
            if (prev != null && fingerprint.equals(prev.fingerprint) && moduleFullyCached(previousState, paths)) {
                unchangedModules++;
                bulkReuse.addAll(paths);
            }
        }

        return new IncrementalScanContext(
                Map.copyOf(byteHashes), Set.copyOf(bulkReuse), Map.copyOf(moduleFingerprints),
                unchangedModules, filesByModule.size());
    }

    private static boolean moduleFullyCached(AnalyzerState state, List<String> relativePaths) {
        if (state.files == null) {
            return false;
        }
        for (String path : relativePaths) {
            AnalyzerState.FileState file = state.files.get(path);
            if (file == null || file.cachedFileMetric == null) {
                return false;
            }
        }
        return true;
    }

    private static String relativePath(Path base, Path file) {
        Path relative = base != null && file.startsWith(base) ? base.relativize(file) : file;
        return relative.toString().replace('\\', '/');
    }
}
