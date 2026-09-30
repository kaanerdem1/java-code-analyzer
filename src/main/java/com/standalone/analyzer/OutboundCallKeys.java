package com.standalone.analyzer;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;

import java.util.Optional;

/** Heuristic keys for distinct outbound calls (no symbol resolution). */
final class OutboundCallKeys {

    private OutboundCallKeys() {
    }

    static String methodCall(MethodCallExpr call) {
        String method = call.getNameAsString();
        return call.getScope()
                .map(scope -> expressionLabel(scope) + "." + method)
                .orElse("<local>." + method);
    }

    static String objectCreation(ObjectCreationExpr creation) {
        return "new " + creation.getType().asString();
    }

    static int methodCallChainLength(MethodCallExpr call) {
        int length = 1;
        var scope = call.getScope();
        while (scope.isPresent()) {
            Expression expression = scope.get();
            if (expression instanceof MethodCallExpr nested) {
                length++;
                scope = nested.getScope();
            } else if (expression instanceof FieldAccessExpr field) {
                length++;
                scope = optionalScope(field.getScope());
            } else {
                break;
            }
        }
        return length;
    }

    private static String expressionLabel(Expression expr) {
        if (expr.isNameExpr()) {
            return expr.asNameExpr().getNameAsString();
        }
        if (expr.isFieldAccessExpr()) {
            FieldAccessExpr field = expr.asFieldAccessExpr();
            Expression scope = field.getScope();
            if (scope != null) {
                return expressionLabel(scope) + "." + field.getNameAsString();
            }
            return field.getNameAsString();
        }
        if (expr.isMethodCallExpr()) {
            return methodCall(expr.asMethodCallExpr());
        }
        if (expr.isThisExpr()) {
            return "this";
        }
        if (expr.isEnclosedExpr()) {
            return expressionLabel(expr.asEnclosedExpr().getInner());
        }
        if (expr.isClassExpr()) {
            return expr.asClassExpr().getType().asString();
        }
        if (expr.isObjectCreationExpr()) {
            return objectCreation(expr.asObjectCreationExpr());
        }
        return expr.toString();
    }

    private static Optional<Expression> optionalScope(Expression scope) {
        return scope == null ? Optional.empty() : Optional.of(scope);
    }
}
