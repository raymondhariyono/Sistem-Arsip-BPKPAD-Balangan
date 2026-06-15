package com.example.arsipbpkpad.presentation.archive.add.manual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.ArchiveMetadata
import com.example.arsipbpkpad.domain.model.DocCopyStatus
import com.example.arsipbpkpad.domain.model.DocStatus
import com.example.arsipbpkpad.domain.model.DocType
import com.example.arsipbpkpad.domain.model.StagedBox
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import com.example.arsipbpkpad.domain.repository.StagingRepository
import com.example.arsipbpkpad.domain.usecase.BulkInsertArchivesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class BoxContext(
    val warehouse: String = "",
    val rack: String = "",
    val box: String = "",
    val year: String = ""
)

data class RapidInputUiState(
    val currentSessionId: String? = null,
    val boxContext: BoxContext = BoxContext(),
    val isBoxContextSet: Boolean = false,
    val stagedDocuments: List<ArchiveDocument> = emptyList(),
    val existingStagedBoxes: List<StagedBox> = emptyList(),
    // Form fields
    val docType: String = "SP2D",
    val copyStatus: String = "ORIGINAL",
    val documentNumber: String = "",
    val subject: String = "",
    val nominal: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val validationErrors: Map<String, String> = emptyMap(),
    val isUploadSuccess: Boolean = false,
    val editingId: String? = null,
    val showDuplicateWarning: Boolean = false
)

sealed class RapidInputUiEvent {
    // Session Management
    data class SetCurrentSession(val sessionId: String) : RapidInputUiEvent()
    data object CreateNewSession : RapidInputUiEvent()

    // Box Context
    data class OnWarehouseChange(val value: String) : RapidInputUiEvent()
    data class OnRackChange(val value: String) : RapidInputUiEvent()
    data class OnBoxChange(val value: String) : RapidInputUiEvent()
    data class OnYearChange(val value: String) : RapidInputUiEvent()
    data object OnConfirmBoxContext : RapidInputUiEvent()

    // Form
    data class OnDocTypeChange(val value: String) : RapidInputUiEvent()
    data class OnCopyStatusChange(val value: String) : RapidInputUiEvent()
    data class OnDocNumberChange(val value: String) : RapidInputUiEvent()
    data class OnSubjectChange(val value: String) : RapidInputUiEvent()
    data class OnNominalChange(val value: String) : RapidInputUiEvent()
    data class OnAddToBoxClick(val forceSave: Boolean = false) : RapidInputUiEvent()
    data object DismissDuplicateWarning : RapidInputUiEvent()

    // Staging Actions
    data class OnDeleteStagedDoc(val id: String) : RapidInputUiEvent()
    data class OnEditStagedDoc(val doc: ArchiveDocument) : RapidInputUiEvent()
    
    // Bulk Actions
    data object ResetState : RapidInputUiEvent()
    data class OnDeleteBoxSession(val sessionId: String) : RapidInputUiEvent()
    data class OnConfirmUpload(val sessionId: String) : RapidInputUiEvent()
    data object OnConfirmAllUpload : RapidInputUiEvent()
    data object OnHandledNavigation : RapidInputUiEvent()
}

