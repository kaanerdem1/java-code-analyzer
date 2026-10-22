package com.standalone.analyzer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CatalogInventoryMethodResolverTest {

    @Test
    void fqcnFromMainJavaPath() {
        String path = "module-auth/src/main/java/com/mock/auth/AuthFacade.java";
        assertEquals("com.mock.auth.AuthFacade",
                CatalogInventoryMethodResolver.fqcnFromJavaFilePath(path, "AuthFacade"));
    }

    @Test
    void fqcnWhenClassNameAlreadyFqcn() {
        assertEquals("com.example.Foo",
                CatalogInventoryMethodResolver.fqcnFromJavaFilePath("any/where/Foo.java", "com.example.Foo"));
    }

    @Test
    void methodSimpleNameAndParamCount() {
        assertEquals("login", CatalogInventoryMethodResolver.methodSimpleName("login(String, String)"));
        assertEquals(2, CatalogInventoryMethodResolver.paramCountFromSignature("login(String, String)"));
        assertEquals(0, CatalogInventoryMethodResolver.paramCountFromSignature("run()"));
    }
}
