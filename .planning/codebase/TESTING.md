# Testing

**Analysis Date:** 2026-05-15

## Test Types

| Type | Runner | Location | Scope |
|------|--------|----------|-------|
| Unit tests | JUnit 4 | `src/test/` | ViewModel logic, use cases, mappers, domain models, utilities |
| Screenshot tests | Roborazzi + Robolectric | `src/test/` (JVM, no emulator) | Compose screen rendering golden images |
| Architecture tests | Konsist | `src/test/` (runs as unit test) | Layer boundary rules, naming conventions |
| Instrumented tests | AndroidJUnit4 | `src/androidTest/` | Compose UI interactions, e2e smoke scenarios |

## Testing Frameworks

**Unit / ViewModel testing:**
- JUnit 4 (`junit:junit:4.13.2`) — `@Test`, `@Before`, `@After`
- MockK (`io.mockk:mockk:1.13.12`) — mocking and stubbing
- `kotlinx-coroutines-test` (`1.9.0`) — `runTest`, `UnconfinedTestDispatcher`, `advanceUntilIdle`

**Screenshot testing:**
- Roborazzi (`1.60.0`) — golden image comparison via `captureRoboImage()`
- Robolectric (`4.16.1`) — JVM-based Android rendering
- `@RunWith(RobolectricTestRunner::class)`, `@Config(sdk = [34])`, `@GraphicsMode(GraphicsMode.Mode.NATIVE)`
- Compose test rule: `createComposeRule()`

**Architecture testing:**
- Konsist (`0.16.1`) — compile-scope assertions on Kotlin source tree

**Instrumented (on-device) testing:**
- AndroidJUnit4 (`@RunWith(AndroidJUnit4::class)`)
- Compose UI test: `createComposeRule()`, `onNodeWithText()`, `performClick()`, `assertIsDisplayed()`
- MockK Android (`io.mockk:mockk-android:1.13.14`)

## Test File Location Pattern

**Unit and screenshot tests:**
```
<module>/src/test/java/io/shellify/app/<layer>/<feature>/
```
Examples:
- `feature/home/src/test/java/io/shellify/app/presentation/home/HomeViewModelTest.kt`
- `core/database/src/test/java/io/shellify/app/data/mapper/WebAppMapperTest.kt`
- `app/src/test/java/io/shellify/app/screenshot/HomeScreenScreenshotTest.kt`
- `app/src/test/java/io/shellify/app/konsist/ArchitectureTest.kt`

**Instrumented tests** (all in `:app` module only):
```
app/src/androidTest/java/io/shellify/app/
├── e2e/             # Smoke tests for full user journeys
├── mock/            # Screen-level interaction tests with mocked VMs
│   ├── navigation/
│   └── screen/
└── util/            # Shared test utilities (FakeData)
```

**Test class naming:**
- `*ViewModelTest` — ViewModel unit tests
- `*MapperTest` — mapper unit tests
- `*Test` — general unit/domain tests
- `*ScreenshotTest` — Roborazzi golden image tests
- `Smoke*Test` — instrumented e2e smoke scenarios
- `ArchitectureTest` — Konsist rules (lives in `app` module only)

## Coverage Approach

No coverage percentage threshold is configured. Coverage is enforced by policy:

- **New use cases, mappers, utilities:** require unit tests (stated in `CONTRIBUTING.md`)
- **New Compose screens:** require at least one Roborazzi screenshot test
- **Architecture rules:** automatically covered by Konsist running with every unit test execution

Tests run on the `debug` build type only in CI.

## CI Integration

CI config: `.github/workflows/pull-request.yml`

All four jobs run in parallel on every PR targeting `main`. Concurrency group cancels in-progress runs for the same PR.

| CI Job | Gradle task | What it checks |
|--------|-------------|----------------|
| Android Lint | `./gradlew lintDebug` | Lint rules from `config/lint/lint.xml` |
| Detekt | `./gradlew detekt` | Static analysis from `config/detekt/detekt.yml` |
| Unit Tests & Konsist | `./gradlew testDebugUnitTest` | All unit tests including Konsist architecture checks |
| Screenshot Tests | `./gradlew verifyRoborazziDebug` | Golden image comparison |

