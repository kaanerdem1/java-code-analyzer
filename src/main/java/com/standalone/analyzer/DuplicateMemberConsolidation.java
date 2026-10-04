package com.standalone.analyzer;

import com.standalone.analyzer.AnalysisReport.DuplicateGroup;
import com.standalone.analyzer.AnalysisReport.DuplicateMember;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** PMD CPD bazen aynı metod için kaydırmalı satır pencereleri üretir; raporda metod başına tek satır. */
final class DuplicateMemberConsolidation {

    private DuplicateMemberConsolidation() {
    }

    static List<DuplicateMember> consolidate(List<DuplicateMember> members) {
        if (members == null || members.isEmpty()) {
            return List.of();
        }
        Map<String, DuplicateMember> merged = new LinkedHashMap<>();
        for (DuplicateMember member : members) {
            String key = member.file() + "\0" + member.className() + "\0" + member.method();
            merged.merge(key, member, DuplicateMemberConsolidation::unionLines);
        }
        List<DuplicateMember> out = new ArrayList<>(merged.values());
        out.sort(Comparator
                .comparing(DuplicateMember::file)
                .thenComparing(DuplicateMember::className)
                .thenComparing(DuplicateMember::method));
        return out;
    }

    static DuplicateGroup consolidateGroup(DuplicateGroup group) {
        if (group == null) {
            return null;
        }
        List<DuplicateMember> members = consolidate(group.members());
        return new DuplicateGroup(group.groupId(), group.similarityType(), group.matchedTokenCount(),
                group.duplicatedLines(), members);
    }

    private static DuplicateMember unionLines(DuplicateMember a, DuplicateMember b) {
        return new DuplicateMember(a.file(), a.className(), a.method(),
                Math.min(a.startLine(), b.startLine()), Math.max(a.endLine(), b.endLine()));
    }
}
