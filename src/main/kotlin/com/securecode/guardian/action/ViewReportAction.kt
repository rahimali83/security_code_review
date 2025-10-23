package com.securecode.guardian.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager

/**
 * Action to view the latest scan report
 */
class ViewReportAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("SecureCode Guardian")
        toolWindow?.show()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}