Instrumented tests (emulator) are defined in CI config but **commented out** — they would run `./gradlew connectedDebugAndroidTest` on an Android 34 `google_apis` x86_64 emulator.

Reports uploaded as artifacts on failure:
- `lint-reports` → `**/build/reports/lint-results-*.html`
- `detekt-reports` → `**/build/reports/detekt/`
- `unit-test-reports` → `**/build/reports/tests/`
- `screenshot-diffs` → `**/build/outputs/roborazzi/`

## Test Structure

**ViewModel unit test pattern:**
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val getWebApps = mockk<GetWebAppsUseCase>()
    // ... other mocks

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { getWebApps() } returns flowOf(...)
        viewModel = HomeViewModel(getWebApps, ...)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads apps and categories`() = runTest {
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(expected, viewModel.uiState.value.someField)
    }
}
```

**Screenshot test pattern:**
```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HomeScreenScreenshotTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun emptyState() {
        composeTestRule.setContent {
            ShellifyTheme {
                HomeScreen(viewModel = buildVm(HomeUiState(...)), ...)
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
```

**Instrumented e2e pattern:**
```kotlin
@RunWith(AndroidJUnit4::class)
class SmokePwaLifecycleTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun emptyHome_tappingAddWebsite_invokesOnAddApp() {
        composeTestRule.setContent {
            ShellifyTheme {
                HomeScreen(viewModel = homeVm(HomeUiState(...)), onAddApp = { ... }, ...)
            }
        }
        composeTestRule.onNodeWithText("Add a website").performClick()
        assert(addTapped)
    }

    private fun homeVm(state: HomeUiState): HomeViewModel =
        mockk<HomeViewModel>(relaxed = true).also {
            every { it.uiState } returns MutableStateFlow(state)
        }
}
```

**Konsist architecture test pattern:**
```kotlin
class ArchitectureTest {
    private val mainScope = Konsist.scopeFromProduction()

    @Test
    fun `domain layer does not import from data layer`() {
        mainScope.files
            .withPackage("io.shellify.app.domain..")
            .assertFalse { file ->
                file.imports.any { it.name.startsWith("io.shellify.app.data") }
            }
    }
}
```

## Mocking

**Framework:** MockK

**Standard patterns:**
```kotlin
// Relaxed mock (all calls return defaults)
val vm = mockk<HomeViewModel>(relaxed = true)

// Stub return value
every { useCase() } returns flowOf(listOf(...))

// Stub suspend function
coEvery { saveWebApp(any()) } returns 0L

// Verify call count
coVerify(exactly = 1) { saveWebApp(match { it.id == 1L }) }

// Stub StateFlow property for Compose screens
every { vm.uiState } returns MutableStateFlow(HomeUiState(...))
```

**What to mock:** Use case dependencies, coroutine dispatchers (set via `Dispatchers.setMain`), StateFlow sources, Android `Context`.

**What NOT to mock:** Domain models (use `FakeData` builders), Compose test rules.

## Fixtures and Factories

**Test data builder:** `app/src/androidTest/java/io/shellify/app/util/FakeData.kt`

Provides builder functions with default arguments for all major domain types:

```kotlin
object FakeData {
    fun webApp(id: Long = 0L, name: String = "Test App", url: String = "https://example.com", ...): WebApp
    fun webAppList(count: Int = 3): List<WebApp>
    fun category(id: Long = 0L, name: String = "Test Category", ...): Category
    fun categoryList(count: Int = 2): List<Category>
    fun webAppEntity(...): WebAppEntity
    fun categoryEntity(...): CategoryEntity
}
```

For unit tests (in `src/test/`), inline builders are used directly in the test class (no shared `FakeData` object — each test class defines its own helper functions).

## Run Commands

```bash
# Unit tests (includes Konsist architecture checks)
./gradlew testDebugUnitTest

# Screenshot verification (compare against stored goldens)
./gradlew verifyRoborazziDebug

# Regenerate screenshot goldens after intentional UI changes
./gradlew recordRoborazziDebug

# Static analysis
./gradlew detekt

# Android lint
./gradlew lintDebug

# Full check suite (mirrors CI)
./gradlew lintDebug detekt testDebugUnitTest verifyRoborazziDebug
```

Coverage report: Not configured — no `jacocoTestReport` or similar task detected.
