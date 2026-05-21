# Tech Stack

**Analysis Date:** 2026-05-15

## Languages

**Primary:**
- Kotlin — all Android application and library modules (`app/`, `core/`, `feature/`, `build-logic/`)

**Secondary:**
- JavaScript (inline strings) — WebView bridge scripts injected via `core/translate` (`TranslateBridge.kt`)

## Runtime

**Environment:**
- Android (minSdk 23 / API 23 — Android 6.0 Marshmallow)
- JVM target: Java 17 (all Kotlin compile tasks)

**Package Manager:**
- Gradle 8.x with Kotlin DSL (`.kts` files throughout)
- Version catalog: `gradle/libs.versions.toml`
- Lockfile: none (version catalog pins versions explicitly)

## Frameworks & Libraries

**UI:**
- Jetpack Compose (BOM `2024.12.01`) — all feature and UI modules use Compose
- Material3 (`compose-material3`) — design system
- Material Icons Extended — extended icon set
- Jetpack Navigation Compose `2.8.5` — in-app navigation

**Architecture:**
- Lifecycle ViewModel Compose `2.8.7` — `ViewModelStoreOwner` integration across all feature modules
- Lifecycle Runtime KTX `2.8.7` — lifecycle-aware coroutine scopes

**Async:**
- Kotlinx Coroutines Core `1.9.0` — pure-JVM coroutines in `:core:domain`
- Kotlinx Coroutines Android `1.9.0` — `Dispatchers.Main` in Android modules

**Persistence:**
- Room Runtime + KTX `2.6.1` — ORM for local SQLite database (`core/database`)
- SQLCipher `4.5.4` — encrypted SQLite via `net.zetetic:android-database-sqlcipher`
- AndroidX SQLite KTX `2.4.0` — SQLCipher SupportFactory bridge
- DataStore Preferences `1.1.1` — lightweight key-value preferences (used in `core:security`, `core:isolation`, `core:theme`, `feature:onboarding`, `feature:settings`)

**Networking:**
- OkHttp `4.12.0` — HTTP client (used in `core:engine`, `core:iconpack`, `core:pwa`, `core:translate`)

**Image Loading:**
- Coil Compose `2.7.0` — async image loading in Compose
- Coil SVG `2.7.0` — SVG rendering support

**Serialization:**
- Gson `2.11.0` — JSON serialization in `core:backup`, `core:pwa`, `core:translate`
- `org.json` `20240303` — JSON parsing in `core:domain`, `core:iconpack`

**Browser / WebView Engine:**
- GeckoView `128.0.20240704121409` (arm64-v8a) — Mozilla-based web engine in `core:engine`
- AndroidX WebKit `1.12.1` — WebView compatibility wrapper in `core:engine`, `core:isolation`

**Biometrics & Security:**
- AndroidX Biometric `1.1.0` — fingerprint/face unlock in `core:security`

**Background Work:**
- WorkManager `2.9.1` — background task scheduling in `core:backup`

**File Access:**
- AndroidX DocumentFile `1.0.1` — SAF document access in `core:backup`

**QR / Barcode:**
- ZXing Core `3.5.3` — QR code generation/parsing in `core:deeplink`

**About / Attribution:**
- AboutLibraries Compose M3 `11.2.3` (`com.mikepenz`) — open-source licenses screen in `feature:settings`

**AppCompat:**
- AndroidX AppCompat `1.7.0` — required for `BiometricPrompt` (`FragmentActivity`) and locale support

## Build System

**Tool:** Gradle with Kotlin DSL
**AGP:** `8.7.3` (Android Gradle Plugin)
**Kotlin:** `2.0.21`
**KSP:** `2.0.21-1.0.28` — Kotlin Symbol Processing (Room annotation processor)

**Convention Plugins (build-logic/):**
- `shellify.android.application` → `AndroidApplicationConventionPlugin.kt` — compileSdk 36, minSdk 23, JVM 17
- `shellify.android.library` → `AndroidLibraryConventionPlugin.kt` — same SDK/JVM settings
- `shellify.compose` → `ComposeConventionPlugin.kt` — Compose compiler plugin
- `shellify.jvm.library` → `JvmLibraryConventionPlugin.kt` — pure-JVM modules (`:core:domain`)
- `shellify.ksp` → `KspConventionPlugin.kt` — KSP annotation processing

