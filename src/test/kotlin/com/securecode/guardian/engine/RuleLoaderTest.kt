package com.securecode.guardian.engine

import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class RuleLoaderTest {

    @Test
    fun testLoadBuiltInRules() {
        val ruleLoader = RuleLoader()
        val rules = ruleLoader.loadBuiltInRules()

        assertTrue(rules.isNotEmpty(), "Should load at least one built-in rule")
        assertTrue(rules.size >= 10, "Should have at least 10 built-in rules")

        // Check that specific critical rules exist
        val hardcodedSecrets = rules.find { it.id == "SEC-001" }
        assertNotNull(hardcodedSecrets, "Should have hardcoded secrets rule")

        val sqlInjection = rules.find { it.id == "SEC-002" }
        assertNotNull(sqlInjection, "Should have SQL injection rule")
    }

    @Test
    fun testRuleStructure() {
        val ruleLoader = RuleLoader()
        val rules = ruleLoader.loadBuiltInRules()

        for (rule in rules) {
            assertNotNull(rule.id, "Rule should have an ID")
            assertNotNull(rule.name, "Rule should have a name")
            assertNotNull(rule.description, "Rule should have a description")
            assertTrue(rule.patterns.isNotEmpty(), "Rule should have at least one pattern")
        }
    }
}
