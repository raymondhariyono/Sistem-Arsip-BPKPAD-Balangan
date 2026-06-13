package com.example.arsipbpkpad.presentation.archive.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.usecase.GetArchivesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ArchiveListViewModel @Inject constructor(
    private val getArchivesUseCase: GetArchivesUseCase
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
        // No longer need observeArchives() as we use archivesPagingData
    }

    fun onEvent(event: ArchiveListUiEvent) {
        when (event) {
            is ArchiveListUiEvent.Refresh -> {
                // Paging3 handles refresh via the LazyPagingItems.refresh() in UI, 
                // but we can trigger a state change here if needed.
            }
            is ArchiveListUiEvent.OnArchiveClick -> {
                /* Handle navigation if needed via side effect or state */
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
                _uiState.update { it.copy(isFilterConfirmed = false) }
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
