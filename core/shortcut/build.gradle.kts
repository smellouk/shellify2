plugins {
    id("shellify.android.library")
}

android {
    namespace = "io.shellify.core.shortcut"
}

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
}
