plugins {
    id("shellify.android.library")
}

android {
    namespace = "io.shellify.core.crypto"
}

dependencies {
    implementation(libs.androidx.core.ktx)
}
