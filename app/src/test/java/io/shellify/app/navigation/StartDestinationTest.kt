package io.shellify.app.navigation

import io.shellify.app.core.theme.ThemeManager
import io.shellify.app.presentation.navigation.Screen
import io.shellify.app.presentation.navigation.resolveStartDestination
import org.junit.Assert.assertEquals
import org.junit.Test

class StartDestinationTest {

    @Test
    fun freshInstall_routesToConsent() {
        assertEquals(
            Screen.Consent.route,
            resolveStartDestination(consentVersion = 0, onboardingDone = false),
        )
    }

    @Test
    fun freshInstall_consentTakesPrecedenceOverOnboarding() {
        assertEquals(
            Screen.Consent.route,
            resolveStartDestination(consentVersion = 0, onboardingDone = true),
        )
    }

    @Test
    fun outdatedConsent_routesToUpdateConsent() {
        assertEquals(
            Screen.UpdateConsent.route,
            resolveStartDestination(consentVersion = 1, onboardingDone = true),
        )
    }

    @Test
    fun outdatedConsent_onboardingPending_stillRoutesToUpdateConsent() {
        assertEquals(
            Screen.UpdateConsent.route,
            resolveStartDestination(consentVersion = 1, onboardingDone = false),
        )
    }

    @Test
    fun currentConsent_onboardingPending_routesToOnboarding() {
        assertEquals(
            Screen.Onboarding.route,
            resolveStartDestination(
                consentVersion = ThemeManager.CURRENT_CONSENT_VERSION,
                onboardingDone = false,
            ),
        )
    }

    @Test
    fun currentConsent_onboardingDone_routesToHome() {
        assertEquals(
            Screen.Home.route,
            resolveStartDestination(
                consentVersion = ThemeManager.CURRENT_CONSENT_VERSION,
                onboardingDone = true,
            ),
        )
    }
}
