plugins {
    id("shellify.android.library")
}

android {
    namespace = "io.shellify.core.translate"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okhttp)
    implementation(libs.gson)
}
