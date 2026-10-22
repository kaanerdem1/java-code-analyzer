package com.standalone.analyzer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Tarama raporunu PostgreSQL env katalog tablolarına yazar. */
final class CatalogWriteSupport {

    private CatalogWriteSupport() {
    }

    static void writeAfterScan(AnalysisReport report, boolean isParser, CatalogDbSettings settings) {
        if (settings == null || !settings.enabled()) {
            return;
        }
        try {
            int rows = writeReport(report, isParser, settings);
            System.err.println("[CATALOG] PostgreSQL: " + rows + " satır yazıldı"
                    + " | schema=" + settings.schema()
                    + " | scan_run_id=" + settings.scanRunId()
                    + " | pipeline=" + (isParser ? "parser" : "pmd"));
            if (System.getenv(CatalogDbSettings.ENV_SCAN_RUN) == null
                    || System.getenv(CatalogDbSettings.ENV_SCAN_RUN).isBlank()) {
                System.err.println("[CATALOG] Parser+PMD aynı koşu için: export CATALOG_SCAN_RUN="
                        + settings.scanRunId());
            }
        } catch (IOException | SQLException e) {
            String msg = "[CATALOG] DB yazımı başarısız: " + e.getMessage();
            if (settings.failOnError()) {
                throw new RuntimeException(msg, e);
            }
            System.err.println(msg);
        }
    }

    static int writeReport(AnalysisReport report, boolean isParser, CatalogDbSettings settings)
            throws IOException, SQLException {
        JsonObject root = reportToJsonObject(report);
        try (Connection connection = DriverManager.getConnection(
                settings.jdbcUrl(), settings.user(), settings.password())) {
            connection.setAutoCommit(false);
            return CatalogEnvLoader.loadReport(
                    connection, settings.schema(), settings.scanRunId(), root, isParser);
        }
    }

    private static JsonObject reportToJsonObject(AnalysisReport report) throws IOException {
        StringWriter buffer = new StringWriter();
        AnalysisReportJsonWriter.write(report, buffer, false);
        return JsonParser.parseString(buffer.toString()).getAsJsonObject();
    }
}
