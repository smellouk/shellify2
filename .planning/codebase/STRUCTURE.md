# Project Structure

**Analysis Date:** 2026-05-15

## Root Layout

```
shellify/                          # Project root
├── app/                           # Application module (entry point, NavHost, DI container)
├── core/                          # Shared library modules (no UI, no Activities)
│   ├── backup/                    # .pwab export/import + WorkManager scheduler
│   ├── crypto/                    # Android Keystore passphrase management
│   ├── database/                  # Room (SQLCipher) DAOs, entities, mappers, repo impls
│   ├── deeplink/                  # shellify:// and https://shellify.app/add URI handling
│   ├── domain/                    # Domain models, repository interfaces, use cases
│   ├── engine/                    # BrowserEngine abstraction, SystemWebView, GeckoView
│   ├── iconpack/                  # Simple Icons catalogue reader/manager
│   ├── isolation/                 # Per-app WebView profile / cookie isolation
│   ├── locale/                    # Language code wrapping (LocaleHelper)
│   ├── navigation/                # WebViewIntentFactory — DI boundary between link-dispatcher and webview
│   ├── pwa/                       # PWA manifest analysis, favicon fetching
│   ├── security/                  # Password, biometrics, Base64 codecs
│   ├── shortcut/                  # Launcher shortcut creation/removal
│   ├── theme/                     # ThemeManager (DataStore), ThemeMode
│   ├── translate/                 # TranslateBridge JS injection
│   └── ui/                        # Shared Composables, Color, Dimens, Theme
├── feature/                       # Feature modules (presentation layer only)
│   ├── add/                       # Add / edit PWA screen
│   ├── category/                  # Category CRUD screen
│   ├── home/                      # Main app grid screen
│   ├── link-dispatcher/           # "Open with…" handler for http/https intents + share targets
│   ├── onboarding/                # First-run consent + wizard
│   ├── settings/                  # Per-app and global settings screens
│   ├── share/                     # QR code + deep-link share sheet
│   ├── shortcut/                  # Transparent trampoline Activity
│   ├── shortcuts/                 # Pinned shortcuts management screen
│   ├── translate/                 # Per-app translation config screen
│   └── webview/                   # Full-screen PWA browser Activity
├── build-logic/                   # Convention plugins (Gradle included build)
│   └── src/main/kotlin/           # Plugin source files
├── config/
│   ├── detekt/                    # Detekt static analysis config
│   └── lint/                      # Android Lint config
├── gradle/
│   └── wrapper/                   # Gradle wrapper files
├── docs/                          # Project docs + demo assets
├── .planning/                     # Planning artifacts (phases, codebase maps)
├── settings.gradle.kts            # Module declarations
├── gradle/libs.versions.toml      # Version catalog (all dependency versions)
└── build.gradle.kts               # Root build config
```

## Module Breakdown

### `:app`

```
app/
├── src/main/
│   ├── AndroidManifest.xml        # MainActivity, FileProvider; WebViewActivity/ShortcutActivity declared in feature modules
│   ├── java/io/shellify/app/
│   │   ├── ShellifyApplication.kt # Manual DI container — all lazy singletons
│   │   └── presentation/
│   │       ├── MainActivity.kt    # Single activity; theme + deep link wiring
│   │       └── navigation/
│   │           ├── AppNavigation.kt  # NavHost, bottom bar, VM instantiation
│   │           └── Screen.kt         # Sealed route definitions
│   └── res/                       # Launcher icons, strings, themes, xml configs
└── build.gradle.kts
```

### `core:domain`

