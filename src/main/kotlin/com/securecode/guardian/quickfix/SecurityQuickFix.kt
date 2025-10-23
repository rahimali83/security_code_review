package com.securecode.guardian.quickfix

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.securecode.guardian.model.QuickFix
import com.securecode.guardian.model.QuickFixType

/**
 * Quick fix implementation for security vulnerabilities
 */
class SecurityQuickFix(
    private val quickFix: QuickFix,
    private val element: PsiElement
) : LocalQuickFix {

    override fun getName(): String = quickFix.description

    override fun getFamilyName(): String = "SecureCode Guardian"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        when (quickFix.type) {
            QuickFixType.REMOVE -> {
                // Remove the problematic element
                element.delete()
            }
            QuickFixType.REPLACE -> {
                // Replace with suggested text
                quickFix.replacement?.let { replacement ->
                    // This would require PSI manipulation based on language
                    // Simplified implementation
                }
            }
            QuickFixType.SUGGEST -> {
                // Show suggestion to user (already done via description)
            }
            QuickFixType.REFACTOR -> {
                // Trigger refactoring action
            }
        }
    }
}
