# Code Conventions

**Analysis Date:** 2026-05-15

## Naming Conventions

**Classes:**
- PascalCase: `HomeViewModel`, `WebAppMapper`, `BackupCryptoTest`
- Pattern enforced by detekt: `[A-Z][a-zA-Z0-9]*`

**Composable functions:**
- PascalCase (exception to the standard function rule, explicitly whitelisted in detekt)
- Examples: `HomeScreen`, `AddScreen`, `ShellifyTheme`

**Regular functions:**
- camelCase: `resolveStartDestination`, `selectCategory`, `clearData`
- Pattern: `([a-z][a-zA-Z0-9]*)|(\`.*\`)`

**Variables and parameters:**
- camelCase: `webApp`, `categoryId`, `isolationId`
- Private variables may optionally be prefixed with `_`: `_uiState`

**Boolean properties:**
- Must be prefixed with: `is`, `has`, `are`, `was`, `show`, `enable`, `disable`, `should`, `can`, `will`, `need`, `allow`, `auto`
- Examples: `isLoading`, `hasAnyApps`, `adBlockEnabled`, `isFullscreen`

**Enum entries:**
- SCREAMING_SNAKE_CASE: `[A-Z][_A-Z0-9]*`

**Constants (top-level):**
- SCREAMING_SNAKE_CASE: `[A-Z][_A-Z0-9]*`

**Packages:**
- All lowercase: `io.shellify.app.presentation.home`
- Root package: `io.shellify.app` (enforced by detekt `InvalidPackageDeclaration`)

**Architecture-specific suffixes (enforced by Konsist):**
- ViewModels: `*ViewModel` (e.g., `HomeViewModel`)
- UI state classes: `*UiState` (e.g., `HomeUiState`) — must be data classes
- Use cases: `*UseCase` (e.g., `GetWebAppsUseCase`) — must have exactly one `operator fun invoke()`
- Repository interfaces: `*Repository` (e.g., `ShortcutRepository`)
- Repository implementations: `*RepositoryImpl` (e.g., `WebAppRepositoryImpl`)
- Room entities: `*Entity` (e.g., `WebAppEntity`, `CategoryEntity`)
- Data mappers: `*Mapper` (e.g., `WebAppMapper`)

## Package Structure

All production code lives under `io.shellify.app`:

```
io.shellify.app
├── domain.model          # Domain models (data classes)
├── domain.usecase        # Use cases (operator fun invoke)
├── domain.repository     # Repository interfaces
├── data.local.entity     # Room @Entity classes
├── data.repository       # *RepositoryImpl classes
├── data.mapper           # *Mapper classes
├── presentation.<feature> # Compose screens + ViewModels per feature
└── core.<module>         # Infrastructure (engine, crypto, backup, …)
```

Feature modules use namespace `io.shellify.feature.<name>` in their `build.gradle.kts`.

## Code Style Tools

### Detekt (primary static analysis)
- Config: `config/detekt/detekt.yml`
- Includes ktlint-based formatting rules via `detekt-formatting` plugin
- Run: `./gradlew detekt`
- Applied only to the `:app` module (`src/main/java`, `src/main/kotlin`); debug build type only
- Zero tolerance: `maxIssues: 0`

**Key thresholds:**
- Max line length: 140 characters
- Max method length: 60 lines (`@Composable` functions exempt)
- Max cyclomatic complexity: 15
- Max cognitive complexity: 15
- Max function parameters: 8 (default params ignored; `@Composable` exempt)
- Max functions per class: 20; per file: 25; per interface: 15
- Max destructuring entries: 3
- Max return statements per function: 4 (guard clauses excluded)
- Max nested block depth: 5

**Import ordering (ktlint):**
`*` (project imports), then `java.**`, `javax.**`, `kotlin.**`, then aliases — separated by blank lines.
Layout string: `'*,java.**,javax.**,kotlin.**,^'`

