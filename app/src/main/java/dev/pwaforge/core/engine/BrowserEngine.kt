package dev.pwaforge.core.engine

import android.content.Context
import android.view.View
import dev.pwaforge.domain.model.EngineType
import dev.pwaforge.domain.model.WebApp

interface BrowserEngine {
    val engineType: EngineType
    fun createView(context: Context, app: WebApp, callback: BrowserEngineCallback): View
    fun loadUrl(url: String)
    fun evaluateJavascript(script: String, resultCallback: ((String?) -> Unit)? = null)
    fun canGoBack(): Boolean
    fun goBack()
    fun reload()
    fun stopLoading()
    fun getCurrentUrl(): String?
    fun getView(): View?
    fun destroy()
    fun clearCache(includeDiskFiles: Boolean = true)
}
