package com.securecode.guardian.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.securecode.guardian.engine.RuleLoader
import com.securecode.guardian.model.Rule
import java.nio.file.Paths

/**
 * Service for managing security rules
 */
@Service(Service.Level.PROJECT)
class RuleEngineService(private val project: Project) {

    private val ruleLoader = RuleLoader()

    /**
     * Get all available rules (built-in + custom)
     */
    fun getAllRules(): List<Rule> {
        val projectPath = project.basePath
        val customRulesPath = if (projectPath != null) {
            Paths.get(projectPath, ".securecode", "rules").toString()
        } else {
            null
        }

        return ruleLoader.loadAllRules(customRulesPath)
    }

    /**
     * Get only built-in rules
     */
    fun getBuiltInRules(): List<Rule> {
        return ruleLoader.loadBuiltInRules()
    }

    /**
     * Get only custom rules
     */
    fun getCustomRules(): List<Rule> {
        val projectPath = project.basePath ?: return emptyList()
        val customRulesPath = Paths.get(projectPath, ".securecode", "rules").toFile()
        return ruleLoader.loadCustomRules(customRulesPath)
    }

    /**
     * Get rule by ID
     */
    fun getRuleById(ruleId: String): Rule? {
        return getAllRules().find { it.id == ruleId }
    }

    /**
     * Get rules by category
     */
    fun getRulesByCategory(category: com.securecode.guardian.model.RuleCategory): List<Rule> {
        return getAllRules().filter { it.category == category }
    }

    /**
     * Get rules by severity
     */
    fun getRulesBySeverity(severity: com.securecode.guardian.model.Severity): List<Rule> {
        return getAllRules().filter { it.severity == severity }
    }
}
