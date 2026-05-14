plugins {
    id("shellify.android.library")
}
android { namespace = "io.shellify.feature.shortcut" }
dependencies {
    implementation(project(":core:shortcut"))
    implementation(project(":feature:webview"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
