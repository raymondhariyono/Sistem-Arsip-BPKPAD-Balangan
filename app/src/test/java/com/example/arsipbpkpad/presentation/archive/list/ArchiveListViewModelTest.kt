package com.example.arsipbpkpad.presentation.archive.list

import androidx.lifecycle.SavedStateHandle
import com.example.arsipbpkpad.domain.usecase.DeleteArchiveUseCase
import com.example.arsipbpkpad.domain.usecase.ExportArchivesUseCase
import com.example.arsipbpkpad.domain.usecase.GetArchivedYearsUseCase
import com.example.arsipbpkpad.domain.usecase.GetArchivesUseCase
import com.example.arsipbpkpad.domain.usecase.GetYearStatsUseCase
import com.example.arsipbpkpad.domain.usecase.ImportArchivesUseCase
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.OutputStream

@OptIn(ExperimentalCoroutinesApi::class)
class ArchiveListViewModelTest {

    private val getArchivesUseCase = mockk<GetArchivesUseCase>(relaxed = true)
    private val getArchivedYearsUseCase = mockk<GetArchivedYearsUseCase>(relaxed = true)
    private val getYearStatsUseCase = mockk<GetYearStatsUseCase>(relaxed = true)
    private val deleteArchiveUseCase = mockk<DeleteArchiveUseCase>(relaxed = true)
    private val importArchivesUseCase = mockk<ImportArchivesUseCase>(relaxed = true)
    private val exportArchivesUseCase = mockk<ExportArchivesUseCase>(relaxed = true)
    private val savedStateHandle = SavedStateHandle()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ArchiveListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ArchiveListViewModel(
            getArchivesUseCase,
            getArchivedYearsUseCase,
            getYearStatsUseCase,
            deleteArchiveUseCase,
            importArchivesUseCase,
            exportArchivesUseCase,
            savedStateHandle
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `SCH_002 - filter by year and confirm updates state`() = runTest {
        viewModel.onEvent(ArchiveListUiEvent.OnYearToggle(2026))
        viewModel.onEvent(ArchiveListUiEvent.OnConfirmFilter)
        testDispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value
        assertEquals(setOf(2026), state.selectedYears)
        assertEquals(true, state.isFilterConfirmed)
    }

    @Test
    fun `EXP_011 - double tap export prevention`() = runTest {
        val outputStream = mockk<OutputStream>()
        viewModel.onEvent(ArchiveListUiEvent.OnYearToggle(2026))
        viewModel.onEvent(ArchiveListUiEvent.OnConfirmFilter)
        testDispatcher.scheduler.runCurrent()
        
        // Trigger twice quickly
        viewModel.onEvent(ArchiveListUiEvent.ExportExcel(outputStream))
        viewModel.onEvent(ArchiveListUiEvent.ExportExcel(outputStream))
        
        // Use advanceUntilIdle to ensure all coroutines finish
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify it was only called once because isLoading was true
        coVerify(exactly = 1) { exportArchivesUseCase(any(), any()) }
    }
}
