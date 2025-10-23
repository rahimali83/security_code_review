package com.securecode.guardian.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.securecode.guardian.engine.ScanEngine
import com.securecode.guardian.model.ScanConfig
import com.securecode.guardian.model.ScanReport

/**
 * Main service for coordinating security scans
 */
@Service(Service.Level.PROJECT)
class ScanService(private val project: Project) {

    private val scanEngine = ScanEngine()
    private val reportService = ReportService()

    /**
     * Execute a security scan on the current project
     */
    fun executeScan(config: ScanConfig = ScanConfig()): ScanReport {
        val projectPath = project.basePath ?: throw IllegalStateException("Project path is null")
        val projectName = project.name

        println("Starting SecureCode Guardian scan for project: $projectName")
        println("Scanning directory: $projectPath")

        // Load previous report for stateful tracking
        val previousReport = reportService.loadLatestReport(projectPath)
        if (previousReport != null) {
            println("Found previous report: ${previousReport.reportId}")
            println("Will track vulnerability changes since last scan")
        } else {
            println("No previous report found - performing baseline scan")
        }

        // Execute scan
        val scanResult = scanEngine.scan(projectPath, config)

        println("Scan complete!")
        println("- Files scanned: ${scanResult.filesScanned}")
        println("- Lines scanned: ${scanResult.linesScanned}")
        println("- Rules executed: ${scanResult.rulesExecuted}")
        println("- Vulnerabilities found: ${scanResult.vulnerabilities.size}")

        // Generate report with stateful tracking
        val report = reportService.generateReport(
            projectPath = projectPath,
            projectName = projectName,
            scanResult = scanResult,
            previousReport = previousReport
        )

        // Save report
        reportService.saveReport(projectPath, report)

        println("\nVulnerability Summary:")
        println("- Total: ${report.summary.total}")
        println("- New: ${report.summary.new}")
        println("- Persistent: ${report.summary.persistent}")
        println("- Closed: ${report.summary.closed}")

        if (report.summary.bySeverity.isNotEmpty()) {
            println("\nBy Severity:")
            report.summary.bySeverity.forEach { (severity, count) ->
                println("- $severity: $count")
            }
        }

        return report
    }

    /**
     * Get the latest scan report
     */
    fun getLatestReport(): ScanReport? {
        val projectPath = project.basePath ?: return null
        return reportService.loadLatestReport(projectPath)
    }

    /**
     * Get all scan reports
     */
    fun getAllReports(): List<ScanReport> {
        val projectPath = project.basePath ?: return emptyList()
        return reportService.getAllReports(projectPath)
    }
}
