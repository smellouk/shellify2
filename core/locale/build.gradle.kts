plugins {
    id("shellify.android.library")
}

android {
    namespace = "io.shellify.core.locale"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
}
