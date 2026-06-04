package com.example.arsipbpkpad.presentation.archive.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.archive.usecase.GetArchivesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    init {
        observeArchives()
    }

    private fun observeArchives() {
        _searchQuery
            .debounce(300L)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                getArchivesUseCase(query)
            }
            .onEach { result ->
                _uiState.update {
                    when (result) {
                        is ResultState.Loading -> it.copy(isLoading = true)
                        is ResultState.Success -> it.copy(
                            isLoading = false,
                            archives = result.data
                        )
                        is ResultState.Error -> it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                        is ResultState.Idle -> it.copy(isLoading = false)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: ArchiveListUiEvent) {
        when (event) {
            is ArchiveListUiEvent.Refresh -> {
                // To refresh, we can just re-trigger the flow with current query
                val currentQuery = _searchQuery.value
                _searchQuery.value = currentQuery
            }
            is ArchiveListUiEvent.OnArchiveClick -> { 
                /* Handle navigation if needed via side effect or state */ 
            }
            is ArchiveListUiEvent.OnSearchQueryChange -> {
                _searchQuery.value = event.query
                _uiState.update { it.copy(searchQuery = event.query) }
            }
        }
    }
}
