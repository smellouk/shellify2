<!-- refreshed: 2026-05-15 -->
# Architecture

**Analysis Date:** 2026-05-15

## System Overview

```text
┌──────────────────────────────────────────────────────────────────────┐
│                         :app  (NavHost)                               │
│  MainActivity  ·  AppNavigation  ·  ShellifyApplication               │
│  `app/src/main/java/io/shellify/app/`                                 │
├─────────┬──────────┬───────────┬────────────┬────────────┬───────────┤
│feature: │feature:  │feature:   │feature:    │feature:    │feature:   │
│  home   │  add     │ settings  │  webview   │  shortcuts │  …(+5)    │
│`feature/│`feature/ │`feature/  │`feature/   │`feature/   │           │
│ home/`  │  add/`   │settings/` │ webview/`  │shortcuts/` │           │
└────┬────┴────┬─────┴─────┬─────┴──────┬─────┴─────┬──────┴─────┬────┘
     │         │           │            │            │            │
     ▼         ▼           ▼            ▼            ▼            ▼
┌──────────────────────────────────────────────────────────────────────┐
│                        core:domain                                    │
│   Models · Repository interfaces · Use cases                          │
│   `core/domain/src/main/java/io/shellify/app/domain/`                │
├────────────────────────────────────────────────────────────────────  ┤
│  core:database  │  core:engine  │  core:theme  │  core:security  │…  │
│  `core/database`│  `core/engine`│  `core/theme`│  `core/security`│   │
└─────────────────┴───────────────┴──────────────┴─────────────────┴──┘
                              │
                              ▼
┌──────────────────────────────────────────────────────────────────────┐
│  SQLCipher-encrypted Room DB   +   DataStore   +   FileSystem         │
│  `core/database/src/…/AppDatabase.kt`                                 │
└──────────────────────────────────────────────────────────────────────┘
```

## Component Responsibilities

| Component | Responsibility | Key File |
|-----------|----------------|----------|
| `:app` | Application class, DI container, Nav host | `app/src/main/java/io/shellify/app/ShellifyApplication.kt` |
| `MainActivity` | Single activity entry, theme/security setup | `app/src/main/java/io/shellify/app/presentation/MainActivity.kt` |
| `AppNavigation` | NavHost + bottom nav bar, VM instantiation | `app/src/main/java/io/shellify/app/presentation/navigation/AppNavigation.kt` |
| `feature:home` | App grid, search, category filter, quick-add | `feature/home/src/main/java/io/shellify/app/presentation/home/` |
| `feature:add` | Add / edit PWA form, manifest analysis | `feature/add/src/main/java/io/shellify/app/presentation/add/` |
| `feature:webview` | Full-screen PWA browser (Activity) | `feature/webview/src/main/java/io/shellify/app/presentation/webview/WebViewActivity.kt` |
| `feature:settings` | Per-app (`AppSettings`) + global (`GlobalSettings`) | `feature/settings/src/main/java/io/shellify/app/presentation/settings/` |
| `feature:category` | Category CRUD | `feature/category/src/main/java/io/shellify/app/presentation/category/` |
| `feature:shortcuts` | Pinned launcher shortcuts list | `feature/shortcuts/src/main/java/io/shellify/app/presentation/shortcuts/` |
| `feature:shortcut` | Transparent trampoline Activity for shortcut taps | `feature/shortcut/src/main/java/io/shellify/app/presentation/shortcut/ShortcutActivity.kt` |
| `feature:onboarding` | First-run consent + wizard | `feature/onboarding/src/main/java/io/shellify/app/presentation/onboarding/` |
| `feature:translate` | Per-app translation config screen | `feature/translate/src/main/java/io/shellify/app/presentation/translate/` |
| `feature:share` | QR code + deep-link export sheet | `feature/share/src/main/java/io/shellify/app/presentation/share/AppShareSheet.kt` |
| `core:domain` | Domain models, repository interfaces, use cases | `core/domain/src/main/java/io/shellify/app/domain/` |
| `core:database` | Room DAOs, entities, mappers, repository impls | `core/database/src/main/java/io/shellify/app/data/` |
| `core:engine` | `BrowserEngine` abstraction, `SystemWebViewEngine`, `GeckoViewEngine` | `core/engine/src/main/java/io/shellify/app/core/engine/` |
| `core:isolation` | Per-app WebView profile / cookie isolation | `core/isolation/src/main/java/io/shellify/app/core/isolation/IsolationManager.kt` |
| `core:theme` | `ThemeManager` (DataStore-backed preferences) | `core/theme/src/main/java/io/shellify/app/core/theme/ThemeManager.kt` |
| `core:security` | `PasswordManager`, `BiometricHelper`, `CryptoManager` (in `core:crypto`) | `core/security/src/main/java/io/shellify/app/core/security/` |
| `core:pwa` | `PwaAnalyzer` (manifest/meta fetch), `FaviconFetcher` | `core/pwa/src/main/java/io/shellify/app/core/pwa/` |
| `core:backup` | `.pwab` encrypted ZIP export/import, `BackupWorker` | `core/backup/src/main/java/io/shellify/app/core/backup/` |
| `core:deeplink` | `DeepLinkHandler` — parses `shellify://add` and `https://shellify.app/add` | `core/deeplink/src/main/java/io/shellify/app/core/deeplink/DeepLinkHandler.kt` |
| `core:shortcut` | `PwaShortcutManager`, launcher shortcut icons | `core/shortcut/src/main/java/io/shellify/app/core/shortcut/` |
| `core:iconpack` | Simple Icons catalogue reader | `core/iconpack/src/main/java/io/shellify/app/core/iconpack/` |
| `core:translate` | `TranslateBridge` (JS injection) | `core/translate/src/main/java/io/shellify/app/core/translate/TranslateBridge.kt` |
| `core:ui` | Shared Composables, theme colors, Dimens | `core/ui/src/main/java/io/shellify/app/` |
| `core:locale` | `LocaleHelper` — language wrapping | `core/locale/src/main/java/io/shellify/app/core/locale/LocaleHelper.kt` |
| `core:crypto` | `CryptoManager` — Android Keystore passphrase | `core/crypto/src/main/java/io/shellify/app/core/crypto/CryptoManager.kt` |

