# Building SecureCode Guardian

## Prerequisites

- **JDK 17 or higher**: Required for compilation
- **Gradle 8.4+**: Build automation tool
- **Internet connection**: For downloading dependencies

## Build Instructions

### 1. Initialize Gradle Wrapper

```bash
gradle wrapper --gradle-version 8.4
```

This creates `gradlew` and `gradlew.bat` scripts for building without a system-wide Gradle installation.

### 2. Build the Plugin

```bash
./gradlew buildPlugin
```

This command will:
- Compile Kotlin source files
- Process resources (rules, icons, plugin.xml)
- Generate the plugin JAR
- Create distributable ZIP file

### 3. Run Tests

```bash
./gradlew test
```

### 4. Verify the Plugin

```bash
./gradlew verifyPlugin
```

This validates the plugin structure and compatibility with IntelliJ Platform.

## Build Output

After a successful build, you'll find:

- **Plugin JAR**: `build/libs/securecode-guardian-1.0.0.jar`
- **Distribution ZIP**: `build/distributions/securecode-guardian-1.0.0.zip`
- **Test Results**: `build/reports/tests/test/index.html`

## Installing the Built Plugin

### Option 1: Install from Disk

1. Open IntelliJ IDEA or any JetBrains IDE
2. Go to `File > Settings > Plugins`
3. Click the gear icon ⚙️
4. Select `Install Plugin from Disk...`
5. Navigate to `build/distributions/securecode-guardian-1.0.0.zip`
6. Click OK and restart the IDE

### Option 2: Run in Development Mode

```bash
./gradlew runIde
```

This launches a new IDE instance with the plugin installed for testing.

## Project Structure

```
securecode-guardian/
├── build.gradle.kts              # Main build configuration
├── settings.gradle.kts           # Gradle settings
├── gradle.properties             # Gradle properties
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/securecode/guardian/
│   │   │       ├── model/        # Data models (Rule, Vulnerability, Report)
│   │   │       ├── engine/       # Core scanning engine
│   │   │       ├── service/      # IDE services
│   │   │       ├── ui/           # User interface components
│   │   │       ├── action/       # IDE actions
│   │   │       ├── quickfix/     # Quick fix implementations
│   │   │       ├── inspection/   # Code inspections
│   │   │       └── startup/      # Startup activities
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   └── plugin.xml    # Plugin manifest
│   │       ├── rules/            # Built-in security rules (YAML)
│   │       └── icons/            # Plugin icons (SVG)
│   └── test/
│       └── kotlin/               # Unit tests
├── docs/
│   ├── CUSTOM_RULES.md           # Custom rule creation guide
│   └── REPORT_TEMPLATE.md        # Report format documentation
├── README.md                      # Main documentation
├── BUILD.md                       # This file
└── LICENSE                        # Apache 2.0 license
```

## Key Components

### Core Engine (17 Kotlin files)

1. **Data Models** (3 files)
   - `Rule.kt` - Rule definitions and schemas
   - `Vulnerability.kt` - Vulnerability data and status tracking
   - `Report.kt` - Report generation models

2. **Scanning Engine** (3 files)
   - `RuleLoader.kt` - Loads built-in and custom rules
   - `PatternMatcher.kt` - Pattern matching against source code
   - `ScanEngine.kt` - Orchestrates the scan process

3. **Services** (3 files)
   - `ScanService.kt` - Main scan coordination service
   - `ReportService.kt` - Report generation and stateful tracking
   - `RuleEngineService.kt` - Rule management service

4. **UI Components** (2 files)
   - `GuardianToolWindowFactory.kt` - Tool window factory
   - `GuardianToolWindowPanel.kt` - Results display panel

5. **Actions** (3 files)
   - `RunScanAction.kt` - Triggers security scan
   - `ViewReportAction.kt` - Opens report viewer
   - `ConfigureRulesAction.kt` - Opens rule configuration

6. **Other** (3 files)
   - `SecurityQuickFix.kt` - Quick fix implementations
   - `SecurityInspection.kt` - Code inspection base
   - `GuardianStartupActivity.kt` - Plugin initialization

