package com.standalone.analyzer;

import com.standalone.analyzer.AnalysisReport.ClassMetric;
import com.standalone.analyzer.AnalysisReport.DuplicateGroup;
import com.standalone.analyzer.AnalysisReport.DuplicateMember;
import com.standalone.analyzer.AnalysisReport.FileMetric;

import net.sourceforge.pmd.cpd.CPDConfiguration;
import net.sourceforge.pmd.cpd.CpdAnalysis;
import net.sourceforge.pmd.cpd.Mark;
import net.sourceforge.pmd.cpd.Match;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Proje geneli duplicate / near-duplicate tespiti (Type-1/2 yapısal hash, Type-3 PMD CPD).
 */
final class DuplicateDetectionEngine {

    private static final int MIN_CC_FOR_DUPLICATE_CHECK = 3;
    private static final int MIN_LOC_FOR_DUPLICATE_CHECK = 8;

    List<DuplicateGroup> detectExactDuplicates(List<FileMetric> files) {
        Map<String, List<DuplicateMember>> byHash = new LinkedHashMap<>();
        for (FileMetric file : files) {
            for (ClassMetric type : file.classes()) {
                for (MethodMetric method : type.methods()) {
                    if (!qualifiesForDuplicateCheck(method)) {
                        continue;
                    }
                    String hash = method.structuralHash();
                    if (hash == null || hash.isEmpty()) {
                        continue;
                    }
                    byHash.computeIfAbsent(hash, k -> new ArrayList<>())
                            .add(new DuplicateMember(file.path(), type.name(), method.signature(),
                                    method.startLine(), method.endLine()));
                }
            }
        }

        List<DuplicateGroup> groups = new ArrayList<>();
        int groupId = 1;
        for (List<DuplicateMember> members : byHash.values()) {
            if (members.size() < 2) {
                continue;
            }
            int duplicatedLines = members.stream()
                    .mapToInt(m -> Math.max(0, m.endLine() - m.startLine() + 1))
                    .max().orElse(0);
            groups.add(new DuplicateGroup("EXACT-" + groupId++, "EXACT_STRUCTURE", 0, duplicatedLines, members));
        }
        return groups;
    }

    List<DuplicateGroup> detectNearMissDuplicates(Path root, int minimumTokens, List<FileMetric> files) {
        Map<String, FileMetric> filesByPath = files.stream()
                .collect(Collectors.toMap(FileMetric::path, f -> f, (a, b) -> a));

        CPDConfiguration config = new CPDConfiguration();
        config.setMinimumTileSize(minimumTokens);
        config.setOnlyRecognizeLanguage(config.getLanguageRegistry().getLanguageById("java"));
        config.setIgnoreIdentifiers(true);
        config.setIgnoreLiterals(true);
        config.setSourceEncoding(StandardCharsets.UTF_8);
        config.addInputPath(root);

        List<DuplicateGroup> groups = new ArrayList<>();
        try (CpdAnalysis cpd = CpdAnalysis.create(config)) {
            cpd.performAnalysis(report -> {
                int groupId = 1;
                for (Match match : report.getMatches()) {
                    List<DuplicateMember> members = new ArrayList<>();
                    for (Mark mark : match.getMarkSet()) {
                        var location = mark.getLocation();
                        Path absoluteFile = Path.of(location.getFileId().getAbsolutePath());
                        int beginLine = location.getStartLine();
                        int endLine = location.getEndLine();
                        String relative = relativize(root, absoluteFile);
                        FileMetric file = filesByPath.get(relative);
                        ResolvedMethod resolved = resolveMethod(file, beginLine, endLine);
                        if (resolved != null && !qualifiesForDuplicateCheck(resolved.method())) {
                            continue;
                        }
                        String className = resolved != null ? resolved.className() : "?";
                        String signature = resolved != null ? resolved.method().signature()
                                : "(satır " + beginLine + "-" + endLine + ")";
                        members.add(new DuplicateMember(relative, className, signature,
                                beginLine, endLine));
                    }
                    List<DuplicateMember> consolidated = DuplicateMemberConsolidation.consolidate(members);
                    if (consolidated.size() >= 2) {
                        groups.add(new DuplicateGroup("NEARMISS-" + groupId++, "NEAR_MISS",
                                match.getTokenCount(), match.getLineCount(), consolidated));
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("PMD CPD analysis failed", e);
        }
        return groups;
    }

    private static boolean qualifiesForDuplicateCheck(MethodMetric method) {
        return method.cyclomaticComplexity() >= MIN_CC_FOR_DUPLICATE_CHECK
                || method.codeLines() >= MIN_LOC_FOR_DUPLICATE_CHECK;
    }

    private static ResolvedMethod resolveMethod(FileMetric file, int beginLine, int endLine) {
        if (file == null) {
            return null;
        }
        ResolvedMethod best = null;
        int bestOverlap = 0;
        for (ClassMetric type : file.classes()) {
            for (MethodMetric method : type.methods()) {
                int overlap = lineOverlap(method.startLine(), method.endLine(), beginLine, endLine);
                if (overlap > bestOverlap) {
                    bestOverlap = overlap;
                    best = new ResolvedMethod(type.name(), method);
                }
            }
        }
        return best;
    }

    private static int lineOverlap(int aStart, int aEnd, int bStart, int bEnd) {
        return Math.max(0, Math.min(aEnd, bEnd) - Math.max(aStart, bStart) + 1);
    }

    private static String relativize(Path root, Path file) {
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path normalizedFile = file.toAbsolutePath().normalize();
        Path relative = normalizedFile.startsWith(normalizedRoot)
                ? normalizedRoot.relativize(normalizedFile) : normalizedFile;
        return relative.toString().replace('\\', '/');
    }

    private record ResolvedMethod(String className, MethodMetric method) {
    }
}