## Pattern Overview

**Overall:** Clean Architecture + MVVM (feature modules), manual service-locator DI via `ShellifyApplication`

**Key Characteristics:**
- Three-layer Clean Architecture: **domain** (`core:domain`) — **data** (`core:database`) — **presentation** (each `feature:*`)
- Feature modules contain only `Screen.kt` (Composable or Activity) and `ViewModel.kt`; no repositories, DAOs, or database references
- `ShellifyApplication` acts as the manual DI container — it constructs and exposes all use cases, managers, and repositories as `lazy` properties
- ViewModels receive use cases via constructor injection (passed from `AppNavigation` at nav-graph composition time); no Hilt/Dagger
- UI state is modelled as immutable `data class UiState` emitted through `StateFlow`; pattern is MVVM with unidirectional data flow
- Features are navigation-isolated: no `feature:*` module depends on another `feature:*` module (exception: `feature:home` → `feature:share` and `feature:webview`, `feature:shortcut` → `feature:webview`)

## Layers

**Domain Layer:**
- Purpose: Business models, repository contracts, use cases
- Location: `core/domain/src/main/java/io/shellify/app/domain/`
- Contains: `model/` (WebApp, Category, PwaManifest, EngineType, …), `repository/` (interfaces), `usecase/` (one class per operation)
- Depends on: nothing (pure Kotlin)
- Used by: every feature module, `core:database`, `core:backup`

**Data Layer:**
- Purpose: Room persistence, entity↔domain mappers, repository implementations
- Location: `core/database/src/main/java/io/shellify/app/data/`
- Contains: `local/` (AppDatabase, DAOs, entities), `mapper/` (extension functions), `repository/` (impl classes)
- Depends on: `core:domain`, Room, SQLCipher
- Used by: `:app` (wires `*RepositoryImpl` to use cases)

**Presentation Layer:**
- Purpose: UI rendering and user interaction
- Location: Each `feature/*/src/main/java/io/shellify/app/presentation/<name>/`
- Contains: `<Name>Screen.kt` (Composable or Activity), `<Name>ViewModel.kt`, inline `<Name>UiState` data class
- Depends on: `core:domain` use cases, `core:ui`, relevant `core:*` managers
- Used by: `:app` NavHost

