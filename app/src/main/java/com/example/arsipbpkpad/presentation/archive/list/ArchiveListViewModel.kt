package com.example.arsipbpkpad.presentation.archive.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.archive.usecase.GetArchivesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveListViewModel @Inject constructor(
    private val getArchivesUseCase: GetArchivesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveListUiState())
    val uiState: StateFlow<ArchiveListUiState> = _uiState.asStateFlow()

    init {
        getArchives()
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
                        is ResultState.Idle -> it
                    }
                }
            }
        }
    }
}
