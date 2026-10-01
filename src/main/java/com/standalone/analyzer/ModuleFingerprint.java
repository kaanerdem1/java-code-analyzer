package com.standalone.analyzer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Modüldeki tüm dosyaların yol + Stage-1 byte hash özetinden tek imza. */
final class ModuleFingerprint {

    private ModuleFingerprint() {
    }

    static String from(List<String> relativePaths, Map<String, String> byteHashByPath) {
        List<String> lines = new ArrayList<>(relativePaths.size());
        for (String path : relativePaths) {
            String hash = byteHashByPath.get(path);
            lines.add(path + "|" + (hash == null ? "" : hash));
        }
        Collections.sort(lines);
        String payload = String.join("\n", lines);
        return sha256Hex(payload.getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256Hex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
