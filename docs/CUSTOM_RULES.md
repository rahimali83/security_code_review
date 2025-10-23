# Custom Rule Creation Guide

SecureCode Guardian allows you to create and use custom security rules specific to your project's needs. This guide will walk you through creating effective custom rules.

## Overview

Custom rules are defined in YAML format and placed in your project's `.securecode/rules/` directory. The plugin automatically loads these rules alongside the built-in rules when performing scans.

## Rule Structure

A rule consists of the following components:

```yaml
id: CUSTOM-001                    # Unique identifier (required)
name: Rule Display Name           # Human-readable name (required)
description: Detailed description # Full description (required)
severity: high                    # critical, high, medium, low, info (required)
category: security                # Rule category (required)
compliance: []                    # Compliance mappings (optional)
patterns: []                      # Pattern matching rules (required)
quickFix: {}                      # Quick fix suggestion (optional)
enabled: true                     # Whether rule is active (optional, default: true)
custom: true                      # Mark as custom rule (optional)
```

## Field Descriptions

### Required Fields

#### `id` (string)
- **Format**: Use a prefix like `CUSTOM-` followed by a unique number
- **Example**: `CUSTOM-001`, `CUSTOM-AUTH-01`
- **Note**: Must be unique across all rules (built-in and custom)

#### `name` (string)
- **Purpose**: Short, descriptive name shown in reports
- **Example**: `"Hardcoded Database Credentials"`

#### `description` (string)
- **Purpose**: Detailed explanation of what the rule checks for
- **Example**: `"Detects hardcoded database credentials in configuration files"`

#### `severity` (enum)
- **Values**: `critical`, `high`, `medium`, `low`, `info`
- **Guidelines**:
  - `critical`: Remote code execution, SQL injection, command injection
  - `high`: Authentication bypass, sensitive data exposure
  - `medium`: Missing validation, weak crypto
  - `low`: Code quality issues
  - `info`: Informational findings

#### `category` (enum)
- **Values**: `security`, `compliance`, `quality`, `documentation`, `api_security`, `data_security`, `cryptography`, `authentication`, `authorization`, `injection`, `secrets`
- **Purpose**: Categorize the type of issue

#### `patterns` (array)
- **Purpose**: Define how to detect violations
- **Required**: At least one pattern
- **See**: Pattern Matching section below

### Optional Fields

#### `compliance` (array)
Map your rule to compliance frameworks:

```yaml
compliance:
  - framework: pci_dss
    control: "6.5.1"
    requirement: "Injection flaws"
  - framework: owasp
    control: "A03:2021"
    requirement: "Injection"
  - framework: soc2
    control: "CC6.1"
    requirement: "Logical access controls"
```

**Supported frameworks**: `pci_dss`, `soc2`, `hipaa`, `gdpr`, `owasp`, `cwe`, `nist`

#### `quickFix` (object)
Provide automated fix suggestions:

```yaml
quickFix:
  type: suggest          # remove, replace, suggest, refactor
  description: "Move credentials to environment variables"
  replacement: "os.getenv('DB_PASSWORD')"  # For 'replace' type
```

#### `enabled` (boolean)
- **Default**: `true`
- **Purpose**: Temporarily disable a rule without deleting it

## Pattern Matching

Patterns define how to detect rule violations in code.

### Pattern Structure

```yaml
patterns:
  - type: regex
    pattern: 'your-regex-pattern'
    fileTypes: ["java", "kt", "py"]
    message: "Custom violation message"
```

### Pattern Types

#### 1. Regex Patterns

The most common pattern type. Uses regular expressions to match code.

**Example - Detect TODO comments**:
```yaml
patterns:
  - type: regex
    pattern: '//\s*TODO:.*'
    fileTypes: ["java", "kt", "js", "ts"]
    message: "TODO comment found - should be tracked in issue tracker"
```

**Example - Detect unsafe API usage**:
```yaml
patterns:
  - type: regex
    pattern: 'eval\s*\('
    fileTypes: ["js", "ts", "py"]
    message: "Unsafe use of eval() function"
```

