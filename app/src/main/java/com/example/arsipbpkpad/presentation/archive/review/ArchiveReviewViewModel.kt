package com.example.arsipbpkpad.presentation.archive.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.DocStatus
import com.example.arsipbpkpad.domain.model.DocType
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import com.example.arsipbpkpad.domain.usecase.SaveArchiveUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ArchiveReviewViewModel @Inject constructor(
    private val saveArchiveUseCase: SaveArchiveUseCase,
    private val archiveRepository: ArchiveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveReviewUiState())
    val uiState: StateFlow<ArchiveReviewUiState> = _uiState.asStateFlow()

    fun onEvent(event: ArchiveReviewUiEvent) {
        when (event) {
            is ArchiveReviewUiEvent.OnDocNumberChange -> _uiState.update { it.copy(docNumber = event.value, error = null) }
            is ArchiveReviewUiEvent.OnSubjectChange -> _uiState.update { it.copy(subject = event.value) }
            is ArchiveReviewUiEvent.OnYearChange -> _uiState.update { 
                it.copy(year = event.value.filter { it.isDigit() }.take(4)) 
            }
            is ArchiveReviewUiEvent.OnWarehouseChange -> _uiState.update { it.copy(warehouse = event.value) }
            is ArchiveReviewUiEvent.OnRackChange -> _uiState.update { it.copy(rack = event.value) }
            is ArchiveReviewUiEvent.OnValidationToggle -> _uiState.update { it.copy(isValidated = event.isValidated) }
            is ArchiveReviewUiEvent.OnSaveClick -> saveArchive()
            is ArchiveReviewUiEvent.DismissSuccessDialog -> _uiState.update { 
                it.copy(showSuccessDialog = false) 
            }
        }
    }

    private fun saveArchive() {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            if (currentState.docNumber.isBlank()) {
                _uiState.update { it.copy(error = "Nomor dokumen tidak boleh kosong") }
                return@launch
            }

            if (currentState.year.length != 4) {
                _uiState.update { it.copy(error = "Tahun harus 4 digit") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            // Check for duplicate document number
            val exists = archiveRepository.checkDocumentNumberExists(currentState.docNumber)
            if (exists) {
                _uiState.update { it.copy(isLoading = false, error = "Nomor dokumen sudah ada di sistem") }
                return@launch
            }

            val archive = ArchiveDocument(
                id = UUID.randomUUID().toString(),
                type = DocType.SP2D,
                documentNumber = currentState.docNumber,
                nominal = null,
                thirdParty = currentState.subject,
                year = currentState.year.toIntOrNull() ?: 2024,
                dateIssued = null, 
                status = DocStatus.UNVERIFIED,
                idStorageLocation = null,
                metadata = null,
                createdBy = null,
                verifiedBy = null,
                createdAt = null,
                updatedAt = null
            )
            
            val result = saveArchiveUseCase(archive)
            
            when (result) {
                is ResultState.Success -> {
                    _uiState.value = ArchiveReviewUiState(showSuccessDialog = true)
                }
                is ResultState.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }
}
