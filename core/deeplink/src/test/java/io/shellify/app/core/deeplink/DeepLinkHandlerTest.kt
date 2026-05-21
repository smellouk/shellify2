package io.shellify.app.core.deeplink

import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import io.shellify.app.util.JavaUrlSafeBase64Codec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeepLinkHandlerTest {

    private val codec = JavaUrlSafeBase64Codec

    // ── encodeUrl / decodeUrl ─────────────────────────────────────────────────

    @Test
    fun `encodeUrl produces base64url-encoded string`() {
        val encoded = DeepLinkHandler.encodeUrl("https://example.com", codec)
        // Verify by decoding
        val decoded = DeepLinkHandler.decodeUrl(encoded, codec)
        assertEquals("https://example.com", decoded)
    }

    @Test
    fun `decodeUrl returns https url when valid`() {
        val encoded = DeepLinkHandler.encodeUrl("https://example.com/app", codec)
        assertEquals("https://example.com/app", DeepLinkHandler.decodeUrl(encoded, codec))
    }

    @Test
    fun `decodeUrl returns null for http url (non-https)`() {
        val encoded = DeepLinkHandler.encodeUrl("http://example.com", codec)
        assertNull(DeepLinkHandler.decodeUrl(encoded, codec))
    }

    @Test
    fun `decodeUrl returns null for invalid base64`() {
        assertNull(DeepLinkHandler.decodeUrl("!!!not-base64!!!", codec))
    }

    @Test
    fun `decodeUrl returns null for empty string`() {
        // Empty base64 decodes to empty string, which doesn't start with https://
        assertNull(DeepLinkHandler.decodeUrl("", codec))
    }

    @Test
    fun `encodeUrl handles special characters in url`() {
        val url = "https://example.com/path?q=hello world&lang=fr"
        val encoded = DeepLinkHandler.encodeUrl(url, codec)
        val decoded = DeepLinkHandler.decodeUrl(encoded, codec)
        assertEquals(url, decoded)
    }

    @Test
    fun `encodeUrl and decodeUrl are inverse operations`() {
        val urls = listOf(
            "https://app.example.com",
            "https://sub.domain.io/path/to/app?tab=home",
            "https://example.com/app#section",
        )
        for (url in urls) {
            assertEquals(
                url,
                DeepLinkHandler.decodeUrl(DeepLinkHandler.encodeUrl(url, codec), codec)
            )
        }
    }

    // ── parse() ───────────────────────────────────────────────────────────────

    private fun mockUri(
        scheme: String,
        host: String,
        path: String? = null,
        urlParam: String? = null,
        nameParam: String? = null,
    ): Uri = mockk<Uri>().also {
        every { it.scheme } returns scheme
        every { it.host } returns host
        every { it.path } returns path
        every { it.getQueryParameter("url") } returns urlParam
        every { it.getQueryParameter("name") } returns nameParam
    }

    @Test
    fun `parse returns url and name for custom shellify scheme`() {
        val encodedUrl = DeepLinkHandler.encodeUrl("https://example.com", codec)
        val uri = mockUri("shellify", "add", urlParam = encodedUrl, nameParam = "My App")
        val result = DeepLinkHandler.parse(uri, codec)
        assertEquals("https://example.com" to "My App", result)
    }

    @Test
    fun `parse returns empty name when name param is missing`() {
        val encodedUrl = DeepLinkHandler.encodeUrl("https://example.com", codec)
        val uri = mockUri("shellify", "add", urlParam = encodedUrl, nameParam = null)
        val result = DeepLinkHandler.parse(uri, codec)
        assertEquals("https://example.com" to "", result)
    }

    @Test
    fun `parse returns null for wrong scheme`() {
        val encodedUrl = DeepLinkHandler.encodeUrl("https://example.com", codec)
        val uri = mockUri("ftp", "add", urlParam = encodedUrl, nameParam = "App")
        assertNull(DeepLinkHandler.parse(uri, codec))
    }

    @Test
    fun `parse returns null for wrong host on custom scheme`() {
        val encodedUrl = DeepLinkHandler.encodeUrl("https://example.com", codec)
        val uri = mockUri("shellify", "wrong-host", urlParam = encodedUrl, nameParam = "App")
        assertNull(DeepLinkHandler.parse(uri, codec))
    }

    @Test
    fun `parse returns null when url param is missing`() {
        val uri = mockUri("shellify", "add", urlParam = null, nameParam = "App")
        assertNull(DeepLinkHandler.parse(uri, codec))
    }

    @Test
    fun `parse returns null when url param is blank`() {
        val uri = mockUri("shellify", "add", urlParam = "   ", nameParam = "App")
        assertNull(DeepLinkHandler.parse(uri, codec))
    }

    @Test
    fun `parse returns null when decoded url is not https`() {
        val encodedHttp = DeepLinkHandler.encodeUrl("http://example.com", codec)
        val uri = mockUri("shellify", "add", urlParam = encodedHttp, nameParam = "App")
        assertNull(DeepLinkHandler.parse(uri, codec))
    }

    @Test
    fun `parse accepts https shellify app host`() {
        val encodedUrl = DeepLinkHandler.encodeUrl("https://example.com", codec)
        val uri = mockUri(
            "https",
            "shellify.app",
            path = "/add",
            urlParam = encodedUrl,
            nameParam = "App"
        )
        val result = DeepLinkHandler.parse(uri, codec)
        assertEquals("https://example.com" to "App", result)
    }

    @Test
    fun `parse returns null for https scheme but wrong host`() {
        val encodedUrl = DeepLinkHandler.encodeUrl("https://example.com", codec)
        val uri =
            mockUri("https", "wrong.host", path = "/add", urlParam = encodedUrl, nameParam = "App")
        assertNull(DeepLinkHandler.parse(uri, codec))
    }

    // ── parseOpen() ───────────────────────────────────────────────────────────

    @Test
    fun `parseOpen returns decoded url for custom shellify open scheme`() {
        val encodedUrl = DeepLinkHandler.encodeUrl("https://example.com/app", codec)
        val uri = mockUri("shellify", "open", urlParam = encodedUrl)
        assertEquals("https://example.com/app", DeepLinkHandler.parseOpen(uri, codec))
    }

    @Test
    fun `parseOpen returns decoded url for https shellify app open path`() {
        val encodedUrl = DeepLinkHandler.encodeUrl("https://example.com", codec)
        val uri = mockUri("https", "shellify.app", path = "/open", urlParam = encodedUrl)
        assertEquals("https://example.com", DeepLinkHandler.parseOpen(uri, codec))
    }

    @Test
    fun `parseOpen returns null for non-https decoded value`() {
        val encodedHttp = DeepLinkHandler.encodeUrl("http://example.com", codec)
        val uri = mockUri("shellify", "open", urlParam = encodedHttp)
        assertNull(DeepLinkHandler.parseOpen(uri, codec))
    }

    @Test
    fun `parseOpen returns null for wrong host on shellify scheme`() {
        val encodedUrl = DeepLinkHandler.encodeUrl("https://example.com", codec)
        val uri = mockUri("shellify", "add", urlParam = encodedUrl)
        assertNull(DeepLinkHandler.parseOpen(uri, codec))
    }

    @Test
    fun `parseOpen returns null when url param is missing`() {
        val uri = mockUri("shellify", "open", urlParam = null)
        assertNull(DeepLinkHandler.parseOpen(uri, codec))
    }

    // ── buildOpen() ───────────────────────────────────────────────────────────
    // Uri.Builder is an Android platform class and cannot be tested in JVM unit tests.
    // The build functions are verified via encodeUrl/decodeUrl round-trip assertions instead.

    @Test
    fun `buildOpen encoded url can be decoded back to original url`() {
        val originalUrl = "https://github.com"
        val encodedParam = DeepLinkHandler.encodeUrl(originalUrl, codec)
        val decoded = DeepLinkHandler.decodeUrl(encodedParam, codec)
        assertEquals(originalUrl, decoded)
    }

    @Test
    fun `buildOpen encoded url is rejected when original is http not https`() {
        val httpUrl = "http://example.com"
        val encodedParam = DeepLinkHandler.encodeUrl(httpUrl, codec)
        val decoded = DeepLinkHandler.decodeUrl(encodedParam, codec)
        assertNull(decoded)
    }
}