### Built-in Rules (10 YAML files)

1. **SEC-001**: Hardcoded Secrets Detection (Critical)
2. **SEC-002**: SQL Injection (Critical)
3. **SEC-003**: Weak Cryptography (High)
4. **SEC-004**: Empty Catch Blocks (Medium)
5. **SEC-005**: Insecure HTTP (High)
6. **SEC-006**: Command Injection (Critical)
7. **SEC-007**: Path Traversal (High)
8. **SEC-008**: SSRF (High)
9. **SEC-009**: Insecure Deserialization (Critical)
10. **SEC-010**: Missing Authentication (High)

### Resources

- **plugin.xml**: Complete IDE integration manifest
- **Icons**: 4 SVG icons (guardian, scan, report, config)
- **Rules**: YAML-based security rule definitions

## Gradle Tasks

### Build Tasks

- `./gradlew clean` - Clean build directory
- `./gradlew build` - Full build with tests
- `./gradlew buildPlugin` - Build plugin distribution
- `./gradlew assemble` - Assemble artifacts without tests

### Testing Tasks

- `./gradlew test` - Run unit tests
- `./gradlew check` - Run tests and validations
- `./gradlew verifyPlugin` - Verify plugin compatibility

### Development Tasks

- `./gradlew runIde` - Run IDE with plugin
- `./gradlew buildSearchableOptions` - Generate searchable options
- `./gradlew listProductsReleases` - List available IDE versions

### Publishing Tasks (requires credentials)

- `./gradlew signPlugin` - Sign the plugin
- `./gradlew publishPlugin` - Publish to JetBrains Marketplace

## Dependencies

### Runtime Dependencies

- **Kotlin Standard Library**: Core Kotlin runtime
- **Jackson**: JSON/YAML parsing
  - jackson-databind: 2.15.3
  - jackson-module-kotlin: 2.15.3
  - jackson-dataformat-yaml: 2.15.3
- **SnakeYAML**: YAML processing (2.2)

### Test Dependencies

- **JUnit**: 4.13.2
- **Kotlin Test**: Testing utilities

### Plugin Dependencies

- **IntelliJ Platform**: 2023.2.5 (Community Edition)
- **Java Plugin**: Built-in Java support

## Compatibility

- **IntelliJ Platform**: 2023.2 to 2024.2
- **JVM Target**: Java 17
- **Kotlin Version**: 1.9.0
- **IDE Types**: IC (Community), IU (Ultimate), PY (PyCharm), WS (WebStorm), etc.

## Troubleshooting

### Build Fails with Plugin Not Found

Ensure your `settings.gradle.kts` includes:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
```

### Compilation Errors

1. Verify JDK 17+ is installed: `java -version`
2. Set JAVA_HOME environment variable
3. Clear Gradle cache: `./gradlew clean`

### Plugin Verification Fails

Check plugin.xml for:
- Valid version numbers
- Correct dependency declarations
- Proper action/extension registrations

## CI/CD Build

### GitHub Actions Example

```yaml
name: Build Plugin

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Grant execute permission
        run: chmod +x ./gradlew

      - name: Build plugin
        run: ./gradlew buildPlugin

      - name: Run tests
        run: ./gradlew test

      - name: Verify plugin
        run: ./gradlew verifyPlugin

      - name: Upload artifact
        uses: actions/upload-artifact@v3
        with:
          name: plugin-distribution
          path: build/distributions/*.zip
```

## Next Steps

After building:

1. **Test locally**: Run the plugin in a development IDE
2. **Create test cases**: Add unit and integration tests
3. **Performance tuning**: Optimize scan performance
4. **Add more rules**: Expand the built-in rule set
5. **Submit to marketplace**: Publish to JetBrains Plugin Repository

## Additional Resources

- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/)
- [Gradle IntelliJ Plugin](https://github.com/JetBrains/gradle-intellij-plugin)
- [Kotlin Documentation](https://kotlinlang.org/docs/)
