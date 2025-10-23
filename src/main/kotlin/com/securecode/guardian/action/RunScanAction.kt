package com.securecode.guardian.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.securecode.guardian.model.ScanConfig
import com.securecode.guardian.service.ScanService
import com.securecode.guardian.ui.GuardianToolWindowPanel

/**
 * Action to trigger a security scan
 */
class RunScanAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Running SecureCode Scan", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Initializing SecureCode Guardian..."
                indicator.isIndeterminate = false

                try {
                    val scanService = project.getService(ScanService::class.java)

                    indicator.text = "Loading security rules..."
                    indicator.fraction = 0.1

                    indicator.text = "Scanning project files..."
                    indicator.fraction = 0.3

                    val config = ScanConfig()
                    val report = scanService.executeScan(config)

                    indicator.text = "Generating report..."
                    indicator.fraction = 0.9

                    indicator.text = "Scan complete!"
                    indicator.fraction = 1.0

                    // Refresh the tool window
                    refreshToolWindow(project)

                    // Show notification
                    showNotification(project, report.summary.total)

                } catch (ex: Exception) {
                    showErrorNotification(project, ex.message ?: "Unknown error")
                }
            }
        })
    }

    private fun refreshToolWindow(project: Project) {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("SecureCode Guardian")
        toolWindow?.contentManager?.getContent(0)?.component?.let { component ->
            if (component is GuardianToolWindowPanel) {
                component.refreshView()
            }
        }
        toolWindow?.show()
    }

    private fun showNotification(project: Project, vulnerabilityCount: Int) {
        // Simplified notification - in real implementation would use NotificationGroup
        println("Scan complete! Found $vulnerabilityCount vulnerabilities.")
    }

    private fun showErrorNotification(project: Project, message: String) {
        println("Scan failed: $message")
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.project != null
    }
}
