plugins {
    id("shellify.android.library")
}

android {
    namespace = "io.shellify.core.navigation"
}

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.androidx.core.ktx)
}
