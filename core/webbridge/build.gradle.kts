plugins {
    id("shellify.android.library")
}

android {
    namespace = "io.shellify.core.webbridge"
    testOptions {
        unitTests {
            // android.util.Log is used in ShellifyBridge for debug logging;
            // return default values in unit tests so Log.d() becomes a no-op.
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
}
