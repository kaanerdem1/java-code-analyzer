package com.standalone.analyzer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogDbSettingsTest {

    @Test
    void disabledWhenNoJdbc() {
        assertFalse(CatalogDbSettings.resolve(false, null, null).enabled());
        assertFalse(CatalogDbSettings.resolve(true, "jdbc:postgresql://x", null).enabled());
    }

    @Test
    void enabledFromCliJdbc() {
        assertTrue(CatalogDbSettings.resolve(false, "jdbc:postgresql://localhost/db", null).enabled());
    }
}
