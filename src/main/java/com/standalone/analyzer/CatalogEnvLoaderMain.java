package com.standalone.analyzer;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.UUID;

/**
 * JSON raporlarını PostgreSQL env şemasına yazar.
 *
 * <pre>
 * java -cp target/java-code-analyzer.jar com.standalone.analyzer.CatalogEnvLoaderMain \
 *   --scan-run=uuid --parser-json=parser.json --pmd-json=pmd.json
 * </pre>
 */
public final class CatalogEnvLoaderMain {

    private CatalogEnvLoaderMain() {
    }

    public static void main(String[] args) throws Exception {
        String catalogJdbc = null;
        String catalogScanRun = null;
        Path parserJson = null;
        Path pmdJson = null;
        for (String arg : args) {
            if (arg.startsWith("--parser-json=")) {
                parserJson = Path.of(arg.substring("--parser-json=".length()));
            } else if (arg.startsWith("--pmd-json=")) {
                pmdJson = Path.of(arg.substring("--pmd-json=".length()));
            } else if (arg.startsWith("--jdbc=") || arg.startsWith("--catalog-jdbc=")) {
                catalogJdbc = arg.substring(arg.indexOf('=') + 1);
            } else if (arg.startsWith("--catalog-scan-run=")) {
                catalogScanRun = arg.substring("--catalog-scan-run=".length());
            }
        }

        if (parserJson == null && pmdJson == null) {
            System.err.println("En az biri gerekli: --parser-json= --pmd-json=");
            System.exit(1);
        }

        CatalogDbSettings settings = CatalogDbSettings.resolve(false, catalogJdbc, catalogScanRun);
        if (!settings.enabled()) {
            System.err.println("CATALOG_JDBC_URL veya --catalog-jdbc= gerekli");
            System.exit(1);
        }

        try (Connection conn = DriverManager.getConnection(
                settings.jdbcUrl(), settings.user(), settings.password())) {
            conn.setAutoCommit(false);
            int total = 0;
            if (parserJson != null) {
                int n = CatalogEnvLoader.loadReport(conn, settings.schema(), settings.scanRunId(), parserJson, true);
                System.err.println("[CATALOG] parser: " + n + " satır (metod+duplicate)");
                total += n;
            }
            if (pmdJson != null) {
                int n = CatalogEnvLoader.loadReport(conn, settings.schema(), settings.scanRunId(), pmdJson, false);
                System.err.println("[CATALOG] pmd: " + n + " satır (metod+duplicate)");
                total += n;
            }
            System.out.println("scan_run_id=" + settings.scanRunId());
            System.out.println("schema=" + settings.schema());
            System.out.println("rows_touched=" + total);
        }
    }
}