**Example - Detect missing input validation**:
```yaml
patterns:
  - type: regex
    pattern: '@RequestMapping[^)]*\)\s*\n\s*public\s+.*request\.getParameter\([^)]*\)'
    fileTypes: ["java"]
    message: "Direct use of request parameter without validation"
```

#### 2. AST Patterns (Placeholder)
*Future feature for abstract syntax tree-based matching*

#### 3. Semantic Patterns (Placeholder)
*Future feature for semantic code analysis*

#### 4. Taint Patterns (Placeholder)
*Future feature for data flow analysis*

### File Type Filtering

Limit patterns to specific file extensions:

```yaml
fileTypes: ["java", "kt"]      # Java and Kotlin only
fileTypes: ["py"]              # Python only
fileTypes: ["js", "ts"]        # JavaScript and TypeScript
fileTypes: []                  # All file types
```

**Supported extensions**: `java`, `kt`, `py`, `js`, `ts`, `go`, `rb`, `php`, `cs`, `cpp`, `c`, `h`, `hpp`

## Complete Examples

### Example 1: Hardcoded AWS Keys

```yaml
id: CUSTOM-AWS-001
name: Hardcoded AWS Credentials
description: Detects hardcoded AWS access keys and secret keys in source code
severity: critical
category: secrets
compliance:
  - framework: pci_dss
    control: "8.2"
    requirement: "No hardcoded credentials"
  - framework: soc2
    control: "CC6.1"
    requirement: "Logical and physical access controls"
patterns:
  - type: regex
    pattern: 'AKIA[0-9A-Z]{16}'
    fileTypes: []
    message: "Hardcoded AWS Access Key ID detected"
  - type: regex
    pattern: '(?i)aws_secret_access_key\s*=\s*["\'][a-zA-Z0-9/+=]{40}["\']'
    fileTypes: []
    message: "Hardcoded AWS Secret Access Key detected"
quickFix:
  type: suggest
  description: "Move AWS credentials to environment variables or AWS Secrets Manager"
enabled: true
custom: true
```

### Example 2: Logging Sensitive Data

```yaml
id: CUSTOM-LOG-001
name: Sensitive Data in Logs
description: Detects logging of potentially sensitive information like passwords, tokens, or PII
severity: high
category: data_security
compliance:
  - framework: gdpr
    control: "Art. 32"
    requirement: "Security of processing"
  - framework: pci_dss
    control: "3.4"
    requirement: "Protect stored cardholder data"
patterns:
  - type: regex
    pattern: 'log\.(info|debug|warn|error)\([^)]*password[^)]*\)'
    fileTypes: ["java", "kt"]
    message: "Potential password being logged"
  - type: regex
    pattern: 'log\.(info|debug|warn|error)\([^)]*token[^)]*\)'
    fileTypes: ["java", "kt"]
    message: "Potential token being logged"
  - type: regex
    pattern: '(console\.log|logger\.(info|debug))\([^)]*password[^)]*\)'
    fileTypes: ["js", "ts"]
    message: "Potential password being logged"
quickFix:
  type: suggest
  description: "Remove sensitive data from log statements or use redaction"
enabled: true
custom: true
```

### Example 3: Missing Error Handling

```yaml
id: CUSTOM-ERR-001
name: Missing Error Handling in API
description: Detects API endpoints without proper error handling
severity: medium
category: quality
patterns:
  - type: regex
    pattern: '@(GetMapping|PostMapping|PutMapping|DeleteMapping)\s*\([^)]*\)\s*\n\s*public\s+[^{]*\{[^}]*\n(?!.*try\s*\{)'
    fileTypes: ["java", "kt"]
    message: "API endpoint missing try-catch error handling"
quickFix:
  type: suggest
  description: "Add try-catch block to handle potential exceptions"
enabled: true
custom: true
```

### Example 4: Company-Specific Pattern

