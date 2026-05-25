package com.example.arsipbpkpad.presentation.archive.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.archive.model.ArchiveDocument
import com.example.arsipbpkpad.domain.archive.usecase.SaveArchiveUseCase
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
    private val saveArchiveUseCase: SaveArchiveUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveReviewUiState())
    val uiState: StateFlow<ArchiveReviewUiState> = _uiState.asStateFlow()

    private val _saveResult = MutableStateFlow<ResultState<Unit>>(ResultState.Idle)
    val saveResult: StateFlow<ResultState<Unit>> = _saveResult.asStateFlow()

    fun onEvent(event: ArchiveReviewUiEvent) {
        when (event) {
            is ArchiveReviewUiEvent.OnDocNumberChange -> _uiState.update { it.copy(docNumber = event.value) }
            is ArchiveReviewUiEvent.OnSubjectChange -> _uiState.update { it.copy(subject = event.value) }
            is ArchiveReviewUiEvent.OnYearChange -> _uiState.update { it.copy(year = event.value) }
            is ArchiveReviewUiEvent.OnWarehouseChange -> _uiState.update { it.copy(warehouse = event.value) }
            is ArchiveReviewUiEvent.OnRackChange -> _uiState.update { it.copy(rack = event.value) }
            is ArchiveReviewUiEvent.OnValidationToggle -> _uiState.update { it.copy(isValidated = event.isValidated) }
            is ArchiveReviewUiEvent.OnSaveClick -> saveArchive()
        }
    }

    private fun saveArchive() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val archive = ArchiveDocument(
                id = UUID.randomUUID().toString(),
                title = "SP2D ${currentState.docNumber}",
                description = currentState.subject,
                date = currentState.year,
                category = "SP2D",
                imageUrl = "" // Placeholder
            )
            _saveResult.value = ResultState.Loading
            val result = saveArchiveUseCase(archive)
            _saveResult.value = result
        }
    }
}
