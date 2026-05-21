package io.shellify.app.presentation.linkdispatcher

import android.net.Uri
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.shellify.app.core.deeplink.DeepLinkHandler
import io.shellify.app.core.security.Base64Codec
import io.shellify.app.domain.model.LockType
import io.shellify.app.domain.model.WebApp
import io.shellify.app.domain.usecase.FindAppsForUrlUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LinkDispatcherViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val findAppsForUrl = mockk<FindAppsForUrlUseCase>()
    private lateinit var viewModel: LinkDispatcherViewModel

    private fun makeApp(name: String = "TestApp", url: String = "https://example.com") = WebApp(
        id = 1L,
        name = name,
        url = url,
        lockType = LockType.NONE,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = LinkDispatcherViewModel(findAppsForUrl)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `dispatch with 1 matching app emits LaunchApp command directly without showing sheet`() = runTest {
        val app = makeApp()
        coEvery { findAppsForUrl(any()) } returns listOf(app)
        val commands = mutableListOf<LinkDispatcherCommand>()
        val job = launch { viewModel.commands.collect { commands.add(it) } }

        viewModel.dispatch("https://example.com")
        advanceUntilIdle()

        assertEquals(1, commands.size)
        val cmd = commands[0]
        assertTrue(cmd is LinkDispatcherCommand.LaunchApp)
        assertEquals(app, (cmd as LinkDispatcherCommand.LaunchApp).app)
        assertEquals("https://example.com", cmd.url)
        assertEquals(DispatchSheet.None, viewModel.uiState.value.sheet)
        job.cancel()
    }

    @Test
    fun `dispatch with 0 matches emits AddAsNew command`() = runTest {
        coEvery { findAppsForUrl(any()) } returns emptyList()
        val commands = mutableListOf<LinkDispatcherCommand>()
        val job = launch { viewModel.commands.collect { commands.add(it) } }

        viewModel.dispatch("https://nomatch.com")
        advanceUntilIdle()

        assertEquals(1, commands.size)
        val cmd = commands[0]
        assertTrue(cmd is LinkDispatcherCommand.AddAsNew)
        assertEquals("https://nomatch.com", (cmd as LinkDispatcherCommand.AddAsNew).url)
        assertEquals(DispatchSheet.None, viewModel.uiState.value.sheet)
        job.cancel()
    }

    @Test
    fun `dispatch with 2+ matches sets Chooser sheet`() = runTest {
        val app1 = makeApp("App1", "https://example.com/app1")
        val app2 = makeApp("App2", "https://example.com/app2")
        coEvery { findAppsForUrl(any()) } returns listOf(app1, app2)

        viewModel.dispatch("https://example.com/page")
        advanceUntilIdle()

        val sheet = viewModel.uiState.value.sheet
        assertTrue(sheet is DispatchSheet.Chooser)
        val chooser = sheet as DispatchSheet.Chooser
        assertEquals(listOf(app1, app2), chooser.matches)
        assertEquals("https://example.com/page", chooser.url)
    }

    @Test
    fun `onAppSelected emits LaunchApp command`() = runTest {
        val app = makeApp()
        val commands = mutableListOf<LinkDispatcherCommand>()
        val job = launch { viewModel.commands.collect { commands.add(it) } }

        viewModel.onAppSelected(app, "https://example.com")
        advanceUntilIdle()

        assertEquals(1, commands.size)
        val cmd = commands[0]
        assertTrue(cmd is LinkDispatcherCommand.LaunchApp)
        assertEquals(app, (cmd as LinkDispatcherCommand.LaunchApp).app)
        assertEquals("https://example.com", cmd.url)
        job.cancel()
    }

    @Test
    fun `onAddAsNew emits AddAsNew command`() = runTest {
        val commands = mutableListOf<LinkDispatcherCommand>()
        val job = launch { viewModel.commands.collect { commands.add(it) } }

        viewModel.onAddAsNew("https://example.com/new")
        advanceUntilIdle()

        assertEquals(1, commands.size)
        val cmd = commands[0]
        assertTrue(cmd is LinkDispatcherCommand.AddAsNew)
        assertEquals("https://example.com/new", (cmd as LinkDispatcherCommand.AddAsNew).url)
        job.cancel()
    }

    @Test
    fun `onDismiss emits FallbackToBrowser command`() = runTest {
        val commands = mutableListOf<LinkDispatcherCommand>()
        val job = launch { viewModel.commands.collect { commands.add(it) } }

        viewModel.onDismiss()
        advanceUntilIdle()

        assertEquals(1, commands.size)
        assertTrue(commands[0] is LinkDispatcherCommand.FallbackToBrowser)
        job.cancel()
    }

    @Test
    fun `extractUrlFromShareText returns url when text is plain url`() {
        assertEquals("https://example.com", extractUrlFromShareText("https://example.com"))
    }

    @Test
    fun `extractUrlFromShareText extracts url from title-prefixed share text`() {
        assertEquals("https://example.com/page", extractUrlFromShareText("Page Title\nhttps://example.com/page"))
    }

    @Test
    fun `extractUrlFromShareText extracts url from inline share text`() {
        assertEquals("https://example.com", extractUrlFromShareText("Check this out https://example.com"))
    }

    @Test
    fun `extractUrlFromShareText returns null when no url present`() {
        assertNull(extractUrlFromShareText("just some plain text"))
    }

    @Test
    fun `AddAsNew command url survives DeepLinkHandler buildCustomScheme parse round-trip`() {
        // Use java.util.Base64 URL-safe (no padding) to mirror UrlSafeBase64Codec in JVM unit tests.
        val jvmCodec = object : Base64Codec {
            override fun encode(bytes: ByteArray): String =
                java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
            override fun decode(str: String): ByteArray =
                java.util.Base64.getUrlDecoder().decode(str)
        }
        val url = "https://example.com"
        val encodedUrl = jvmCodec.encode(url.toByteArray())
        // Mock Uri mirrors the structure that DeepLinkHandler.buildCustomScheme(url, "") produces.
        val uri = mockk<Uri>().also {
            every { it.scheme } returns "shellify"
            every { it.host } returns "add"
            every { it.path } returns null
            every { it.getQueryParameter("url") } returns encodedUrl
            every { it.getQueryParameter("name") } returns ""
        }
        val parsed = DeepLinkHandler.parse(uri, jvmCodec)
        assertEquals("https://example.com", parsed?.first)
        assertEquals("", parsed?.second)
    }
}
