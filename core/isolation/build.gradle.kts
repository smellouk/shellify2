plugins {
    id("shellify.android.library")
}

android {
    namespace = "io.shellify.core.isolation"
}

dependencies {
    implementation(project(":core:crypto"))
    implementation(project(":core:engine"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.webkit)
    implementation(libs.androidx.datastore.preferences)
}
