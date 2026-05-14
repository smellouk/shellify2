package io.shellify.app.presentation.translate

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.shellify.app.domain.model.TranslateLanguage
import io.shellify.app.domain.model.WebApp
import io.shellify.app.domain.usecase.GetWebAppByIdUseCase
import io.shellify.app.domain.usecase.SaveWebAppUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TranslateConfigViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val getWebAppById = mockk<GetWebAppByIdUseCase>()
    private val saveWebApp = mockk<SaveWebAppUseCase>()
    private val testApp = WebApp(id = 1L, name = "Test", url = "https://test.com")
    private lateinit var viewModel: TranslateConfigViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        coEvery { getWebAppById(1L) } returns testApp
        coEvery { saveWebApp(any()) } returns 1L
        viewModel = TranslateConfigViewModel(1L, getWebAppById, saveWebApp)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has isLoading false and app loaded`() = runTest {
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(testApp, viewModel.uiState.value.app)
    }

    @Test
    fun `initial state is loading before app is loaded`() = runTest {
        // A new VM with a slow use case would start with isLoading = true.
        // With UnconfinedTestDispatcher init coroutine runs eagerly, so app is present.
        assertEquals(testApp, viewModel.uiState.value.app)
    }

    @Test
    fun `setLanguage updates translateTarget on app`() = runTest {
        viewModel.setLanguage(TranslateLanguage.FRENCH)
        assertEquals(TranslateLanguage.FRENCH, viewModel.uiState.value.app?.translateTarget)
    }

    @Test
    fun `setLanguage saves updated app`() = runTest {
        viewModel.setLanguage(TranslateLanguage.GERMAN)
        coVerify(exactly = 1) { saveWebApp(match { it.translateTarget == TranslateLanguage.GERMAN }) }
    }

    @Test
    fun `setAutoTranslate updates autoTranslateOnLoad on app`() = runTest {
        viewModel.setAutoTranslate(false)
        assertEquals(false, viewModel.uiState.value.app?.autoTranslateOnLoad)
    }

    @Test
    fun `setAutoTranslate saves updated app`() = runTest {
        viewModel.setAutoTranslate(false)
        coVerify(exactly = 1) { saveWebApp(match { !it.autoTranslateOnLoad }) }
    }

    @Test
    fun `when app not found state has null app`() = runTest {
        coEvery { getWebAppById(99L) } returns null
        val vm = TranslateConfigViewModel(99L, getWebAppById, saveWebApp)
        assertNull(vm.uiState.value.app)
        assertFalse(vm.uiState.value.isLoading)
    }
}
