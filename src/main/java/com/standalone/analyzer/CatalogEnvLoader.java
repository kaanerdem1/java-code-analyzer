package com.standalone.analyzer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** analysis JSON → env.method_risk_analyze / env.method_similarity. */
public final class CatalogEnvLoader {

    static final String TABLE_METHOD_RISK = "method_risk_analyze";
    static final String TABLE_METHOD_SIMILARITY = "method_similarity";

    static final int MIN_SIMILARITY_PERCENT = 50;

    private CatalogEnvLoader() {
    }

    public static String methodUid(String filePath, String className, String signature, int startLine) {
        return filePath + "#" + className + "#" + signature + "#" + startLine;
    }

    public static int loadReport(Connection connection, String schema, UUID scanRunId, Path jsonPath,
                                 boolean isParser) throws IOException, SQLException {
        try (Reader reader = Files.newBufferedReader(jsonPath)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            return loadReport(connection, schema, scanRunId, root, isParser);
        }
    }

    static int loadReport(Connection connection, String schema, UUID scanRunId, JsonObject root,
                          boolean isParser) throws SQLException {
        String methodTable = qualified(schema, TABLE_METHOD_RISK);
        String dupTable = qualified(schema, TABLE_METHOD_SIMILARITY);
        JsonArray violations = pmdViolations(root);
        String rulesetPath = pmdRulesetPath(root);
        String profile = riskProfile(root);
        String analyzedPath = stringOrEmpty(root, "analyzedPath");

        int methodRows = 0;
        boolean linkInventory = CatalogInventoryMethodResolver.inventoryTablesPresent(connection, schema);
        String insertMethod = """
                INSERT INTO %s (
                  scan_run_id, method_uid, file_path, class_name, method_signature,
                  start_line, end_line, is_parser, is_pmd, ncss_loc,
                  risk_score, risk_level, risk_inputs_json, body_hash, structural_hash,
                  java_method_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?)
                ON CONFLICT (scan_run_id, method_uid, is_parser, is_pmd) DO UPDATE SET
                  ncss_loc = EXCLUDED.ncss_loc,
                  risk_score = EXCLUDED.risk_score,
                  risk_level = EXCLUDED.risk_level,
                  risk_inputs_json = EXCLUDED.risk_inputs_json,
                  body_hash = EXCLUDED.body_hash,
                  structural_hash = EXCLUDED.structural_hash,
                  java_method_id = COALESCE(EXCLUDED.java_method_id, method_risk_analyze.java_method_id)
                """.formatted(methodTable);

        try (PreparedStatement ps = connection.prepareStatement(insertMethod)) {
            JsonArray files = root.getAsJsonArray("files");
            if (files == null) {
                return 0;
            }
            for (JsonElement fileEl : files) {
                JsonObject file = fileEl.getAsJsonObject();
                String filePath = stringOrEmpty(file, "path");
                JsonArray classes = file.getAsJsonArray("classes");
                if (classes == null) {
                    continue;
                }
                for (JsonElement classEl : classes) {
                    JsonObject type = classEl.getAsJsonObject();
                    String className = stringOrEmpty(type, "name");
                    JsonArray methods = type.getAsJsonArray("methods");
                    if (methods == null) {
                        continue;
                    }
                    for (JsonElement methodEl : methods) {
                        JsonObject method = methodEl.getAsJsonObject();
                        int start = method.get("startLine").getAsInt();
                        int end = method.get("endLine").getAsInt();
                        String signature = stringOrEmpty(method, "signature");
                        int codeLines = intOrZero(method, "codeLines");
                        double riskScore = method.has("riskScore") ? method.get("riskScore").getAsDouble() : 0;
                        String riskLevel = stringOrEmpty(method, "riskLevel");
                        String bodyHash = stringOrEmpty(method, "bodyHash");
                        String structuralHash = stringOrEmpty(method, "structuralHash");

                        JsonObject riskInputs = buildRiskInputsJson(
                                profile, analyzedPath, isParser, method, violations, filePath, start, end, rulesetPath);

                        ps.setObject(1, scanRunId);
                        ps.setString(2, methodUid(filePath, className, signature, start));
                        ps.setString(3, filePath);
                        ps.setString(4, className);
                        ps.setString(5, signature);
                        ps.setInt(6, start);
                        ps.setInt(7, end);
                        ps.setBoolean(8, isParser);
                        ps.setBoolean(9, !isParser);
                        ps.setInt(10, codeLines);
                        ps.setBigDecimal(11, java.math.BigDecimal.valueOf(riskScore).setScale(2, java.math.RoundingMode.HALF_UP));
                        ps.setString(12, riskLevel.isEmpty() ? null : riskLevel);
                        ps.setString(13, riskInputs.toString());
                        ps.setString(14, bodyHash.isEmpty() ? null : bodyHash);
                        ps.setString(15, structuralHash.isEmpty() ? null : structuralHash);
                        Long javaMethodId = null;
                        if (linkInventory) {
                            String fqcn = CatalogInventoryMethodResolver.fqcnFromJavaFilePath(filePath, className);
                            String simpleName = CatalogInventoryMethodResolver.methodSimpleName(signature);
                            int params = CatalogInventoryMethodResolver.paramCountFromSignature(signature);
                            javaMethodId = CatalogInventoryMethodResolver.lookupJavaMethodId(
                                    connection, schema, fqcn, simpleName, params);
                        }
                        if (javaMethodId != null) {
                            ps.setLong(16, javaMethodId);
                        } else {
                            ps.setNull(16, java.sql.Types.BIGINT);
                        }
                        ps.addBatch();
                        methodRows++;
                    }
                }
            }
            ps.executeBatch();
        }

        int dupRows = 0;
        String insertDup = """
                INSERT INTO %s (
                  scan_run_id, group_key, is_parser, is_pmd, similarity_type,
                  similarity_percent, shared_ncss_lines, members_json, meta_json
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb)
                """.formatted(dupTable);

        JsonArray groups = root.getAsJsonArray("duplicateGroups");
        if (groups != null) {
            try (PreparedStatement ps = connection.prepareStatement(insertDup)) {
                for (JsonElement groupEl : groups) {
                    JsonObject group = groupEl.getAsJsonObject();
                    int percent = group.has("similarityPercent")
                            ? group.get("similarityPercent").getAsInt() : 0;
                    if (percent < MIN_SIMILARITY_PERCENT) {
                        continue;
                    }
                    JsonArray members = group.getAsJsonArray("members");
                    if (members == null || members.size() < 2) {
                        continue;
                    }
                    JsonArray membersOut = enrichMembers(members);
                    JsonObject meta = new JsonObject();
                    if (group.has("matchedTokenCount")) {
                        meta.addProperty("cpdMatchUnits", group.get("matchedTokenCount").getAsInt());
                    }
                    meta.addProperty("engine", isParser ? "parser" : "pmd");

                    ps.setObject(1, scanRunId);
                    ps.setString(2, stringOrEmpty(group, "groupId"));
                    ps.setBoolean(3, isParser);
                    ps.setBoolean(4, !isParser);
                    ps.setString(5, stringOrEmpty(group, "similarityType"));
                    ps.setInt(6, percent);
                    ps.setInt(7, intOrZero(group, "duplicatedLines"));
                    ps.setString(8, membersOut.toString());
                    ps.setString(9, meta.toString());
                    ps.addBatch();
                    dupRows++;
                }
                ps.executeBatch();
            }
        }

        connection.commit();
        return methodRows + dupRows;
    }

