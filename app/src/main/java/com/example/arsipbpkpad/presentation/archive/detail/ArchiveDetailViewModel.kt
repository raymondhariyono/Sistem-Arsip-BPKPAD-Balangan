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
    private val deleteBundleUseCase: com.example.arsipbpkpad.domain.usecase.DeleteBundleUseCase,
    private val getActivityLogsForEntityUseCase: com.example.arsipbpkpad.domain.usecase.GetActivityLogsForEntityUseCase,
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

    fun onDeleteClicked() {
        val archive = _uiState.value.archive ?: return
        if (archive.bundleId != null) {
            _uiState.update { it.copy(showBundleDeleteChoiceDialog = true) }
        } else {
            _uiState.update { it.copy(showDeleteDialog = true) }
        }
    }

    fun onDeleteCurrentArchiveConfirmed(onSuccess: () -> Unit) {
        _uiState.update { it.copy(showDeleteDialog = false, showBundleDeleteChoiceDialog = false, isDeleting = true) }
        viewModelScope.launch {
            when (val result = deleteArchiveUseCase(archiveId)) {
                is DomainResult.Success -> {
                    _uiState.update { it.copy(isDeleting = false, successMessage = "Arsip berhasil dihapus.") }
                    onSuccess()
                }
                is DomainResult.Error -> {
                    _uiState.update { it.copy(isDeleting = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun onDeleteEntireBundleRequested() {
        _uiState.update { it.copy(showBundleDeleteChoiceDialog = false, showDeleteEntireBundleConfirmDialog = true) }
    }

    fun onDeleteEntireBundleConfirmed(onSuccess: () -> Unit) {
        val bundleId = _uiState.value.archive?.bundleId ?: return
        _uiState.update { it.copy(showDeleteEntireBundleConfirmDialog = false, isDeleting = true) }
        viewModelScope.launch {
            when (val result = deleteBundleUseCase(bundleId)) {
                is DomainResult.Success -> {
                    _uiState.update { it.copy(isDeleting = false, successMessage = "Seluruh bundle berhasil dihapus.") }
                    onSuccess()
                }
                is DomainResult.Error -> {
                    _uiState.update { it.copy(isDeleting = false, errorMessage = result.message) }
                }
            }
        }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(
            showDeleteDialog = false,
            showBundleDeleteChoiceDialog = false,
            showDeleteEntireBundleConfirmDialog = false
        ) }
    }

    fun dismissSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
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
                            // Also fetch activity logs for this archive
                            fetchActivityLogs(archive.id)
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

    private fun fetchActivityLogs(id: String) {
        android.util.Log.i("ArchiveDetailVM", "Requesting logs for ID: $id")
        viewModelScope.launch {
            getActivityLogsForEntityUseCase(id).collect { result ->
                when (result) {
                    is DomainResult.Success -> {
                        android.util.Log.i("ArchiveDetailVM", "Received ${result.data.size} logs")
                        _uiState.update { it.copy(
                            activityLogs = result.data,
                            activityLogsErrorMessage = null
                        ) }
                    }
                    is DomainResult.Error -> {
                        android.util.Log.e("ArchiveDetailVM", "Log error: ${result.message}")
                        _uiState.update { it.copy(
                            activityLogsErrorMessage = result.message
                        ) }
                    }
                }
            }
        }
    }
}

data class ArchiveDetailState(
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val archive: ArchiveDocument? = null,
    val relatedBundleDocuments: List<ArchiveDocument> = emptyList(),
    val activityLogs: List<com.example.arsipbpkpad.domain.model.ActivityLog> = emptyList(),
    val activityLogsErrorMessage: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showDeleteDialog: Boolean = false,
    val showBundleDeleteChoiceDialog: Boolean = false,
    val showDeleteEntireBundleConfirmDialog: Boolean = false
)
