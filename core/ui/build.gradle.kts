plugins {
    id("shellify.android.library")
    id("shellify.compose")
}

android {
    namespace = "io.shellify.core.ui"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:iconpack"))

    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)

    debugImplementation(libs.compose.ui.tooling)
}
