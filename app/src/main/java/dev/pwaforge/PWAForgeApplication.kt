package dev.pwaforge

import android.app.Application
import dev.pwaforge.core.adblock.AdBlocker
import dev.pwaforge.core.isolation.IsolationManager
import dev.pwaforge.core.pwa.FaviconFetcher
import dev.pwaforge.core.pwa.PwaAnalyzer
import dev.pwaforge.data.local.AppDatabase
import dev.pwaforge.data.repository.CategoryRepositoryImpl
import dev.pwaforge.data.repository.WebAppRepositoryImpl
import dev.pwaforge.domain.usecase.DeleteWebAppUseCase
import dev.pwaforge.domain.usecase.GetCategoriesUseCase
import dev.pwaforge.domain.usecase.GetWebAppsUseCase
import dev.pwaforge.domain.usecase.SaveCategoryUseCase
import dev.pwaforge.domain.usecase.SaveWebAppUseCase

class PWAForgeApplication : Application() {

    private val database by lazy { AppDatabase.getInstance(this) }

    val webAppRepository by lazy { WebAppRepositoryImpl(database.webAppDao()) }
    val categoryRepository by lazy { CategoryRepositoryImpl(database.categoryDao()) }

    val getWebApps by lazy { GetWebAppsUseCase(webAppRepository) }
    val saveWebApp by lazy { SaveWebAppUseCase(webAppRepository) }
    val deleteWebApp by lazy { DeleteWebAppUseCase(webAppRepository) }
    val getCategories by lazy { GetCategoriesUseCase(categoryRepository) }
    val saveCategory by lazy { SaveCategoryUseCase(categoryRepository) }

    val pwaAnalyzer by lazy { PwaAnalyzer.create() }
    val faviconFetcher by lazy { FaviconFetcher(this) }
    val adBlocker by lazy { AdBlocker() }
    val isolationManager by lazy { IsolationManager(this) }
}
