package com.standalone.analyzer;

import java.nio.file.Path;
import java.util.List;

/** Monorepo / büyük codebase tarama ayarları. */
public record ScanOptions(
        int workers,
        int progressEvery,
        List<String> includeGlobs,
        List<String> excludeGlobs,
        ReportDetail reportDetail,
        boolean applyDefaultIgnoredDirectories,
        Path incrementalStateFile,
        boolean ignoreIncrementalCache) {

    public enum ReportDetail {
        /** Dosya → sınıf → metod tam ağaç (varsayılan). */
        FULL,
        /** Dosya özeti + hotspot; metod listesi JSON/Markdown'da yok (bellek/çıktı küçük). */
        SUMMARY
    }

    public static ScanOptions defaults() {
        int cpus = Runtime.getRuntime().availableProcessors();
        int workers = Math.max(1, Math.min(cpus, cpus <= 4 ? cpus : cpus - 1));
        return new ScanOptions(workers, 500, List.of(), List.of(), ReportDetail.FULL, true, null, false);
    }

    public ScanOptions {
        if (workers < 1) {
            throw new IllegalArgumentException("workers must be >= 1");
        }
        if (progressEvery < 1) {
            throw new IllegalArgumentException("progressEvery must be >= 1");
        }
        includeGlobs = List.copyOf(includeGlobs);
        excludeGlobs = List.copyOf(excludeGlobs);
    }
}
