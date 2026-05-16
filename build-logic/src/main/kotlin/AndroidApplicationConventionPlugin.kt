import com.adarshr.gradle.testlogger.TestLoggerExtension
import com.adarshr.gradle.testlogger.theme.ThemeType
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
                apply("com.adarshr.test-logger")
            }
            extensions.configure<TestLoggerExtension> {
                theme = ThemeType.MOCHA
                slowThreshold = 1000
                showStandardStreams = true
                showPassedStandardStreams = false
                showSkippedStandardStreams = false
            }
            extensions.configure<BaseAppModuleExtension> {
                compileSdk = 36
                defaultConfig {
                    minSdk = 26
                    targetSdk = 36
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }
            tasks.withType<KotlinCompile>().configureEach {
                kotlinOptions.jvmTarget = "17"
            }
        }
    }
}
