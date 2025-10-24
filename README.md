# SecureCode Guardian

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![JetBrains Plugin](https://img.shields.io/badge/JetBrains-Plugin-orange.svg)](https://plugins.jetbrains.com/)

**SecureCode Guardian** is an intelligent and extensible code review agent for JetBrains IDEs (IntelliJ IDEA, PyCharm, WebStorm, etc.). It provides real-time and on-demand static analysis for security vulnerabilities, compliance violations, and code quality issues with a unique focus on **stateful remediation tracking** and a **dynamic, user-expandable rule engine**.

**developed as part of a learning project by integrating existing security check rulebase with Claude Code**

## 🎯 Key Features

### 🔒 Comprehensive Security Analysis
- **OWASP Top 10 Detection**: SQL injection, XSS, command injection, and more
- **CWE Coverage**: Common Weakness Enumeration patterns
- **Cryptographic Failures**: Detect weak algorithms (MD5, SHA1, DES)
- **Secrets Detection**: Find hardcoded passwords, API keys, tokens
- **API Security**: Missing authentication, insecure HTTP, SSRF

### 📋 Compliance Mapping
Built-in rules mapped to industry standards:
- **PCI DSS**: Payment Card Industry Data Security Standard
- **SOC2**: Service Organization Control 2
- **OWASP**: Open Web Application Security Project
- **NIST**: National Institute of Standards and Technology
- **GDPR**: General Data Protection Regulation

### 📊 Stateful Vulnerability Tracking
- **NEW**: Vulnerabilities found in current scan only
- **PERSISTENT**: Vulnerabilities found in both current and previous scans
- **CLOSED**: Vulnerabilities resolved since last scan
- Track remediation progress over time
- Unique fingerprinting prevents duplicate tracking

### 🎨 Dynamic Rule Engine
- **Built-in Rules**: 10+ comprehensive security rules
- **Custom Rules**: Add project-specific rules without recompiling
- **YAML-Based**: Simple, readable rule format
- **Hot Reload**: Custom rules loaded automatically from `.securecode/rules/`
- **Pattern Matching**: Regex-based detection (AST and taint analysis planned)

### 🔧 IDE Integration
- **Tool Window**: Dedicated panel for viewing results
- **Quick Actions**: Run scans via menu, toolbar, or keyboard shortcut (`Ctrl+Shift+S`)
- **Quick Fixes**: Automated suggestions for common issues
- **JSON Reports**: Comprehensive, machine-readable output
- **Real-time Feedback**: See results immediately after scan

## 📦 Installation

### From JetBrains Marketplace
1. Open your JetBrains IDE (IntelliJ IDEA, PyCharm, etc.)
2. Go to `File > Settings > Plugins`
3. Search for "SecureCode Guardian"
4. Click `Install`
5. Restart the IDE

### From Source
1. Clone this repository:
   ```bash
   git clone https://github.com/yourusername/securecode-guardian.git
   cd securecode-guardian
   ```

2. Build the plugin:
   ```bash
   ./gradlew buildPlugin
   ```

3. Install from disk:
   - Go to `File > Settings > Plugins`
   - Click the gear icon ⚙️ > `Install Plugin from Disk`
   - Select `build/distributions/securecode-guardian-1.0.0.zip`
   - Restart the IDE

## 🚀 Quick Start

### Running Your First Scan

1. **Open your project** in a JetBrains IDE
2. **Run a scan** using one of these methods:
   - Menu: `Tools > SecureCode Guardian > Run SecureCode Scan`
   - Right-click project: `SecureCode Guardian > Run SecureCode Scan`
   - Keyboard: `Ctrl+Shift+S` (Windows/Linux) or `Cmd+Shift+S` (Mac)
3. **View results** in the SecureCode Guardian tool window (bottom panel)

### Understanding the Results

The tool window displays:
- **Summary Panel**: Total vulnerabilities, new/persistent/closed counts, severity breakdown
- **Vulnerability Table**: Detailed list of findings with file locations and descriptions
- **Status Indicators**: Each vulnerability marked as NEW, PERSISTENT, or CLOSED

### Viewing Detailed Reports

JSON reports are saved to:
- **Latest**: `.securecode/reports/latest-report.json`
- **Historical**: `.securecode/reports/report-{timestamp}.json`

## 📖 Documentation

### User Guides
- **[Custom Rule Creation Guide](docs/CUSTOM_RULES.md)**: Learn how to write your own security rules
- **[Report Format Documentation](docs/REPORT_TEMPLATE.md)**: Understand the JSON report structure

### Built-in Rules

The plugin includes 10 comprehensive built-in rules:

| Rule ID | Name | Severity | Category | Description |
|---------|------|----------|----------|-------------|
| SEC-001 | Hardcoded Secrets Detection | Critical | Secrets | Detects passwords, API keys, tokens |
| SEC-002 | SQL Injection | Critical | Injection | String concatenation in SQL queries |
| SEC-003 | Weak Cryptography | High | Cryptography | MD5, SHA1, DES usage |
| SEC-004 | Empty Catch Blocks | Medium | Quality | Exception swallowing |
| SEC-005 | Insecure HTTP | High | API Security | HTTP instead of HTTPS |
| SEC-006 | Command Injection | Critical | Injection | OS command injection |
| SEC-007 | Path Traversal | High | Security | File path manipulation |
| SEC-008 | SSRF | High | Security | Server-side request forgery |
| SEC-009 | Insecure Deserialization | Critical | Security | Unsafe object deserialization |
| SEC-010 | Missing Authentication | High | Authentication | API endpoints without auth |

## 🛠️ Creating Custom Rules

### Basic Example

Create `.securecode/rules/my-rule.yaml` in your project:

```yaml
id: CUSTOM-001
name: Detect Unsafe API Usage
description: Detects use of deprecated unsafe API methods
severity: high
category: security

patterns:
  - type: regex
    pattern: 'UnsafeAPI\.(dangerousMethod|riskyCall)'
    fileTypes: ["java", "kt"]
    message: "Use of unsafe API method detected"

quickFix:
  type: suggest
  description: "Replace with SafeAPI equivalent methods"

enabled: true
custom: true
```

### Advanced Example with Compliance

```yaml
id: CUSTOM-002
name: PII in Logs
description: Detects logging of personally identifiable information
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
    pattern: 'log\.(info|debug)\([^)]*ssn[^)]*\)'
    fileTypes: ["java", "kt", "py", "js", "ts"]
    message: "Social Security Number being logged"
  - type: regex
    pattern: 'log\.(info|debug)\([^)]*creditCard[^)]*\)'
    fileTypes: ["java", "kt", "py", "js", "ts"]
    message: "Credit card information being logged"

quickFix:
  type: suggest
  description: "Remove PII from logs or implement data redaction"

enabled: true
```

See the [Custom Rule Creation Guide](docs/CUSTOM_RULES.md) for complete documentation.

## 📊 Report Format

### Summary Statistics

```json
{
  "summary": {
    "total": 25,
    "new": 5,
    "persistent": 18,
    "closed": 2,
    "bySeverity": {
      "critical": 3,
      "high": 8,
      "medium": 10,
      "low": 4
    }
  }
}
```

### Vulnerability Details

```json
{
  "id": "VULN-1234567890-1234",
  "ruleId": "SEC-001",
  "severity": "critical",
  "status": "new",
  "filePath": "/path/to/file.java",
  "lineNumber": 42,
  "description": "Hardcoded password detected",
  "fingerprint": "a1b2c3d4e5f6g7h8"
}
```

See [Report Format Documentation](docs/REPORT_TEMPLATE.md) for complete schema.

## 🔄 CI/CD Integration

### GitHub Actions Example

```yaml
name: Security Scan

on: [push, pull_request]

jobs:
  security:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'

      - name: Run SecureCode Scan
        run: ./gradlew runSecurityScan

      - name: Check Critical Issues
        run: |
          CRITICAL=$(jq '.summary.bySeverity.critical // 0' .securecode/reports/latest-report.json)
          if [ "$CRITICAL" -gt 0 ]; then
            echo "❌ Found $CRITICAL critical vulnerabilities"
            exit 1
          fi

      - name: Upload Report
        uses: actions/upload-artifact@v3
        with:
          name: security-report
          path: .securecode/reports/latest-report.json
```

### GitLab CI Example

```yaml
security_scan:
  stage: test
  script:
    - ./gradlew runSecurityScan
    - |
      CRITICAL=$(jq '.summary.bySeverity.critical // 0' .securecode/reports/latest-report.json)
      if [ "$CRITICAL" -gt 0 ]; then
        echo "Found $CRITICAL critical vulnerabilities"
        exit 1
      fi
  artifacts:
    paths:
      - .securecode/reports/
    reports:
      junit: .securecode/reports/latest-report.json
```

## 🎯 Use Cases

### 1. Pre-Commit Security Checks
Run scans before committing code to catch issues early:
```bash
# Add to .git/hooks/pre-commit
./gradlew runSecurityScan || exit 1
```

### 2. Pull Request Reviews
Automatically scan PRs for security issues:
- Comment on PRs with findings
- Block merges if critical issues found
- Track remediation progress

### 3. Compliance Auditing
Generate compliance reports for auditors:
```bash
jq '.complianceStatus' .securecode/reports/latest-report.json > compliance-report.json
```

### 4. Security Metrics Dashboard
Track security metrics over time:
- Vulnerability trends
- Time to remediation
- Compliance percentage
- Category breakdown

## 🛡️ Supported Languages

- Java
- Kotlin
- Python
- JavaScript
- TypeScript
- Go
- Ruby
- PHP
- C#
- C/C++

Additional languages can be supported via custom rules.

## ⚙️ Configuration

### Scan Configuration

Create `.securecode/config.yaml` in your project:

```yaml
# File patterns to include
includePatterns:
  - "**/*.java"
  - "**/*.kt"
  - "**/*.py"
  - "**/*.js"
  - "**/*.ts"

# File patterns to exclude
excludePatterns:
  - "**/test/**"
  - "**/build/**"
  - "**/node_modules/**"
  - "**/.git/**"

# Minimum severity to report
minSeverity: info  # critical, high, medium, low, info

# Enable/disable specific rules
enabledRules: []  # Empty = all rules
disabledRules:
  - SEC-004  # Disable empty catch block rule

# Custom rules directory
customRulesPath: .securecode/rules
```

## 🤝 Contributing

We welcome contributions! Here's how you can help:

### Reporting Issues
- Search existing issues first
- Provide detailed reproduction steps
- Include plugin version and IDE version

### Contributing Rules
1. Create a new rule following the [Custom Rule Guide](docs/CUSTOM_RULES.md)
2. Test thoroughly on sample code
3. Submit a pull request with:
   - Rule YAML file
   - Test cases
   - Documentation

### Contributing Code
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

## 📝 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **OWASP**: For security vulnerability classifications
- **CWE**: For common weakness enumeration
- **JetBrains**: For the excellent IDE platform
- **Community**: For feedback and contributions

## 📞 Support

- **Documentation**: [docs/](docs/)
- **Issues**: [GitHub Issues](https://github.com/yourusername/securecode-guardian/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/securecode-guardian/discussions)

## 🗺️ Roadmap

### Version 1.1 (Planned)
- [ ] Real-time code analysis (as you type)
- [ ] AST-based pattern matching
- [ ] Taint analysis for data flow tracking
- [ ] Visual Studio Code extension
- [ ] More built-in rules

### Version 2.0 (Future)
- [ ] Machine learning-based detection
- [ ] Auto-fix capabilities
- [ ] Team collaboration features
- [ ] Cloud-based rule sharing
- [ ] Integration with SAST tools

## 📈 Metrics

**Lines of Code**: ~5,000
**Built-in Rules**: 10+
**Supported Frameworks**: 7
**Supported Languages**: 10+
**Test Coverage**: TBD

---

**Made with ❤️ for the security community**

*SecureCode Guardian - Secure your code, one scan at a time.*
