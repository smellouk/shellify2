package dev.pwaforge.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import dev.pwaforge.PWAForgeApplication
import dev.pwaforge.presentation.navigation.AppNavigation
import dev.pwaforge.presentation.theme.PWAForgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as PWAForgeApplication
        setContent {
            PWAForgeTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController, app = app)
            }
        }
    }
}
