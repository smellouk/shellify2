plugins {
    id("shellify.android.library")
}

android {
    namespace = "io.shellify.core.engine"

    packaging {
        jniLibs {
            excludes += "**/libxul.so"
            excludes += "**/libmozglue.so"
            excludes += "**/liblgpllibs.so"
        }
    }
}

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.webkit)
    implementation(libs.geckoview)
    implementation(libs.okhttp)
}


dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
