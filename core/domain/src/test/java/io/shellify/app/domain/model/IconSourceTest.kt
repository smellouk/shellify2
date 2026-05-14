package io.shellify.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class IconSourceTest {

    // ── Path serialization ────────────────────────────────────────────────────

    @Test
    fun `Path toJson produces valid json with type=path and correct path`() {
        val source = IconSource.Path("/data/icons/app.png")
        val obj = org.json.JSONObject(source.toJson())
        assertEquals("path", obj.getString("type"))
        assertEquals("/data/icons/app.png", obj.getString("path"))
    }

    @Test
    fun `Path round-trip via toJson and fromJson`() {
        val original = IconSource.Path("/data/icons/app.png")
        val restored = IconSource.fromJson(original.toJson())
        assertEquals(original, restored)
    }

    // ── SvgIcon serialization ─────────────────────────────────────────────────

    @Test
    fun `SvgIcon toJson produces valid json with all fields`() {
        val source = IconSource.SvgIcon("github", "#24292e", "/cache/github.png")
        val obj = org.json.JSONObject(source.toJson())
        assertEquals("svg", obj.getString("type"))
        assertEquals("github", obj.getString("slug"))
        assertEquals("#24292e", obj.getString("background"))
        assertEquals("/cache/github.png", obj.getString("renderedPath"))
    }

    @Test
    fun `SvgIcon toJson omits renderedPath when null`() {
        val source = IconSource.SvgIcon("github", "#24292e", null)
        val obj = org.json.JSONObject(source.toJson())
        assertEquals("svg", obj.getString("type"))
        assertFalse("renderedPath should be absent", obj.has("renderedPath"))
    }

    @Test
    fun `SvgIcon round-trip with renderedPath`() {
        val original = IconSource.SvgIcon("react", "#61dafb", "/icons/react.png")
        val restored = IconSource.fromJson(original.toJson())
        assertEquals(original, restored)
    }

    @Test
    fun `SvgIcon round-trip without renderedPath`() {
        val original = IconSource.SvgIcon("vue", "#4fc08d", null)
        val restored = IconSource.fromJson(original.toJson())
        assertEquals(original, restored)
    }

    // ── fromJson ──────────────────────────────────────────────────────────────

    @Test
    fun `fromJson returns null for null input`() {
        assertNull(IconSource.fromJson(null))
    }

    @Test
    fun `fromJson returns null for empty string`() {
        assertNull(IconSource.fromJson(""))
    }

    @Test
    fun `fromJson returns null for invalid json`() {
        assertNull(IconSource.fromJson("not-json"))
    }

    @Test
    fun `fromJson returns null for unknown type`() {
        assertNull(IconSource.fromJson("{\"type\":\"unknown\",\"path\":\"/foo\"}"))
    }

    @Test
    fun `fromJson parses path type correctly`() {
        val result = IconSource.fromJson("{\"type\":\"path\",\"path\":\"/some/path.png\"}")
        assertEquals(IconSource.Path("/some/path.png"), result)
    }

    @Test
    fun `fromJson parses svg type correctly`() {
        val result = IconSource.fromJson(
            "{\"type\":\"svg\",\"slug\":\"docker\",\"background\":\"#2496ed\",\"renderedPath\":\"/icons/docker.png\"}"
        )
        assertEquals(IconSource.SvgIcon("docker", "#2496ed", "/icons/docker.png"), result)
    }

    @Test
    fun `fromJson parses svg type with blank renderedPath as null`() {
        val result = IconSource.fromJson(
            "{\"type\":\"svg\",\"slug\":\"docker\",\"background\":\"#2496ed\",\"renderedPath\":\"\"}"
        ) as? IconSource.SvgIcon
        assertNull(result?.renderedPath)
    }

    // ── fromLegacyPath ────────────────────────────────────────────────────────

    @Test
    fun `fromLegacyPath wraps non-null path in Path`() {
        val result = IconSource.fromLegacyPath("/legacy/icon.png")
        assertEquals(IconSource.Path("/legacy/icon.png"), result)
    }

    @Test
    fun `fromLegacyPath returns null for null input`() {
        assertNull(IconSource.fromLegacyPath(null))
    }
}
