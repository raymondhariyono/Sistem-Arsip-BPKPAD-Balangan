package com.example.arsipbpkpad.presentation.archive.add.manual

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.R
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.ArchiveMetadata
import com.example.arsipbpkpad.domain.model.ClassificationCode
import com.example.arsipbpkpad.domain.model.DocCondition
import com.example.arsipbpkpad.domain.model.DocCopyType
import com.example.arsipbpkpad.domain.model.DocStatus
import com.example.arsipbpkpad.domain.model.DocType
import com.example.arsipbpkpad.domain.model.DomainConstants
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.model.ParsedMetadata
import com.example.arsipbpkpad.domain.model.StagedBox
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import com.example.arsipbpkpad.domain.repository.StagingRepository
import com.example.arsipbpkpad.domain.usecase.BulkInsertArchivesUseCase
import com.example.arsipbpkpad.presentation.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
    val copyCount: String = DomainConstants.DEFAULT_COPY_COUNT.toString(),
    val documentNumber: String = "",
    val spmDocumentNumber: String = "",
    val subject: String = "",
    val spjDescription: String = "",
    val nominal: String = "",
    val condition: DocCondition = DocCondition.GOOD,
    val isAutoBundleEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val validationErrors: Map<String, String> = emptyMap(),
    val isUploadSuccess: Boolean = false,
    val editingId: String? = null,
    val showDuplicateWarning: Boolean = false,
    val classificationCode: String = DomainConstants.DEFAULT_CLASSIFICATION_CODE,
    val availableCodes: List<ClassificationCode> = emptyList(),
    val searchQuery: String = "",
    val selectedQuickCategory: String? = null,
    val quickCategories: List<ClassificationCode> = emptyList(),
    // UX Messages
    val successMessage: UiText? = null,
    val warningMessage: UiText? = null,
    val isSyncingClassifications: Boolean = false
)

sealed class RapidInputUiEvent {
    data class SetCurrentSession(val sessionId: String) : RapidInputUiEvent()
    data object CreateNewSession : RapidInputUiEvent()
    data class OnWarehouseChange(val value: String) : RapidInputUiEvent()
    data class OnRackChange(val value: String) : RapidInputUiEvent()
    data class OnBoxChange(val value: String) : RapidInputUiEvent()
    data class OnYearChange(val value: String) : RapidInputUiEvent()
    data object OnConfirmBoxContext : RapidInputUiEvent()
    data class OnDocTypeChange(val value: DocType) : RapidInputUiEvent()
    data class OnCopyTypeChange(val value: DocCopyType) : RapidInputUiEvent()
    data class OnCopyCountChange(val value: String) : RapidInputUiEvent()
    data class OnDocNumberChange(val value: String) : RapidInputUiEvent()
    data class OnSpmDocNumberChange(val value: String) : RapidInputUiEvent()
    data class OnSubjectChange(val value: String) : RapidInputUiEvent()
    data class OnSpjDescriptionChange(val value: String) : RapidInputUiEvent()
    data class OnNominalChange(val value: String) : RapidInputUiEvent()
    data class OnConditionChange(val value: DocCondition) : RapidInputUiEvent()
    data class OnClassificationCodeChange(val value: String) : RapidInputUiEvent()
    data class OnSearchQueryChanged(val query: String) : RapidInputUiEvent()
    data class OnQuickCategorySelected(val categoryCode: String?) : RapidInputUiEvent()
    data class OnAutoBundleToggle(val enabled: Boolean) : RapidInputUiEvent()
    data class OnAddToBoxClick(val forceSave: Boolean = false) : RapidInputUiEvent()
    data class OnOcrResultReceived(val metadata: ParsedMetadata) : RapidInputUiEvent()
    data object DismissDuplicateWarning : RapidInputUiEvent()
    data class OnDeleteStagedDoc(val id: String) : RapidInputUiEvent()
    data class OnEditStagedDoc(val doc: ArchiveDocument) : RapidInputUiEvent()
    data object CancelEditing : RapidInputUiEvent()
    data object TriggerSync : RapidInputUiEvent()
    data class OnDeleteBoxSession(val sessionId: String) : RapidInputUiEvent()
    data class OnConfirmUpload(val sessionId: String) : RapidInputUiEvent()
    data object OnConfirmAllUpload : RapidInputUiEvent()
    data object OnSaveArchiveUpdate : RapidInputUiEvent()
    data object DismissSuccess : RapidInputUiEvent()
    data object DismissError : RapidInputUiEvent()
    data object DismissWarning : RapidInputUiEvent()
}

