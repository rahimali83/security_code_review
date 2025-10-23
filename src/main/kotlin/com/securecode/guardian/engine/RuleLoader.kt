package com.securecode.guardian.engine

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.securecode.guardian.model.Rule
import java.io.File
import java.io.InputStream

/**
 * Loads security rules from YAML files
 */
class RuleLoader {
    private val yamlMapper = ObjectMapper(YAMLFactory()).apply {
        registerModule(KotlinModule.Builder().build())
    }

    /**
     * Load all built-in rules from the plugin resources
     */
    fun loadBuiltInRules(): List<Rule> {
        val rules = mutableListOf<Rule>()
        val resourcePath = "/rules/"

        try {
            // Get all YAML files from resources/rules/
            val ruleFiles = listOf(
                "hardcoded-secrets.yaml",
                "sql-injection.yaml",
                "weak-crypto.yaml",
                "empty-catch.yaml",
                "insecure-http.yaml",
                "command-injection.yaml",
                "path-traversal.yaml",
                "ssrf.yaml",
                "insecure-deserialization.yaml",
                "missing-auth.yaml"
            )

            for (fileName in ruleFiles) {
                val stream = this::class.java.getResourceAsStream("$resourcePath$fileName")
                if (stream != null) {
                    try {
                        val rule = loadRuleFromStream(stream)
                        rules.add(rule)
                    } catch (e: Exception) {
                        println("Error loading built-in rule $fileName: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            println("Error loading built-in rules: ${e.message}")
        }

        return rules
    }

    /**
     * Load custom rules from a project directory
     */
    fun loadCustomRules(rulesDirectory: File): List<Rule> {
        val rules = mutableListOf<Rule>()

        if (!rulesDirectory.exists() || !rulesDirectory.isDirectory) {
            return rules
        }

        rulesDirectory.listFiles { file ->
            file.extension in listOf("yaml", "yml")
        }?.forEach { file ->
            try {
                val rule = loadRuleFromFile(file).copy(custom = true)
                rules.add(rule)
            } catch (e: Exception) {
                println("Error loading custom rule ${file.name}: ${e.message}")
            }
        }

        return rules
    }

    /**
     * Load a single rule from a file
     */
    fun loadRuleFromFile(file: File): Rule {
        return yamlMapper.readValue(file, Rule::class.java)
    }

    /**
     * Load a single rule from an input stream
     */
    fun loadRuleFromStream(stream: InputStream): Rule {
        return yamlMapper.readValue(stream, Rule::class.java)
    }

    /**
     * Load all rules (built-in + custom)
     */
    fun loadAllRules(customRulesPath: String? = null): List<Rule> {
        val allRules = mutableListOf<Rule>()

        // Load built-in rules
        allRules.addAll(loadBuiltInRules())

        // Load custom rules if path provided
        if (customRulesPath != null) {
            val customDir = File(customRulesPath)
            allRules.addAll(loadCustomRules(customDir))
        }

        return allRules.filter { it.enabled }
    }
}
