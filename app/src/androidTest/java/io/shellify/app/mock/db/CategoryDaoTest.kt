package io.shellify.app.mock.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.shellify.app.data.local.AppDatabase
import io.shellify.app.data.local.dao.CategoryDao
import io.shellify.app.util.FakeData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented Room DAO tests for CategoryDao.
 *
 * Uses an in-memory database so SQLCipher is not involved.
 */
@RunWith(AndroidJUnit4::class)
class CategoryDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: CategoryDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.categoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ─── Insert ────────────────────────────────────────────────────────────────

    @Test
    fun insert_returnsNonZeroId() = runTest {
        val entity = FakeData.categoryEntity(name = "Work")
        val id = dao.insert(entity)
        assertTrue("Inserted category ID should be positive", id > 0)
    }

    @Test
    fun insert_entityAppearsInGetAll() = runTest {
        dao.insert(FakeData.categoryEntity(name = "Media", sortIndex = 0))
        val all = dao.getAll().first()
        assertEquals(1, all.size)
        assertEquals("Media", all[0].name)
    }

    @Test
    fun insert_multipleCategories_allPresentInGetAll() = runTest {
        dao.insert(FakeData.categoryEntity(name = "Cat A", sortIndex = 0))
        dao.insert(FakeData.categoryEntity(name = "Cat B", sortIndex = 1))
        dao.insert(FakeData.categoryEntity(name = "Cat C", sortIndex = 2))
        val all = dao.getAll().first()
        assertEquals(3, all.size)
    }

    // ─── Query / Ordering ──────────────────────────────────────────────────────

    @Test
    fun getAll_emptyDatabase_emitsEmptyList() = runTest {
        val all = dao.getAll().first()
        assertTrue("Expected empty list from a fresh DB", all.isEmpty())
    }

    @Test
    fun getAll_orderedBySortIndexThenName() = runTest {
        dao.insert(FakeData.categoryEntity(name = "Z First", sortIndex = 0))
        dao.insert(FakeData.categoryEntity(name = "A Second", sortIndex = 1))
        val all = dao.getAll().first()
        assertEquals("Z First", all[0].name)
        assertEquals("A Second", all[1].name)
    }

    @Test
    fun getAll_sameSortIndex_orderedAlphabetically() = runTest {
        dao.insert(FakeData.categoryEntity(name = "Zebra", sortIndex = 0))
        dao.insert(FakeData.categoryEntity(name = "Apple", sortIndex = 0))
        val all = dao.getAll().first()
        assertEquals("Apple", all[0].name)
        assertEquals("Zebra", all[1].name)
    }

    // ─── Update ────────────────────────────────────────────────────────────────

    @Test
    fun update_changesName_persistedInFlow() = runTest {
        val id = dao.insert(FakeData.categoryEntity(name = "Reading"))
        val inserted = dao.getAll().first().first { it.id == id }
        dao.update(inserted.copy(name = "Books"))
        val updated = dao.getAll().first().first { it.id == id }
        assertEquals("Books", updated.name)
    }

    @Test
    fun update_changesIcon_persistedCorrectly() = runTest {
        val id = dao.insert(FakeData.categoryEntity(name = "Social", icon = "folder"))
        val inserted = dao.getAll().first().first { it.id == id }
        dao.update(inserted.copy(icon = "language"))
        val updated = dao.getAll().first().first { it.id == id }
        assertEquals("language", updated.icon)
    }

    @Test
    fun update_changesColor_persistedCorrectly() = runTest {
        val id = dao.insert(FakeData.categoryEntity(name = "Tools", color = "#6D28D9"))
        val inserted = dao.getAll().first().first { it.id == id }
        dao.update(inserted.copy(color = "#059669"))
        val updated = dao.getAll().first().first { it.id == id }
        assertEquals("#059669", updated.color)
    }

    // ─── Delete ────────────────────────────────────────────────────────────────

    @Test
    fun delete_removesEntityFromFlow() = runTest {
        val id = dao.insert(FakeData.categoryEntity(name = "ToRemove"))
        val entity = dao.getAll().first().first { it.id == id }
        dao.delete(entity)
        val remaining = dao.getAll().first()
        assertTrue("Entity should no longer appear in list", remaining.none { it.id == id })
    }

    @Test
    fun deleteAll_clearsAllCategories() = runTest {
        dao.insert(FakeData.categoryEntity(name = "Alpha"))
        dao.insert(FakeData.categoryEntity(name = "Beta"))
        dao.insert(FakeData.categoryEntity(name = "Gamma"))
        dao.deleteAll()
        val all = dao.getAll().first()
        assertTrue("Expected empty list after deleteAll", all.isEmpty())
    }

    // ─── Flow updates ─────────────────────────────────────────────────────────

    @Test
    fun getAll_flowEmitsUpdatedListAfterInsert() = runTest {
        val initial = dao.getAll().first()
        assertTrue(initial.isEmpty())

        dao.insert(FakeData.categoryEntity(name = "New"))
        val updated = dao.getAll().first()
        assertEquals(1, updated.size)
    }

    @Test
    fun getAll_flowEmitsUpdatedListAfterDelete() = runTest {
        val id = dao.insert(FakeData.categoryEntity(name = "Transient"))
        assertEquals(1, dao.getAll().first().size)

        val entity = dao.getAll().first().first { it.id == id }
        dao.delete(entity)
        assertTrue(dao.getAll().first().isEmpty())
    }
}
