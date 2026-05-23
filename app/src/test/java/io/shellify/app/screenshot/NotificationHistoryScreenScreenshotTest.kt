package io.shellify.app.screenshot

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import io.shellify.app.domain.model.PwaNotification
import io.shellify.app.presentation.settings.notifications.NotificationHistoryContent
import io.shellify.app.presentation.settings.notifications.NotificationHistoryUiState
import io.shellify.app.presentation.theme.ShellifyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NotificationHistoryScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyState() {
        composeTestRule.setContent {
            ShellifyTheme {
                NotificationHistoryContent(
                    state = NotificationHistoryUiState(notifications = emptyList(), isLoading = false),
                    onBack = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = screenshotOptions)
    }

    @Test
    fun populatedState() {
        val baseTime = System.currentTimeMillis()
        val notifications = listOf(
            PwaNotification(
                id = 1L,
                appId = 42L,
                title = "New message from Alice",
                body = "Hey, are you free for a call?",
                timestamp = baseTime - 5 * 60 * 1000L,
            ),
            PwaNotification(
                id = 2L,
                appId = 42L,
                title = "Build finished",
                body = null,
                timestamp = baseTime - 3600 * 1000L,
            ),
            PwaNotification(
                id = 3L,
                appId = 42L,
                title = "Daily digest",
                body = "You have 3 unread items waiting for your review.",
                timestamp = baseTime - 24 * 3600 * 1000L,
            ),
        )
        composeTestRule.setContent {
            ShellifyTheme {
                NotificationHistoryContent(
                    state = NotificationHistoryUiState(notifications = notifications, isLoading = false),
                    onBack = {},
                )
            }
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = screenshotOptions)
    }
}
