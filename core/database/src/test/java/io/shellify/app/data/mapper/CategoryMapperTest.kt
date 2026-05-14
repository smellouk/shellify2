package io.shellify.app.data.mapper

import io.shellify.app.data.local.entity.CategoryEntity
import io.shellify.app.domain.model.Category
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryMapperTest {

    // ── toDomain ──────────────────────────────────────────────────────────────

    @Test
    fun `toDomain maps all fields from entity to domain`() {
        val entity = CategoryEntity(
            id = 5,
            name = "Work",
            sortIndex = 2,
            icon = "briefcase",
            color = "#1e40af"
        )
        val domain = entity.toDomain()
        assertEquals(5L, domain.id)
        assertEquals("Work", domain.name)
        assertEquals(2, domain.sortIndex)
        assertEquals("briefcase", domain.icon)
        assertEquals("#1e40af", domain.color)
    }

    @Test
    fun `toDomain uses entity defaults when not specified`() {
        val entity = CategoryEntity(name = "Misc")
        val domain = entity.toDomain()
        assertEquals(0L, domain.id)
        assertEquals(0, domain.sortIndex)
        assertEquals("folder", domain.icon)
        assertEquals("#6D28D9", domain.color)
    }

    // ── toEntity ──────────────────────────────────────────────────────────────

    @Test
    fun `toEntity maps all fields from domain to entity`() {
        val domain =
            Category(id = 3, name = "Personal", sortIndex = 1, icon = "star", color = "#dc2626")
        val entity = domain.toEntity()
        assertEquals(3L, entity.id)
        assertEquals("Personal", entity.name)
        assertEquals(1, entity.sortIndex)
        assertEquals("star", entity.icon)
        assertEquals("#dc2626", entity.color)
    }

    // ── round-trip ────────────────────────────────────────────────────────────

    @Test
    fun `entity to domain to entity round-trip preserves all data`() {
        val original = CategoryEntity(
            id = 7,
            name = "Games",
            sortIndex = 3,
            icon = "gamepad",
            color = "#7c3aed"
        )
        val restored = original.toDomain().toEntity()
        assertEquals(original, restored)
    }

    @Test
    fun `domain to entity to domain round-trip preserves all data`() {
        val original =
            Category(id = 12, name = "Finance", sortIndex = 4, icon = "currency", color = "#059669")
        val restored = original.toEntity().toDomain()
        assertEquals(original, restored)
    }
}
