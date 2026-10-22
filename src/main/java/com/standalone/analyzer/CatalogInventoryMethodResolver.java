package com.standalone.analyzer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Kaynak taraması satırını {@code env.java_method} ile eşler (FQCN + metod adı + overload).
 * Envanter tabloları yoksa veya eşleşme yoksa {@code null} döner.
 */
final class CatalogInventoryMethodResolver {

    private CatalogInventoryMethodResolver() {
    }

    static String fqcnFromJavaFilePath(String filePath, String className) {
        if (className != null && className.contains(".")) {
            return className.trim();
        }
        if (filePath == null || filePath.isBlank()) {
            return className == null ? "" : className.trim();
        }
        String path = filePath.replace('\\', '/');
        String[] markers = {"src/main/java/", "src/test/java/", "src/integrationTest/java/"};
        for (String marker : markers) {
            int idx = path.indexOf(marker);
            if (idx >= 0) {
                String rest = path.substring(idx + marker.length());
                if (rest.endsWith(".java")) {
                    rest = rest.substring(0, rest.length() - 5);
                }
                return rest.replace('/', '.');
            }
        }
        if (path.endsWith(".java")) {
            int slash = path.lastIndexOf('/');
            String file = slash >= 0 ? path.substring(slash + 1) : path;
            return file.substring(0, file.length() - 5);
        }
        return className == null ? "" : className.trim();
    }

    static String methodSimpleName(String methodSignature) {
        if (methodSignature == null || methodSignature.isBlank()) {
            return "";
        }
        int p = methodSignature.indexOf('(');
        return (p < 0 ? methodSignature : methodSignature.substring(0, p)).trim();
    }

    static int paramCountFromSignature(String methodSignature) {
        if (methodSignature == null) {
            return 0;
        }
        int open = methodSignature.indexOf('(');
        int close = methodSignature.indexOf(')', open);
        if (open < 0 || close <= open + 1) {
            return 0;
        }
        String inside = methodSignature.substring(open + 1, close).trim();
        if (inside.isEmpty()) {
            return 0;
        }
        return inside.split(",").length;
    }

    static boolean inventoryTablesPresent(Connection connection, String schema) throws SQLException {
        String sql = """
                SELECT 1 FROM information_schema.tables
                WHERE table_schema = ? AND table_name = 'java_method'
                LIMIT 1
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schema == null || schema.isBlank() ? "env" : schema.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    static Long lookupJavaMethodId(Connection connection, String schema, String fqcn,
                                   String methodName, int paramCount) throws SQLException {
        if (fqcn == null || fqcn.isBlank() || methodName == null || methodName.isBlank()) {
            return null;
        }
        String javaClass = qualified(schema, "java_class");
        String javaMethod = qualified(schema, "java_method");
        String sql = """
                SELECT jm.id, jm.descriptor, jm.param_count
                FROM %s jm
                JOIN %s jc ON jc.id = jm.class_id
                WHERE jc.fqcn = ? AND jm.name = ?
                """.formatted(javaMethod, javaClass);
        List<Candidate> candidates = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, fqcn);
            ps.setString(2, methodName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    candidates.add(new Candidate(
                            rs.getLong("id"),
                            rs.getString("descriptor"),
                            rs.getObject("param_count") == null ? null : rs.getShort("param_count")));
                }
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        if (candidates.size() == 1) {
            return candidates.get(0).id;
        }
        List<Candidate> narrowed = new ArrayList<>();
        for (Candidate c : candidates) {
            int jvmParams = jvmParameterCount(c.descriptor);
            if (jvmParams == paramCount) {
                narrowed.add(c);
            } else if (c.paramCount != null && c.paramCount.intValue() == paramCount) {
                narrowed.add(c);
            }
        }
        if (narrowed.size() == 1) {
            return narrowed.get(0).id;
        }
        return null;
    }

    private static int jvmParameterCount(String descriptor) {
        if (descriptor == null || descriptor.isEmpty() || descriptor.charAt(0) != '(') {
            return -1;
        }
        int i = 1;
        int count = 0;
        while (i < descriptor.length() && descriptor.charAt(i) != ')') {
            count++;
            i = skipOneJvmType(descriptor, i);
            if (i < 0) {
                return -1;
            }
        }
        return count;
    }

    private static int skipOneJvmType(String descriptor, int i) {
        if (i >= descriptor.length()) {
            return -1;
        }
        char c = descriptor.charAt(i);
        switch (c) {
            case 'B', 'C', 'D', 'F', 'I', 'J', 'S', 'Z' -> {
                return i + 1;
            }
            case '[' -> {
                return skipOneJvmType(descriptor, i + 1);
            }
            case 'L' -> {
                int semi = descriptor.indexOf(';', i);
                return semi < 0 ? -1 : semi + 1;
            }
            default -> {
                return -1;
            }
        }
    }

    private static String qualified(String schema, String table) {
        if (schema == null || schema.isBlank()) {
            return table;
        }
        return schema.trim() + "." + table;
    }

    private record Candidate(long id, String descriptor, Short paramCount) {
    }
}
