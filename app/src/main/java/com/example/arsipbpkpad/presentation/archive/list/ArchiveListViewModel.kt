package com.example.arsipbpkpad.presentation.archive.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.archive.usecase.GetArchivesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveListViewModel @Inject constructor(
    private val getArchivesUseCase: GetArchivesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveListUiState())
    val uiState: StateFlow<ArchiveListUiState> = _uiState.asStateFlow()

    private val _allArchives = MutableStateFlow<List<com.example.arsipbpkpad.domain.archive.model.ArchiveDocument>>(emptyList())

    init {
        getArchives()
        observeSearch()
    }

    private fun observeSearch() {
        combine(_allArchives, _uiState) { archives, state ->
            val query = state.searchQuery
            if (query.isEmpty()) {
                archives
            } else {
                archives.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.id.contains(query, ignoreCase = true) ||
                            it.category.contains(query, ignoreCase = true)
                }
            }
        }.onEach { filteredArchives ->
            _uiState.update { it.copy(archives = filteredArchives) }
        }.launchIn(viewModelScope)
    }

    fun onEvent(event: ArchiveListUiEvent) {
        when (event) {
            is ArchiveListUiEvent.Refresh -> getArchives()
            is ArchiveListUiEvent.OnArchiveClick -> { 
                /* Handle navigation if needed via side effect or state */ 
            }
            is ArchiveListUiEvent.OnSearchQueryChange -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
        }
    }

    private fun getArchives() {
        viewModelScope.launch {
            getArchivesUseCase().collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is ResultState.Success -> {
                        _allArchives.value = result.data
                        _uiState.update { it.copy(isLoading = false) }
                    }
                    is ResultState.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                    is ResultState.Idle -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }
        }
    }
}