@HiltViewModel
class RapidInputViewModel @Inject constructor(
    private val stagingRepository: StagingRepository,
    private val archiveRepository: ArchiveRepository,
    private val bulkInsertArchivesUseCase: BulkInsertArchivesUseCase,
    private val getArchiveDetailUseCase: com.example.arsipbpkpad.domain.usecase.GetArchiveDetailUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(RapidInputUiState())
    val uiState: StateFlow<RapidInputUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        observeAllStaging()
        observeClassificationCodes()
        syncClassificationCodes()
        
        val boxSessionId: String? = savedStateHandle["sessionId"]
        val archiveId: String? = savedStateHandle["archiveId"]
        
        boxSessionId?.let { if (it.isNotEmpty()) onEvent(RapidInputUiEvent.SetCurrentSession(it)) }
        archiveId?.let { if (it.isNotEmpty()) loadArchiveForEditing(it) }
    }

    private fun syncClassificationCodes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingClassifications = true) }
            archiveRepository.syncClassificationCodes()
            _uiState.update { it.copy(isSyncingClassifications = false) }
        }
    }

    private fun observeAllStaging() {
        viewModelScope.launch {
            stagingRepository.getAllStagedBoxes().collect { boxes ->
                _uiState.update { it.copy(existingStagedBoxes = boxes) }
            }
        }
    }

    private fun observeClassificationCodes() {
        viewModelScope.launch {
            archiveRepository.observeClassificationCodes().collect { codes ->
                val quickCats = codes.filter { it.code.count { char -> char == '.' } == 2 }
                _uiState.update { it.copy(availableCodes = codes, quickCategories = quickCats) }
            }
        }
    }

    private fun observeSessionStaging(sessionId: String) {
        viewModelScope.launch {
            val box = stagingRepository.getStagedBoxById(sessionId)
            if (box != null) {
                _uiState.update { it.copy(
                    boxContext = BoxContext(box.warehouse, box.rack, box.box, box.year),
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
            is RapidInputUiEvent.SetCurrentSession -> handleSetCurrentSession(event.sessionId)
            is RapidInputUiEvent.CreateNewSession -> handleCreateNewSession()
            is RapidInputUiEvent.OnWarehouseChange -> _uiState.update { it.copy(boxContext = it.boxContext.copy(warehouse = event.value)) }
            is RapidInputUiEvent.OnRackChange -> _uiState.update { it.copy(boxContext = it.boxContext.copy(rack = event.value)) }
            is RapidInputUiEvent.OnBoxChange -> _uiState.update { it.copy(boxContext = it.boxContext.copy(box = event.value)) }
            is RapidInputUiEvent.OnYearChange -> _uiState.update { it.copy(boxContext = it.boxContext.copy(year = event.value)) }
            is RapidInputUiEvent.OnConfirmBoxContext -> validateBoxContext()
            is RapidInputUiEvent.OnDocTypeChange -> _uiState.update { it.copy(docType = event.value, isAutoBundleEnabled = false) }
            is RapidInputUiEvent.OnCopyTypeChange -> handleCopyTypeChange(event.value)
            is RapidInputUiEvent.OnCopyCountChange -> _uiState.update { it.copy(copyCount = event.value) }
            is RapidInputUiEvent.OnDocNumberChange -> _uiState.update { it.copy(documentNumber = event.value) }
            is RapidInputUiEvent.OnSpmDocNumberChange -> _uiState.update { it.copy(spmDocumentNumber = event.value) }
            is RapidInputUiEvent.OnSubjectChange -> _uiState.update { it.copy(subject = event.value) }
            is RapidInputUiEvent.OnSpjDescriptionChange -> _uiState.update { it.copy(spjDescription = event.value) }
            is RapidInputUiEvent.OnNominalChange -> _uiState.update { it.copy(nominal = event.value) }
            is RapidInputUiEvent.OnConditionChange -> _uiState.update { it.copy(condition = event.value) }
            is RapidInputUiEvent.OnClassificationCodeChange -> _uiState.update { it.copy(classificationCode = event.value) }
            is RapidInputUiEvent.OnSearchQueryChanged -> _uiState.update { it.copy(searchQuery = event.query) }
            is RapidInputUiEvent.OnQuickCategorySelected -> handleQuickCategorySelected(event.categoryCode)
            is RapidInputUiEvent.OnAutoBundleToggle -> _uiState.update { it.copy(isAutoBundleEnabled = event.enabled) }
            is RapidInputUiEvent.OnAddToBoxClick -> addToStaging(event.forceSave)
            is RapidInputUiEvent.OnOcrResultReceived -> handleOcrResult(event.metadata)
            is RapidInputUiEvent.DismissDuplicateWarning -> _uiState.update { it.copy(showDuplicateWarning = false) }
            is RapidInputUiEvent.OnDeleteStagedDoc -> viewModelScope.launch { stagingRepository.deleteFromStaging(event.id) }
            is RapidInputUiEvent.OnEditStagedDoc -> startEditing(event.doc)
            is RapidInputUiEvent.CancelEditing -> cancelEditing()
            is RapidInputUiEvent.OnConfirmUpload -> executeBulkUpload(event.sessionId)
            is RapidInputUiEvent.OnConfirmAllUpload -> uploadAllBoxes()
            is RapidInputUiEvent.OnSaveArchiveUpdate -> updateExistingArchive()
            is RapidInputUiEvent.OnDeleteBoxSession -> viewModelScope.launch { stagingRepository.deleteStagedBox(event.sessionId) }
            is RapidInputUiEvent.TriggerSync -> viewModelScope.launch { archiveRepository.syncPendingArchives() }
            is RapidInputUiEvent.DismissSuccess -> handleDismissSuccess()
            is RapidInputUiEvent.DismissError -> _uiState.update { it.copy(error = null) }
            is RapidInputUiEvent.DismissWarning -> _uiState.update { it.copy(warningMessage = null) }
        }
    }

    private fun handleSetCurrentSession(sessionId: String) {
        if (_uiState.value.currentSessionId != sessionId) {
            _uiState.update { it.copy(currentSessionId = sessionId) }
            observeSessionStaging(sessionId)
        }
    }

    private fun handleCreateNewSession() {
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

    private fun handleCopyTypeChange(value: DocCopyType) {
        val newCount = if (value == DocCopyType.ORIGINAL) DomainConstants.DEFAULT_COPY_COUNT.toString() else _uiState.value.copyCount
        _uiState.update { it.copy(copyType = value, copyCount = newCount) }
    }

    private fun handleQuickCategorySelected(categoryCode: String?) {
        _uiState.update { state -> 
            val newSelection = if (state.selectedQuickCategory == categoryCode) null else categoryCode
            state.copy(selectedQuickCategory = newSelection) 
        }
    }

    private fun handleDismissSuccess() {
        if (_uiState.value.isUploadSuccess) {
            _uiState.update { state ->
                RapidInputUiState(
                    existingStagedBoxes = state.existingStagedBoxes,
                    availableCodes = state.availableCodes,
                    quickCategories = state.quickCategories
                )
            }
        } else {
            _uiState.update { it.copy(successMessage = null) }
        }
    }

    private fun handleOcrResult(metadata: ParsedMetadata) {
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
                } else state.boxContext
            )
        }
    }

    private fun validateBoxContext() {
        viewModelScope.launch {
            val ctx = _uiState.value.boxContext
            val errors = mutableMapOf<String, String>()
            if (ctx.warehouse.isBlank()) errors["warehouse"] = "Gudang wajib diisi"
            if (ctx.rack.isBlank()) errors["rack"] = "Nomor rak wajib diisi"
            if (ctx.box.isBlank()) errors["box"] = "Nomor box wajib diisi"
            
            val yearInt = ctx.year.toIntOrNull()
            if (ctx.year.length != 4 || yearInt == null) {
                errors["year"] = "Format tahun tidak valid (4 digit)"
            } else {
                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                if (currentYear - yearInt > 10 && _uiState.value.warningMessage == null) {
                    _uiState.update { it.copy(
                        warningMessage = UiText.StringResource(R.string.val_retention_warning, yearInt)
                    ) }
                }
            }
            
            if (errors.isEmpty()) {
                val sessionId = _uiState.value.currentSessionId ?: UUID.randomUUID().toString()
                stagingRepository.saveStagedBox(StagedBox(sessionId, ctx.warehouse, ctx.rack, ctx.box, ctx.year))
                _uiState.update { it.copy(currentSessionId = sessionId, validationErrors = emptyMap()) }
                _navigationEvent.emit(sessionId)
            } else {
                _uiState.update { it.copy(validationErrors = errors) }
            }
        }
    }

    private fun addToStaging(forceSave: Boolean) {
        viewModelScope.launch {
            val state = _uiState.value
            val sessionId = state.currentSessionId ?: return@launch
            
            val errors = mutableMapOf<String, String>()
            if (state.documentNumber.isBlank()) errors["docNumber"] = "Nomor dokumen wajib diisi"
            if (state.subject.isBlank()) errors["subject"] = "Uraian dokumen wajib diisi"
            if (state.isAutoBundleEnabled && state.spmDocumentNumber.isBlank()) errors["spmDocNumber"] = "Nomor SPM wajib diisi"
            if (state.copyType == DocCopyType.COPY && (state.copyCount.toIntOrNull() ?: 0) < 1) errors["copyCount"] = "Jumlah salinan minimal 1"
            
            if (errors.isNotEmpty()) {
                _uiState.update { it.copy(validationErrors = errors) }
                return@launch
            }

            // Duplicate check
            val exactExists = archiveRepository.checkDocumentNumberAndTypeExists(state.documentNumber, state.copyType.name)
            if (exactExists && state.editingId == null) {
                _uiState.update { it.copy(error = DomainConstants.VAL_DUPLICATE_DOC) }
                return@launch
            }

            if (!forceSave && state.editingId == null) {
                if (archiveRepository.checkDocumentNumberExists(state.documentNumber)) {
                    _uiState.update { it.copy(showDuplicateWarning = true) }
                    return@launch
                }
            }

            val bundleId = if (state.isAutoBundleEnabled || state.docType == DocType.SPJ) UUID.randomUUID().toString() else null
            val documents = mutableListOf<ArchiveDocument>()
            
            documents.add(createBaseDocument(state, sessionId, bundleId))
            
            if (state.isAutoBundleEnabled && (state.docType == DocType.SP2D || state.docType == DocType.SPM)) {
                if (state.docType == DocType.SP2D) {
                    documents.add(createBaseDocument(state, sessionId, bundleId).copy(
                        id = UUID.randomUUID().toString(),
                        type = DocType.SPM,
                        documentNumber = state.spmDocumentNumber
                    ))
                }
                
                if (state.spjDescription.isNotBlank()) {
                    documents.add(createBaseDocument(state, sessionId, bundleId).copy(
                        id = UUID.randomUUID().toString(),
                        type = DocType.SPJ,
                        documentNumber = "SPJ-" + state.documentNumber,
                        description = state.spjDescription
                    ))
                }
            }

            for (doc in documents) stagingRepository.insertToStaging(doc)
            
            _uiState.update { it.copy(
                documentNumber = "", spmDocumentNumber = "", subject = "", spjDescription = "",
                nominal = "", isAutoBundleEnabled = false, editingId = null,
                validationErrors = emptyMap(), error = null, showDuplicateWarning = false
            ) }
        }
    }

    private fun createBaseDocument(state: RapidInputUiState, sessionId: String, bundleId: String?): ArchiveDocument {
        return ArchiveDocument(
            id = state.editingId ?: UUID.randomUUID().toString(),
            boxSessionId = sessionId,
            type = state.docType,
            documentNumber = state.documentNumber,
            classificationCode = state.classificationCode,
            copyType = state.copyType,
            copyCount = state.copyCount.toIntOrNull() ?: DomainConstants.DEFAULT_COPY_COUNT,
            description = state.subject,
            nominal = state.nominal.toDoubleOrNull(),
            year = state.boxContext.year.toIntOrNull() ?: DomainConstants.DEFAULT_YEAR,
            condition = state.condition,
            status = DocStatus.UNVERIFIED,
            metadata = ArchiveMetadata(warehouse = state.boxContext.warehouse, rack = state.boxContext.rack, boxNumber = state.boxContext.box),
            idStorageLocation = null,
            bundleId = bundleId
        )
    }

    private fun startEditing(doc: ArchiveDocument) {
        _uiState.update { it.copy(
            editingId = doc.id,
            docType = doc.type,
            copyType = doc.copyType,
            copyCount = doc.copyCount.toString(),
            classificationCode = doc.classificationCode,
            documentNumber = doc.documentNumber ?: "",
            subject = doc.description ?: "",
            nominal = doc.nominal?.toString() ?: "",
            condition = doc.condition,
            isAutoBundleEnabled = false
        ) }
    }

    private fun cancelEditing() {
        _uiState.update { it.copy(
            editingId = null, documentNumber = "", spmDocumentNumber = "", subject = "",
            spjDescription = "", nominal = "", condition = DocCondition.GOOD, isAutoBundleEnabled = false,
            validationErrors = emptyMap(), error = null
        ) }
    }

    private fun updateExistingArchive() {
        viewModelScope.launch {
            val state = _uiState.value
            val archiveId = state.editingId ?: return@launch
            
            _uiState.update { it.copy(isLoading = true) }
            
            // Re-fetch to get original non-form fields (like createdAt, status, etc)
            val result = getArchiveDetailUseCase(archiveId).first()
            if (result is DomainResult.Success) {
                val original = result.data
                val updated = original.copy(
                    type = state.docType,
                    documentNumber = state.documentNumber,
                    classificationCode = state.classificationCode,
                    copyType = state.copyType,
                    copyCount = state.copyCount.toIntOrNull() ?: original.copyCount,
                    description = state.subject,
                    nominal = state.nominal.toDoubleOrNull(),
                    condition = state.condition
                )
                
                val saveResult = archiveRepository.saveArchives(listOf(updated))
                if (saveResult is DomainResult.Success) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        isUploadSuccess = true,
                        successMessage = UiText.DynamicString("Data Berhasil Diperbarui")
                    ) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = (saveResult as DomainResult.Error).message) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = (result as DomainResult.Error).message) }
            }
        }
    }

    private fun loadArchiveForEditing(archiveId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getArchiveDetailUseCase(archiveId).collect { result ->
                if (result is DomainResult.Success) {
                    val doc = result.data
                    _uiState.update { it.copy(
                        isLoading = false,
                        editingId = doc.id,
                        docType = doc.type,
                        copyType = doc.copyType,
                        copyCount = doc.copyCount.toString(),
                        classificationCode = doc.classificationCode,
                        documentNumber = doc.documentNumber ?: "",
                        subject = doc.description ?: "",
                        nominal = doc.nominal?.toLong()?.toString() ?: "",
                        condition = doc.condition,
                        boxContext = BoxContext(
                            warehouse = doc.metadata?.warehouse ?: "",
                            rack = doc.metadata?.rack ?: "",
                            box = doc.metadata?.boxNumber ?: "",
                            year = doc.year.toString()
                        ),
                        isBoxContextSet = true
                    ) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = (result as DomainResult.Error).message) }
                }
            }
        }
    }

    private fun executeBulkUpload(sessionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = bulkInsertArchivesUseCase(sessionId)) {
                is DomainResult.Success -> _uiState.update { it.copy(isLoading = false, isUploadSuccess = true, successMessage = UiText.StringResource(R.string.msg_upload_success)) }
                is DomainResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
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
                if (result is DomainResult.Error) {
                    allSuccess = false
                    lastError = result.message
                }
            }

            if (allSuccess) {
                _uiState.update { it.copy(isLoading = false, isUploadSuccess = true, successMessage = UiText.StringResource(R.string.msg_upload_success)) }
            } else {
                _uiState.update { it.copy(isLoading = false, error = lastError) }
            }
        }
    }
}
