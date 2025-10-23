package com.securecode.guardian.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.securecode.guardian.model.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant

/**
 * Manages report generation, storage, and retrieval
 */
class ReportService {

    private val jsonMapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
        enable(SerializationFeature.INDENT_OUTPUT)
    }

    companion object {
        const val REPORTS_DIR = ".securecode/reports"
        const val LATEST_REPORT = "latest-report.json"
    }

    /**
     * Generate a comprehensive scan report
     */
    fun generateReport(
        projectPath: String,
        projectName: String,
        scanResult: com.securecode.guardian.engine.ScanResult,
        previousReport: ScanReport? = null
    ): ScanReport {
        val startTime = System.currentTimeMillis() - scanResult.scanDuration
        val endTime = System.currentTimeMillis()

        // Apply stateful tracking
        val trackedVulnerabilities = if (previousReport != null) {
            trackVulnerabilityStatus(scanResult.vulnerabilities, previousReport.vulnerabilities)
        } else {
            // First scan - all vulnerabilities are new
            scanResult.vulnerabilities.map { it.copy(status = VulnerabilityStatus.NEW) }
        }

        // Calculate summary statistics
        val summary = calculateSummary(trackedVulnerabilities)

        // Calculate compliance status
        val complianceStatus = calculateComplianceStatus(trackedVulnerabilities)

        return ScanReport(
            projectName = projectName,
            projectPath = projectPath,
            scanStartTime = startTime,
            scanEndTime = endTime,
            scanDuration = scanResult.scanDuration,
            vulnerabilities = trackedVulnerabilities,
            summary = summary,
            rulesExecuted = scanResult.rulesExecuted,
            filesScanned = scanResult.filesScanned,
            linesScanned = scanResult.linesScanned,
            previousReportId = previousReport?.reportId,
            complianceStatus = complianceStatus
        )
    }

    /**
     * Track vulnerability status between scans (NEW, PERSISTENT, CLOSED)
     */
    private fun trackVulnerabilityStatus(
        currentVulnerabilities: List<Vulnerability>,
        previousVulnerabilities: List<Vulnerability>
    ): List<Vulnerability> {
        val previousFingerprints = previousVulnerabilities.associateBy { it.fingerprint }
        val currentFingerprints = currentVulnerabilities.map { it.fingerprint }.toSet()

        val tracked = mutableListOf<Vulnerability>()

        // Process current vulnerabilities
        for (vuln in currentVulnerabilities) {
            val previousVuln = previousFingerprints[vuln.fingerprint]

            val updatedVuln = if (previousVuln != null) {
                // Vulnerability exists in both scans - PERSISTENT
                vuln.copy(
                    status = VulnerabilityStatus.PERSISTENT,
                    firstDetected = previousVuln.firstDetected,
                    lastDetected = System.currentTimeMillis()
                )
            } else {
                // Vulnerability is new
                vuln.copy(
                    status = VulnerabilityStatus.NEW,
                    firstDetected = System.currentTimeMillis(),
                    lastDetected = System.currentTimeMillis()
                )
            }

            tracked.add(updatedVuln)
        }

        // Find closed vulnerabilities (in previous but not in current)
        for (prevVuln in previousVulnerabilities) {
            if (prevVuln.fingerprint !in currentFingerprints) {
                tracked.add(
                    prevVuln.copy(
                        status = VulnerabilityStatus.CLOSED,
                        lastDetected = System.currentTimeMillis()
                    )
                )
            }
        }

        return tracked
    }

    /**
     * Calculate vulnerability summary statistics
     */
    private fun calculateSummary(vulnerabilities: List<Vulnerability>): VulnerabilitySummary {
        val activeVulnerabilities = vulnerabilities.filter {
            it.status != VulnerabilityStatus.CLOSED && it.status != VulnerabilityStatus.FIXED
        }

        return VulnerabilitySummary(
            total = activeVulnerabilities.size,
            new = vulnerabilities.count { it.status == VulnerabilityStatus.NEW },
            persistent = vulnerabilities.count { it.status == VulnerabilityStatus.PERSISTENT },
            closed = vulnerabilities.count { it.status == VulnerabilityStatus.CLOSED },
            bySeverity = activeVulnerabilities.groupingBy { it.severity }.eachCount(),
            byCategory = activeVulnerabilities.groupingBy { it.category }.eachCount(),
            byStatus = vulnerabilities.groupingBy { it.status }.eachCount()
        )
    }

    /**
     * Calculate compliance status for different frameworks
     */
    private fun calculateComplianceStatus(
        vulnerabilities: List<Vulnerability>
    ): Map<ComplianceFramework, ComplianceStatus> {
        val statusMap = mutableMapOf<ComplianceFramework, ComplianceStatus>()

        // Group vulnerabilities by compliance framework
        val frameworkViolations = mutableMapOf<ComplianceFramework, MutableList<Vulnerability>>()

        for (vuln in vulnerabilities) {
            if (vuln.status == VulnerabilityStatus.CLOSED || vuln.status == VulnerabilityStatus.FIXED) {
                continue
            }

            for (tag in vuln.compliance) {
                frameworkViolations.getOrPut(tag.framework) { mutableListOf() }.add(vuln)
            }
        }

        // Calculate status for each framework
        for ((framework, violations) in frameworkViolations) {
            val uniqueControls = violations.flatMap { it.compliance }
                .filter { it.framework == framework }
                .map { it.control }
                .distinct()

            statusMap[framework] = ComplianceStatus(
                framework = framework,
                totalControls = uniqueControls.size,
                passedControls = 0,
                failedControls = uniqueControls.size,
                violations = violations
            )
        }

        return statusMap
    }

    /**
     * Save report to file system
     */
    fun saveReport(projectPath: String, report: ScanReport) {
        val reportsDir = Paths.get(projectPath, REPORTS_DIR).toFile()
        reportsDir.mkdirs()

        // Save timestamped report
        val timestamp = Instant.now().epochSecond
        val reportFile = File(reportsDir, "report-$timestamp.json")
        jsonMapper.writeValue(reportFile, report)

        // Save as latest report
        val latestFile = File(reportsDir, LATEST_REPORT)
        jsonMapper.writeValue(latestFile, report)

        println("Report saved to: ${reportFile.absolutePath}")
    }

    /**
     * Load the latest report
     */
    fun loadLatestReport(projectPath: String): ScanReport? {
        val latestFile = Paths.get(projectPath, REPORTS_DIR, LATEST_REPORT).toFile()

        return if (latestFile.exists()) {
            try {
                jsonMapper.readValue(latestFile, ScanReport::class.java)
            } catch (e: Exception) {
                println("Error loading latest report: ${e.message}")
                null
            }
        } else {
            null
        }
    }

    /**
     * Load a specific report by ID
     */
    fun loadReport(projectPath: String, reportId: String): ScanReport? {
        val reportsDir = Paths.get(projectPath, REPORTS_DIR).toFile()
        if (!reportsDir.exists()) {
            return null
        }

        val reportFiles = reportsDir.listFiles { file ->
            file.extension == "json" && file.name != LATEST_REPORT
        } ?: return null

        for (file in reportFiles) {
            try {
                val report = jsonMapper.readValue(file, ScanReport::class.java)
                if (report.reportId == reportId) {
                    return report
                }
            } catch (e: Exception) {
                // Skip invalid files
            }
        }

        return null
    }

    /**
     * Get all reports for a project
     */
    fun getAllReports(projectPath: String): List<ScanReport> {
        val reportsDir = Paths.get(projectPath, REPORTS_DIR).toFile()
        if (!reportsDir.exists()) {
            return emptyList()
        }

        val reports = mutableListOf<ScanReport>()

        reportsDir.listFiles { file ->
            file.extension == "json" && file.name != LATEST_REPORT
        }?.forEach { file ->
            try {
                val report = jsonMapper.readValue(file, ScanReport::class.java)
                reports.add(report)
            } catch (e: Exception) {
                // Skip invalid files
            }
        }

        return reports.sortedByDescending { it.scanEndTime }
    }
}
