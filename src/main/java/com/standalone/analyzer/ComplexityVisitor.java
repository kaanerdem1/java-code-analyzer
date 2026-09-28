package com.standalone.analyzer;

import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.SynchronizedStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.standalone.analyzer.AnalysisReport.ClassMetric;
import com.standalone.analyzer.RiskCalculator.Assessment;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Walks one compilation unit and collects per-method and per-type metrics.
 *
 * <ul>
 *   <li><b>Cyclomatic complexity</b>: 1 + if / else-if / case / for / foreach / while / do /
 *       catch / ternary / && / ||.</li>
 *   <li><b>Nesting depth</b>: if, for, foreach, while, do, switch, try, synchronized. An
 *       {@code else if} does not deepen the nesting.</li>
 *   <li><b>Code lines</b>: lines inside the method range that contain at least one real token
 *       (blank lines and comment-only lines are ignored).</li>
 * </ul>
 * Methods declared inside anonymous/local classes get their own context, so their complexity is
 * not added to the enclosing method. One instance analyses exactly one compilation unit.
 */
public class ComplexityVisitor extends VoidVisitorAdapter<Void> {

    private final RiskCalculator riskCalculator;
    private final BitSet codeLines;
    private final Deque<TypeContext> typeStack = new ArrayDeque<>();
    private final Deque<MethodContext> methodStack = new ArrayDeque<>();
    private final List<ClassMetric> completedTypes = new ArrayList<>();

    public ComplexityVisitor(RiskCalculator riskCalculator, BitSet codeLines) {
        this.riskCalculator = riskCalculator;
        this.codeLines = codeLines;
    }

    /** Marks every line that carries at least one non-whitespace, non-comment token. */
    public static BitSet computeCodeLines(CompilationUnit cu) {
        BitSet lines = new BitSet();
        cu.getTokenRange().ifPresent(range -> {
            for (JavaToken token : range) {
                if (token.getCategory().isWhitespaceOrComment()) {
                    continue;
                }
                token.getRange().ifPresent(r -> lines.set(r.begin.line, r.end.line + 1));
            }
        });
        return lines;
    }

    public List<ClassMetric> getClassMetrics() {
        List<ClassMetric> sorted = new ArrayList<>(completedTypes);
        sorted.sort(Comparator.comparingInt(ClassMetric::startLine).thenComparing(ClassMetric::name));
        return sorted;
    }

