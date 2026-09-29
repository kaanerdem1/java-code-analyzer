package com.standalone.analyzer;

import java.util.ArrayList;
import java.util.List;

/** Dosya → paket → sınıf → metod ata zinciri (monorepo / modül kaybı olmasın). */
final class MethodHierarchy {

    private MethodHierarchy() {
    }

    static String moduleRoot(String relativeFilePath) {
        if (relativeFilePath == null || relativeFilePath.isEmpty()) {
            return "";
        }
        int slash = relativeFilePath.indexOf('/');
        return slash > 0 ? relativeFilePath.substring(0, slash) : relativeFilePath;
    }

    static String qualifiedClass(String packageName, String className) {
        return packageName == null || packageName.isEmpty() ? className : packageName + "." + className;
    }

    static List<String> ancestorPath(String relativeFilePath, String packageName, String className,
                                       String methodSignature) {
        List<String> chain = new ArrayList<>(4);
        chain.add(moduleRoot(relativeFilePath));
        chain.add(relativeFilePath);
        chain.add(qualifiedClass(packageName, className));
        chain.add(methodSignature);
        return List.copyOf(chain);
    }

    static String breadcrumb(List<String> ancestorPath) {
        return String.join(" › ", ancestorPath);
    }
}