    private static JsonArray enrichMembers(JsonArray members) {
        JsonArray out = new JsonArray();
        for (JsonElement memberEl : members) {
            JsonObject member = memberEl.getAsJsonObject().deepCopy();
            String file = stringOrEmpty(member, "file");
            String className = stringOrEmpty(member, "className");
            String method = stringOrEmpty(member, "method");
            int start = member.has("startLine") ? member.get("startLine").getAsInt() : 0;
            member.addProperty("method_uid", methodUid(file, className, method, start));
            out.add(member);
        }
        return out;
    }

    private static JsonObject buildRiskInputsJson(String profile, String analyzedPath, boolean isParser,
                                                  JsonObject method, JsonArray pmdViolations,
                                                  String filePath, int start, int end, String rulesetPath) {
        JsonObject out = new JsonObject();
        out.addProperty("profile", profile);
        out.addProperty("analyzedPath", analyzedPath);
        out.addProperty("pipeline", isParser ? "parser" : "pmd-native");

        JsonObject metrics = new JsonObject();
        copyMetric(method, metrics, "cyclomaticComplexity");
        copyMetric(method, metrics, "cognitiveComplexity");
        copyMetric(method, metrics, "codeLines");
        copyMetric(method, metrics, "npathComplexity");
        copyMetric(method, metrics, "ncssCount");
        copyMetric(method, metrics, "maxNestingDepth");
        copyMetric(method, metrics, "parameterCount");
        copyMetric(method, metrics, "outboundDistinctCalls");
        copyMetric(method, metrics, "rawTypeUsage");
        out.add("metrics", metrics);

        if (method.has("riskBreakdown")) {
            out.add("riskBreakdown", method.get("riskBreakdown"));
        }
        if (method.has("riskFactors")) {
            out.add("riskFactors", method.get("riskFactors"));
        }
        if (method.has("halstead")) {
            out.add("halstead", method.get("halstead"));
        }

        JsonObject pmd = new JsonObject();
        pmd.addProperty("rulesetPath", rulesetPath);
        JsonArray inMethod = violationsInMethod(pmdViolations, filePath, start, end);
        pmd.add("violationsInMethod", inMethod);
        pmd.add("ruleHits", ruleHits(inMethod));
        out.add("pmd", pmd);

        if (isParser && method.has("outboundDistinctCalls")) {
            JsonObject parserOnly = new JsonObject();
            parserOnly.addProperty("outboundDistinctCalls", method.get("outboundDistinctCalls").getAsInt());
            out.add("parserOnly", parserOnly);
        }
        return out;
    }

