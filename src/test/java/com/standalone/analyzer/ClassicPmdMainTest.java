package com.standalone.analyzer;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.PmdAnalysis;
import net.sourceforge.pmd.lang.rule.RuleSet;
import net.sourceforge.pmd.lang.rule.RuleSetLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassicPmdMainTest {

    @Test
    void rulesetLoadsFromConfigFile() throws Exception {
        Path ruleset = Path.of("config/pmd-ruleset-complexity.xml");
        if (!ruleset.toFile().isFile()) {
            return;
        }
        PMDConfiguration config = new PMDConfiguration();
        String rsPath = ruleset.toAbsolutePath().normalize().toString();
        config.addRuleSet(rsPath);
        try (PmdAnalysis analysis = PmdAnalysis.create(config)) {
            RuleSet loaded = RuleSetLoader.fromPmdConfig(config).loadFromResource(rsPath);
            analysis.addRuleSet(loaded);
            int rules = loaded.getRules().size();
            assertTrue(rules >= 15, "expected expanded enterprise ruleset, got " + rules);
        }
    }

    @Test
    void classicScanProducesViolationsOnMockModules() throws Exception {
        Path root = Path.of("mock-modules");
        if (!root.toFile().isDirectory()) {
            return;
        }
        Path out = Path.of("target/classic-pmd-mock-test.xml");
        int code = ClassicPmdMain.run(new String[] {
                "--path=" + root,
                "--output=" + out
        });
        assertTrue(out.toFile().length() > 400, "xml too small");
        String xml = java.nio.file.Files.readString(out);
        assertTrue(xml.contains("violation") || code == 4,
                "expected violations in mock-modules, exit=" + code);
    }
}
