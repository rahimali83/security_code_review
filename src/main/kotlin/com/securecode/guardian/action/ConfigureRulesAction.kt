package com.securecode.guardian.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File
import java.nio.file.Paths

/**
 * Action to configure custom rules
 */
class ConfigureRulesAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val projectPath = project.basePath ?: return

        // Create custom rules directory if it doesn't exist
        val rulesDir = Paths.get(projectPath, ".securecode", "rules").toFile()
        if (!rulesDir.exists()) {
            rulesDir.mkdirs()

            // Create a template custom rule
            val templateFile = File(rulesDir, "custom-rule-template.yaml")
            templateFile.writeText(createRuleTemplate())
        }

        // Open the rules directory
        val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(rulesDir)
        if (virtualFile != null) {
            virtualFile.refresh(false, true)
            FileEditorManager.getInstance(project).openFile(virtualFile, true)
        }
    }

    private fun createRuleTemplate(): String {
        return """
# Custom Security Rule Template
# Copy this file and modify it to create your own custom rules

id: CUSTOM-001
name: Your Custom Rule Name
description: Detailed description of what this rule checks for
severity: high  # critical, high, medium, low, info
category: security  # security, compliance, quality, documentation, etc.

# Compliance mappings (optional)
compliance:
  - framework: pci_dss
    control: "6.5"
    requirement: "Your compliance requirement"
  - framework: owasp
    control: "A01:2021"
    requirement: "Relevant OWASP category"

# Pattern matching rules
patterns:
  # Regex-based pattern
  - type: regex
    pattern: 'your-regex-pattern-here'
    fileTypes: ["java", "kt", "py", "js", "ts"]
    message: "Custom message when this pattern is found"

# Quick fix suggestion (optional)
quickFix:
  type: suggest  # remove, replace, suggest, refactor
  description: "Suggested fix for this issue"
  replacement: "replacement-text-if-applicable"

enabled: true
custom: true
        """.trimIndent()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}