    private static void copyMetric(JsonObject method, JsonObject metrics, String key) {
        if (method.has(key) && !method.get(key).isJsonNull()) {
            metrics.add(key, method.get(key));
        }
    }

    private static JsonArray violationsInMethod(JsonArray all, String filePath, int start, int end) {
        JsonArray out = new JsonArray();
        if (all == null) {
            return out;
        }
        for (JsonElement el : all) {
            JsonObject row = el.getAsJsonObject();
            if (!filePath.equals(stringOrEmpty(row, "file"))) {
                continue;
            }
            int line = row.has("beginLine") ? row.get("beginLine").getAsInt() : 0;
            if (line >= start && line <= end) {
                out.add(row);
            }
        }
        return out;
    }

    private static JsonObject ruleHits(JsonArray inMethod) {
        Map<String, Integer> hits = new HashMap<>();
        for (JsonElement el : inMethod) {
            String rule = stringOrEmpty(el.getAsJsonObject(), "ruleName");
            if (!rule.isEmpty()) {
                hits.merge(rule, 1, Integer::sum);
            }
        }
        JsonObject out = new JsonObject();
        hits.forEach(out::addProperty);
        return out;
    }

    private static JsonArray pmdViolations(JsonObject root) {
        if (!root.has("pmdRulesetPhase") || root.get("pmdRulesetPhase").isJsonNull()) {
            return new JsonArray();
        }
        JsonObject phase = root.getAsJsonObject("pmdRulesetPhase");
        JsonArray sample = phase.getAsJsonArray("violationsSample");
        return sample == null ? new JsonArray() : sample;
    }

    private static String pmdRulesetPath(JsonObject root) {
        if (!root.has("pmdRulesetPhase") || root.get("pmdRulesetPhase").isJsonNull()) {
            return "";
        }
        return stringOrEmpty(root.getAsJsonObject("pmdRulesetPhase"), "rulesetPath");
    }

    private static String riskProfile(JsonObject root) {
        if (!root.has("riskModel") || root.get("riskModel").isJsonNull()) {
            return "";
        }
        return stringOrEmpty(root.getAsJsonObject("riskModel"), "version");
    }

    private static String stringOrEmpty(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return "";
        }
        return obj.get(key).getAsString();
    }

    private static int intOrZero(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return 0;
        }
        return obj.get(key).getAsInt();
    }

    private static String qualified(String schema, String table) {
        if (schema == null || schema.isBlank()) {
            return table;
        }
        return schema.trim() + "." + table;
    }
}
