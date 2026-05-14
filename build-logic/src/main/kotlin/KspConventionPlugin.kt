import org.gradle.api.Plugin
import org.gradle.api.Project

class KspConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.devtools.ksp")
        }
    }
}