**Infrastructure / Core Services:**
- Purpose: Cross-cutting platform capabilities
- Location: `core/<name>/src/main/java/io/shellify/app/core/<name>/`
- Key services: `IsolationManager`, `ThemeManager`, `PasswordManager`, `GeckoEngineManager`, `BackupManager`, `PwaAnalyzer`, `AdBlocker`
- Depends on: `core:domain` models, Android platform APIs
- Used by: feature modules and `:app`

## Data Flow

### Primary Request Path — display the app list

1. `ShellifyApplication.getWebApps` exposes `GetWebAppsUseCase(webAppRepository)` (`app/src/main/java/io/shellify/app/ShellifyApplication.kt:45`)
2. `AppNavigation` passes `app.getWebApps` into `HomeViewModel` constructor (`app/src/main/java/io/shellify/app/presentation/navigation/AppNavigation.kt:160`)
3. `HomeViewModel.uiState` calls `getWebApps()` → `WebAppRepository.getAll()` → Room Flow → maps entities to domain models (`feature/home/src/main/java/io/shellify/app/presentation/home/HomeViewModel.kt:53`)
4. `HomeScreen` collects `uiState` as `StateFlow` and renders the list

### Add / Edit PWA

1. User navigates to `Screen.Add.createRoute(appId)` (`app/.../navigation/Screen.kt`)
2. `AddViewModel` calls `GetWebAppByIdUseCase` to pre-fill form; calls `PwaAnalyzer.analyze(url)` to fetch manifest
3. On save: `SaveWebAppUseCase(webAppRepository)` → `WebAppRepositoryImpl.save()` → Room upsert

### Launch a WebApp (shortcut tap)

1. `ShortcutActivity` receives home-screen intent, reads `EXTRA_APP_ID` (`feature/shortcut/.../ShortcutActivity.kt`)
2. Starts `WebViewActivity` with app ID
3. `WebViewActivity` loads `GetWebAppByIdUseCase`, calls `IsolationManager.attachProfile` + `restoreSession`, then `BrowserEngine.loadUrl`

### Deep Link

1. `MainActivity.onNewIntent` receives `shellify://add?url=…`
2. `DeepLinkHandler.parse(uri)` decodes Base64-URL encoded URL (`core/deeplink/.../DeepLinkHandler.kt`)
3. Emitted to `ShellifyApplication.pendingDeepLink` (MutableSharedFlow)
4. `AppNavigation` collects it and navigates to `Screen.Add.createRoute(url, name)` after user confirms

**State Management:**
- App-level preferences (theme, language, consent): `ThemeManager` via Jetpack DataStore (`core/theme/.../ThemeManager.kt`)
- Per-screen transient UI state: `MutableStateFlow<UiState>` inside ViewModel, exposed as `StateFlow`
- Deep link pending state: `MutableSharedFlow<Pair<String,String>>(replay=1)` on `ShellifyApplication`

## Key Abstractions

**BrowserEngine:**
- Purpose: Swap WebView engine (System WebView ↔ GeckoView) without changing call sites
- Interface: `core/engine/src/main/java/io/shellify/app/core/engine/BrowserEngine.kt`
- Implementations: `SystemWebViewEngine.kt`, `GeckoViewEngine.kt` (same directory)
- Pattern: Strategy

**WebViewServiceProvider:**
- Purpose: Interface allowing `WebViewActivity` to receive services without coupling to `ShellifyApplication`
- File: `feature/webview/src/main/java/io/shellify/app/presentation/webview/WebViewServiceProvider.kt`
- Pattern: Service-locator interface / dependency inversion

**Use Cases:**
- Purpose: Single-responsibility actions bridging presentation to data
- Examples: `core/domain/src/main/java/io/shellify/app/domain/usecase/` (10 use cases)
- Pattern: Command / Interactor — each class has one `operator fun invoke()` method

**IsolationManager:**
- Purpose: Abstracts API-level difference in WebView isolation (Profiles on API 33+, CookieJar on API 23–32)
- File: `core/isolation/src/main/java/io/shellify/app/core/isolation/IsolationManager.kt`
- Pattern: Facade

