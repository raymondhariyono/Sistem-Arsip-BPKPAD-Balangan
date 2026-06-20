package com.example.arsipbpkpad.presentation.archive.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import com.example.arsipbpkpad.domain.usecase.DeleteArchiveUseCase
import com.example.arsipbpkpad.domain.usecase.GetArchiveDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveDetailViewModel @Inject constructor(
    private val getArchiveDetailUseCase: GetArchiveDetailUseCase,
    private val deleteArchiveUseCase: DeleteArchiveUseCase,
    private val archiveRepository: ArchiveRepository,
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
            when (val result = deleteArchiveUseCase(archiveId)) {
                is DomainResult.Success -> onSuccess()
                is DomainResult.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun getArchiveDetail(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getArchiveDetailUseCase(id).flatMapLatest { result ->
                when (result) {
                    is DomainResult.Success -> {
                        val archive = result.data
                        if (archive.bundleId != null) {
                            archiveRepository.getArchivesByBundleId(archive.bundleId).map { related ->
                                DomainResult.Success(archive to related)
                            }
                        } else {
                            flowOf(DomainResult.Success(archive to emptyList()))
                        }
                    }
                    is DomainResult.Error -> flowOf(DomainResult.Error(result.message))
                }
            }.collect { result ->
                _uiState.update { state ->
                    when (result) {
                        is DomainResult.Success -> {
                            val (archive, related) = result.data
                            state.copy(
                                isLoading = false,
                                archive = archive,
                                relatedBundleDocuments = related.filter { it.id != archive.id }
                            )
                        }
                        is DomainResult.Error -> state.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }
}

data class ArchiveDetailState(
    val isLoading: Boolean = false,
    val archive: ArchiveDocument? = null,
    val relatedBundleDocuments: List<ArchiveDocument> = emptyList(),
    val errorMessage: String? = null
)
