package com.standalone.analyzer;

import java.util.Locale;
import java.util.UUID;

/** PostgreSQL katalog yazımı — ortam değişkenleri + isteğe bağlı CLI. */
public record CatalogDbSettings(
        String jdbcUrl,
        String user,
        String password,
        String schema,
        UUID scanRunId,
        boolean failOnError) {

    public static final String ENV_JDBC = "CATALOG_JDBC_URL";
    public static final String ENV_SCHEMA = "CATALOG_SCHEMA";
    public static final String ENV_SCAN_RUN = "CATALOG_SCAN_RUN";
    public static final String ENV_FAIL = "CATALOG_FAIL_ON_ERROR";

    public boolean enabled() {
        return jdbcUrl != null && !jdbcUrl.isBlank();
    }

    /**
     * @param noCatalogWrite CLI {@code --no-catalog-write}
     * @param cliJdbc        {@code --catalog-jdbc=} (boş = env)
     * @param cliScanRun     {@code --catalog-scan-run=} (boş = env veya yeni UUID)
     */
    public static CatalogDbSettings resolve(boolean noCatalogWrite, String cliJdbc, String cliScanRun) {
        if (noCatalogWrite) {
            return disabled();
        }
        String jdbc = firstNonBlank(cliJdbc, env(ENV_JDBC));
        if (jdbc == null || jdbc.isBlank()) {
            return disabled();
        }
        String user = firstNonBlank(env("PGUSER"), System.getenv("USER"));
        String password = nullToEmpty(env("PGPASSWORD"));
        String schema = firstNonBlank(env(ENV_SCHEMA), "env");
        UUID scanRun = parseScanRun(firstNonBlank(cliScanRun, env(ENV_SCAN_RUN)));
        if (scanRun == null) {
            scanRun = UUID.randomUUID();
        }
        boolean fail = parseBool(env(ENV_FAIL));
        return new CatalogDbSettings(jdbc, user, password, schema, scanRun, fail);
    }

    public static CatalogDbSettings disabled() {
        return new CatalogDbSettings(null, null, null, "env", null, false);
    }

    private static UUID parseScanRun(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return UUID.fromString(raw.trim());
    }

    private static String env(String key) {
        return System.getenv(key);
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }

    private static String nullToEmpty(String v) {
        return v == null ? "" : v;
    }

    private static boolean parseBool(String v) {
        if (v == null) {
            return false;
        }
        return switch (v.trim().toLowerCase(Locale.ROOT)) {
            case "1", "true", "yes", "on" -> true;
            default -> false;
        };
    }
}
