package dev.pwaforge.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.pwaforge.PWAForgeApplication
import dev.pwaforge.presentation.add.AddScreen
import dev.pwaforge.presentation.add.AddViewModel
import dev.pwaforge.presentation.category.CategoryScreen
import dev.pwaforge.presentation.category.CategoryViewModel
import dev.pwaforge.presentation.home.HomeScreen
import dev.pwaforge.presentation.home.HomeViewModel
import dev.pwaforge.presentation.settings.AppSettingsScreen
import dev.pwaforge.presentation.settings.AppSettingsViewModel
import dev.pwaforge.presentation.translate.TranslateConfigScreen
import dev.pwaforge.presentation.translate.TranslateConfigViewModel

@Composable
fun AppNavigation(navController: NavHostController, app: PWAForgeApplication) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = HomeViewModel(app.getWebApps, app.deleteWebApp, app.getCategories),
                onAddApp = { navController.navigate(Screen.Add.createRoute()) },
                onEditApp = { id -> navController.navigate(Screen.Add.createRoute(id)) },
                onOpenApp = { /* handled in HomeScreen via context */ },
                onOpenSettings = { id -> navController.navigate(Screen.Settings.createRoute(id)) },
                onOpenCategories = { navController.navigate(Screen.Categories.route) },
            )
        }

        composable(
            route = Screen.Add.route,
            arguments = listOf(navArgument("appId") { type = NavType.LongType; defaultValue = 0L }),
        ) { back ->
            val appId = back.arguments?.getLong("appId") ?: 0L
            AddScreen(
                viewModel = AddViewModel(appId, app.webAppRepository, app.saveWebApp,
                    app.getCategories, app.pwaAnalyzer, app.faviconFetcher),
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.Settings.route,
            arguments = listOf(navArgument("appId") { type = NavType.LongType }),
        ) { back ->
            val appId = back.arguments!!.getLong("appId")
            AppSettingsScreen(
                viewModel = AppSettingsViewModel(appId, app.webAppRepository, app.saveWebApp,
                    app.deleteWebApp, app.isolationManager),
                onBack = { navController.popBackStack() },
                onDeleted = { navController.popBackStack(Screen.Home.route, inclusive = false) },
                onOpenTranslate = { navController.navigate(Screen.TranslateConfig.createRoute(appId)) },
            )
        }

        composable(Screen.Categories.route) {
            CategoryScreen(
                viewModel = CategoryViewModel(app.getCategories, app.saveCategory, app.categoryRepository),
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.TranslateConfig.route,
            arguments = listOf(navArgument("appId") { type = NavType.LongType }),
        ) { back ->
            val appId = back.arguments!!.getLong("appId")
            TranslateConfigScreen(
                viewModel = TranslateConfigViewModel(appId, app.webAppRepository, app.saveWebApp),
                onBack = { navController.popBackStack() },
            )
        }
    }
}
