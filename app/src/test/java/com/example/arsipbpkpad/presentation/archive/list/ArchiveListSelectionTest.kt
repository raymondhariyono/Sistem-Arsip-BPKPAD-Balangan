package com.example.arsipbpkpad.presentation.archive.list

import androidx.lifecycle.SavedStateHandle
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.usecase.DeleteArchiveUseCase
import com.example.arsipbpkpad.domain.usecase.ExportArchivesUseCase
import com.example.arsipbpkpad.domain.usecase.GetArchivedYearsUseCase
import com.example.arsipbpkpad.domain.usecase.GetArchivesUseCase
import com.example.arsipbpkpad.domain.usecase.GetYearStatsUseCase
import com.example.arsipbpkpad.domain.usecase.ImportArchivesUseCase
import io.mockk.coEvery
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArchiveListSelectionTest {

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
    fun `long press enters selection mode and selects item`() = runTest {
        val archiveId = "123"
        viewModel.onEvent(ArchiveListUiEvent.ToggleSelectionMode(archiveId))
        
        val state = viewModel.uiState.value
        assertTrue(state.isSelectionMode)
        assertTrue(state.selectedArchiveIds.contains(archiveId))
    }

    @Test
    fun `toggle selection adds and removes item`() = runTest {
        val archiveId = "123"
        viewModel.onEvent(ArchiveListUiEvent.ToggleSelectionMode(archiveId))
        
        val archiveId2 = "456"
        viewModel.onEvent(ArchiveListUiEvent.ToggleArchiveSelection(archiveId2))
        assertTrue(viewModel.uiState.value.selectedArchiveIds.contains(archiveId2))
        
        viewModel.onEvent(ArchiveListUiEvent.ToggleArchiveSelection(archiveId2))
        assertFalse(viewModel.uiState.value.selectedArchiveIds.contains(archiveId2))
    }

    @Test
    fun `select all selects all given ids`() = runTest {
        val ids = listOf("1", "2", "3")
        viewModel.onEvent(ArchiveListUiEvent.SelectAllArchives(ids))
        
        assertEquals(ids.toSet(), viewModel.uiState.value.selectedArchiveIds)
    }

    @Test
    fun `clear selection empties selected ids`() = runTest {
        viewModel.onEvent(ArchiveListUiEvent.SelectAllArchives(listOf("1", "2")))
        viewModel.onEvent(ArchiveListUiEvent.ClearSelection)
        
        assertTrue(viewModel.uiState.value.selectedArchiveIds.isEmpty())
    }

    @Test
    fun `confirm delete calls delete use case for each selected id`() = runTest {
        val ids = listOf("1", "2")
        coEvery { deleteArchiveUseCase(any()) } returns DomainResult.Success(Unit)
        
        viewModel.onEvent(ArchiveListUiEvent.SelectAllArchives(ids))
        viewModel.onEvent(ArchiveListUiEvent.ConfirmDeleteSelected)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        coVerify(exactly = 1) { deleteArchiveUseCase("1") }
        coVerify(exactly = 1) { deleteArchiveUseCase("2") }
        
        val state = viewModel.uiState.value
        assertFalse(state.isSelectionMode)
        assertTrue(state.selectedArchiveIds.isEmpty())
        assertTrue(state.successMessage != null)
    }
}
