package io.shellify.app.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withAllAnnotationsOf
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/**
 * Konsist architecture consistency tests.
 *
 * Enforced Clean Architecture boundaries:
 *   domain ← data ← presentation  (arrows = "depends on")
 *   core is a shared infrastructure layer; it may depend on domain but not on data or presentation.
 *
 * Layer access rule (enforced below):
 *   presentation → domain.usecase (only) — never domain.repository directly
 *   domain.usecase → domain.model + domain.repository (only) — never core/data/presentation
 */
class ArchitectureTest {

    // Production source files only — excludes test files from analysis
    private val mainScope = Konsist.scopeFromProduction()

    // ── Layer isolation ───────────────────────────────────────────────────────

    @Test
    fun `domain layer does not import from data layer`() {
        mainScope.files
            .withPackage("io.shellify.app.domain..")
            .assertFalse(additionalMessage = "Domain must not depend on data") { file ->
                file.imports.any { it.name.startsWith("io.shellify.app.data") }
            }
    }

    @Test
    fun `domain layer does not import from presentation layer`() {
        mainScope.files
            .withPackage("io.shellify.app.domain..")
            .assertFalse(additionalMessage = "Domain must not depend on presentation") { file ->
                file.imports.any { it.name.startsWith("io.shellify.app.presentation") }
            }
    }

    @Test
    fun `data layer does not import from presentation layer`() {
        mainScope.files
            .withPackage("io.shellify.app.data..")
            .assertFalse(additionalMessage = "Data must not depend on presentation") { file ->
                file.imports.any { it.name.startsWith("io.shellify.app.presentation") }
            }
    }

    @Test
    fun `core layer does not import from presentation layer`() {
        mainScope.files
            .withPackage("io.shellify.app.core..")
            .assertFalse(additionalMessage = "Core must not depend on presentation") { file ->
                file.imports.any { it.name.startsWith("io.shellify.app.presentation") }
            }
    }

    @Test
    fun `core layer does not import from data layer`() {
        // BackupManager is intentionally excluded: it accesses AppDatabase directly to dump SQL
        mainScope.files
            .withPackage("io.shellify.app.core..")
            .assertFalse(additionalMessage = "Core must not depend on data (except BackupManager)") { file ->
                file.name != "BackupManager" &&
                    file.imports.any { it.name.startsWith("io.shellify.app.data") }
            }
    }

    // ── Use Case purity ───────────────────────────────────────────────────────

    @Test
    fun `use cases must only depend on the domain layer`() {
        mainScope.files
            .withPackage("io.shellify.app.domain.usecase")
            .assertFalse(
                additionalMessage = "Use cases must only import from domain — never core, data, or presentation"
            ) { file ->
                file.imports.any { import ->
                    import.name.startsWith("io.shellify.app.core") ||
                        import.name.startsWith("io.shellify.app.data") ||
                        import.name.startsWith("io.shellify.app.presentation")
                }
            }
    }

    // ── Presentation access rules ─────────────────────────────────────────────

    @Test
    fun `ViewModels must not import domain repository interfaces directly`() {
        mainScope.classes()
            .withNameEndingWith("ViewModel")
            .assertFalse(
                additionalMessage = "ViewModels must access data through use cases, not repositories directly"
            ) { cls ->
                cls.containingFile.imports.any {
                    it.name.startsWith("io.shellify.app.domain.repository")
                }
            }
    }

    @Test
    fun `presentation layer must not import domain repository interfaces directly`() {
        mainScope.files
            .withPackage("io.shellify.app.presentation..")
            .assertFalse(
                additionalMessage = "Presentation must access data through use cases, not repositories directly"
            ) { file ->
                file.imports.any { it.name.startsWith("io.shellify.app.domain.repository") }
            }
    }

    // ── ViewModels ────────────────────────────────────────────────────────────

    @Test
    fun `all ViewModels reside in the presentation package`() {
        mainScope.classes()
            .withNameEndingWith("ViewModel")
            .assertTrue(additionalMessage = "All *ViewModel classes must be in io.shellify.app.presentation..") { cls ->
                cls.resideInPackage("io.shellify.app.presentation..")
            }
    }

    @Test
    fun `all ViewModels import and extend androidx ViewModel`() {
        // Konsist cannot resolve external library parent classes, so we verify via imports + text
        mainScope.classes()
            .withNameEndingWith("ViewModel")
            .assertTrue(additionalMessage = "All *ViewModel classes must extend androidx.lifecycle.ViewModel") { cls ->
                cls.containingFile.imports.any { it.name.contains("androidx.lifecycle.ViewModel") } &&
                    cls.text.contains(": ViewModel()")
            }
    }

