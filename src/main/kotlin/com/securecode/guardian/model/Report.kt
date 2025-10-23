package com.securecode.guardian.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

/**
 * Security scan report
 */
data class ScanReport(
    @JsonProperty("reportId") val reportId: String = generateReportId(),
    @JsonProperty("projectName") val projectName: String,
    @JsonProperty("projectPath") val projectPath: String,
    @JsonProperty("scanStartTime") val scanStartTime: Long,
    @JsonProperty("scanEndTime") val scanEndTime: Long,
    @JsonProperty("scanDuration") val scanDuration: Long = scanEndTime - scanStartTime,
    @JsonProperty("version") val version: String = "1.0.0",
    @JsonProperty("vulnerabilities") val vulnerabilities: List<Vulnerability>,
    @JsonProperty("summary") val summary: VulnerabilitySummary,
    @JsonProperty("rulesExecuted") val rulesExecuted: Int,
    @JsonProperty("filesScanned") val filesScanned: Int,
    @JsonProperty("linesScanned") val linesScanned: Long,
    @JsonProperty("previousReportId") val previousReportId: String? = null,
    @JsonProperty("complianceStatus") val complianceStatus: Map<ComplianceFramework, ComplianceStatus> = emptyMap()
) {
    companion object {
        private fun generateReportId(): String {
            return "REPORT-${Instant.now().epochSecond}-${(1000..9999).random()}"
        }
    }
}

/**
 * Compliance status for a framework
 */
data class ComplianceStatus(
    @JsonProperty("framework") val framework: ComplianceFramework,
    @JsonProperty("totalControls") val totalControls: Int,
    @JsonProperty("passedControls") val passedControls: Int,
    @JsonProperty("failedControls") val failedControls: Int,
    @JsonProperty("violations") val violations: List<Vulnerability>
) {
    @JsonProperty("compliancePercentage")
    val compliancePercentage: Double
        get() = if (totalControls > 0) {
            (passedControls.toDouble() / totalControls.toDouble()) * 100
        } else 0.0
}

/**
 * Scan configuration
 */
data class ScanConfig(
    @JsonProperty("includePatterns") val includePatterns: List<String> = listOf("**/*.java", "**/*.kt", "**/*.py", "**/*.js", "**/*.ts", "**/*.go"),
    @JsonProperty("excludePatterns") val excludePatterns: List<String> = listOf("**/test/**", "**/build/**", "**/target/**", "**/node_modules/**", "**/.git/**"),
    @JsonProperty("enabledRules") val enabledRules: List<String> = emptyList(),
    @JsonProperty("disabledRules") val disabledRules: List<String> = emptyList(),
    @JsonProperty("customRulesPath") val customRulesPath: String = ".securecode/rules",
    @JsonProperty("minSeverity") val minSeverity: Severity = Severity.INFO
)
