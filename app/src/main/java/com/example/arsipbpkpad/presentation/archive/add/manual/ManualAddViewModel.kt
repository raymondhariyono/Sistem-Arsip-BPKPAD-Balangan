package com.example.arsipbpkpad.presentation.archive.add.manual

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

// --- STATE & EVENTS ---
data class ManualAddUiState(
    val docType: String = "",
    val docName: String = "",
    val docNumber: String = "",
    val department: String = "",
    val year: String = "",
    val validity: String = "",
    val subject: String = "",
    val warehouse: String = "",
    val rackNo: String = "",
    val boxNo: String = ""
)

sealed class ManualAddUiEvent {
    data class OnDocTypeChange(val value: String) : ManualAddUiEvent()
    data class OnDocNameChange(val value: String) : ManualAddUiEvent()
    data class OnDocNumberChange(val value: String) : ManualAddUiEvent()
    data class OnDepartmentChange(val value: String) : ManualAddUiEvent()
    data class OnYearChange(val value: String) : ManualAddUiEvent()
    data class OnValidityChange(val value: String) : ManualAddUiEvent()
    data class OnSubjectChange(val value: String) : ManualAddUiEvent()
    data class OnWarehouseChange(val value: String) : ManualAddUiEvent()
    data class OnRackNoChange(val value: String) : ManualAddUiEvent()
    data class OnBoxNoChange(val value: String) : ManualAddUiEvent()
    data object OnSaveClick : ManualAddUiEvent()
}

@HiltViewModel
class ManualAddViewModel @Inject constructor(
    private val saveArchiveUseCase: SaveArchiveUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManualAddUiState())
    val uiState: StateFlow<ManualAddUiState> = _uiState.asStateFlow()

    private val _saveResult = MutableStateFlow<ResultState<Unit>>(ResultState.Idle)
    val saveResult: StateFlow<ResultState<Unit>> = _saveResult.asStateFlow()

    fun onEvent(event: ManualAddUiEvent) {
        when (event) {
            is ManualAddUiEvent.OnDocTypeChange -> _uiState.update { it.copy(docType = event.value) }
            is ManualAddUiEvent.OnDocNameChange -> _uiState.update { it.copy(docName = event.value) }
            is ManualAddUiEvent.OnDocNumberChange -> _uiState.update { it.copy(docNumber = event.value) }
            is ManualAddUiEvent.OnDepartmentChange -> _uiState.update { it.copy(department = event.value) }
            is ManualAddUiEvent.OnYearChange -> _uiState.update { it.copy(year = event.value) }
            is ManualAddUiEvent.OnValidityChange -> _uiState.update { it.copy(validity = event.value) }
            is ManualAddUiEvent.OnSubjectChange -> _uiState.update { it.copy(subject = event.value) }
            is ManualAddUiEvent.OnWarehouseChange -> _uiState.update { it.copy(warehouse = event.value) }
            is ManualAddUiEvent.OnRackNoChange -> _uiState.update { it.copy(rackNo = event.value) }
            is ManualAddUiEvent.OnBoxNoChange -> _uiState.update { it.copy(boxNo = event.value) }
            is ManualAddUiEvent.OnSaveClick -> saveArchive()
        }
    }

    private fun saveArchive() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val archive = ArchiveDocument(
                id = UUID.randomUUID().toString(),
                title = currentState.docName,
                description = currentState.subject,
                date = currentState.year,
                category = currentState.docType
            )
            _saveResult.value = ResultState.Loading
            val result = saveArchiveUseCase(archive)
            _saveResult.value = result
        }
    }
}
