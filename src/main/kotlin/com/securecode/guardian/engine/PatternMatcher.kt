package com.securecode.guardian.engine

import com.securecode.guardian.model.*
import java.io.File
import java.util.regex.Pattern

/**
 * Matches rule patterns against source code
 */
class PatternMatcher {

    /**
     * Match a rule against a file's content
     */
    fun matchRule(rule: Rule, file: File): List<Match> {
        if (!rule.enabled) {
            return emptyList()
        }

        val content = try {
            file.readText()
        } catch (e: Exception) {
            return emptyList()
        }

        val matches = mutableListOf<Match>()

        for (pattern in rule.patterns) {
            // Check file type filter
            if (pattern.fileTypes.isNotEmpty()) {
                val fileExtension = file.extension
                if (!pattern.fileTypes.contains(fileExtension)) {
                    continue
                }
            }

            when (pattern.type) {
                PatternType.REGEX -> {
                    matches.addAll(matchRegexPattern(rule, pattern, file, content))
                }
                PatternType.AST -> {
                    // AST-based matching would require language-specific parsers
                    // Placeholder for future implementation
                }
                PatternType.SEMANTIC -> {
                    // Semantic analysis would require more complex analysis
                    // Placeholder for future implementation
                }
                PatternType.TAINT -> {
                    // Taint analysis would track data flow
                    // Placeholder for future implementation
                }
            }
        }

        return matches
    }

    /**
     * Match a regex pattern against file content
     */
    private fun matchRegexPattern(
        rule: Rule,
        pattern: RulePattern,
        file: File,
        content: String
    ): List<Match> {
        val matches = mutableListOf<Match>()

        try {
            val regex = Pattern.compile(pattern.pattern, Pattern.MULTILINE)
            val matcher = regex.matcher(content)

            while (matcher.find()) {
                val lineNumber = getLineNumber(content, matcher.start())
                val columnNumber = getColumnNumber(content, matcher.start())
                val snippet = extractCodeSnippet(content, lineNumber)

                matches.add(
                    Match(
                        rule = rule,
                        file = file,
                        lineNumber = lineNumber,
                        columnNumber = columnNumber,
                        matchedText = matcher.group(),
                        codeSnippet = snippet,
                        message = pattern.message ?: rule.description
                    )
                )
            }
        } catch (e: Exception) {
            // Invalid regex pattern
            println("Error matching pattern in rule ${rule.id}: ${e.message}")
        }

        return matches
    }

    /**
     * Get line number from character position
     */
    private fun getLineNumber(content: String, position: Int): Int {
        return content.substring(0, position).count { it == '\n' } + 1
    }

    /**
     * Get column number from character position
     */
    private fun getColumnNumber(content: String, position: Int): Int {
        val lastNewline = content.lastIndexOf('\n', position - 1)
        return position - lastNewline
    }

    /**
     * Extract code snippet around the match
     */
    private fun extractCodeSnippet(content: String, lineNumber: Int, context: Int = 2): String {
        val lines = content.lines()
        val startLine = maxOf(0, lineNumber - context - 1)
        val endLine = minOf(lines.size, lineNumber + context)

        return lines.subList(startLine, endLine).joinToString("\n")
    }
}

/**
 * Represents a pattern match in a file
 */
data class Match(
    val rule: Rule,
    val file: File,
    val lineNumber: Int,
    val columnNumber: Int,
    val matchedText: String,
    val codeSnippet: String,
    val message: String
) {
    /**
     * Convert match to vulnerability
     */
    fun toVulnerability(): Vulnerability {
        return Vulnerability(
            ruleId = rule.id,
            ruleName = rule.name,
            severity = rule.severity,
            category = rule.category,
            description = message,
            filePath = file.path,
            lineNumber = lineNumber,
            columnNumber = columnNumber,
            codeSnippet = codeSnippet,
            compliance = rule.compliance,
            quickFix = rule.quickFix
        )
    }
}
