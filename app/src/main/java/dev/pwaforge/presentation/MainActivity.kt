package dev.pwaforge.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import dev.pwaforge.PWAForgeApplication
import dev.pwaforge.core.theme.ThemeMode
import dev.pwaforge.presentation.navigation.AppNavigation
import dev.pwaforge.presentation.theme.PWAForgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as PWAForgeApplication
        setContent {
            val themeMode by app.themeManager.themeMode.collectAsState(ThemeMode.SYSTEM)
            val dynamicColor by app.themeManager.dynamicColor.collectAsState(true)

            PWAForgeTheme(themeMode = themeMode, dynamicColor = dynamicColor) {
                val navController = rememberNavController()
                AppNavigation(
                    navController = navController,
                    app = app,
                    themeMode = themeMode,
                    dynamicColor = dynamicColor,
                )
            }
        }
    }
}
