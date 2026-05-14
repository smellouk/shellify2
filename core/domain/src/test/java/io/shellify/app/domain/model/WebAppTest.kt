package io.shellify.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WebAppTest {

    private fun app(iconSource: IconSource? = null) =
        WebApp(name = "Test", url = "https://example.com", iconSource = iconSource)

    // ── iconPath computed property ────────────────────────────────────────────

    @Test
    fun `iconPath returns path when iconSource is Path`() {
        val app = app(IconSource.Path("/data/icons/app.png"))
        assertEquals("/data/icons/app.png", app.iconPath)
    }

    @Test
    fun `iconPath returns renderedPath when iconSource is SvgIcon with renderedPath`() {
        val app = app(IconSource.SvgIcon("github", "#24292e", "/cache/github.png"))
        assertEquals("/cache/github.png", app.iconPath)
    }

    @Test
    fun `iconPath returns null when iconSource is SvgIcon without renderedPath`() {
        val app = app(IconSource.SvgIcon("github", "#24292e", null))
        assertNull(app.iconPath)
    }

    @Test
    fun `iconPath returns null when iconSource is null`() {
        val app = app(null)
        assertNull(app.iconPath)
    }

    // ── defaults ──────────────────────────────────────────────────────────────

    @Test
    fun `default lockType is NONE`() {
        assertEquals(LockType.NONE, app().lockType)
    }

    @Test
    fun `default engineType is SYSTEM_WEBVIEW`() {
        assertEquals(EngineType.SYSTEM_WEBVIEW, app().engineType)
    }

    @Test
    fun `default uaMode is CHROME_MOBILE`() {
        assertEquals(UserAgentMode.CHROME_MOBILE, app().uaMode)
    }

    @Test
    fun `default adBlockEnabled is true`() {
        assertEquals(true, app().adBlockEnabled)
    }

    @Test
    fun `default translateTarget is ENGLISH`() {
        assertEquals(TranslateLanguage.ENGLISH, app().translateTarget)
    }

    @Test
    fun `isolationId is a valid UUID format`() {
        val uuid = app().isolationId
        val uuidRegex = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
        assert(uuidRegex.matches(uuid)) { "isolationId '$uuid' is not a valid UUID" }
    }

    // ── enum coverage ─────────────────────────────────────────────────────────

    @Test
    fun `LockType has exactly three values`() {
        assertEquals(3, LockType.entries.size)
    }

    @Test
    fun `UserAgentMode DEFAULT has null uaString`() {
        assertNull(UserAgentMode.DEFAULT.uaString)
    }

    @Test
    fun `all non-DEFAULT UserAgentModes have non-null uaString`() {
        UserAgentMode.entries.filter { it != UserAgentMode.DEFAULT }.forEach {
            assert(it.uaString != null) { "${it.name} should have a uaString" }
        }
    }

    @Test
    fun `TranslateLanguage ENGLISH has code en`() {
        assertEquals("en", TranslateLanguage.ENGLISH.code)
    }

    @Test
    fun `all TranslateLanguage entries have non-blank code and displayName`() {
        TranslateLanguage.entries.forEach { lang ->
            assert(lang.code.isNotBlank()) { "${lang.name} code is blank" }
            assert(lang.displayName.isNotBlank()) { "${lang.name} displayName is blank" }
        }
    }
}
