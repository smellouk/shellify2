package io.shellify.app.e2e

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies that BiometricManager returns a recognised status code on the test device.
 * The actual biometric prompt UI is not exercised here — automated tests cannot
 * simulate fingerprint/face/PIN input.
 */
@RunWith(AndroidJUnit4::class)
class BiometricAvailabilityTest {

    @Test
    fun biometricManager_canAuthenticate_returnsRecognisedCode() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val result = BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        val validCodes = setOf(
            BiometricManager.BIOMETRIC_SUCCESS,
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED,
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED,
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN,
        )
        assertTrue("canAuthenticate must return a defined result code, got $result", result in validCodes)
    }
}
