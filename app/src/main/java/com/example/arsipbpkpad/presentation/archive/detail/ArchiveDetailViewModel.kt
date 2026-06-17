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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveDetailViewModel @Inject constructor(
    private val getArchiveDetailUseCase: GetArchiveDetailUseCase,
    private val deleteArchiveUseCase: com.example.arsipbpkpad.domain.usecase.DeleteArchiveUseCase,
    private val archiveRepository: com.example.arsipbpkpad.domain.repository.ArchiveRepository,
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

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun getArchiveDetail(id: String) {
        viewModelScope.launch {
            getArchiveDetailUseCase(id).flatMapLatest { result ->
                if (result is ResultState.Success) {
                    val archive = result.data
                    if (archive.bundleId != null) {
                        archiveRepository.getArchivesByBundleId(archive.bundleId).map { related ->
                            ResultState.Success(archive to related)
                        }
                    } else {
                        kotlinx.coroutines.flow.flowOf(ResultState.Success(archive to emptyList()))
                    }
                } else if (result is ResultState.Error) {
                    kotlinx.coroutines.flow.flowOf(ResultState.Error(result.message))
                } else {
                    kotlinx.coroutines.flow.flowOf(ResultState.Loading)
                }
            }.collect { result ->
                _uiState.update { state ->
                    when (result) {
                        is ResultState.Loading -> state.copy(isLoading = true)
                        is ResultState.Success -> {
                            val (archive, related) = result.data as Pair<com.example.arsipbpkpad.domain.model.ArchiveDocument, List<com.example.arsipbpkpad.domain.model.ArchiveDocument>>
                            state.copy(
                                isLoading = false,
                                archive = archive,
                                relatedBundleDocuments = related.filter { it.id != archive.id }
                            )
                        }
                        is ResultState.Error -> state.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                        else -> state
                    }
                }
            }
        }
    }
}

data class ArchiveDetailState(
    val isLoading: Boolean = false,
    val archive: com.example.arsipbpkpad.domain.model.ArchiveDocument? = null,
    val relatedBundleDocuments: List<com.example.arsipbpkpad.domain.model.ArchiveDocument> = emptyList(),
    val errorMessage: String? = null
)
