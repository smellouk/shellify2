plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("shellifyJvmLibrary") {
            id = "shellify.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("shellifyAndroidLibrary") {
            id = "shellify.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("shellifyAndroidApplication") {
            id = "shellify.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("shellifyCompose") {
            id = "shellify.compose"
            implementationClass = "ComposeConventionPlugin"
        }
        register("shellifyKsp") {
            id = "shellify.ksp"
            implementationClass = "KspConventionPlugin"
        }
    }
}
