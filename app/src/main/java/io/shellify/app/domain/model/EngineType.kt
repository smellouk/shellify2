package io.shellify.app.domain.model

enum class EngineType(
    val displayName: String,
    val description: String,
    val estimatedSizeMb: Int,
) {
    SYSTEM_WEBVIEW(
        displayName = "System WebView (Chrome)",
        description = "Device's built-in Chromium engine. Zero extra size.",
        estimatedSizeMb = 0,
    ),
    GECKOVIEW(
        displayName = "GeckoView (Firefox)",
        description = "Mozilla's Gecko engine with built-in tracking protection. ~55 MB download required.",
        estimatedSizeMb = 55,
    );
}
