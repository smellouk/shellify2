plugins {
    id("shellify.android.library")
    id("shellify.compose")
}
android { namespace = "io.shellify.feature.shortcuts" }
dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:iconpack"))
    implementation(project(":core:pwa"))
    implementation(project(":core:shortcut"))
    implementation(project(":core:ui"))
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
