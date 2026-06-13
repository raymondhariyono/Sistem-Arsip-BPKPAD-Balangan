package com.example.arsipbpkpad.presentation.archive.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.repository.StagingRepository
import com.example.arsipbpkpad.domain.usecase.BulkInsertArchivesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArchiveReviewUiState(
    val stagedDocuments: List<ArchiveDocument> = emptyList(),
    val warehouse: String = "",
    val rack: String = "",
    val box: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showSuccessDialog: Boolean = false,
    val validationErrors: Map<String, String> = emptyMap()
)

sealed class ArchiveReviewUiEvent {
    data class OnWarehouseChange(val value: String) : ArchiveReviewUiEvent()
    data class OnRackChange(val value: String) : ArchiveReviewUiEvent()
    data class OnBoxChange(val value: String) : ArchiveReviewUiEvent()
    data class OnDeleteStagedDoc(val id: String) : ArchiveReviewUiEvent()
    data object OnApplyClick : ArchiveReviewUiEvent()
    data object DismissSuccessDialog : ArchiveReviewUiEvent()
}

@HiltViewModel
class ArchiveReviewViewModel @Inject constructor(
    private val stagingRepository: StagingRepository,
    private val bulkInsertArchivesUseCase: BulkInsertArchivesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveReviewUiState())
    val uiState: StateFlow<ArchiveReviewUiState> = _uiState.asStateFlow()

    init {
        observeStaging()
    }

    private fun observeStaging() {
        viewModelScope.launch {
            stagingRepository.getAllStagingArchives().collect { docs ->
                _uiState.update { it.copy(stagedDocuments = docs) }
            }
        }
    }

    fun onEvent(event: ArchiveReviewUiEvent) {
        when (event) {
            is ArchiveReviewUiEvent.OnWarehouseChange -> _uiState.update { it.copy(warehouse = event.value, validationErrors = it.validationErrors - "warehouse") }
            is ArchiveReviewUiEvent.OnRackChange -> _uiState.update { it.copy(rack = event.value, validationErrors = it.validationErrors - "rack") }
            is ArchiveReviewUiEvent.OnBoxChange -> _uiState.update { it.copy(box = event.value, validationErrors = it.validationErrors - "box") }
            is ArchiveReviewUiEvent.OnDeleteStagedDoc -> {
                viewModelScope.launch {
                    stagingRepository.deleteFromStaging(event.id)
                }
            }
            is ArchiveReviewUiEvent.OnApplyClick -> applyBulkInsert()
            is ArchiveReviewUiEvent.DismissSuccessDialog -> _uiState.update { it.copy(showSuccessDialog = false) }
        }
    }

    private fun applyBulkInsert() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val errors = mutableMapOf<String, String>()
            if (currentState.warehouse.isBlank()) errors["warehouse"] = "Gudang wajib diisi"
            if (currentState.rack.isBlank()) errors["rack"] = "Rak wajib diisi"
            if (currentState.box.isBlank()) errors["box"] = "Box wajib diisi"
            
            if (errors.isNotEmpty()) {
                _uiState.update { it.copy(validationErrors = errors) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = bulkInsertArchivesUseCase(
                warehouse = currentState.warehouse,
                rack = currentState.rack,
                box = currentState.box
            )
            
            when (result) {
                is ResultState.Success -> {
                    _uiState.update { it.copy(isLoading = false, showSuccessDialog = true) }
                }
                is ResultState.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
