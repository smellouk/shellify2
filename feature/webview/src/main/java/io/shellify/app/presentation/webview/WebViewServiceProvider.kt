package io.shellify.app.presentation.webview

import io.shellify.app.core.adblock.AdBlocker
import io.shellify.app.core.engine.GeckoEngineManager
import io.shellify.app.core.isolation.IsolationManager
import io.shellify.app.core.security.PasswordManager
import io.shellify.app.core.theme.ThemeManager
import io.shellify.app.domain.usecase.GetWebAppByIdUseCase
import io.shellify.app.domain.usecase.SaveWebAppUseCase

interface WebViewServiceProvider {
    val isolationManager: IsolationManager
    val passwordManager: PasswordManager
    val geckoEngineManager: GeckoEngineManager
    val themeManager: ThemeManager
    val adBlocker: AdBlocker
    val getWebAppById: GetWebAppByIdUseCase
    val saveWebApp: SaveWebAppUseCase
}
