package com.securecode.guardian.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

/**
 * Factory for creating the SecureCode Guardian tool window
 */
class GuardianToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val guardianPanel = GuardianToolWindowPanel(project)
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(guardianPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project): Boolean = true
}
