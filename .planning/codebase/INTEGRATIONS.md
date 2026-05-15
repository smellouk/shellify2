# Integrations

**Analysis Date:** 2026-05-15

## External APIs

**Google Translate (unofficial):**
- Endpoint: `https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=<lang>`
- Usage: In-page translation of web content via injected JavaScript
- Implementation: `core/translate/src/main/java/io/shellify/app/core/translate/TranslateBridge.kt`
- Method: JavaScript bridge injected into GeckoView / WebView; the JS makes the fetch call client-side, not server-side via OkHttp
- Auth: None (public unofficial endpoint, no API key)

**Google Favicon Service:**
- Endpoint: `https://www.google.com/s2/favicons?domain=<host>&sz=128`
- Usage: Fallback favicon resolution for PWA/shortcut icons
- Implementation: `core/pwa/src/main/java/io/shellify/app/core/pwa/FaviconFetcher.kt`
- Auth: None (public endpoint)

**Simple Icons CDN (jsDelivr):**
- Endpoint: `https://cdn.jsdelivr.net/npm/simple-icons/data/simple-icons.json`
- Usage: Fetches the complete Simple Icons catalog for icon pack browser
- Implementation: `core/iconpack/src/main/java/io/shellify/app/core/iconpack/SimpleIconsManager.kt`
- Client: OkHttp (`okhttp` `4.12.0`)
- Auth: None (public CDN)

**Target website (user-supplied URLs):**
- Usage: `core/pwa/src/main/java/io/shellify/app/core/pwa/PwaAnalyzer.kt` fetches `manifest.json` and page HTML to detect PWA metadata
- Client: OkHttp
- Auth: None (user-provided URL)

## Third-party SDKs

**GeckoView (Mozilla Firefox engine):**
- Package: `org.mozilla.geckoview:geckoview-arm64-v8a:128.0.20240704121409`
- Purpose: Full Firefox-based web engine used to render all web content; replaces Android System WebView
- Module: `:core:engine`
- Repository: `https://maven.mozilla.org/maven2/`
- Native libs excluded from packaging: `libxul.so`, `libmozglue.so`, `liblgpllibs.so`

**SQLCipher:**
- Package: `net.zetetic:android-database-sqlcipher:4.5.4`
- Purpose: AES-256 transparent encryption of the Room database
- Module: `:core:database`
- Used alongside: `androidx.sqlite:sqlite-ktx:2.4.0` (SupportFactory bridge)

**ZXing:**
- Package: `com.google.zxing:core:3.5.3`
- Purpose: QR code generation and parsing for deep link / shortcut sharing
- Module: `:core:deeplink`

**Coil:**
- Package: `io.coil-kt:coil-compose:2.7.0`, `io.coil-kt:coil-svg:2.7.0`
- Purpose: Async image loading in Compose; SVG support for icon rendering
- Modules: `:core:ui`, `:core:shortcut`, `:feature:add`, `:feature:home`, `:feature:settings`, `:feature:shortcuts`

**AboutLibraries:**
- Package: `com.mikepenz:aboutlibraries-compose-m3:11.2.3`
- Purpose: Auto-generates open-source license attribution screen
- Module: `:feature:settings`
- Gradle plugin: `com.mikepenz.aboutlibraries.plugin`

## Analytics / Monitoring

**None detected.** No analytics SDKs (Firebase, Mixpanel, Amplitude, etc.), crash reporting (Crashlytics, Sentry), or performance monitoring tools are present in any module's dependencies.

## Authentication

**Biometric Authentication (local device only):**
- SDK: `androidx.biometric:biometric:1.1.0`
- Purpose: Fingerprint / face unlock to gate app access
- Module: `:core:security`
- No remote identity provider (no OAuth, no Firebase Auth, no SSO)

**Encrypted Preferences:**
- SDK: AndroidX DataStore Preferences `1.1.1`
- Purpose: Persists security settings (lock state, biometric toggle) locally
- Module: `:core:security`

## Other Services

**CI/CD — GitHub Actions:**
- Workflows: `.github/workflows/pull-request.yml`, `.github/workflows/release.yml`
- PR checks: Android Lint, Detekt, unit tests + Konsist, screenshot tests (Roborazzi)
- Release flow: tag-triggered, builds signed APK/AAB, generates CHANGELOG via `git-cliff` (`cliff.toml`), publishes GitHub Release
- Java distribution: Temurin 17 (via `actions/setup-java@v4`)
- Gradle action: `gradle/actions/setup-gradle@v4`

**Release Signing:**
- Configured via environment variables: `SIGNING_STORE_FILE`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`
- Only applied to `release` build type
- Config: `app/build.gradle.kts` `signingConfigs` block

**Changelog Generation:**
- Tool: `git-cliff` (via `orhun/git-cliff-action@v3`)
- Config: `cliff.toml` at repo root
- Output: `CHANGELOG.md` (committed back to `main` on release)

## Webhooks & Callbacks

**Incoming:** None detected.
**Outgoing:** None detected. No webhook dispatch to external services.

---

*Integration audit: 2026-05-15*