```
core/domain/src/main/java/io/shellify/app/domain/
├── model/
│   ├── WebApp.kt          # Primary entity; holds all per-app settings
│   ├── Category.kt
│   ├── IconSource.kt      # Sealed: Path | SvgIcon | Url
│   ├── PwaManifest.kt     # Parsed Web App Manifest + icons
│   ├── EngineType.kt      # SYSTEM_WEBVIEW | GECKOVIEW
│   └── (TranslateLanguage, LockType, UserAgentMode inline in WebApp.kt)
├── repository/
│   ├── WebAppRepository.kt    # Interface: Flow-based reads + suspend writes
│   └── CategoryRepository.kt
└── usecase/
    ├── GetWebAppsUseCase.kt
    ├── GetWebAppByIdUseCase.kt
    ├── GetWebAppByNameUseCase.kt
    ├── SaveWebAppUseCase.kt
    ├── DeleteWebAppUseCase.kt
    ├── DeleteAllAppsUseCase.kt
    ├── GetCategoriesUseCase.kt
    ├── SaveCategoryUseCase.kt
    ├── DeleteCategoryUseCase.kt
    ├── DeleteAllCategoriesUseCase.kt
    └── FindAppsForUrlUseCase.kt
```

### `core:database`

```
core/database/src/main/java/io/shellify/app/data/
├── local/
│   ├── AppDatabase.kt             # Room + SQLCipher singleton
│   ├── dao/
│   │   ├── WebAppDao.kt
│   │   └── CategoryDao.kt
│   ├── entity/
│   │   ├── WebAppEntity.kt
│   │   └── CategoryEntity.kt
│   └── converter/
│       └── IconSourceConverter.kt # Room TypeConverter for IconSource sealed class
├── mapper/
│   ├── WebAppMapper.kt            # WebAppEntity ↔ WebApp extension fns
│   └── CategoryMapper.kt
└── repository/
    ├── WebAppRepositoryImpl.kt
    └── CategoryRepositoryImpl.kt
```

### `core:engine`

```
core/engine/src/main/java/io/shellify/app/core/
├── engine/
│   ├── BrowserEngine.kt           # Interface: createView, loadUrl, goBack, …
│   ├── BrowserEngineCallback.kt   # Callbacks: onPageStarted, onExternalLink, …
│   ├── SystemWebViewEngine.kt     # Android WebView impl
│   ├── GeckoViewEngine.kt         # Mozilla GeckoView impl
│   ├── GeckoEngineManager.kt      # Download/install/init GeckoView AAR at runtime
│   └── GeckoNativeLoader.kt       # System.load() for GeckoView .so files
├── adblock/
│   ├── AdBlocker.kt               # WebResourceRequest filter
│   └── AdBlockFilterCache.kt
└── webview/
    └── WebViewManager.kt          # Shared WebView configuration helper
```

### `feature/*` (each follows this layout)

```
feature/<name>/
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml        # Activity declarations when needed
│   │   └── java/io/shellify/app/presentation/<name>/
│   │       ├── <Name>Screen.kt        # @Composable screen (or Activity)
│   │       ├── <Name>ViewModel.kt     # ViewModel + UiState data class
│   │       └── (additional composables/helpers inline)
│   └── test/
│       └── java/io/shellify/app/presentation/<name>/
│           └── <Name>ViewModelTest.kt
└── build.gradle.kts
```

## Key Directories

**Domain models:** `core/domain/src/main/java/io/shellify/app/domain/model/`
- `WebApp.kt` is the central entity — read this first to understand all app settings fields

**Use cases:** `core/domain/src/main/java/io/shellify/app/domain/usecase/`
- One class per operation; all follow `operator fun invoke()` convention

**DI container:** `app/src/main/java/io/shellify/app/ShellifyApplication.kt`
- All repository impls, use cases, and managers are wired here as `lazy` properties

**Navigation routes:** `app/src/main/java/io/shellify/app/presentation/navigation/Screen.kt`
- All route strings live here; `AppNavigation.kt` wires them to screens

**Build convention plugins:** `build-logic/src/main/kotlin/`
- `AndroidApplicationConventionPlugin.kt` — `:app` plugin
- `AndroidLibraryConventionPlugin.kt` — all `core:*` and `feature:*` modules
- `ComposeConventionPlugin.kt` — adds Compose BOM + compiler plugin
- `JvmLibraryConventionPlugin.kt` — pure-Kotlin modules
- `KspConventionPlugin.kt` — enables KSP (used for Room)

