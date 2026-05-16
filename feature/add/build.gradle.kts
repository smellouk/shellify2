plugins {
    id("shellify.android.library")
    id("shellify.compose")
}
android { namespace = "io.shellify.feature.add" }
dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:security"))
    implementation(project(":core:iconpack"))
    implementation(project(":core:engine"))
    implementation(project(":core:shortcut"))
    implementation(project(":core:pwa"))
    implementation(project(":core:theme"))
    implementation(project(":core:ui"))
    implementation(project(":feature:webview"))
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
