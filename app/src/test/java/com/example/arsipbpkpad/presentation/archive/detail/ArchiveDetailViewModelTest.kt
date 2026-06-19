package com.example.arsipbpkpad.presentation.archive.detail

import androidx.lifecycle.SavedStateHandle
import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.usecase.DeleteArchiveUseCase
import com.example.arsipbpkpad.domain.usecase.GetArchiveDetailUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArchiveDetailViewModelTest {

    private val getArchiveDetailUseCase = mockk<GetArchiveDetailUseCase>(relaxed = true)
    private val deleteArchiveUseCase = mockk<DeleteArchiveUseCase>()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ArchiveDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val savedStateHandle = SavedStateHandle(mapOf("archiveId" to "123"))
        viewModel = ArchiveDetailViewModel(
            getArchiveDetailUseCase,
            deleteArchiveUseCase,
            savedStateHandle
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `MNG_002 - deleteArchive successful`() = runTest {
        coEvery { deleteArchiveUseCase("123") } returns ResultState.Success(Unit)

        var successCalled = false
        viewModel.deleteArchive {
            successCalled = true
        }
        testDispatcher.scheduler.runCurrent()

        assertTrue(successCalled)
    }

    @Test
    fun `MNG_005 - deleteArchive server error`() = runTest {
        coEvery { deleteArchiveUseCase("123") } returns ResultState.Error("Server Error")

        viewModel.deleteArchive {}
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value
        assertEquals("Server Error", state.errorMessage)
    }
}
