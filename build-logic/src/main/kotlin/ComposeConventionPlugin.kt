import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
            // Wire buildFeatures.compose=true for whichever Android plugin is present.
            // withPlugin fires lazily, so order of plugin application doesn't matter.
            pluginManager.withPlugin("com.android.library") {
                extensions.configure<LibraryExtension> {
                    buildFeatures { compose = true }
                }
            }
            pluginManager.withPlugin("com.android.application") {
                extensions.configure<BaseAppModuleExtension> {
                    buildFeatures { compose = true }
                }
            }
        }
    }
}
