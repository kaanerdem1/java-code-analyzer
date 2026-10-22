package com.standalone.analyzer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** .env.catalog / CATALOG_* ile JDBC test (psql gerekmez). */
public final class CatalogJdbcPingMain {

    public static void main(String[] args) {
        CatalogDbSettings settings = CatalogDbSettings.resolve(false, null, null);
        if (!settings.enabled()) {
            System.err.println("[CATALOG-PING] CATALOG_JDBC_URL tanımlı değil.");
            System.err.println("  .env.catalog veya export CATALOG_JDBC_URL=...");
            System.exit(2);
        }
        String user = settings.user();
        System.err.println("[CATALOG-PING] URL: " + maskPasswordInUrl(settings.jdbcUrl()));
        System.err.println("[CATALOG-PING] user: " + user + " | schema: " + settings.schema());

        try (Connection c = DriverManager.getConnection(
                settings.jdbcUrl(), user, settings.password())) {
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT current_database(), current_user, version()")) {
                if (rs.next()) {
                    System.out.println("[CATALOG-PING] OK");
                    System.out.println("  database: " + rs.getString(1));
                    System.out.println("  user:     " + rs.getString(2));
                    System.out.println("  server:   " + abbreviate(rs.getString(3), 120));
                }
            }
            String schema = settings.schema();
            if (schema != null && !schema.isBlank()) {
                try (Statement st = c.createStatement();
                     ResultSet rs = st.executeQuery(
                             "SELECT COUNT(*) FROM information_schema.tables "
                                     + "WHERE table_schema = '" + schema.replace("'", "''") + "'")) {
                    if (rs.next()) {
                        System.out.println("  tables in schema '" + schema + "': " + rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[CATALOG-PING] FAILED");
            System.err.println("  message:   " + e.getMessage());
            System.err.println("  SQLState:  " + e.getSQLState());
            System.err.println("  errorCode: " + e.getErrorCode());
            Throwable cause = e.getCause();
            if (cause != null) {
                System.err.println("  cause:     " + cause.getMessage());
            }
            printHints(e);
            System.exit(1);
        }
    }

    private static void printHints(SQLException e) {
        String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        if (msg.contains("password authentication failed")) {
            System.err.println();
            System.err.println("İpucu: pgAdmin ile aynı host/port/database/user/şifre dene.");
            System.err.println("  PostgreSQL rol adı büyük/küçük harfe duyarlı olabilir (SystemInventoryDbUser vs systeminventorydbuser).");
            System.err.println("  Şifrede ! varsa bash'te export PGPASSWORD='...' tek tırnak kullan.");
        }
        if (msg.contains("connection refused") || msg.contains("timeout") || msg.contains("could not connect")) {
            System.err.println();
            System.err.println("İpucu: VPN, firewall, host/port (5000) ve sslmode=require.");
        }
        if (msg.contains("ssl") || msg.contains("certificate")) {
            System.err.println();
            System.err.println("İpucu: sslmode=require; kurumsal CA gerekebilir (DBA).");
        }
    }

    private static String maskPasswordInUrl(String url) {
        return url == null ? "" : url;
    }

    private static String abbreviate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