```yaml
id: CUSTOM-COMPANY-001
name: Deprecated Internal API Usage
description: Detects usage of deprecated internal company APIs
severity: medium
category: quality
patterns:
  - type: regex
    pattern: 'import\s+com\.company\.legacy\.(AuthService|UserService)'
    fileTypes: ["java", "kt"]
    message: "Using deprecated legacy service - migrate to new API"
quickFix:
  type: suggest
  description: "Migrate to com.company.api.v2 services"
enabled: true
custom: true
```

## Best Practices

### 1. Start Simple
Begin with straightforward regex patterns and test thoroughly before adding complexity.

### 2. Test Your Patterns
Create test files with both positive and negative cases:
- Files that should trigger the rule
- Files that should NOT trigger the rule

### 3. Avoid False Positives
- Be specific in your patterns
- Use word boundaries (`\b`) when matching keywords
- Consider context (comments vs. code)

### 4. Provide Clear Messages
Write descriptive messages that help developers understand:
- What was detected
- Why it's a problem
- How to fix it

### 5. Use Appropriate Severity
- Don't mark everything as `critical`
- Reserve `critical` for exploitable vulnerabilities
- Use `info` for best practices and suggestions

### 6. Document Compliance
If your rule helps meet compliance requirements, document them in the `compliance` field.

### 7. Offer Quick Fixes
When possible, provide actionable quick fixes to help developers remediate issues quickly.

## Testing Custom Rules

### 1. Create the Rule File
Place your YAML file in `.securecode/rules/`:
```
project/
  .securecode/
    rules/
      my-custom-rule.yaml
```

### 2. Run a Scan
Use the IDE action: `Tools > SecureCode Guardian > Run SecureCode Scan`

Or keyboard shortcut: `Ctrl+Shift+S`

### 3. Check the Results
Open the SecureCode Guardian tool window to see detected violations.

### 4. Review the Report
Check `.securecode/reports/latest-report.json` for detailed results.

## Regex Tips

### Common Patterns

**Match assignment to a variable**:
```regex
variableName\s*=\s*["\'].*["\']
```

**Match function calls**:
```regex
functionName\s*\([^)]*\)
```

**Match import statements**:
```regex
import\s+.*packageName
```

**Case-insensitive matching**:
```regex
(?i)password
```

**Match across multiple lines** (use sparingly):
```regex
function\s*\([^)]*\)\s*\{[^}]*\}
```

### Testing Regex

Use online tools to test your patterns:
- [regex101.com](https://regex101.com) - Choose "Java" flavor
- [regexr.com](https://regexr.com)

## Troubleshooting

### Rule Not Loading
- Check YAML syntax (use a YAML validator)
- Ensure file has `.yaml` or `.yml` extension
- Check plugin console for error messages

### Pattern Not Matching
- Test regex separately in regex testing tool
- Verify `fileTypes` includes the target file extension
- Check for escaped special characters

### Too Many False Positives
- Make pattern more specific
- Add contextual constraints
- Use negative lookahead: `(?!pattern)`

### Performance Issues
- Avoid overly complex regex patterns
- Limit use of `.*` and backtracking
- Consider splitting one complex pattern into multiple simpler ones

## Advanced Topics

### Multiple Patterns in One Rule

You can define multiple patterns for the same rule:

```yaml
patterns:
  - type: regex
    pattern: 'pattern1'
    message: "Message 1"
  - type: regex
    pattern: 'pattern2'
    message: "Message 2"
```

### Language-Specific Rules

Create different patterns for different languages:

```yaml
patterns:
  - type: regex
    pattern: 'System\.out\.println'
    fileTypes: ["java"]
    message: "Use logger instead of System.out"
  - type: regex
    pattern: 'console\.log'
    fileTypes: ["js", "ts"]
    message: "Use proper logging framework"
```

## Support

For questions or issues with custom rules:
1. Check this documentation
2. Review example rules in `src/main/resources/rules/`
3. Open an issue on GitHub

## Rule Contribution

If you create rules that might benefit others, consider contributing them:
1. Test thoroughly
2. Document clearly
3. Submit a pull request
4. Include test cases
