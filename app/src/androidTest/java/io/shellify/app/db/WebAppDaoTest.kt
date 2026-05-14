package io.shellify.app.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.shellify.app.data.local.AppDatabase
import io.shellify.app.data.local.dao.WebAppDao
import io.shellify.app.util.FakeData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented Room DAO tests for WebAppDao.
 *
 * Uses an in-memory database (no SQLCipher passphrase needed) to exercise
 * all CRUD operations and Flow-based queries in isolation.
 */
@RunWith(AndroidJUnit4::class)
class WebAppDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: WebAppDao

    @Before
    fun setUp() {
        // In-memory builder bypasses the SQLCipher SupportFactory used in production.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.webAppDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ─── Insert ────────────────────────────────────────────────────────────────

    @Test
    fun insert_returnsNonZeroId() = runTest {
        val entity = FakeData.webAppEntity(name = "YouTube", url = "https://youtube.com")
        val id = dao.insert(entity)
        assertTrue("Inserted row ID should be positive", id > 0)
    }

    @Test
    fun insert_canBeRetrievedById() = runTest {
        val entity = FakeData.webAppEntity(name = "WhatsApp", url = "https://web.whatsapp.com")
        val id = dao.insert(entity)
        val retrieved = dao.getById(id)
        assertNotNull(retrieved)
        assertEquals("WhatsApp", retrieved!!.name)
        assertEquals("https://web.whatsapp.com", retrieved.url)
    }

    @Test
    fun insert_multipleEntities_allAppearsInGetAll() = runTest {
        dao.insert(FakeData.webAppEntity(name = "App A", url = "https://a.com"))
        dao.insert(FakeData.webAppEntity(name = "App B", url = "https://b.com"))
        dao.insert(FakeData.webAppEntity(name = "App C", url = "https://c.com"))

        val all = dao.getAll().first()
        assertEquals(3, all.size)
    }

    // ─── Query ─────────────────────────────────────────────────────────────────

    @Test
    fun getById_withUnknownId_returnsNull() = runTest {
        val result = dao.getById(99999L)
        assertNull(result)
    }

    @Test
    fun getByUrl_matchesExactUrl() = runTest {
        val url = "https://spotify.com"
        dao.insert(FakeData.webAppEntity(name = "Spotify", url = url))
        val result = dao.getByUrl(url)
        assertNotNull(result)
        assertEquals("Spotify", result!!.name)
    }

    @Test
    fun getByUrl_withUnknownUrl_returnsNull() = runTest {
        val result = dao.getByUrl("https://does-not-exist.test")
        assertNull(result)
    }

    @Test
    fun getByName_matchesExactName() = runTest {
        dao.insert(FakeData.webAppEntity(name = "Notion", url = "https://notion.so"))
        val result = dao.getByName("Notion")
        assertNotNull(result)
        assertEquals("https://notion.so", result!!.url)
    }

    @Test
    fun getByName_withUnknownName_returnsNull() = runTest {
        val result = dao.getByName("NonExistentApp")
        assertNull(result)
    }

    @Test
    fun getAll_emptyDatabase_emitsEmptyList() = runTest {
        val all = dao.getAll().first()
        assertTrue("Expected empty list", all.isEmpty())
    }

    // ─── Update ────────────────────────────────────────────────────────────────

    @Test
    fun update_changesName_persistedCorrectly() = runTest {
        val id = dao.insert(FakeData.webAppEntity(name = "Old Name", url = "https://example.com"))
        val inserted = dao.getById(id)!!
        dao.update(inserted.copy(name = "New Name"))
        val updated = dao.getById(id)
        assertEquals("New Name", updated!!.name)
    }

    @Test
    fun update_changesUrl_persistedCorrectly() = runTest {
        val id = dao.insert(FakeData.webAppEntity(name = "App", url = "https://old.com"))
        val inserted = dao.getById(id)!!
        dao.update(inserted.copy(url = "https://new.com"))
        val updated = dao.getById(id)
        assertEquals("https://new.com", updated!!.url)
    }

    // ─── Delete ────────────────────────────────────────────────────────────────

    @Test
    fun delete_removesEntityFromDatabase() = runTest {
        val id = dao.insert(FakeData.webAppEntity(name = "ToDelete"))
        val entity = dao.getById(id)!!
        dao.delete(entity)
        assertNull(dao.getById(id))
    }

    @Test
    fun deleteById_removesEntityFromDatabase() = runTest {
        val id = dao.insert(FakeData.webAppEntity(name = "ToDeleteById"))
        dao.deleteById(id)
        assertNull(dao.getById(id))
    }

    @Test
    fun deleteAll_clearsAllEntities() = runTest {
        dao.insert(FakeData.webAppEntity(name = "A"))
        dao.insert(FakeData.webAppEntity(name = "B"))
        dao.insert(FakeData.webAppEntity(name = "C"))
        dao.deleteAll()
        val all = dao.getAll().first()
        assertTrue("Expected empty list after deleteAll", all.isEmpty())
    }

    // ─── Flow updates ─────────────────────────────────────────────────────────

    @Test
    fun getAll_flowEmitsUpdatedListAfterInsert() = runTest {
        val before = dao.getAll().first()
        assertTrue("Expected empty initially", before.isEmpty())

        dao.insert(FakeData.webAppEntity(name = "Dynamic"))
        val after = dao.getAll().first()
        assertEquals(1, after.size)
        assertEquals("Dynamic", after[0].name)
    }

    @Test
    fun getAll_flowEmitsUpdatedListAfterDelete() = runTest {
        val id = dao.insert(FakeData.webAppEntity(name = "Ephemeral"))
        val withOne = dao.getAll().first()
        assertEquals(1, withOne.size)

        dao.deleteById(id)
        val empty = dao.getAll().first()
        assertTrue("Expected empty after delete", empty.isEmpty())
    }
}
