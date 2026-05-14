plugins {
    id("shellify.android.library")
}

android {
    namespace = "io.shellify.core.deeplink"
}

dependencies {
    implementation(project(":core:security"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.zxing.core)
}


dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
