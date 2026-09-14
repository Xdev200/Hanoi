# Gemini / AI Project Context & Coding Guidelines

## Project Overview
- **Project Name**: Hanoi (Binary Hanoi Puzzle)
- **Package**: `com.hanoi.binaryhanoi`
- **Build System**: Gradle Kotlin DSL (`settings.gradle.kts`, `build.gradle.kts`)
- **Target Java/JVM**: Java 17 toolchain (`jvmToolchain(17)`)
- **AGP Version**: 8.11.1
- **Compose BOM**: `2024.12.01`

---

## Coding Rules & Conventions

### 1. Kotlin File Structure & Import Rules
- **Top-Level Imports Only**: All `import` statements MUST strictly be placed at the top of the file, immediately after the `package` declaration and before any classes or `@Composable` functions.
- **No Inline Imports**: NEVER insert `import` directives in the middle of a file or between function declarations.

### 2. Gradle & Toolchain Setup
- **Block Order in `settings.gradle.kts`**: `pluginManagement` MUST always appear before `plugins`.
- **Toolchain Resolver**: Toolchain downloads require `org.gradle.toolchains.foojay-resolver-convention` in `settings.gradle.kts`.
- **AGP Compatibility**: Dependency upgrades (e.g. Compose BOM, Lifecycle) must remain compatible with AGP 8.11.1 unless AGP itself is upgraded.

---

## Verification Strategy
- Always run `./gradlew app:assembleDebug` or `gradle_build("app:assembleDebug")` after code modifications to verify syntax and compilation before completing tasks.
