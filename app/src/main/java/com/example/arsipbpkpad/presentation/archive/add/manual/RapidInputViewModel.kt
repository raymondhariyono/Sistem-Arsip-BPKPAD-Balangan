package com.example.arsipbpkpad.presentation.archive.add.manual

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.ArchiveMetadata
import com.example.arsipbpkpad.domain.model.DocCondition
import com.example.arsipbpkpad.domain.model.DocCopyType
import com.example.arsipbpkpad.domain.model.DocStatus
import com.example.arsipbpkpad.domain.model.DocType
import com.example.arsipbpkpad.domain.model.ParsedMetadata
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
    val docType: DocType = DocType.SP2D,
    val copyType: DocCopyType = DocCopyType.ORIGINAL,
    val copyCount: String = "1",
    val documentNumber: String = "",
    val spmDocumentNumber: String = "",
    val subject: String = "",
    val spjDescription: String = "",
    val nominal: String = "",
    val isAutoBundleEnabled: Boolean = false,
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
    data class OnDocTypeChange(val value: DocType) : RapidInputUiEvent()
    data class OnCopyTypeChange(val value: DocCopyType) : RapidInputUiEvent()
    data class OnCopyCountChange(val value: String) : RapidInputUiEvent()
    data class OnDocNumberChange(val value: String) : RapidInputUiEvent()
    data class OnSpmDocNumberChange(val value: String) : RapidInputUiEvent()
    data class OnSubjectChange(val value: String) : RapidInputUiEvent()
    data class OnSpjDescriptionChange(val value: String) : RapidInputUiEvent()
    data class OnNominalChange(val value: String) : RapidInputUiEvent()
    data class OnAutoBundleToggle(val enabled: Boolean) : RapidInputUiEvent()
    data class OnAddToBoxClick(val forceSave: Boolean = false) : RapidInputUiEvent()
    data class OnOcrResultReceived(val metadata: ParsedMetadata) : RapidInputUiEvent()
    data object DismissDuplicateWarning : RapidInputUiEvent()

    // Staging Actions
    data class OnDeleteStagedDoc(val id: String) : RapidInputUiEvent()
    data class OnEditStagedDoc(val doc: ArchiveDocument) : RapidInputUiEvent()
    data object CancelEditing : RapidInputUiEvent()
    
    // Bulk Actions
    data object ResetState : RapidInputUiEvent()
    data object TriggerSync : RapidInputUiEvent()
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

    init {
        Log.e("RapidInputVM", "ViewModel created! Instance: ${this.hashCode()}")
        observeAllStaging()
        
        // Handle initial session if passed
        val boxSessionId: String? = savedStateHandle["sessionId"]
        boxSessionId?.let { sessionId ->
            onEvent(RapidInputUiEvent.SetCurrentSession(sessionId))
        }
    }

    private fun handleOcrResult(metadata: ParsedMetadata) {
        Log.e("RapidInputVM", "EVENT: handleOcrResult called with $metadata")
        _uiState.update { state ->
            state.copy(
                documentNumber = metadata.docNumber ?: state.documentNumber,
                subject = metadata.subject ?: state.subject,
                docType = if (metadata.docType != null) {
                    try { DocType.valueOf(metadata.docType) } catch (e: Exception) { state.docType }
                } else state.docType,
                nominal = metadata.nominal?.toLong()?.toString() ?: state.nominal,
                boxContext = if (state.boxContext.year.isBlank() && metadata.year != null) {
                    state.boxContext.copy(year = metadata.year.toString())
                } else {
                    state.boxContext
                }
            )
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
            
            is RapidInputUiEvent.OnDocTypeChange -> _uiState.update { it.copy(docType = event.value, isAutoBundleEnabled = false) }
            is RapidInputUiEvent.OnCopyTypeChange -> {
                val newCount = if (event.value == DocCopyType.ORIGINAL) "1" else _uiState.value.copyCount
                _uiState.update { it.copy(copyType = event.value, copyCount = newCount) }
            }
            is RapidInputUiEvent.OnCopyCountChange -> _uiState.update { it.copy(copyCount = event.value) }
            is RapidInputUiEvent.OnDocNumberChange -> _uiState.update { it.copy(documentNumber = event.value) }
            is RapidInputUiEvent.OnSpmDocNumberChange -> _uiState.update { it.copy(spmDocumentNumber = event.value) }
            is RapidInputUiEvent.OnSubjectChange -> _uiState.update { it.copy(subject = event.value) }
            is RapidInputUiEvent.OnSpjDescriptionChange -> _uiState.update { it.copy(spjDescription = event.value) }
            is RapidInputUiEvent.OnNominalChange -> _uiState.update { it.copy(nominal = event.value) }
            is RapidInputUiEvent.OnAutoBundleToggle -> _uiState.update { it.copy(isAutoBundleEnabled = event.enabled) }
            
            is RapidInputUiEvent.OnAddToBoxClick -> addToStaging(event.forceSave)
            is RapidInputUiEvent.OnOcrResultReceived -> handleOcrResult(event.metadata)
            is RapidInputUiEvent.DismissDuplicateWarning -> _uiState.update { it.copy(showDuplicateWarning = false) }
            
            is RapidInputUiEvent.OnDeleteStagedDoc -> deleteFromStaging(event.id)
            is RapidInputUiEvent.OnEditStagedDoc -> startEditing(event.doc)
            is RapidInputUiEvent.CancelEditing -> cancelEditing()
            is RapidInputUiEvent.OnConfirmUpload -> executeBulkUpload(event.sessionId)
            is RapidInputUiEvent.OnConfirmAllUpload -> uploadAllBoxes()
            is RapidInputUiEvent.OnDeleteBoxSession -> deleteBoxSession(event.sessionId)
            is RapidInputUiEvent.TriggerSync -> triggerSync()
            is RapidInputUiEvent.OnHandledNavigation -> { /* Using SharedFlow */ }
            is RapidInputUiEvent.ResetState -> _uiState.value = RapidInputUiState()
        }
    }

    private fun triggerSync() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            archiveRepository.syncPendingArchives()
            _uiState.update { it.copy(isLoading = false) }
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
            if (state.isAutoBundleEnabled) {
                if (state.spmDocumentNumber.isBlank()) errors["spmDocNumber"] = "Wajib diisi"
            }
            
            if (errors.isNotEmpty()) {
                _uiState.update { it.copy(validationErrors = errors) }
                return@launch
            }

            // Duplicate check
            val exactExists = archiveRepository.checkDocumentNumberAndTypeExists(state.documentNumber, state.copyType.name)
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
            val bundleId = if (state.isAutoBundleEnabled) UUID.randomUUID().toString() else null
            
            val documents = mutableListOf<ArchiveDocument>()
            
            // Primary document (SP2D, SPM, or SPP)
            documents.add(createBaseDocument(state, sessionId, bundleId))
            
            // Auto-bundle documents
            if (state.isAutoBundleEnabled && state.docType == DocType.SP2D) {
                // SPM
                documents.add(createBaseDocument(state, sessionId, bundleId).copy(
                    id = UUID.randomUUID().toString(),
                    type = DocType.SPM,
                    documentNumber = state.spmDocumentNumber
                ))
                // SPJ
                documents.add(createBaseDocument(state, sessionId, bundleId).copy(
                    id = UUID.randomUUID().toString(),
                    type = DocType.SPJ,
                    documentNumber = "SPJ-" + state.documentNumber,
                    description = if (state.spjDescription.isNotBlank()) state.spjDescription else "SPJ dari " + state.documentNumber
                ))
            }

            for (doc in documents) {
                stagingRepository.insertToStaging(doc)
            }
            
            _uiState.update { it.copy(
                documentNumber = "",
                spmDocumentNumber = "",
                subject = "",
                spjDescription = "",
                nominal = "",
                isAutoBundleEnabled = false,
                editingId = null,
                validationErrors = emptyMap(),
                error = null,
                showDuplicateWarning = false
            ) }
        }
    }

    private fun createBaseDocument(state: RapidInputUiState, sessionId: String, bundleId: String?): ArchiveDocument {
        return ArchiveDocument(
            id = state.editingId ?: UUID.randomUUID().toString(),
            boxSessionId = sessionId,
            type = state.docType,
            documentNumber = state.documentNumber,
            copyType = state.copyType,
            copyCount = state.copyCount.toIntOrNull() ?: 1,
            description = state.subject,
            nominal = state.nominal.toDoubleOrNull(),
            year = state.boxContext.year.toIntOrNull() ?: 2026,
            condition = DocCondition.GOOD,
            status = DocStatus.UNVERIFIED,
            metadata = ArchiveMetadata(
                warehouse = state.boxContext.warehouse,
                rack = state.boxContext.rack,
                boxNumber = state.boxContext.box
            ),
            idStorageLocation = null,
            bundleId = bundleId,
            createdBy = null,
            verifiedBy = null,
            createdAt = null,
            updatedAt = null
        )
    }

    private fun deleteFromStaging(id: String) {
        viewModelScope.launch {
            stagingRepository.deleteFromStaging(id)
        }
    }

    private fun startEditing(doc: ArchiveDocument) {
        _uiState.update { it.copy(
            editingId = doc.id,
            docType = doc.type,
            copyType = doc.copyType,
            copyCount = doc.copyCount.toString(),
            documentNumber = doc.documentNumber ?: "",
            subject = doc.description ?: "",
            nominal = doc.nominal?.toString() ?: "",
            isAutoBundleEnabled = false // Disable auto-bundle on edit to avoid complexity
        ) }
    }

    private fun cancelEditing() {
        _uiState.update { it.copy(
            editingId = null,
            documentNumber = "",
            spmDocumentNumber = "",
            subject = "",
            spjDescription = "",
            nominal = "",
            isAutoBundleEnabled = false,
            validationErrors = emptyMap(),
            error = null
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
