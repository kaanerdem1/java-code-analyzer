package com.standalone.analyzer;

import com.standalone.analyzer.AnalysisReport.DuplicateMember;

import java.util.ArrayList;
import java.util.List;

/** Benzerlik yüzdesi ve metod etiketleri (rapor / JSON). */
final class DuplicateSimilarity {

    private DuplicateSimilarity() {
    }

    static int percent(String similarityType, int duplicatedLines, List<DuplicateMember> members) {
        if ("EXACT_TEXT".equals(similarityType)) {
            return 100;
        }
        if ("EXACT_STRUCTURE".equals(similarityType)) {
            return 95;
        }
        int maxSpan = 0;
        if (members != null) {
            for (DuplicateMember member : members) {
                maxSpan = Math.max(maxSpan, span(member));
            }
        }
        if (maxSpan <= 0 || duplicatedLines <= 0) {
            return 0;
        }
        int raw = (int) Math.round(100.0 * duplicatedLines / maxSpan);
        return Math.min(99, Math.max(1, raw));
    }

    static String methodLabel(DuplicateMember member) {
        if (member == null) {
            return "?";
        }
        String type = member.className() == null || member.className().isBlank() || "?".equals(member.className())
                ? "" : member.className().trim();
        String method = simpleMethodName(member.method());
        if (type.isEmpty()) {
            return method;
        }
        if (method.isEmpty() || method.startsWith("(satır")) {
            return type;
        }
        return type + "." + method;
    }

    static String simpleMethodName(String signatureOrName) {
        if (signatureOrName == null || signatureOrName.isBlank()) {
            return "";
        }
        String raw = signatureOrName.trim();
        int paren = raw.indexOf('(');
        String head = paren >= 0 ? raw.substring(0, paren).trim() : raw;
        int space = head.lastIndexOf(' ');
        if (space >= 0) {
            head = head.substring(space + 1);
        }
        int dot = head.lastIndexOf('.');
        if (dot >= 0) {
            head = head.substring(dot + 1);
        }
        return head;
    }

    static String pairHeadline(List<DuplicateMember> members) {
        if (members == null || members.size() < 2) {
            return "";
        }
        List<String> labels = new ArrayList<>();
        for (DuplicateMember member : members) {
            String label = methodLabel(member);
            if (!label.isEmpty() && !labels.contains(label)) {
                labels.add(label);
            }
        }
        if (labels.size() < 2) {
            if (labels.size() == 1) {
                return "**`" + labels.get(0) + "`** benzer kopyaları (aynı metod imzası, farklı konum).";
            }
            return "";
        }
        String first = labels.get(0);
        StringBuilder rest = new StringBuilder();
        for (int i = 1; i < labels.size(); i++) {
            if (i > 1) {
                rest.append(i == labels.size() - 1 ? " ve " : ", ");
            }
            rest.append("**`").append(labels.get(i)).append("`**");
        }
        if (labels.size() == 2) {
            return "**`" + first + "`** şu metoda benziyor: " + rest + ".";
        }
        return "**`" + first + "`** şu metodlara benziyor: " + rest + ".";
    }

    private static int span(DuplicateMember member) {
        return Math.max(0, member.endLine() - member.startLine() + 1);
    }
}
