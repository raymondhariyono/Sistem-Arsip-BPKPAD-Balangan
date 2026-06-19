package com.example.arsipbpkpad.presentation.archive.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.usecase.GetArchivesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ArchiveListViewModel @Inject constructor(
    private val getArchivesUseCase: GetArchivesUseCase,
    private val getArchivedYearsUseCase: com.example.arsipbpkpad.domain.usecase.GetArchivedYearsUseCase,
    private val getYearStatsUseCase: com.example.arsipbpkpad.domain.usecase.GetYearStatsUseCase,
    savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveListUiState())
    val uiState: StateFlow<ArchiveListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("Semua")
    private val _selectedYears = MutableStateFlow<Set<Int>>(emptySet())
    private val _isFilterConfirmed = MutableStateFlow(false)

    val archivesPagingData: Flow<PagingData<ArchiveDocument>> = kotlinx.coroutines.flow.combine(
        _searchQuery.debounce(300L).distinctUntilChanged(),
        _selectedFilter,
        _selectedYears,
        _isFilterConfirmed
    ) { query, filter, years, confirmed ->
        FilterParams(query, filter, years.toList(), confirmed)
    }
        .flatMapLatest { params ->
            if (!params.confirmed || params.years.isEmpty()) {
                flowOf(PagingData.empty())
            } else {
                getArchivesUseCase(params.query, params.years)
                    .map { pagingData ->
                        if (params.filter == "Semua") {
                            pagingData
                        } else {
                            pagingData.filter { it.type.name.equals(params.filter, ignoreCase = true) }
                        }
                    }
                    .cachedIn(viewModelScope)
            }
        }

    init {
        val initialYear = savedStateHandle.get<String>("year")?.toIntOrNull()
        if (initialYear != null) {
            _selectedYears.value = setOf(initialYear)
            _isFilterConfirmed.value = true
            _uiState.update { 
                it.copy(
                    selectedYears = setOf(initialYear),
                    isFilterConfirmed = true
                )
            }
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
                it.copy(
                    selectedYears = setOf(year),
                    isFilterConfirmed = true
                )
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
            is ArchiveListUiEvent.Refresh -> {
                // Paging3 handles refresh via the LazyPagingItems.refresh() in UI
            }
            is ArchiveListUiEvent.OnArchiveClick -> {
                /* Handle navigation if needed */
            }
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
        }
    }

    private data class FilterParams(
        val query: String,
        val filter: String,
        val years: List<Int>,
        val confirmed: Boolean
    )
}