    // ── UI State ──────────────────────────────────────────────────────────────

    @Test
    fun `UI state classes must be data classes`() {
        mainScope.classes()
            .withNameEndingWith("UiState")
            .assertTrue(additionalMessage = "*UiState classes must be data classes to enforce immutability") { cls ->
                cls.hasDataModifier
            }
    }

    @Test
    fun `UI state classes must reside in the presentation package`() {
        mainScope.classes()
            .withNameEndingWith("UiState")
            .assertTrue(additionalMessage = "*UiState classes must be in io.shellify.app.presentation..") { cls ->
                cls.resideInPackage("io.shellify.app.presentation..")
            }
    }

    // ── Use Cases ─────────────────────────────────────────────────────────────

    @Test
    fun `all use cases reside in the domain usecase package`() {
        mainScope.classes()
            .withNameEndingWith("UseCase")
            .assertTrue(additionalMessage = "All *UseCase classes must be in io.shellify.app.domain.usecase") { cls ->
                cls.resideInPackage("io.shellify.app.domain.usecase")
            }
    }

    @Test
    fun `all use cases expose logic via an invoke operator function`() {
        mainScope.classes()
            .withNameEndingWith("UseCase")
            .assertTrue(additionalMessage = "Each UseCase must have exactly one operator fun invoke()") { cls ->
                cls.functions(includeNested = false, includeLocal = false)
                    .count { fn -> fn.name == "invoke" && fn.hasOperatorModifier }
                    .let { count -> count == 1 }
            }
    }

    // ── Repositories ──────────────────────────────────────────────────────────

    @Test
    fun `repository interfaces reside in the domain repository package`() {
        mainScope.interfaces()
            .withNameEndingWith("Repository")
            .assertTrue(additionalMessage = "Repository interfaces must be in io.shellify.app.domain.repository") { iface ->
                iface.resideInPackage("io.shellify.app.domain.repository")
            }
    }

    @Test
    fun `repository implementations reside in the data repository package`() {
        mainScope.classes()
            .withNameEndingWith("RepositoryImpl")
            .assertTrue(additionalMessage = "Repository implementations must be in io.shellify.app.data.repository") { cls ->
                cls.resideInPackage("io.shellify.app.data.repository")
            }
    }

    @Test
    fun `repository implementations implement a domain repository interface`() {
        mainScope.classes()
            .withNameEndingWith("RepositoryImpl")
            .assertTrue(additionalMessage = "Each *RepositoryImpl must implement the corresponding *Repository interface") { cls ->
                cls.hasParentInterface { parent ->
                    parent.name.endsWith("Repository") && !parent.name.endsWith("RepositoryImpl")
                }
            }
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    @Test
    fun `mapper classes reside in the data mapper package`() {
        mainScope.classes()
            .withNameEndingWith("Mapper")
            .assertTrue(additionalMessage = "*Mapper classes must be in io.shellify.app.data.mapper") { cls ->
                cls.resideInPackage("io.shellify.app.data.mapper")
            }
    }

    // ── Entities ──────────────────────────────────────────────────────────────

    @Test
    fun `all Room entities reside in the data local entity package`() {
        mainScope.classes()
            .withAllAnnotationsOf(androidx.room.Entity::class)
            .assertTrue(additionalMessage = "@Entity classes must be in io.shellify.app.data.local.entity") { cls ->
                cls.resideInPackage("io.shellify.app.data.local.entity")
            }
    }

    @Test
    fun `Room entity class names end with Entity`() {
        mainScope.classes()
            .withAllAnnotationsOf(androidx.room.Entity::class)
            .assertTrue(additionalMessage = "@Entity classes must be named *Entity") { cls ->
                cls.name.endsWith("Entity")
            }
    }

    // ── General rules ─────────────────────────────────────────────────────────

    @Test
    fun `no production file uses System dot out`() {
        mainScope.files
            .assertFalse(additionalMessage = "Use Logcat instead of System.out.print*") { file ->
                file.text.contains("System.out")
            }
    }

    @Test
    fun `no production file uses printStackTrace`() {
        mainScope.files
            .assertFalse(additionalMessage = "Use Logcat instead of .printStackTrace()") { file ->
                file.text.contains(".printStackTrace()")
            }
    }

    @Test
    fun `all source files have a package declaration`() {
        mainScope.files
            .assertFalse(additionalMessage = "Every Kotlin file must declare a package") { file ->
                file.packagee == null
            }
    }
}
