package com.example.arsipbpkpad.presentation.archive.add.manual

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.ArchiveMetadata
import com.example.arsipbpkpad.domain.model.DocCondition
import com.example.arsipbpkpad.domain.model.DocCopyType
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
    val docType: DocType = DocType.SP2D,
    val copyType: DocCopyType = DocCopyType.ORIGINAL,
    val copyCount: String = "1",
    val documentNumber: String = "",
    val spmDocumentNumber: String = "",
    val nominal: String = "",
    val description: String = "",
    val spjDescription: String = "",
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
    data class OnNominalChange(val value: String) : RapidInputUiEvent()
    data class OnDescriptionChange(val value: String) : RapidInputUiEvent()
    data class OnSpjDescriptionChange(val value: String) : RapidInputUiEvent()
    data class OnAutoBundleToggle(val enabled: Boolean) : RapidInputUiEvent()
    
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
}

@HiltViewModel
class RapidInputViewModel @Inject constructor(
    private val stagingRepository: StagingRepository,
    private val archiveRepository: ArchiveRepository,
    private val bulkInsertArchivesUseCase: BulkInsertArchivesUseCase,
    private val savedStateHandle: SavedStateHandle
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
                            docType = metadata.docType
                        ) }
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
            
            is RapidInputUiEvent.OnDocTypeChange -> {
                val isSp2d = event.value == DocType.SP2D
                _uiState.update { it.copy(
                    docType = event.value,
                    isAutoBundleEnabled = if (isSp2d) it.isAutoBundleEnabled else false
                ) }
            }
            is RapidInputUiEvent.OnCopyTypeChange -> {
                val newCount = if (event.value == DocCopyType.ORIGINAL) "1" else _uiState.value.copyCount
                _uiState.update { it.copy(copyType = event.value, copyCount = newCount) }
            }
            is RapidInputUiEvent.OnCopyCountChange -> {
                if (_uiState.value.copyType == DocCopyType.COPY) {
                    _uiState.update { it.copy(copyCount = event.value) }
                }
            }
            is RapidInputUiEvent.OnDocNumberChange -> _uiState.update { it.copy(documentNumber = event.value) }
            is RapidInputUiEvent.OnSpmDocNumberChange -> _uiState.update { it.copy(spmDocumentNumber = event.value) }
            is RapidInputUiEvent.OnNominalChange -> _uiState.update { it.copy(nominal = event.value) }
            is RapidInputUiEvent.OnDescriptionChange -> _uiState.update { it.copy(description = event.value) }
            is RapidInputUiEvent.OnSpjDescriptionChange -> _uiState.update { it.copy(spjDescription = event.value) }
            is RapidInputUiEvent.OnAutoBundleToggle -> _uiState.update { it.copy(isAutoBundleEnabled = event.enabled) }
            
            is RapidInputUiEvent.OnAddToBoxClick -> addToStaging(event.forceSave)
            is RapidInputUiEvent.DismissDuplicateWarning -> _uiState.update { it.copy(showDuplicateWarning = false) }
            
            is RapidInputUiEvent.OnDeleteStagedDoc -> deleteFromStaging(event.id)
            is RapidInputUiEvent.OnEditStagedDoc -> startEditing(event.doc)
            is RapidInputUiEvent.OnConfirmUpload -> executeBulkUpload(event.sessionId)
            is RapidInputUiEvent.OnConfirmAllUpload -> uploadAllBoxes()
            is RapidInputUiEvent.OnDeleteBoxSession -> deleteBoxSession(event.sessionId)
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
            
            // Validation based on Type and Bundle
            if (state.isAutoBundleEnabled && state.docType == DocType.SP2D) {
                if (state.documentNumber.isBlank()) errors["docNumber"] = "Nomor SP2D wajib diisi"
                if (state.spmDocumentNumber.isBlank()) errors["spmDocNumber"] = "Nomor SPM wajib diisi"
                if (state.description.isBlank()) errors["description"] = "Uraian wajib diisi"
                if (state.spjDescription.isBlank()) errors["spjDescription"] = "Uraian SPJ wajib diisi"
            } else {
                if (state.docType != DocType.SPJ && state.documentNumber.isBlank()) {
                    errors["docNumber"] = "Nomor dokumen wajib diisi"
                }
                if (state.description.isBlank()) errors["description"] = "Uraian wajib diisi"
            }
            
            val count = state.copyCount.toIntOrNull() ?: 0
            if (state.copyType == DocCopyType.COPY && count < 1) {
                errors["copyCount"] = "Jumlah salinan minimal 1"
            }
            
            if (errors.isNotEmpty()) {
                _uiState.update { it.copy(validationErrors = errors) }
                return@launch
            }

            // Exact duplicate check
            val exactExists = archiveRepository.checkDocumentNumberAndTypeExists(
                state.documentNumber, 
                state.copyType.name
            )
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
            val sharedNominal = state.nominal.toDoubleOrNull()
            
            if (state.isAutoBundleEnabled && state.docType == DocType.SP2D && state.editingId == null) {
                // Auto-Bundle Logic: Create SP2D, SPM, SPJ
                val bundleId = UUID.randomUUID().toString()
                val types = listOf(DocType.SPM, DocType.SP2D, DocType.SPJ)
                
                types.forEach { type ->
                    val docNum = when (type) {
                        DocType.SP2D -> state.documentNumber
                        DocType.SPM -> state.spmDocumentNumber
                        else -> null 
                    }
                    
                    val docDesc = when (type) {
                        DocType.SPJ -> state.spjDescription
                        else -> state.description
                    }
                    
                    val doc = createArchiveDocument(
                        id = UUID.randomUUID().toString(),
                        sessionId = sessionId,
                        type = type,
                        docNumber = docNum,
                        copyType = state.copyType,
                        copyCount = count,
                        nominal = sharedNominal,
                        year = docYear,
                        bundleId = bundleId,
                        description = docDesc
                    )
                    stagingRepository.insertToStaging(doc)
                }
            } else {
                // Single Entry
                val doc = createArchiveDocument(
                    id = state.editingId ?: UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    type = state.docType,
                    docNumber = if (state.docType == DocType.SPJ) null else state.documentNumber,
                    copyType = state.copyType,
                    copyCount = count,
                    nominal = sharedNominal,
                    year = docYear,
                    bundleId = null,
                    description = state.description
                )
                stagingRepository.insertToStaging(doc)
            }
            
            // Reset form
            _uiState.update { it.copy(
                documentNumber = "",
                spmDocumentNumber = "",
                nominal = "",
                description = "",
                spjDescription = "",
                isAutoBundleEnabled = false,
                editingId = null,
                validationErrors = emptyMap(),
                error = null,
                showDuplicateWarning = false
            ) }
        }
    }

    private fun createArchiveDocument(
        id: String,
        sessionId: String,
        type: DocType,
        docNumber: String?,
        copyType: DocCopyType,
        copyCount: Int,
        nominal: Double?,
        year: Int,
        bundleId: String?,
        description: String? = null
    ) = ArchiveDocument(
        id = id,
        boxSessionId = sessionId,
        type = type,
        documentNumber = docNumber,
        copyType = copyType,
        copyCount = copyCount,
        nominal = nominal,
        description = description,
        year = year,
        condition = DocCondition.GOOD,
        status = DocStatus.UNVERIFIED,
        idStorageLocation = null,
        metadata = ArchiveMetadata(
            warehouse = _uiState.value.boxContext.warehouse,
            rack = _uiState.value.boxContext.rack,
            boxNumber = _uiState.value.boxContext.box
        ),
        bundleId = bundleId,
        createdBy = null,
        verifiedBy = null,
        createdAt = null,
        updatedAt = null
    )

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
            nominal = doc.nominal?.toString() ?: "",
            description = doc.description ?: "",
            isAutoBundleEnabled = false // Auto-bundle disabled when editing single doc
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
