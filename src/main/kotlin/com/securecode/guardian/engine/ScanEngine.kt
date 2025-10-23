package com.securecode.guardian.engine

import com.securecode.guardian.model.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList

/**
 * Main scan engine that coordinates the security analysis
 */
class ScanEngine(
    private val ruleLoader: RuleLoader = RuleLoader(),
    private val patternMatcher: PatternMatcher = PatternMatcher()
) {

    /**
     * Execute a full security scan on a project
     */
    fun scan(
        projectPath: String,
        config: ScanConfig = ScanConfig()
    ): ScanResult {
        val startTime = System.currentTimeMillis()

        // Load rules
        val customRulesPath = Paths.get(projectPath, config.customRulesPath).toString()
        val rules = ruleLoader.loadAllRules(customRulesPath)
            .filter { rule ->
                // Apply rule filters from config
                when {
                    config.enabledRules.isNotEmpty() -> rule.id in config.enabledRules
                    config.disabledRules.isNotEmpty() -> rule.id !in config.disabledRules
                    else -> true
                }
            }
            .filter { it.severity.ordinal <= config.minSeverity.ordinal }

        // Find files to scan
        val files = findFilesToScan(projectPath, config)

        // Scan files with rules
        val vulnerabilities = mutableListOf<Vulnerability>()
        var linesScanned = 0L

        for (file in files) {
            try {
                linesScanned += Files.lines(file.toPath()).count()

                for (rule in rules) {
                    val matches = patternMatcher.matchRule(rule, file)
                    vulnerabilities.addAll(matches.map { it.toVulnerability() })
                }
            } catch (e: Exception) {
                println("Error scanning file ${file.path}: ${e.message}")
            }
        }

        val endTime = System.currentTimeMillis()

        return ScanResult(
            vulnerabilities = vulnerabilities,
            rulesExecuted = rules.size,
            filesScanned = files.size,
            linesScanned = linesScanned,
            scanDuration = endTime - startTime
        )
    }

    /**
     * Find all files to scan based on include/exclude patterns
     */
    private fun findFilesToScan(projectPath: String, config: ScanConfig): List<File> {
        val projectDir = File(projectPath)
        if (!projectDir.exists() || !projectDir.isDirectory) {
            return emptyList()
        }

        val files = mutableListOf<File>()

        Files.walk(Paths.get(projectPath))
            .filter { Files.isRegularFile(it) }
            .forEach { path ->
                val relativePath = Paths.get(projectPath).relativize(path).toString()

                // Check exclude patterns first
                val excluded = config.excludePatterns.any { pattern ->
                    matchesGlobPattern(relativePath, pattern)
                }

                if (!excluded) {
                    // Check include patterns
                    val included = config.includePatterns.any { pattern ->
                        matchesGlobPattern(relativePath, pattern)
                    }

                    if (included) {
                        files.add(path.toFile())
                    }
                }
            }

        return files
    }

    /**
     * Simple glob pattern matching
     */
    private fun matchesGlobPattern(path: String, pattern: String): Boolean {
        val regexPattern = pattern
            .replace(".", "\\.")
            .replace("**/", ".*")
            .replace("**", ".*")
            .replace("*", "[^/]*")
            .replace("?", "[^/]")

        return Regex(regexPattern).matches(path)
    }
}

/**
 * Result of a security scan
 */
data class ScanResult(
    val vulnerabilities: List<Vulnerability>,
    val rulesExecuted: Int,
    val filesScanned: Int,
    val linesScanned: Long,
    val scanDuration: Long
)
