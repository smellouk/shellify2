package io.shellify.app.data.local.converter

import io.shellify.app.domain.model.IconSource
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class IconSourceConverterTest {

    private val converter = IconSourceConverter()

    // ── fromIconSource ────────────────────────────────────────────────────────

    @Test
    fun `fromIconSource returns null for null input`() {
        assertNull(converter.fromIconSource(null))
    }

    @Test
    fun `fromIconSource serializes Path to valid json with correct fields`() {
        val json = converter.fromIconSource(IconSource.Path("/icons/app.png"))!!
        val obj = JSONObject(json)
        assertEquals("path", obj.getString("type"))
        assertEquals("/icons/app.png", obj.getString("path"))
    }

    @Test
    fun `fromIconSource serializes SvgIcon with renderedPath to valid json`() {
        val json =
            converter.fromIconSource(IconSource.SvgIcon("github", "#24292e", "/cache/github.png"))!!
        val obj = JSONObject(json)
        assertEquals("svg", obj.getString("type"))
        assertEquals("github", obj.getString("slug"))
        assertEquals("#24292e", obj.getString("background"))
        assertEquals("/cache/github.png", obj.getString("renderedPath"))
    }

    @Test
    fun `fromIconSource serializes SvgIcon without renderedPath omits that field`() {
        val json = converter.fromIconSource(IconSource.SvgIcon("vue", "#4fc08d", null))!!
        val obj = JSONObject(json)
        assertEquals("svg", obj.getString("type"))
        assertFalse("renderedPath should be absent", obj.has("renderedPath"))
    }

    // ── toIconSource ──────────────────────────────────────────────────────────

    @Test
    fun `toIconSource returns null for null input`() {
        assertNull(converter.toIconSource(null))
    }

    @Test
    fun `toIconSource deserializes path json to Path`() {
        val source = converter.toIconSource("{\"type\":\"path\",\"path\":\"/icons/app.png\"}")
        assertEquals(IconSource.Path("/icons/app.png"), source)
    }

    @Test
    fun `toIconSource deserializes svg json to SvgIcon`() {
        val source = converter.toIconSource(
            "{\"type\":\"svg\",\"slug\":\"react\",\"background\":\"#61dafb\",\"renderedPath\":\"/cache/react.png\"}"
        )
        assertEquals(IconSource.SvgIcon("react", "#61dafb", "/cache/react.png"), source)
    }

    @Test
    fun `toIconSource returns null for invalid json`() {
        assertNull(converter.toIconSource("not-json"))
    }

    // ── round-trip ────────────────────────────────────────────────────────────

    @Test
    fun `round-trip for Path preserves all data`() {
        val original = IconSource.Path("/data/icons/42.png")
        val restored = converter.toIconSource(converter.fromIconSource(original))
        assertEquals(original, restored)
    }

    @Test
    fun `round-trip for SvgIcon preserves all data`() {
        val original = IconSource.SvgIcon("docker", "#2496ed", "/cache/docker.png")
        val restored = converter.toIconSource(converter.fromIconSource(original))
        assertEquals(original, restored)
    }
}
