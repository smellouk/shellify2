package io.shellify.app.core.deeplink

import android.net.Uri
import io.shellify.app.core.security.Base64Codec
import io.shellify.app.core.security.UrlSafeBase64Codec

object DeepLinkHandler {

    fun parse(uri: Uri, codec: Base64Codec = UrlSafeBase64Codec): Pair<String, String>? {
        val isCustom = uri.scheme == "shellify" && uri.host == "add"
        val isHttps =
            uri.scheme == "https" && uri.host == "shellify.app" && uri.path?.startsWith("/add") == true
        if (!isCustom && !isHttps) return null
        val rawUrl = uri.getQueryParameter("url")?.takeIf { it.isNotBlank() } ?: return null
        val name = uri.getQueryParameter("name") ?: ""
        val url = decodeUrl(rawUrl, codec) ?: return null
        return url to name
    }

    fun buildCustomScheme(
        url: String,
        name: String,
        codec: Base64Codec = UrlSafeBase64Codec
    ): String =
        Uri.Builder().scheme("shellify").authority("add")
            .appendQueryParameter("url", encodeUrl(url, codec))
            .appendQueryParameter("name", name)
            .build().toString()

    fun buildHttps(url: String, name: String, codec: Base64Codec = UrlSafeBase64Codec): String =
        Uri.Builder().scheme("https").authority("shellify.app").path("/add")
            .appendQueryParameter("url", encodeUrl(url, codec))
            .appendQueryParameter("name", name)
            .build().toString()

    internal fun encodeUrl(url: String, codec: Base64Codec = UrlSafeBase64Codec): String =
        codec.encode(url.toByteArray())

    /**
     * Decodes a base64url-encoded URL and returns it only if it uses https.
     * Returns null on decode failure or when the decoded value is not an https URL.
     */
    internal fun decodeUrl(raw: String, codec: Base64Codec = UrlSafeBase64Codec): String? =
        runCatching {
            val decoded = String(codec.decode(raw))
            decoded.takeIf { it.startsWith("https://") }
        }.getOrNull()
}
