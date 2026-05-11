package dev.pwaforge.presentation

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import dev.pwaforge.PWAForgeApplication
import dev.pwaforge.core.theme.ThemeMode
import dev.pwaforge.presentation.navigation.AppNavigation
import dev.pwaforge.presentation.theme.PWAForgeTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as PWAForgeApplication
        setContent {
            val themeMode by app.themeManager.themeMode.collectAsState(ThemeMode.SYSTEM)
            val dynamicColor by app.themeManager.dynamicColor.collectAsState(true)
            val screenshotProtection by app.passwordManager.screenshotProtection.collectAsState(false)

            LaunchedEffect(screenshotProtection) {
                if (screenshotProtection) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }

            PWAForgeTheme(themeMode = themeMode, dynamicColor = dynamicColor) {
                val navController = rememberNavController()
                AppNavigation(
                    navController = navController,
                    app = app,
                )
            }
        }
    }
}
