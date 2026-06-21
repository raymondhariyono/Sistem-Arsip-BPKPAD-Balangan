package com.example.arsipbpkpad.presentation.archive.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.domain.model.ArchiveDocument
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
                        documents.filter { it.type.name.equals(params.filter, ignoreCase = true) }
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
                _uiState.update { it.copy(selectedYears = updated) }
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
                            _uiState.update { it.copy(isLoading = false, excelOperationMessage = "Import Successful") }
                        }
                        is com.example.arsipbpkpad.domain.model.DomainResult.Error -> {
                            _uiState.update { it.copy(isLoading = false, excelOperationMessage = result.message) }
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
                        _uiState.update { it.copy(isLoading = false, excelOperationMessage = "Export Successful") }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(isLoading = false, excelOperationMessage = e.message) }
                    }
                }
            }
            else -> {}
        }
    }

    private data class FilterParams(
        val query: String,
        val filter: String,
        val years: List<Int>,
        val confirmed: Boolean
    )
}
