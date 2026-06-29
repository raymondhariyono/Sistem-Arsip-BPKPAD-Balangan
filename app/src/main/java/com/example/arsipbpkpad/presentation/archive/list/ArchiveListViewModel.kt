package com.example.arsipbpkpad.presentation.archive.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.usecase.DeleteArchiveUseCase
import com.example.arsipbpkpad.domain.usecase.GetArchivedYearsUseCase
import com.example.arsipbpkpad.domain.usecase.GetArchivesUseCase
import com.example.arsipbpkpad.domain.usecase.GetYearStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ArchiveListViewModel @Inject constructor(
    private val getArchivesUseCase: GetArchivesUseCase,
    private val getArchivedYearsUseCase: GetArchivedYearsUseCase,
    private val getYearStatsUseCase: GetYearStatsUseCase,
    private val deleteArchiveUseCase: DeleteArchiveUseCase,
    private val importArchivesUseCase: com.example.arsipbpkpad.domain.usecase.ImportArchivesUseCase,
    private val exportArchivesUseCase: com.example.arsipbpkpad.domain.usecase.ExportArchivesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveListUiState())
    val uiState: StateFlow<ArchiveListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedFilter = MutableStateFlow("Semua")
    private val _selectedYears = MutableStateFlow<Set<Int>>(emptySet())
    private val _isFilterConfirmed = MutableStateFlow(false)

    // Note: Replaced PagingData with simple Flow<List> due to Stage 1 purity rules.
    // If Paging is strictly required, a domain abstraction or repository implementation detail would be needed.
    val archivesFlow: Flow<List<ArchiveDocument>> = combine(
        _searchQuery.debounce(300L).distinctUntilChanged(),
        _selectedFilter,
        _selectedYears,
        _isFilterConfirmed
    ) { query, filter, years, confirmed ->
        FilterParams(query, filter, years.toList(), confirmed)
    }
        .flatMapLatest { params ->
            // If year selection is empty but we are on the content screen, 
            // it might mean we should load all available years or just wait for selection.
            // However, the user said "data doesn't show", so let's make it load something if confirmed.
            getArchivesUseCase(params.query, params.years)
                .map { documents ->
                    if (params.filter == "Semua") {
                        documents
                    } else {
                        documents.filter { it.type.equals(params.filter, ignoreCase = true) }
                    }
                }
        }

    init {
        val initialYear = savedStateHandle.get<String>("year")?.toIntOrNull()
        if (initialYear != null) {
            updateInitialYear(initialYear)
        }
        observeAvailableYears()
        observeYearStats()
        syncRemoteData()
    }

    private fun syncRemoteData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            importArchivesUseCase::class.java.getDeclaredField("repository").apply {
                isAccessible = true
            }.get(importArchivesUseCase).let { repo ->
                if (repo is com.example.arsipbpkpad.domain.repository.ArchiveRepository) {
                    repo.syncArchives()
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun observeYearStats() {
        viewModelScope.launch {
            getYearStatsUseCase().collect { stats ->
                _uiState.update { it.copy(yearStats = stats) }
            }
        }
    }

    fun updateInitialYear(year: Int?) {
        if (year != null) {
            _selectedYears.value = setOf(year)
            _isFilterConfirmed.value = true
            _uiState.update { 
                it.copy(selectedYears = setOf(year), isFilterConfirmed = true)
            }
        }
    }

    private fun observeAvailableYears() {
        viewModelScope.launch {
            getArchivedYearsUseCase().collect { dbYears ->
                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                val baseYears = (2015..currentYear).toSet()
                val combined = (baseYears + dbYears).sortedDescending()
                _uiState.update { it.copy(availableYears = combined) }
            }
        }
    }

    fun onEvent(event: ArchiveListUiEvent) {
        when (event) {
            is ArchiveListUiEvent.OnSearchQueryChange -> {
                _searchQuery.value = event.query
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is ArchiveListUiEvent.OnFilterChange -> {
                _selectedFilter.value = event.type
                _uiState.update { it.copy(selectedFilter = event.type) }
            }
            is ArchiveListUiEvent.OnYearToggle -> {
                val current = _selectedYears.value
                val updated = if (current.contains(event.year)) current - event.year else current + event.year
                _selectedYears.value = updated
                // Automatically confirm when a year is selected from the grid
                if (updated.isNotEmpty()) {
                    _isFilterConfirmed.value = true
                    _uiState.update { it.copy(selectedYears = updated, isFilterConfirmed = true) }
                } else {
                    _uiState.update { it.copy(selectedYears = updated) }
                }
            }
            is ArchiveListUiEvent.OnSelectAllYears -> {
                val allYears = _uiState.value.availableYears.toSet()
                val updated = if (_selectedYears.value.size == allYears.size) emptySet() else allYears
                _selectedYears.value = updated
                _uiState.update { it.copy(selectedYears = updated) }
            }
            is ArchiveListUiEvent.OnConfirmFilter -> {
                if (_selectedYears.value.isNotEmpty()) {
                    _isFilterConfirmed.value = true
                    _uiState.update { it.copy(isFilterConfirmed = true) }
                }
            }
            is ArchiveListUiEvent.OnResetFilter -> {
                _isFilterConfirmed.value = false
                _uiState.update { it.copy(isFilterConfirmed = false, selectedYears = emptySet()) }
            }
            is ArchiveListUiEvent.ImportExcel -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true) }
                    val result = importArchivesUseCase(event.inputStream)
                    when (result) {
                        is com.example.arsipbpkpad.domain.model.DomainResult.Success -> {
                            val isSynced = result.data
                            val msgKey = if (isSynced) "IMPORT_SUCCESS_SYNCED" else "IMPORT_SUCCESS_LOCAL"
                            _uiState.update { it.copy(isLoading = false, excelOperationMessage = msgKey) }
                        }
                        is com.example.arsipbpkpad.domain.model.DomainResult.Error -> {
                            _uiState.update { it.copy(isLoading = false, excelOperationMessage = "ERROR_IMPORT:${result.message}") }
                        }
                    }
                }
            }
            is ArchiveListUiEvent.ExportExcel -> {
                if (_uiState.value.isLoading) {
                    return
                }
                _uiState.update { it.copy(isLoading = true) }
                viewModelScope.launch {
                    try {
                        exportArchivesUseCase(event.outputStream, _selectedYears.value.toList())
                        _uiState.update { it.copy(isLoading = false, excelOperationMessage = "EXPORT_SUCCESS") }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(isLoading = false, excelOperationMessage = "ERROR_EXPORT:${e.message}") }
                    }
                }
            }
            is ArchiveListUiEvent.ToggleSelectionMode -> {
                _uiState.update { state ->
                    val newMode = !state.isSelectionMode
                    state.copy(
                        isSelectionMode = newMode,
                        selectedArchiveIds = if (newMode && event.archiveId != null) setOf(event.archiveId) else emptySet()
                    )
                }
            }
            is ArchiveListUiEvent.ToggleArchiveSelection -> {
                _uiState.update { state ->
                    val current = state.selectedArchiveIds
                    val updated = if (current.contains(event.archiveId)) current - event.archiveId else current + event.archiveId
                    state.copy(selectedArchiveIds = updated)
                }
            }
            is ArchiveListUiEvent.SelectAllArchives -> {
                _uiState.update { it.copy(selectedArchiveIds = event.archiveIds.toSet()) }
            }
            is ArchiveListUiEvent.ClearSelection -> {
                _uiState.update { it.copy(selectedArchiveIds = emptySet()) }
            }
            is ArchiveListUiEvent.RequestDeleteSelected -> {
                if (_uiState.value.selectedArchiveIds.isNotEmpty()) {
                    _uiState.update { it.copy(showDeleteConfirmDialog = true) }
                }
            }
            is ArchiveListUiEvent.ConfirmDeleteSelected -> {
                deleteSelectedArchives()
            }
            is ArchiveListUiEvent.DismissDeleteConfirm -> {
                _uiState.update { it.copy(showDeleteConfirmDialog = false) }
            }
            is ArchiveListUiEvent.DismissSuccess -> {
                _uiState.update { it.copy(successMessage = null) }
            }
            is ArchiveListUiEvent.DismissError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
            else -> {}
        }
    }

    private fun deleteSelectedArchives() {
        val idsToDelete = _uiState.value.selectedArchiveIds
        if (idsToDelete.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, showDeleteConfirmDialog = false) }
            
            var successCount = 0
            var failCount = 0
            
            idsToDelete.forEach { id ->
                val result = deleteArchiveUseCase(id)
                if (result is com.example.arsipbpkpad.domain.model.DomainResult.Success) {
                    successCount++
                } else {
                    failCount++
                }
            }

            _uiState.update { state ->
                state.copy(
                    isDeleting = false,
                    isSelectionMode = false,
                    selectedArchiveIds = emptySet(),
                    successMessage = if (failCount == 0) "$successCount arsip berhasil dihapus." else null,
                    errorMessage = if (failCount > 0) "Sebagian arsip gagal dihapus ($failCount gagal, $successCount berhasil)." else null
                )
            }
        }
    }

    private data class FilterParams(
        val query: String,
        val filter: String,
        val years: List<Int>,
        val confirmed: Boolean
    )
}