    // ------------------------------------------------------------------ types

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Void arg) {
        analyseType(n, n.isInterface() ? "INTERFACE" : "CLASS", () -> super.visit(n, arg));
    }

    @Override
    public void visit(EnumDeclaration n, Void arg) {
        analyseType(n, "ENUM", () -> super.visit(n, arg));
    }

    private void analyseType(TypeDeclaration<?> declaration, String kind, Runnable descend) {
        String simpleName = declaration.getNameAsString();
        TypeContext parent = typeStack.peek();
        String qualifiedName = parent == null ? simpleName : parent.name + "." + simpleName;

        TypeContext context = new TypeContext(qualifiedName, kind);
        typeStack.push(context);
        try {
            descend.run();
        } finally {
            typeStack.pop();
        }
        completedTypes.add(buildClassMetric(context, declaration));
    }

    private ClassMetric buildClassMetric(TypeContext context, TypeDeclaration<?> declaration) {
        List<MethodMetric> methods = context.methods;
        int start = beginLine(declaration);
        int end = endLine(declaration);
        int wmc = methods.stream().mapToInt(MethodMetric::cyclomaticComplexity).sum();
        int maxCc = methods.stream().mapToInt(MethodMetric::cyclomaticComplexity).max().orElse(0);
        double avgCc = methods.isEmpty() ? 0.0 : Math.round(wmc * 100.0 / methods.size()) / 100.0;
        int loc = countCodeLines(start, end);

        Assessment risk = riskCalculator.assessAggregate(methods, loc, wmc, RiskCalculator.Scope.CLASS);
        return new ClassMetric(context.name, context.kind, start, end, methods.size(), loc, wmc, maxCc,
                avgCc, risk.score(), risk.level(), risk.factors(), List.copyOf(methods));
    }

    // ------------------------------------------------------------------ methods

    @Override
    public void visit(MethodDeclaration n, Void arg) {
        if (n.getBody().isEmpty()) {           // abstract / interface / native: nothing to measure
            super.visit(n, arg);
            return;
        }
        analyseCallable(n, "METHOD", n.getBody().get(), () -> super.visit(n, arg));
    }

    @Override
    public void visit(ConstructorDeclaration n, Void arg) {
        analyseCallable(n, "CONSTRUCTOR", n.getBody(), () -> super.visit(n, arg));
    }

    private void analyseCallable(CallableDeclaration<?> declaration, String kind, BlockStmt body,
                                 Runnable descend) {
        MethodContext context = new MethodContext();
        methodStack.push(context);
        try {
            descend.run();
        } finally {
            methodStack.pop();
        }

        TypeContext owner = typeStack.peek();
        if (owner == null) {
            return;
        }

        int start = beginLine(declaration);
        int end = endLine(declaration);
        int parameterCount = declaration.getParameters().size();
        int loc = countCodeLines(start, end);

        Assessment risk = riskCalculator.assessMethod(context.cyclomatic, loc, context.maxDepth, parameterCount);
        boolean god = riskCalculator.isGodMethod(context.cyclomatic, loc, parameterCount);

        owner.methods.add(new MethodMetric(
                declaration.getNameAsString(), kind, signatureOf(declaration), start, end,
                context.cyclomatic, Math.max(0, end - start + 1), loc, countStatements(body),
                context.maxDepth, parameterCount, god, risk.score(), risk.level(), risk.factors(),
                risk.breakdown()));
    }

    // ------------------------------------------------------------------ decision points

    @Override
    public void visit(IfStmt n, Void arg) {
        addDecisionPoint();
        if (isElseIf(n)) {
            super.visit(n, arg);
        } else {
            withNesting(() -> super.visit(n, arg));
        }
    }

    @Override
    public void visit(ForStmt n, Void arg) {
        addDecisionPoint();
        withNesting(() -> super.visit(n, arg));
    }

    @Override
    public void visit(ForEachStmt n, Void arg) {
        addDecisionPoint();
        withNesting(() -> super.visit(n, arg));
    }

    @Override
    public void visit(WhileStmt n, Void arg) {
        addDecisionPoint();
        withNesting(() -> super.visit(n, arg));
    }

    @Override
    public void visit(DoStmt n, Void arg) {
        addDecisionPoint();
        withNesting(() -> super.visit(n, arg));
    }

    @Override
    public void visit(SwitchStmt n, Void arg) {
        withNesting(() -> super.visit(n, arg));
    }

    @Override
    public void visit(SwitchEntry n, Void arg) {
        if (!n.getLabels().isEmpty()) {         // "default" has no labels and is not a decision
            addDecisionPoint();
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(TryStmt n, Void arg) {
        withNesting(() -> super.visit(n, arg));
    }

    @Override
    public void visit(CatchClause n, Void arg) {
        addDecisionPoint();
        super.visit(n, arg);
    }

    @Override
    public void visit(SynchronizedStmt n, Void arg) {
        withNesting(() -> super.visit(n, arg));
    }

    @Override
    public void visit(ConditionalExpr n, Void arg) {
        addDecisionPoint();
        super.visit(n, arg);
    }

    @Override
    public void visit(BinaryExpr n, Void arg) {
        if (n.getOperator() == BinaryExpr.Operator.AND || n.getOperator() == BinaryExpr.Operator.OR) {
            addDecisionPoint();
        }
        super.visit(n, arg);
    }

    // ------------------------------------------------------------------ helpers

    private void addDecisionPoint() {
        MethodContext context = methodStack.peek();
        if (context != null) {
            context.cyclomatic++;
        }
    }

    private void withNesting(Runnable descend) {
        MethodContext context = methodStack.peek();
        if (context != null) {
            context.depth++;
            context.maxDepth = Math.max(context.maxDepth, context.depth);
        }
        try {
            descend.run();
        } finally {
            if (context != null) {
                context.depth--;
            }
        }
    }

    private static boolean isElseIf(IfStmt node) {
        return node.getParentNode()
                .filter(parent -> parent instanceof IfStmt)
                .map(parent -> (IfStmt) parent)
                .flatMap(IfStmt::getElseStmt)
                .filter(elseStmt -> elseStmt == node)
                .isPresent();
    }

    private int countCodeLines(int start, int end) {
        if (start <= 0 || end < start) {
            return 0;
        }
        return codeLines.get(start, end + 1).cardinality();
    }

    /** Statement count (blocks excluded); statements of nested anonymous classes are included. */
    private static int countStatements(BlockStmt body) {
        return (int) body.findAll(Statement.class).stream()
                .filter(statement -> !(statement instanceof BlockStmt))
                .count();
    }

    private static String signatureOf(CallableDeclaration<?> declaration) {
        String parameters = declaration.getParameters().stream()
                .map(p -> typeLabel(p.getType()) + (p.isVarArgs() ? "..." : ""))
                .collect(Collectors.joining(", "));
        return declaration.getNameAsString() + "(" + parameters + ")";
    }

    /** Avoid Turkish-locale corruption of primitive names (e.g. int → ınt). */
    private static String typeLabel(com.github.javaparser.ast.type.Type type) {
        if (type.isPrimitiveType()) {
            return type.asPrimitiveType().getType().name().toLowerCase(Locale.ROOT);
        }
        return type.asString();
    }

    private static int beginLine(Node node) {
        return node.getBegin().map(position -> position.line).orElse(0);
    }

    private static int endLine(Node node) {
        return node.getEnd().map(position -> position.line).orElse(0);
    }

    private static final class MethodContext {
        private int cyclomatic = 1;          // base score
        private int depth = 0;
        private int maxDepth = 0;
    }

    private static final class TypeContext {
        private final String name;
        private final String kind;
        private final List<MethodMetric> methods = new ArrayList<>();

        private TypeContext(String name, String kind) {
            this.name = name;
            this.kind = kind;
        }
    }
}