## Entry Points

**App launch:**
- Location: `app/src/main/java/io/shellify/app/presentation/MainActivity.kt`
- Triggers: Launcher intent
- Responsibilities: Theme setup, deep link handling, hosts Compose content, wires `AppNavigation`

**Shortcut tap:**
- Location: `feature/shortcut/src/main/java/io/shellify/app/presentation/shortcut/ShortcutActivity.kt`
- Triggers: Home-screen launcher shortcut
- Responsibilities: Reads app ID, starts `WebViewActivity`, finishes self

**Deep Link:**
- Schemes: `shellify://add?url=…` and `https://shellify.app/add?url=…`
- Handler: `MainActivity.onNewIntent` → `DeepLinkHandler.parse` → `ShellifyApplication.pendingDeepLink`

**BackupWorker:**
- Location: `core/backup/src/main/java/io/shellify/app/core/backup/BackupWorker.kt`
- Triggers: WorkManager scheduled job
- Responsibilities: Automated `.pwab` encrypted backup to user-selected directory

## Architectural Constraints

- **Threading:** Main thread for UI; coroutines (`Dispatchers.IO`) for DB, network, and file I/O in all use cases and managers
- **Global state:** `ShellifyApplication` holds all singleton instances as `lazy` properties; `AppDatabase` uses a double-checked lock singleton
- **Circular imports:** No feature-to-feature cycles; `feature:home` → `feature:share` and `feature:webview` are the only cross-feature deps (both are unidirectional)
- **No Hilt/Dagger:** Manual DI exclusively via `ShellifyApplication` lazy properties and constructor injection at nav graph time
- **GeckoView native loading:** GeckoView `.so` files must be pre-loaded in every process via `GeckoNativeLoader.injectAndLoad` before first use (`app/.../ShellifyApplication.kt:87`)

## Anti-Patterns

### ViewModel construction in NavHost lambda

**What happens:** `AppNavigation` instantiates every ViewModel with `remember { MyViewModel(app.useCase1, …) }` inside a `composable { }` lambda (`app/.../AppNavigation.kt`)
**Why it's wrong:** ViewModels are not scoped to the back stack entry, so they are re-created on configuration change if the remember key changes; also makes testing the nav graph harder
**Do this instead:** Use `viewModel(factory = …)` with a `ViewModelProvider.Factory` or Hilt's `hiltViewModel()`, scoped to the `NavBackStackEntry`

### ThemeManager overloaded with unrelated state

**What happens:** `ThemeManager` stores consent, onboarding state, language code, UA mode, engine type, and accent color in the same DataStore file (`core/theme/.../ThemeManager.kt`)
**Why it's wrong:** Conceptually unrelated concerns (browser engine preference, GDPR consent, theme) share one store, increasing change-coupling
**Do this instead:** Extract `OnboardingManager` (consent + onboarding flags) and `BrowserPreferencesManager` (UA mode, engine type) into separate DataStore files

## Error Handling

**Strategy:** `runCatching` at I/O boundaries; Result-wrapped returns for backup operations; null returns for optional lookups

**Patterns:**
- Network calls in `PwaAnalyzer` and `GeckoEngineManager` use `runCatching { … }.getOrNull()` — failures are silently swallowed and callers receive `null` or empty defaults
- `BackupManager.backup()` returns `Result<String>` — callers can check `isSuccess`/`isFailure`
- ViewModels catch exceptions per-operation via `viewModelScope.launch { runCatching { … } }` and surface errors through `UiState` fields

## Cross-Cutting Concerns

**Logging:** Android `Log.*` calls (no structured logging library detected); tag-based prefixes per class (e.g., `TAG = "GeckoEngineManager"`)
**Validation:** In-ViewModel (URL format checks, name uniqueness via `GetWebAppByNameUseCase`) and domain model level (`DeepLinkHandler.decodeUrl` enforces `https://` prefix)
**Authentication:** `PasswordManager` + `BiometricHelper` for per-app and global app lock; `CryptoManager` (Android Keystore) for database passphrase and backup encryption

---

*Architecture analysis: 2026-05-15*
