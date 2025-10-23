package com.securecode.guardian.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a security/compliance rule
 */
data class Rule(
    @JsonProperty("id") val id: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String,
    @JsonProperty("severity") val severity: Severity,
    @JsonProperty("category") val category: RuleCategory,
    @JsonProperty("compliance") val compliance: List<ComplianceTag> = emptyList(),
    @JsonProperty("patterns") val patterns: List<RulePattern> = emptyList(),
    @JsonProperty("quickFix") val quickFix: QuickFix? = null,
    @JsonProperty("enabled") val enabled: Boolean = true,
    @JsonProperty("custom") val custom: Boolean = false
)

/**
 * Pattern matching configuration for a rule
 */
data class RulePattern(
    @JsonProperty("type") val type: PatternType,
    @JsonProperty("pattern") val pattern: String,
    @JsonProperty("fileTypes") val fileTypes: List<String> = emptyList(),
    @JsonProperty("message") val message: String? = null
)

enum class PatternType {
    @JsonProperty("regex") REGEX,
    @JsonProperty("ast") AST,
    @JsonProperty("semantic") SEMANTIC,
    @JsonProperty("taint") TAINT
}

enum class Severity {
    @JsonProperty("critical") CRITICAL,
    @JsonProperty("high") HIGH,
    @JsonProperty("medium") MEDIUM,
    @JsonProperty("low") LOW,
    @JsonProperty("info") INFO
}

enum class RuleCategory {
    @JsonProperty("security") SECURITY,
    @JsonProperty("compliance") COMPLIANCE,
    @JsonProperty("quality") QUALITY,
    @JsonProperty("documentation") DOCUMENTATION,
    @JsonProperty("api_security") API_SECURITY,
    @JsonProperty("data_security") DATA_SECURITY,
    @JsonProperty("cryptography") CRYPTOGRAPHY,
    @JsonProperty("authentication") AUTHENTICATION,
    @JsonProperty("authorization") AUTHORIZATION,
    @JsonProperty("injection") INJECTION,
    @JsonProperty("secrets") SECRETS
}

/**
 * Compliance framework tags
 */
data class ComplianceTag(
    @JsonProperty("framework") val framework: ComplianceFramework,
    @JsonProperty("control") val control: String,
    @JsonProperty("requirement") val requirement: String
)

enum class ComplianceFramework {
    @JsonProperty("pci_dss") PCI_DSS,
    @JsonProperty("soc2") SOC2,
    @JsonProperty("hipaa") HIPAA,
    @JsonProperty("gdpr") GDPR,
    @JsonProperty("owasp") OWASP,
    @JsonProperty("cwe") CWE,
    @JsonProperty("nist") NIST
}

/**
 * Quick fix suggestion
 */
data class QuickFix(
    @JsonProperty("type") val type: QuickFixType,
    @JsonProperty("description") val description: String,
    @JsonProperty("replacement") val replacement: String? = null
)

enum class QuickFixType {
    @JsonProperty("remove") REMOVE,
    @JsonProperty("replace") REPLACE,
    @JsonProperty("suggest") SUGGEST,
    @JsonProperty("refactor") REFACTOR
}
