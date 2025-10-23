package com.securecode.guardian.ui

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.securecode.guardian.model.ScanReport
import com.securecode.guardian.model.Severity
import com.securecode.guardian.model.Vulnerability
import com.securecode.guardian.model.VulnerabilityStatus
import com.securecode.guardian.service.ScanService
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.*
import javax.swing.table.DefaultTableModel

/**
 * Main panel for the SecureCode Guardian tool window
 */
class GuardianToolWindowPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val scanService = project.getService(ScanService::class.java)
    private val vulnerabilityTable: JBTable
    private val tableModel: DefaultTableModel
    private val summaryPanel: JPanel

    init {
        // Create summary panel
        summaryPanel = createSummaryPanel()
        add(summaryPanel, BorderLayout.NORTH)

        // Create vulnerability table
        tableModel = DefaultTableModel(
            arrayOf("Status", "Severity", "Category", "File", "Line", "Description"),
            0
        ) {
            override fun isCellEditable(row: Int, column: Int) = false
        }

        vulnerabilityTable = JBTable(tableModel).apply {
            setShowGrid(true)
            autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS
        }

        val scrollPane = JBScrollPane(vulnerabilityTable)
        add(scrollPane, BorderLayout.CENTER)

        // Load latest report
        refreshView()
    }

    private fun createSummaryPanel(): JPanel {
        return JPanel(GridLayout(2, 4, 10, 5)).apply {
            border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

            // Labels will be populated in refreshView()
            add(JLabel("Total: 0"))
            add(JLabel("New: 0"))
            add(JLabel("Persistent: 0"))
            add(JLabel("Closed: 0"))
            add(JLabel("Critical: 0"))
            add(JLabel("High: 0"))
            add(JLabel("Medium: 0"))
            add(JLabel("Low: 0"))
        }
    }

    fun refreshView() {
        val report = scanService.getLatestReport()

        if (report != null) {
            updateSummary(report)
            updateTable(report)
        } else {
            clearView()
        }
    }

    private fun updateSummary(report: ScanReport) {
        val components = summaryPanel.components

        if (components.size >= 8) {
            (components[0] as JLabel).text = "Total: ${report.summary.total}"
            (components[1] as JLabel).text = "New: ${report.summary.new}"
            (components[2] as JLabel).text = "Persistent: ${report.summary.persistent}"
            (components[3] as JLabel).text = "Closed: ${report.summary.closed}"
            (components[4] as JLabel).text = "Critical: ${report.summary.bySeverity[Severity.CRITICAL] ?: 0}"
            (components[5] as JLabel).text = "High: ${report.summary.bySeverity[Severity.HIGH] ?: 0}"
            (components[6] as JLabel).text = "Medium: ${report.summary.bySeverity[Severity.MEDIUM] ?: 0}"
            (components[7] as JLabel).text = "Low: ${report.summary.bySeverity[Severity.LOW] ?: 0}"
        }
    }

    private fun updateTable(report: ScanReport) {
        tableModel.rowCount = 0

        // Filter out closed vulnerabilities for main view
        val activeVulnerabilities = report.vulnerabilities.filter {
            it.status != VulnerabilityStatus.CLOSED
        }

        for (vuln in activeVulnerabilities) {
            tableModel.addRow(arrayOf(
                vuln.status.name,
                vuln.severity.name,
                vuln.category.name,
                getRelativePath(vuln.filePath),
                vuln.lineNumber,
                vuln.description
            ))
        }
    }

    private fun getRelativePath(fullPath: String): String {
        val basePath = project.basePath ?: return fullPath
        return if (fullPath.startsWith(basePath)) {
            fullPath.substring(basePath.length + 1)
        } else {
            fullPath
        }
    }

    private fun clearView() {
        tableModel.rowCount = 0
        val components = summaryPanel.components
        for (i in 0 until minOf(8, components.size)) {
            (components[i] as? JLabel)?.text = when (i) {
                0 -> "Total: 0"
                1 -> "New: 0"
                2 -> "Persistent: 0"
                3 -> "Closed: 0"
                4 -> "Critical: 0"
                5 -> "High: 0"
                6 -> "Medium: 0"
                7 -> "Low: 0"
                else -> ""
            }
        }
    }
}