@HiltViewModel
class RapidInputViewModel @Inject constructor(
    private val stagingRepository: StagingRepository,
    private val archiveRepository: ArchiveRepository,
    private val bulkInsertArchivesUseCase: BulkInsertArchivesUseCase,
    private val savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(RapidInputUiState())
    val uiState: StateFlow<RapidInputUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private var stagingJob: kotlinx.coroutines.Job? = null
    
    private val boxSessionId: String? = savedStateHandle["sessionId"]

    init {
        observeAllStaging()
        boxSessionId?.let { sessionId ->
            onEvent(RapidInputUiEvent.SetCurrentSession(sessionId))
        }
        observeOcrResults()
    }

    private fun observeOcrResults() {
        viewModelScope.launch {
            savedStateHandle.getStateFlow<com.example.arsipbpkpad.domain.usecase.ParsedMetadata?>("ocr_result", null)
                .collect { metadata ->
                    if (metadata != null) {
                        _uiState.update { it.copy(
                            documentNumber = metadata.docNumber ?: it.documentNumber,
                            subject = metadata.subject ?: it.subject,
                            docType = metadata.docType.name
                        ) }
                        // Clear after consumption
                        savedStateHandle["ocr_result"] = null
                    }
                }
        }
    }

    private fun observeAllStaging() {
        viewModelScope.launch {
            stagingRepository.getAllStagedBoxes().collect { boxes ->
                _uiState.update { it.copy(existingStagedBoxes = boxes) }
            }
        }
    }

    private fun observeSessionStaging(sessionId: String) {
        stagingJob?.cancel()
        stagingJob = viewModelScope.launch {
            // Fetch box metadata first
            val box = stagingRepository.getStagedBoxById(sessionId)
            if (box != null) {
                _uiState.update { it.copy(
                    boxContext = BoxContext(
                        warehouse = box.warehouse,
                        rack = box.rack,
                        box = box.box,
                        year = box.year
                    ),
                    isBoxContextSet = true
                ) }
            }

            stagingRepository.getStagingArchivesBySession(sessionId).collect { docs ->
                _uiState.update { it.copy(stagedDocuments = docs) }
            }
        }
    }

    fun onEvent(event: RapidInputUiEvent) {
        when (event) {
            is RapidInputUiEvent.SetCurrentSession -> {
                if (_uiState.value.currentSessionId != event.sessionId) {
                    _uiState.update { it.copy(currentSessionId = event.sessionId) }
                    observeSessionStaging(event.sessionId)
                }
            }
            is RapidInputUiEvent.CreateNewSession -> {
                val newId = UUID.randomUUID().toString()
                _uiState.update { it.copy(
                    currentSessionId = newId,
                    boxContext = BoxContext(),
                    stagedDocuments = emptyList(),
                    validationErrors = emptyMap(),
                    error = null
                ) }
                observeSessionStaging(newId)
            }
            is RapidInputUiEvent.OnWarehouseChange -> _uiState.update { it.copy(boxContext = it.boxContext.copy(warehouse = event.value)) }
            is RapidInputUiEvent.OnRackChange -> _uiState.update { it.copy(boxContext = it.boxContext.copy(rack = event.value)) }
            is RapidInputUiEvent.OnBoxChange -> _uiState.update { it.copy(boxContext = it.boxContext.copy(box = event.value)) }
            is RapidInputUiEvent.OnYearChange -> _uiState.update { it.copy(boxContext = it.boxContext.copy(year = event.value)) }
            is RapidInputUiEvent.OnConfirmBoxContext -> validateBoxContext()
            
            is RapidInputUiEvent.OnDocTypeChange -> _uiState.update { it.copy(docType = event.value) }
            is RapidInputUiEvent.OnCopyStatusChange -> _uiState.update { it.copy(copyStatus = event.value) }
            is RapidInputUiEvent.OnDocNumberChange -> _uiState.update { it.copy(documentNumber = event.value) }
            is RapidInputUiEvent.OnSubjectChange -> _uiState.update { it.copy(subject = event.value) }
            is RapidInputUiEvent.OnNominalChange -> _uiState.update { it.copy(nominal = event.value) }
            
            is RapidInputUiEvent.OnAddToBoxClick -> addToStaging(event.forceSave)
            is RapidInputUiEvent.DismissDuplicateWarning -> _uiState.update { it.copy(showDuplicateWarning = false) }
            
            is RapidInputUiEvent.OnDeleteStagedDoc -> deleteFromStaging(event.id)
            is RapidInputUiEvent.OnEditStagedDoc -> startEditing(event.doc)
            is RapidInputUiEvent.OnConfirmUpload -> executeBulkUpload(event.sessionId)
            is RapidInputUiEvent.OnConfirmAllUpload -> uploadAllBoxes()
            is RapidInputUiEvent.OnDeleteBoxSession -> deleteBoxSession(event.sessionId)
            is RapidInputUiEvent.OnHandledNavigation -> { /* Using SharedFlow */ }
            is RapidInputUiEvent.ResetState -> _uiState.value = RapidInputUiState()
        }
    }

    private fun deleteBoxSession(sessionId: String) {
        viewModelScope.launch {
            stagingRepository.deleteStagedBox(sessionId)
        }
    }

    private fun validateBoxContext() {
        viewModelScope.launch {
            val ctx = _uiState.value.boxContext
            val errors = mutableMapOf<String, String>()
            if (ctx.warehouse.isBlank()) errors["warehouse"] = "Wajib diisi"
            if (ctx.rack.isBlank()) errors["rack"] = "Wajib diisi"
            if (ctx.box.isBlank()) errors["box"] = "Wajib diisi"
            if (ctx.year.length != 4) errors["year"] = "Tahun tidak valid"
            
            if (errors.isEmpty()) {
                val newId = _uiState.value.currentSessionId ?: UUID.randomUUID().toString()
                
                stagingRepository.saveStagedBox(
                    StagedBox(
                        sessionId = newId,
                        warehouse = ctx.warehouse,
                        rack = ctx.rack,
                        box = ctx.box,
                        year = ctx.year
                    )
                )

                _uiState.update { it.copy(
                    currentSessionId = newId,
                    validationErrors = emptyMap()
                ) }
                observeSessionStaging(newId)
                _navigationEvent.emit(newId)
            } else {
                _uiState.update { it.copy(validationErrors = errors) }
            }
        }
    }

    private fun addToStaging(forceSave: Boolean = false) {
        viewModelScope.launch {
            val state = _uiState.value
            val sessionId = state.currentSessionId ?: return@launch
            
            val errors = mutableMapOf<String, String>()
            if (state.documentNumber.isBlank()) errors["docNumber"] = "Wajib diisi"
            if (state.subject.isBlank()) errors["subject"] = "Wajib diisi"
            
            if (errors.isNotEmpty()) {
                _uiState.update { it.copy(validationErrors = errors) }
                return@launch
            }

            val exactExists = archiveRepository.checkDocumentNumberAndStatusExists(state.documentNumber, state.copyStatus)
            if (exactExists && state.editingId == null) {
                _uiState.update { it.copy(error = "Nomor dokumen ini sudah ada dengan status yang sama.") }
                return@launch
            }

            if (!forceSave && state.editingId == null) {
                val numberExists = archiveRepository.checkDocumentNumberExists(state.documentNumber)
                if (numberExists) {
                    _uiState.update { it.copy(showDuplicateWarning = true) }
                    return@launch
                }
            }

            val docYear = state.boxContext.year.toIntOrNull() ?: 2026
            val doc = ArchiveDocument(
                id = state.editingId ?: UUID.randomUUID().toString(),
                boxSessionId = sessionId,
                type = DocType.valueOf(state.docType),
                copyStatus = DocCopyStatus.valueOf(state.copyStatus),
                documentNumber = state.documentNumber,
                nominal = state.nominal.toDoubleOrNull(),
                thirdParty = state.subject,
                year = docYear,
                dateIssued = "${docYear + 10}-12-31",
                status = DocStatus.UNVERIFIED,
                idStorageLocation = null,
                metadata = ArchiveMetadata(
                    warehouse = state.boxContext.warehouse,
                    rack = state.boxContext.rack,
                    boxNumber = state.boxContext.box
                ),
                createdBy = null,
                verifiedBy = null,
                createdAt = null,
                updatedAt = null
            )

            stagingRepository.insertToStaging(doc)
            
            _uiState.update { it.copy(
                documentNumber = "",
                subject = "",
                nominal = "",
                editingId = null,
                validationErrors = emptyMap(),
                error = null,
                showDuplicateWarning = false
            ) }
        }
    }

    private fun deleteFromStaging(id: String) {
        viewModelScope.launch {
            stagingRepository.deleteFromStaging(id)
        }
    }

    private fun startEditing(doc: ArchiveDocument) {
        _uiState.update { it.copy(
            editingId = doc.id,
            docType = doc.type.name,
            copyStatus = doc.copyStatus.name,
            documentNumber = doc.documentNumber,
            subject = doc.thirdParty ?: "",
            nominal = doc.nominal?.toString() ?: ""
        ) }
    }

    private fun executeBulkUpload(sessionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = bulkInsertArchivesUseCase(sessionId)
            
            when (result) {
                is ResultState.Success -> {
                    _uiState.update { it.copy(isLoading = false, isUploadSuccess = true) }
                }
                is ResultState.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                else -> _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun uploadAllBoxes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val boxes = _uiState.value.existingStagedBoxes
            var allSuccess = true
            var lastError: String? = null

            for (box in boxes) {
                val result = bulkInsertArchivesUseCase(box.sessionId)
                if (result !is ResultState.Success) {
                    allSuccess = false
                    lastError = (result as? ResultState.Error)?.message
                }
            }

            if (allSuccess) {
                _uiState.update { it.copy(isLoading = false, isUploadSuccess = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = lastError ?: "Gagal mengupload beberapa box") }
            }
        }
    }
}
