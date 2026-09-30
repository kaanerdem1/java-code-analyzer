package com.standalone.analyzer;

import java.util.ArrayList;
import java.util.List;

/** Dosya → paket → sınıf → metod ata zinciri (monorepo / modül kaybı olmasın). */
final class MethodHierarchy {

    private MethodHierarchy() {
    }

    static String moduleRoot(String relativeFilePath) {
        return moduleRoot(null, relativeFilePath);
    }

    static String moduleRoot(ModuleRootIndex index, String relativeFilePath) {
        if (index != null) {
            return index.moduleRoot(relativeFilePath);
        }
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
        return ancestorPath(null, relativeFilePath, packageName, className, methodSignature);
    }

    static List<String> ancestorPath(ModuleRootIndex index, String relativeFilePath, String packageName,
                                   String className, String methodSignature) {
        List<String> chain = new ArrayList<>(4);
        chain.add(moduleRoot(index, relativeFilePath));
        chain.add(relativeFilePath);
        chain.add(qualifiedClass(packageName, className));
        chain.add(methodSignature);
        return List.copyOf(chain);
    }

    static String breadcrumb(List<String> ancestorPath) {
        return String.join(" › ", ancestorPath);
    }

    /** Tam nitelikli sınıf + metod imzası (JSON / arama). */
    static String compactMethodLabel(String packageName, String className, String methodSignature) {
        return qualifiedClass(packageName, className) + "." + methodSignature;
    }

    /** Ana tablo: sınıf (servis) adı + metod imzası — paket ve dosya yolu yok. */
    static String shortMethodLabel(String className, String methodSignature) {
        return className + "." + methodSignature;
    }
}
