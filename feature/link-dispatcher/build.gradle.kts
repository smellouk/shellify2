plugins {
    id("shellify.android.library")
    id("shellify.compose")
}
android { namespace = "io.shellify.feature.linkdispatcher" }
dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:deeplink"))
    implementation(project(":core:security"))
    implementation(project(":core:ui"))
    implementation(project(":core:navigation"))
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
