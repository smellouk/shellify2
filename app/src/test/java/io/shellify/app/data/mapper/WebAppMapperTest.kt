package io.shellify.app.data.mapper

import io.shellify.app.data.local.entity.WebAppEntity
import io.shellify.app.domain.model.EngineType
import io.shellify.app.domain.model.IconSource
import io.shellify.app.domain.model.LockType
import io.shellify.app.domain.model.TranslateLanguage
import io.shellify.app.domain.model.UserAgentMode
import io.shellify.app.domain.model.WebApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WebAppMapperTest {

    private val isolationId = "550e8400-e29b-41d4-a716-446655440000"

    private fun baseEntity() = WebAppEntity(
        id = 1,
        name = "GitHub",
        url = "https://github.com",
        isolationId = isolationId,
    )

    private fun baseApp() = WebApp(
        id = 1,
        name = "GitHub",
        url = "https://github.com",
        isolationId = isolationId,
    )

    // ── toDomain ──────────────────────────────────────────────────────────────

    @Test
    fun `toDomain maps basic fields correctly`() {
        val entity = baseEntity()
        val domain = entity.toDomain()
        assertEquals(1L, domain.id)
        assertEquals("GitHub", domain.name)
        assertEquals("https://github.com", domain.url)
        assertEquals(isolationId, domain.isolationId)
    }

    @Test
    fun `toDomain uses iconSource when present and ignores iconPath`() {
        val source = IconSource.Path("/icons/github.png")
        val entity = baseEntity().copy(iconSource = source, iconPath = "/old/path.png")
        val domain = entity.toDomain()
        assertEquals(source, domain.iconSource)
    }

    @Test
    fun `toDomain falls back to legacy iconPath when iconSource is null`() {
        val entity = baseEntity().copy(iconSource = null, iconPath = "/legacy/icon.png")
        val domain = entity.toDomain()
        assertEquals(IconSource.Path("/legacy/icon.png"), domain.iconSource)
    }

    @Test
    fun `toDomain returns null iconSource when both are null`() {
        val entity = baseEntity().copy(iconSource = null, iconPath = null)
        val domain = entity.toDomain()
        assertNull(domain.iconSource)
    }

    @Test
    fun `toDomain parses valid uaMode enum`() {
        val entity = baseEntity().copy(uaMode = "CHROME_DESKTOP")
        assertEquals(UserAgentMode.CHROME_DESKTOP, entity.toDomain().uaMode)
    }

    @Test
    fun `toDomain defaults to CHROME_MOBILE for invalid uaMode`() {
        val entity = baseEntity().copy(uaMode = "INVALID_MODE")
        assertEquals(UserAgentMode.CHROME_MOBILE, entity.toDomain().uaMode)
    }

    @Test
    fun `toDomain parses valid engineType enum`() {
        val entity = baseEntity().copy(engineType = "GECKOVIEW")
        assertEquals(EngineType.GECKOVIEW, entity.toDomain().engineType)
    }

    @Test
    fun `toDomain defaults to SYSTEM_WEBVIEW for invalid engineType`() {
        val entity = baseEntity().copy(engineType = "UNKNOWN_ENGINE")
        assertEquals(EngineType.SYSTEM_WEBVIEW, entity.toDomain().engineType)
    }

    @Test
    fun `toDomain parses valid lockType enum`() {
        val entity = baseEntity().copy(lockType = "PASSWORD")
        assertEquals(LockType.PASSWORD, entity.toDomain().lockType)
    }

    @Test
    fun `toDomain defaults to NONE for invalid lockType`() {
        val entity = baseEntity().copy(lockType = "BAD_LOCK")
        assertEquals(LockType.NONE, entity.toDomain().lockType)
    }

    @Test
    fun `toDomain finds translateTarget by code`() {
        val entity = baseEntity().copy(translateTarget = "fr")
        assertEquals(TranslateLanguage.FRENCH, entity.toDomain().translateTarget)
    }

    @Test
    fun `toDomain defaults to ENGLISH for unknown translateTarget code`() {
        val entity = baseEntity().copy(translateTarget = "xx")
        assertEquals(TranslateLanguage.ENGLISH, entity.toDomain().translateTarget)
    }

    @Test
    fun `toDomain splits adBlockCustomRules on newlines`() {
        val entity = baseEntity().copy(adBlockCustomRules = "rule1\nrule2\nrule3")
        assertEquals(listOf("rule1", "rule2", "rule3"), entity.toDomain().adBlockCustomRules)
    }

    @Test
    fun `toDomain filters blank lines in adBlockCustomRules`() {
        val entity = baseEntity().copy(adBlockCustomRules = "rule1\n\n\nrule2")
        assertEquals(listOf("rule1", "rule2"), entity.toDomain().adBlockCustomRules)
    }

    @Test
    fun `toDomain returns empty list for empty adBlockCustomRules`() {
        val entity = baseEntity().copy(adBlockCustomRules = "")
        assertTrue(entity.toDomain().adBlockCustomRules.isEmpty())
    }

    // ── toEntity ──────────────────────────────────────────────────────────────

    @Test
    fun `toEntity maps basic fields correctly`() {
        val app = baseApp()
        val entity = app.toEntity()
        assertEquals(1L, entity.id)
        assertEquals("GitHub", entity.name)
        assertEquals("https://github.com", entity.url)
        assertEquals(isolationId, entity.isolationId)
    }

    @Test
    fun `toEntity stores uaMode as enum name string`() {
        val app = baseApp().copy(uaMode = UserAgentMode.SAFARI_MOBILE)
        assertEquals("SAFARI_MOBILE", app.toEntity().uaMode)
    }

    @Test
    fun `toEntity stores engineType as enum name string`() {
        val app = baseApp().copy(engineType = EngineType.GECKOVIEW)
        assertEquals("GECKOVIEW", app.toEntity().engineType)
    }

    @Test
    fun `toEntity stores lockType as enum name string`() {
        val app = baseApp().copy(lockType = LockType.SYSTEM)
        assertEquals("SYSTEM", app.toEntity().lockType)
    }

    @Test
    fun `toEntity stores translateTarget as language code`() {
        val app = baseApp().copy(translateTarget = TranslateLanguage.JAPANESE)
        assertEquals("ja", app.toEntity().translateTarget)
    }

    @Test
    fun `toEntity joins adBlockCustomRules with newlines`() {
        val app = baseApp().copy(adBlockCustomRules = listOf("rule1", "rule2", "rule3"))
        assertEquals("rule1\nrule2\nrule3", app.toEntity().adBlockCustomRules)
    }

    @Test
    fun `toEntity stores empty string for empty adBlockCustomRules list`() {
        val app = baseApp().copy(adBlockCustomRules = emptyList())
        assertEquals("", app.toEntity().adBlockCustomRules)
    }

    @Test
    fun `toEntity preserves iconSource`() {
        val source = IconSource.SvgIcon("react", "#61dafb", "/cache/react.png")
        val app = baseApp().copy(iconSource = source)
        assertEquals(source, app.toEntity().iconSource)
    }
}