**Shared UI tokens:** `core/ui/src/main/java/io/shellify/app/presentation/theme/`
- `Color.kt`, `Dimens.kt`, `Theme.kt` — use these for all new UI work

**Version catalog:** `gradle/libs.versions.toml`
- Single source of truth for all dependency and plugin versions

## Naming Conventions

**Files:**
- Screens: `<FeatureName>Screen.kt` (Composable) or `<FeatureName>Activity.kt`
- ViewModels: `<FeatureName>ViewModel.kt`
- UI state: `<FeatureName>UiState` data class, defined inline in the ViewModel file
- Use cases: `<Verb><Entity>UseCase.kt` (e.g., `SaveWebAppUseCase.kt`)
- Repository impls: `<Entity>RepositoryImpl.kt`
- Managers: `<Domain>Manager.kt` (e.g., `IsolationManager.kt`, `ThemeManager.kt`)

**Packages:** `io.shellify.app.<layer>.<feature>` — all modules share the root package `io.shellify.app`

**Gradle modules:** `:core:<name>` and `:feature:<name>` — always lowercase with hyphens for multi-word names

## Where to Add New Code

**New feature screen:**
1. Create `feature/<name>/` directory
2. Add `build.gradle.kts` with `shellify.android.library` + `shellify.compose` plugins
3. Register in `settings.gradle.kts`: `include(":feature:<name>")`
4. Create `<Name>Screen.kt` + `<Name>ViewModel.kt` under `src/main/java/io/shellify/app/presentation/<name>/`
5. Add a `Screen` object in `app/.../navigation/Screen.kt`
6. Wire composable in `app/.../navigation/AppNavigation.kt` — pass use cases from `app` (ShellifyApplication)
7. Add `:feature:<name>` dependency in `app/build.gradle.kts`

**New use case:**
1. Add class in `core/domain/src/main/java/io/shellify/app/domain/usecase/<Verb><Entity>UseCase.kt`
2. Expose as a `lazy` property in `ShellifyApplication.kt`
3. Inject into relevant ViewModels via `AppNavigation.kt`

**New core service / manager:**
1. Create a new `core/<name>/` module (or add to existing `core:<name>` if closely related)
2. Follow `AndroidLibraryConventionPlugin` / `JvmLibraryConventionPlugin` depending on Android dependency
3. Expose as `lazy` in `ShellifyApplication.kt`; declare in `WebViewServiceProvider` interface if needed by `WebViewActivity`

**New domain model field:**
1. Add to `WebApp.kt` or `Category.kt` in `core/domain/src/main/java/io/shellify/app/domain/model/`
2. Mirror in `WebAppEntity.kt` / `CategoryEntity.kt` in `core/database`
3. Update `WebAppMapper.kt` / `CategoryMapper.kt` mappers
4. Increment Room `@Database(version = …)` and write a migration

**Shared UI component:**
- Add to `core/ui/src/main/java/io/shellify/app/presentation/components/SharedComponents.kt`
- Use `Dimens.kt` for spacing and `Color.kt` / `Theme.kt` for colors

## Special Directories

**`build-logic/`:**
- Purpose: Convention Gradle plugins applied to every module via `plugins { id("shellify.android.library") }`
- Generated: No
- Committed: Yes

**`core/database/schemas/`:**
- Purpose: Room schema JSON exports for migration tracking (`1.json`, `2.json`)
- Generated: Yes (by KSP during build)
- Committed: Yes (required for migration validation)

**`.planning/`:**
- Purpose: Phase plans, codebase maps, feature ideas
- Generated: By GSD tooling
- Committed: Yes

**`config/`:**
- Purpose: Detekt (`config/detekt/detekt.yml`) and Lint (`config/lint/lint.xml`) rule configuration
- Generated: No
- Committed: Yes

---

*Structure analysis: 2026-05-15 — updated 2026-05-21*
