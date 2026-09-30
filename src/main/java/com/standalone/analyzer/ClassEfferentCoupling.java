package com.standalone.analyzer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;

import java.util.HashSet;
import java.util.Set;

/** CK Ce proxy: distinct referenced type names in a type (no symbol solver). */
final class ClassEfferentCoupling {

    private static final Set<String> IGNORED_SIMPLE = Set.of(
            "void", "boolean", "byte", "char", "short", "int", "long", "float", "double",
            "String", "Object", "Class", "Void", "Boolean", "Integer", "Long", "Double",
            "Exception", "RuntimeException", "Throwable", "Error");

    private ClassEfferentCoupling() {
    }

    static int distinctReferencedTypes(TypeDeclaration<?> type, CompilationUnit unit) {
        return distinctReferencedTypes((Node) type, unit);
    }

    static int distinctReferencedTypes(Node root, CompilationUnit unit) {
        Set<String> keys = new HashSet<>();
        root.findAll(ClassOrInterfaceType.class).forEach(type -> addType(keys, type));
        root.findAll(ObjectCreationExpr.class).forEach(creation -> addType(keys, creation.getType()));
        if (unit != null) {
            addMatchingImports(keys, root, unit);
        }
        return keys.size();
    }

    private static void addMatchingImports(Set<String> keys, Node root, CompilationUnit unit) {
        Set<String> names = new HashSet<>();
        root.findAll(NameExpr.class).forEach(name -> names.add(name.getNameAsString()));
        unit.getImports().forEach(importDecl -> {
            if (importDecl.isAsterisk() || importDecl.isStatic()) {
                return;
            }
            String fqcn = importDecl.getNameAsString();
            int dot = fqcn.lastIndexOf('.');
            String simple = dot >= 0 ? fqcn.substring(dot + 1) : fqcn;
            if (names.contains(simple)) {
                keys.add(fqcn);
            }
        });
    }

    private static void addType(Set<String> keys, Type type) {
        if (type instanceof PrimitiveType) {
            return;
        }
        if (type instanceof ClassOrInterfaceType classType) {
            String raw = classType.asString();
            int generic = raw.indexOf('<');
            if (generic >= 0) {
                raw = raw.substring(0, generic);
            }
            raw = raw.trim();
            if (raw.isEmpty()) {
                return;
            }
            String simple = raw.contains(".") ? raw.substring(raw.lastIndexOf('.') + 1) : raw;
            if (IGNORED_SIMPLE.contains(simple)) {
                return;
            }
            if (raw.startsWith("java.lang.") && IGNORED_SIMPLE.contains(simple)) {
                return;
            }
            keys.add(raw);
        }
    }
}