**Formatting rules active:**
- No semicolons (`NoSemicolons`)
- No trailing spaces
- No consecutive blank lines
- 4-space indentation
- Spacing enforced around colons, commas, curly braces, operators, parentheses, dots, angle brackets
- No wildcard imports (exceptions: `androidx.compose.material3.*`, `androidx.compose.material.icons.*`, `java.util.*`)

**Suppressing rules inline:**
```kotlin
@Suppress("MagicNumber")
val timeout = 5000
```

### Android Lint
- Config: `config/lint/lint.xml`
- Run: `./gradlew lintDebug`
- Security-focused: hardcoded debug mode, world-readable/writable files, unsafe dynamic code loading all error-level
- `HardcodedText` is warning-level (internationalization enforced)
- `NewApi` is error-level (minSdk = 23 enforced)

## Module Conventions

**Convention plugins** (defined in `build-logic/src/main/kotlin/`):

| Plugin ID | File | Use case |
|-----------|------|----------|
| `shellify.android.library` | `AndroidLibraryConventionPlugin.kt` | Android library modules |
| `shellify.android.application` | `AndroidApplicationConventionPlugin.kt` | App module only |
| `shellify.compose` | `ComposeConventionPlugin.kt` | Any module with Compose UI |
| `shellify.jvm.library` | `JvmLibraryConventionPlugin.kt` | Pure Kotlin modules (no Android) |
| `shellify.ksp` | `KspConventionPlugin.kt` | Modules using KSP (Room, etc.) |

**Standard SDK versions (set by convention plugins):**
- `compileSdk = 36`
- `minSdk = 23`
- `targetSdk = 36`
- `jvmTarget = "17"` / `sourceCompatibility = JavaVersion.VERSION_17`

**Module build.gradle.kts pattern:**
```kotlin
// Android library with Compose
plugins {
    id("shellify.android.library")
    id("shellify.compose")
}
android { namespace = "io.shellify.feature.<name>" }
dependencies { ... }
```

**Dependency catalog:** `gradle/libs.versions.toml` — all library versions centralized here. Reference as `libs.<alias>`.

**Inter-module dependency rules (enforced by Konsist):**
- `feature:*` modules must not import `core:database` directly
- `feature:*` modules must not import other `feature:*` modules
- `domain` layer must not import from `data` or `presentation`
- `data` layer must not import from `presentation`
- `core` layer must not import from `data` or `presentation` (BackupManager is the sole exception)
- ViewModels must not import `domain.repository` interfaces directly — use use cases

## Git Conventions

**Commit format:** Conventional Commits (`<type>(<scope>): <description>`)

| Type | Use |
|------|-----|
| `feat` | New user-visible feature |
| `fix` | Bug fix |
| `perf` | Performance improvement |
| `refactor` | Non-fix, non-feature restructure |
| `test` | Add/update tests |
| `docs` | Documentation only |
| `ci` | CI/CD workflow changes |
| `build` | Build system or dependency changes |
| `chore` | Maintenance |
| `revert` | Revert a previous commit |

**Scope:** Module name, e.g. `feat(feature:add): ...`, `fix(core:isolation): ...`

**Branch naming:**
```
feat/<short-description>
fix/<short-description>
refactor/<short-description>
chore/<short-description>
```

**Changelog:** Auto-generated from commit messages using `git-cliff` (`cliff.toml`) on `v*` tag push.

## Comment Policy

- Comments explain *why*, not *what*
- Self-documenting code is preferred
- `FIXME:`, `HACK:`, and `STOPSHIP:` are forbidden (detekt `ForbiddenComment` rule)
- `TODO` without assignee is implicitly tolerated but not ideal — prefer issue tracker

## Forbidden Patterns

- `System.out.*` — use Logcat (enforced by Konsist)
- `.printStackTrace()` — use Logcat (enforced by Konsist)
- `java.lang.Math` — use `kotlin.math`
- `java.util.stream.*` — use Kotlin collections
- Wildcard imports (outside allowed exceptions)
- `var` for mutable collections that could be `val` (`VarCouldBeVal`)
- Double mutability: `var` + mutable collection type (`DoubleMutabilityForCollection`)