**Gradle Optimizations:**
- Configuration cache: enabled (`org.gradle.configuration-cache=true`, warnings mode)
- Build cache: enabled (`org.gradle.caching=true`)
- JVM args: `-Xmx2048m`
- Non-transitive R class: enabled (`android.nonTransitiveRClass=true`)

**Code Quality Plugins:**
- Detekt `1.23.7` — static analysis; config at `config/detekt/detekt.yml`
- Detekt Formatting (ktlint-based) — formatting rules
- Android Lint — config at `config/lint/lint.xml`
- Roborazzi `1.60.0` — screenshot testing plugin

## Min/Target SDK

| Setting | Value |
|---------|-------|
| `minSdk` | 23 (Android 6.0) |
| `targetSdk` | 36 |
| `compileSdk` | 36 |
| Java source/target compatibility | 17 |
| Kotlin JVM target | 17 |

## Key Dependencies (with versions)

| Dependency | Version | Used In |
|------------|---------|---------|
| AGP | 8.7.3 | build toolchain |
| Kotlin | 2.0.21 | all modules |
| KSP | 2.0.21-1.0.28 | `:core:database`, `:app` |
| Compose BOM | 2024.12.01 | all UI/feature modules |
| Navigation Compose | 2.8.5 | `:app` |
| Lifecycle ViewModel | 2.8.7 | all feature modules |
| Room | 2.6.1 | `:core:database`, `:app` |
| SQLCipher | 4.5.4 | `:core:database` |
| DataStore Preferences | 1.1.1 | `:core:security`, `:core:isolation`, `:core:theme`, features |
| OkHttp | 4.12.0 | `:core:engine`, `:core:iconpack`, `:core:pwa`, `:core:translate` |
| GeckoView (arm64) | 128.0.20240704121409 | `:core:engine` |
| WebKit | 1.12.1 | `:core:engine`, `:core:isolation` |
| Coil Compose + SVG | 2.7.0 | `:core:shortcut`, `:core:ui`, feature modules |
| Biometric | 1.1.0 | `:core:security` |
| WorkManager | 2.9.1 | `:core:backup` |
| ZXing Core | 3.5.3 | `:core:deeplink` |
| Gson | 2.11.0 | `:core:backup`, `:core:pwa`, `:core:translate` |
| Coroutines | 1.9.0 | all modules |
| JUnit | 4.13.2 | all modules (unit tests) |
| MockK | 1.13.12 | all modules (unit tests) |
| Konsist | 0.16.1 | `:app` (architecture tests) |
| Roborazzi | 1.60.0 | `:app` (screenshot tests) |
| Robolectric | 4.16.1 | `:app` (JVM Android tests) |
| AboutLibraries | 11.2.3 | `:feature:settings` |
| Detekt | 1.23.7 | all modules |

## Module Structure

```
:app                     — Application entry point; wires all core + feature modules
:core:domain             — Pure-JVM; domain models, use cases (no Android deps)
:core:crypto             — Encryption utilities
:core:security           — Biometrics, auth gating, DataStore-backed settings
:core:locale             — Language/locale switching
:core:ui                 — Shared Compose UI components
:core:database           — Room + SQLCipher database layer
:core:engine             — GeckoView + WebKit browser engine abstraction
:core:isolation          — WebView isolation / sandboxing logic
:core:iconpack           — Simple Icons CDN integration
:core:pwa                — PWA manifest parsing, favicon fetching
:core:shortcut           — Android shortcut management
:core:deeplink           — Deep link handling + QR code support
:core:translate          — Google Translate API bridge (JS injection)
:core:theme              — Dynamic theming via Compose + DataStore
:core:backup             — WorkManager-backed import/export
:feature:home            — Home screen
:feature:add             — Add shortcut flow
:feature:category        — Category management
:feature:settings        — App settings + license screen
:feature:onboarding      — First-run onboarding
:feature:shortcuts       — Shortcuts list
:feature:translate       — Per-page translation UI
:feature:webview         — WebView screen wrapping GeckoView engine
:feature:share           — Share intent handling
:feature:shortcut        — Pinned shortcut launcher
:feature:link-dispatcher — Android "Open with…" handler for http/https intents and share targets
:core:navigation         — WebViewIntentFactory interface (DI boundary between link-dispatcher and webview)
```

---

*Stack analysis: 2026-05-15 — updated 2026-05-21*
