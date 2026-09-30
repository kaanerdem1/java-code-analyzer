package com.standalone.analyzer;

import com.github.javaparser.JavaToken;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.CompactConstructorDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.ThrowStmt;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
    private CompilationUnit currentCompilationUnit;

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

    @Override
    public void visit(CompilationUnit n, Void arg) {
        currentCompilationUnit = n;
        super.visit(n, arg);
    }

    public List<ClassMetric> getClassMetrics() {
        List<ClassMetric> sorted = new ArrayList<>(completedTypes);
        sorted.sort(Comparator.comparingInt(ClassMetric::startLine).thenComparing(ClassMetric::name));
        return sorted;
    }

    // ------------------------------------------------------------------ types

    @Override
    public void visit(ClassOrInterfaceDeclaration n, Void arg) {
        boolean anonymous = isAnonymousClassDeclaration(n);
        String kind = n.isInterface() ? "INTERFACE" : anonymous ? "ANONYMOUS_CLASS" : "CLASS";
        analyseType(n, kind, anonymous, () -> super.visit(n, arg));
    }

    @Override
    public void visit(EnumDeclaration n, Void arg) {
        analyseType(n, "ENUM", false, () -> super.visit(n, arg));
    }

    @Override
    public void visit(RecordDeclaration n, Void arg) {
        analyseType(n, "RECORD", false, () -> super.visit(n, arg));
    }

    @Override
    public void visit(InitializerDeclaration n, Void arg) {
        TypeContext owner = typeStack.peek();
        if (owner == null) {
            super.visit(n, arg);
            return;
        }
        BlockStmt body = n.getBody();
        MethodContext context = new MethodContext();
        methodStack.push(context);
        try {
            body.accept(this, arg);
        } finally {
            methodStack.pop();
        }
        int start = beginLine(n);
        int end = endLine(n);
        int loc = countCodeLines(start, end);
        String name = n.isStatic() ? "<static-init>" : "<instance-init>";
        String kind = n.isStatic() ? "STATIC_INITIALIZER" : "INSTANCE_INITIALIZER";
        owner.methods.add(buildMethodMetric(name, kind, name + "()", start, end, body, 0, context, owner));
    }

    private void analyseType(TypeDeclaration<?> declaration, String kind, boolean anonymous, Runnable descend) {
        String simpleName = declaration.getNameAsString();
        TypeContext parent = typeStack.peek();
        String qualifiedName = parent == null ? simpleName : parent.name + "." + simpleName;

        TypeContext context = new TypeContext(qualifiedName, kind, anonymous);
        typeStack.push(context);
        try {
            descend.run();
        } finally {
            typeStack.pop();
        }
        completedTypes.add(buildClassMetric(context, declaration));
    }

    private ClassMetric buildClassMetricFromContext(TypeContext context, int start, int end) {
        List<MethodMetric> methods = context.methods;
        int wmc = methods.stream().mapToInt(MethodMetric::cyclomaticComplexity).sum();
        int maxCc = methods.stream().mapToInt(MethodMetric::cyclomaticComplexity).max().orElse(0);
        double avgCc = methods.isEmpty() ? 0.0 : Math.round(wmc * 100.0 / methods.size()) / 100.0;
        int spanLoc = countCodeLines(start, end);
        int methodLoc = methods.stream().mapToInt(MethodMetric::codeLines).sum();
        int loc = methods.isEmpty() ? spanLoc : methodLoc;
        Assessment risk = riskCalculator.assessClassAggregate(
                methods, loc, wmc, context.publicMethodCount, context.efferentCouplingProxy);
        return new ClassMetric(context.name, context.kind, start, end, methods.size(),
                context.publicMethodCount, loc, context.efferentCouplingProxy, wmc, maxCc,
                avgCc, risk.score(), risk.level(), risk.factors(), List.copyOf(methods));
    }

    private ClassMetric buildClassMetric(TypeContext context, TypeDeclaration<?> declaration) {
        List<MethodMetric> methods = context.methods;
        int start = beginLine(declaration);
        int end = endLine(declaration);
        int wmc = methods.stream().mapToInt(MethodMetric::cyclomaticComplexity).sum();
        int maxCc = methods.stream().mapToInt(MethodMetric::cyclomaticComplexity).max().orElse(0);
        double avgCc = methods.isEmpty() ? 0.0 : Math.round(wmc * 100.0 / methods.size()) / 100.0;
        int spanLoc = countCodeLines(start, end);
        int methodLoc = methods.stream().mapToInt(MethodMetric::codeLines).sum();
        int loc = methods.isEmpty() ? spanLoc : methodLoc;
        int ce = ClassEfferentCoupling.distinctReferencedTypes(declaration, currentCompilationUnit);

        Assessment risk = riskCalculator.assessClassAggregate(
                methods, loc, wmc, context.publicMethodCount, ce);
        return new ClassMetric(context.name, context.kind, start, end, methods.size(),
                context.publicMethodCount, loc, ce, wmc, maxCc,
                avgCc, risk.score(), risk.level(), risk.factors(), List.copyOf(methods));
    }

    // ------------------------------------------------------------------ methods

    @Override
    public void visit(MethodDeclaration n, Void arg) {
        countPublicApiMethod(n);
        if (n.getBody().isEmpty()) {           // abstract / interface / native: nothing to measure
            super.visit(n, arg);
            return;
        }
        analyseCallable(n, "METHOD", n.getBody().get(), () -> super.visit(n, arg));
    }

    @Override
    public void visit(ConstructorDeclaration n, Void arg) {
        countPublicApiConstructor(n);
        analyseCallable(n, "CONSTRUCTOR", n.getBody(), () -> super.visit(n, arg));
    }

    @Override
    public void visit(CompactConstructorDeclaration n, Void arg) {
        TypeContext owner = typeStack.peek();
        if (owner != null) {
            owner.publicMethodCount++;
        }
        MethodContext context = new MethodContext();
        methodStack.push(context);
        try {
            n.getBody().accept(this, arg);
        } finally {
            methodStack.pop();
        }
        if (owner == null) {
            return;
        }
        int start = beginLine(n);
        int end = endLine(n);
        int loc = countCodeLines(start, end);
        String name = n.getNameAsString();
        owner.methods.add(buildMethodMetric(name, "COMPACT_CONSTRUCTOR", name + "()", start, end,
                n.getBody(), 0, context, owner));
    }

    @Override
    public void visit(ObjectCreationExpr n, Void arg) {
        if (n.getAnonymousClassBody().isPresent()) {
            n.getArguments().forEach(argExpr -> argExpr.accept(this, arg));
            analyseAnonymousClassBody(n, arg);
            return;
        }
        MethodContext context = methodStack.peek();
        if (context != null) {
            context.outboundCallKeys.add(OutboundCallKeys.objectCreation(n));
        }
        super.visit(n, arg);
    }

    private void analyseAnonymousClassBody(ObjectCreationExpr creation, Void arg) {
        TypeContext parent = typeStack.peek();
        int line = beginLine(creation);
        String name = parent == null ? "$anon@" + line : parent.name + ".$" + line;
        TypeContext context = new TypeContext(name, "ANONYMOUS_CLASS", true);
        typeStack.push(context);
        try {
            for (BodyDeclaration<?> member : creation.getAnonymousClassBody().get()) {
                member.accept(this, arg);
            }
        } finally {
            typeStack.pop();
        }
        context.efferentCouplingProxy = ClassEfferentCoupling.distinctReferencedTypes(
                creation, currentCompilationUnit);
        int start = beginLine(creation);
        int end = endLine(creation);
        completedTypes.add(buildClassMetricFromContext(context, start, end));
    }

    @Override
    public void visit(LambdaExpr n, Void arg) {
        if (methodStack.isEmpty() && typeStack.peek() != null && isFieldInitializerLambda(n)) {
            analyseFieldLambda(n, arg);
            return;
        }
        MethodContext context = methodStack.peek();
        if (context != null) {
            context.lambdaCount++;
        }
        super.visit(n, arg);
    }

    private void analyseFieldLambda(LambdaExpr lambda, Void arg) {
        TypeContext owner = typeStack.peek();
        if (owner == null) {
            return;
        }
        MethodContext context = new MethodContext();
        methodStack.push(context);
        try {
            lambda.getBody().accept(this, arg);
        } finally {
            methodStack.pop();
        }
        int start = beginLine(lambda);
        int end = endLine(lambda);
        int loc = countCodeLines(start, end);
        String fieldName = lambda.findAncestor(VariableDeclarator.class)
                .map(VariableDeclarator::getNameAsString)
                .orElse("<field-lambda>");
        String signature = fieldName + "=<lambda> @" + start;
        owner.methods.add(buildMethodMetric(fieldName, "FIELD_LAMBDA", signature, start, end,
                null, 0, context, owner, lambda.getBody()));
    }

    private static boolean isAnonymousClassDeclaration(ClassOrInterfaceDeclaration n) {
        return n.getParentNode().filter(ObjectCreationExpr.class::isInstance).isPresent();
    }

    private void countPublicApiMethod(MethodDeclaration method) {
        TypeContext owner = typeStack.peek();
        if (owner == null) {
            return;
        }
        if (method.isPublic() || ("INTERFACE".equals(owner.kind) && !method.isPrivate())) {
            owner.publicMethodCount++;
        }
    }

    private void countPublicApiConstructor(ConstructorDeclaration constructor) {
        TypeContext owner = typeStack.peek();
        if (owner != null && constructor.isPublic()) {
            owner.publicMethodCount++;
        }
    }

    private static boolean isFieldInitializerLambda(LambdaExpr lambda) {
        return lambda.findAncestor(VariableDeclarator.class)
                .flatMap(VariableDeclarator::getInitializer)
                .filter(init -> init == lambda)
                .isPresent();
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

        String signature = signatureOf(declaration);
        if (owner.anonymous) {
            signature = signature + " @" + start;
        }
        owner.methods.add(buildMethodMetric(declaration.getNameAsString(), kind, signature, start, end,
                body, parameterCount, context, owner));
    }

    private MethodMetric buildMethodMetric(String name, String kind, String signature, int start, int end,
                                           BlockStmt body, int parameterCount, MethodContext context,
                                           TypeContext owner) {
        return buildMethodMetric(name, kind, signature, start, end, body, parameterCount, context, owner, body);
    }

    private MethodMetric buildMethodMetric(String name, String kind, String signature, int start, int end,
                                           BlockStmt body, int parameterCount, MethodContext context,
                                           TypeContext owner, Node cognitiveRoot) {
        int endLine = end;
        int loc = countCodeLines(start, endLine);
        Node root = cognitiveRoot != null ? cognitiveRoot : body;
        int cognitive = CognitiveComplexity.of(root);
        int statements = body != null ? countStatements(body) : 0;
        MethodScanValues scan = new MethodScanValues(
                context.cyclomatic, loc, context.maxDepth, parameterCount, cognitive, statements,
                context.exitPoints, context.catchClauses, context.switchCases,
                context.outboundCallKeys.size(), context.lambdaCount, context.maxTryDepth,
                context.localVariableCount, context.maxMethodCallChainLength,
                context.emptyCatchBlocks, context.catchExceptionOrThrowable,
                context.catchWithOnlyPrintStackTrace);
        Assessment risk = riskCalculator.assessMethod(scan);
        boolean god = riskCalculator.isGodMethod(scan);
        return new MethodMetric(
                name, kind, signature, start, endLine,
                context.cyclomatic, Math.max(0, endLine - start + 1), loc, statements,
                cognitive, context.exitPoints, context.catchClauses, context.switchCases,
                context.outboundCallKeys.size(), context.lambdaCount, context.maxTryDepth,
                context.localVariableCount, context.maxMethodCallChainLength,
                context.emptyCatchBlocks, context.catchExceptionOrThrowable,
                context.catchWithOnlyPrintStackTrace,
                context.maxDepth, parameterCount, god, risk.score(), risk.level(), risk.factors(),
                risk.breakdown());
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
            addSwitchCase();
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(TryStmt n, Void arg) {
        withTryNesting(() -> withNesting(() -> super.visit(n, arg)));
    }

    @Override
    public void visit(CatchClause n, Void arg) {
        addDecisionPoint();
        addCatchClause();
        MethodContext context = methodStack.peek();
        if (context != null) {
            if (CatchClauseMetrics.isEmpty(n)) {
                context.emptyCatchBlocks++;
            }
            if (CatchClauseMetrics.catchesExceptionOrThrowable(n)) {
                context.catchExceptionOrThrowable++;
            }
            if (CatchClauseMetrics.isPrintStackTraceOnly(n)) {
                context.catchWithOnlyPrintStackTrace++;
            }
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(ReturnStmt n, Void arg) {
        addExitPoint();
        super.visit(n, arg);
    }

    @Override
    public void visit(BreakStmt n, Void arg) {
        addExitPoint();
        super.visit(n, arg);
    }

    @Override
    public void visit(ContinueStmt n, Void arg) {
        addExitPoint();
        super.visit(n, arg);
    }

    @Override
    public void visit(ThrowStmt n, Void arg) {
        addExitPoint();
        super.visit(n, arg);
    }

    @Override
    public void visit(MethodCallExpr n, Void arg) {
        MethodContext context = methodStack.peek();
        if (context != null) {
            context.outboundCallKeys.add(OutboundCallKeys.methodCall(n));
            context.maxMethodCallChainLength = Math.max(context.maxMethodCallChainLength,
                    OutboundCallKeys.methodCallChainLength(n));
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(VariableDeclarationExpr n, Void arg) {
        MethodContext context = methodStack.peek();
        if (context != null) {
            context.localVariableCount += n.getVariables().size();
        }
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

    private void addExitPoint() {
        MethodContext context = methodStack.peek();
        if (context != null) {
            context.exitPoints++;
        }
    }

    private void addCatchClause() {
        MethodContext context = methodStack.peek();
        if (context != null) {
            context.catchClauses++;
        }
    }

    private void addSwitchCase() {
        MethodContext context = methodStack.peek();
        if (context != null) {
            context.switchCases++;
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

    private void withTryNesting(Runnable descend) {
        MethodContext context = methodStack.peek();
        if (context != null) {
            context.tryDepth++;
            context.maxTryDepth = Math.max(context.maxTryDepth, context.tryDepth);
        }
        try {
            descend.run();
        } finally {
            if (context != null) {
                context.tryDepth--;
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
        private int exitPoints;
        private int catchClauses;
        private int switchCases;
        private final Set<String> outboundCallKeys = new HashSet<>();
        private int lambdaCount;
        private int tryDepth;
        private int maxTryDepth;
        private int localVariableCount;
        private int maxMethodCallChainLength;
        private int emptyCatchBlocks;
        private int catchExceptionOrThrowable;
        private int catchWithOnlyPrintStackTrace;
    }

    private static final class TypeContext {
        private final String name;
        private final String kind;
        private final boolean anonymous;
        private int publicMethodCount;
        private int efferentCouplingProxy;
        private final List<MethodMetric> methods = new ArrayList<>();

        private TypeContext(String name, String kind, boolean anonymous) {
            this.name = name;
            this.kind = kind;
            this.anonymous = anonymous;
        }
    }
}
