package com.example.arsipbpkpad.presentation.archive.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.usecase.GetArchiveDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveDetailViewModel @Inject constructor(
    private val getArchiveDetailUseCase: GetArchiveDetailUseCase,
    private val deleteArchiveUseCase: com.example.arsipbpkpad.domain.usecase.DeleteArchiveUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val archiveId: String = savedStateHandle["archiveId"] ?: ""

    private val _uiState = MutableStateFlow(ArchiveDetailState())
    val uiState: StateFlow<ArchiveDetailState> = _uiState.asStateFlow()

    init {
        if (archiveId.isNotEmpty()) {
            getArchiveDetail(archiveId)
        }
    }

    fun deleteArchive(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = deleteArchiveUseCase(archiveId)
            if (result is ResultState.Success) {
                onSuccess()
            } else if (result is ResultState.Error) {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    private fun getArchiveDetail(id: String) {
        viewModelScope.launch {
            getArchiveDetailUseCase(id).collect { result ->
                _uiState.update {
                    when (result) {
                        is ResultState.Loading -> it.copy(isLoading = true)
                        is ResultState.Success -> it.copy(
                            isLoading = false,
                            archive = result.data
                        )
                        is ResultState.Error -> it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                        else -> it
                    }
                }
            }
        }
    }
}

data class ArchiveDetailState(
    val isLoading: Boolean = false,
    val archive: com.example.arsipbpkpad.domain.model.ArchiveDocument? = null,
    val errorMessage: String? = null
)
