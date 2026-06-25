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
import com.example.arsipbpkpad.domain.model.Room
import com.example.arsipbpkpad.domain.model.Shelf
import com.example.arsipbpkpad.domain.model.StagedBox
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import com.example.arsipbpkpad.domain.repository.StagingRepository
import com.example.arsipbpkpad.domain.repository.StorageLocationRepository
import com.example.arsipbpkpad.domain.usecase.BulkInsertArchivesUseCase
import com.example.arsipbpkpad.presentation.util.UiText
import com.example.arsipbpkpad.utils.ResultState
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

data class StagedBundle(
    val id: String,
    val name: String,
    val nominal: Double? = null
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
    val showDuplicateWarning: Boolean = false,
    val editingId: String? = null,
    val classificationCode: String = DomainConstants.DEFAULT_CLASSIFICATION_CODE,
    val availableCodes: List<ClassificationCode> = emptyList(),
    val searchQuery: String = "",
    val selectedQuickCategory: String? = null,
    val quickCategories: List<ClassificationCode> = emptyList(),
    val selectedBundleId: String? = null,
    val stagedBundles: List<StagedBundle> = emptyList(),
    // Hierarchical Location states
    val roomsList: ResultState<List<Room>> = ResultState.Idle,
    val shelvesList: ResultState<List<Shelf>> = ResultState.Idle,
    val selectedRoom: Room? = null,
    val selectedShelf: Shelf? = null,
    val typedRoom: String = "",
    val typedShelf: String = "",
    val typedBox: String = "",
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
    
    // Hierarchical Location Events
    data class OnRoomChange(val value: String) : RapidInputUiEvent()
    data class OnRoomSelected(val room: Room?) : RapidInputUiEvent()
    data class OnCreateRoom(val name: String) : RapidInputUiEvent()
    data class OnShelfChange(val value: String) : RapidInputUiEvent()
    data class OnShelfSelected(val shelf: Shelf?) : RapidInputUiEvent()
    data class OnCreateShelf(val name: String) : RapidInputUiEvent()
    data class OnBoxLocationChange(val value: String) : RapidInputUiEvent()

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
    data class OnBundleSelected(val bundleId: String?) : RapidInputUiEvent()
    data class OnAddToBoxClick(val forceSave: Boolean = false) : RapidInputUiEvent()
    data class OnOcrResultReceived(val metadata: ParsedMetadata) : RapidInputUiEvent()
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
    data object DismissDuplicateWarning : RapidInputUiEvent()
}

@HiltViewModel
class RapidInputViewModel @Inject constructor(
    private val stagingRepository: StagingRepository,
    private val archiveRepository: ArchiveRepository,
    private val storageLocationRepository: StorageLocationRepository,
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
        loadRooms()
        
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
                val bundles = docs.filter { it.bundleId != null }
                    .groupBy { it.bundleId }
                    .map { (bundleId, bundleDocs) ->
                        val sp2d = bundleDocs.find { it.type == DocType.SP2D }
                        StagedBundle(
                            id = bundleId!!,
                            name = sp2d?.documentNumber ?: bundleDocs.first().documentNumber ?: "Bundle",
                            nominal = sp2d?.nominal ?: bundleDocs.firstOrNull { it.nominal != null }?.nominal
                        )
                    }
                _uiState.update { it.copy(stagedDocuments = docs, stagedBundles = bundles) }
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
            
            // Hierarchical Location
            is RapidInputUiEvent.OnRoomChange -> _uiState.update { it.copy(typedRoom = event.value) }
            is RapidInputUiEvent.OnRoomSelected -> handleRoomSelected(event.room)
            is RapidInputUiEvent.OnCreateRoom -> createRoom(event.name)
            is RapidInputUiEvent.OnShelfChange -> _uiState.update { it.copy(typedShelf = event.value) }
            is RapidInputUiEvent.OnShelfSelected -> handleShelfSelected(event.shelf)
            is RapidInputUiEvent.OnCreateShelf -> createShelf(event.name)
            is RapidInputUiEvent.OnBoxLocationChange -> _uiState.update { it.copy(typedBox = event.value) }

            is RapidInputUiEvent.OnDocTypeChange -> handleDocTypeChange(event.value)
            is RapidInputUiEvent.OnCopyTypeChange -> handleCopyTypeChange(event.value)
            is RapidInputUiEvent.OnCopyCountChange -> {
                if (_uiState.value.copyType != DocCopyType.ORIGINAL) {
                    _uiState.update { it.copy(copyCount = event.value) }
                }
            }
            is RapidInputUiEvent.OnDocNumberChange -> _uiState.update { it.copy(documentNumber = event.value, validationErrors = it.validationErrors - "docNumber") }
            is RapidInputUiEvent.OnSpmDocNumberChange -> _uiState.update { it.copy(spmDocumentNumber = event.value, validationErrors = it.validationErrors - "spmDocNumber") }
            is RapidInputUiEvent.OnSubjectChange -> _uiState.update { it.copy(subject = event.value, validationErrors = it.validationErrors - "subject") }
            is RapidInputUiEvent.OnSpjDescriptionChange -> _uiState.update { it.copy(spjDescription = event.value) }
            is RapidInputUiEvent.OnNominalChange -> _uiState.update { it.copy(nominal = event.value, validationErrors = it.validationErrors - "nominal") }
            is RapidInputUiEvent.OnConditionChange -> _uiState.update { it.copy(condition = event.value) }
            is RapidInputUiEvent.OnClassificationCodeChange -> _uiState.update { it.copy(classificationCode = event.value) }
            is RapidInputUiEvent.OnSearchQueryChanged -> _uiState.update { it.copy(searchQuery = event.query) }
            is RapidInputUiEvent.OnQuickCategorySelected -> handleQuickCategorySelected(event.categoryCode)
            is RapidInputUiEvent.OnAutoBundleToggle -> _uiState.update { it.copy(isAutoBundleEnabled = event.enabled) }
            is RapidInputUiEvent.OnBundleSelected -> handleBundleSelected(event.bundleId)
            is RapidInputUiEvent.OnAddToBoxClick -> addToStaging(event.forceSave)
            is RapidInputUiEvent.OnOcrResultReceived -> handleOcrResult(event.metadata)
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
            is RapidInputUiEvent.DismissDuplicateWarning -> _uiState.update { it.copy(showDuplicateWarning = false) }
        }
    }

    private fun loadRooms() {
        viewModelScope.launch {
            storageLocationRepository.getRooms().collect { state ->
                _uiState.update { it.copy(roomsList = state) }
            }
        }
    }

    private fun handleRoomSelected(room: Room?) {
        _uiState.update { it.copy(
            selectedRoom = room,
            typedRoom = room?.name ?: it.typedRoom,
            selectedShelf = null,
            typedShelf = "",
            shelvesList = ResultState.Idle,
            typedBox = ""
        ) }
        if (room != null) {
            viewModelScope.launch {
                storageLocationRepository.getShelvesByRoom(room.id).collect { state ->
                    _uiState.update { it.copy(shelvesList = state) }
                }
            }
        }
    }

    private fun handleShelfSelected(shelf: Shelf?) {
        _uiState.update { it.copy(
            selectedShelf = shelf,
            typedShelf = shelf?.name ?: it.typedShelf,
            typedBox = ""
        ) }
    }

    private fun createRoom(name: String) {
        viewModelScope.launch {
            storageLocationRepository.createRoom(name).onSuccess { room ->
                loadRooms()
                handleRoomSelected(room)
            }
        }
    }

    private fun createShelf(name: String) {
        val roomId = _uiState.value.selectedRoom?.id ?: return
        viewModelScope.launch {
            storageLocationRepository.createShelf(roomId, name).onSuccess { shelf ->
                handleShelfSelected(shelf)
            }
        }
    }

    private fun handleDocTypeChange(value: DocType) {
        _uiState.update { it.copy(
            docType = value,
            isAutoBundleEnabled = false,
            selectedBundleId = null,
            validationErrors = emptyMap()
        ) }
    }

    private fun handleBundleSelected(bundleId: String?) {
        _uiState.update { state ->
            val bundle = state.stagedBundles.find { it.id == bundleId }
            state.copy(
                selectedBundleId = bundleId,
                nominal = bundle?.nominal?.toLong()?.toString() ?: state.nominal,
                validationErrors = state.validationErrors - "nominal"
            )
        }
    }

    private fun validateInput(): Boolean {
        val state = _uiState.value
        val errors = state.validationErrors.toMutableMap()
        var isValid = true

        if (state.documentNumber.isBlank()) {
            errors["docNumber"] = "Nomor dokumen wajib diisi"
            isValid = false
        } else {
            errors.remove("docNumber")
        }

        if (state.subject.isBlank()) {
            errors["subject"] = "Uraian dokumen wajib diisi"
            isValid = false
        } else {
            errors.remove("subject")
        }

        if (state.isAutoBundleEnabled && state.spmDocumentNumber.isBlank()) {
            errors["spmDocNumber"] = "Nomor SPM wajib diisi"
            isValid = false
        } else {
            errors.remove("spmDocNumber")
        }

        if (state.copyType == DocCopyType.COPY && (state.copyCount.toIntOrNull() ?: 0) < 1) {
            errors["copyCount"] = "Jumlah salinan minimal 1"
            isValid = false
        } else {
            errors.remove("copyCount")
        }

        val nominalValue = state.nominal.toDoubleOrNull() ?: 0.0
        val isNominalRequired = when (state.docType) {
            DocType.SP2D, DocType.SPM -> true
            DocType.SPJ -> state.selectedBundleId == null
            else -> false
        }

        if (nominalValue < 0) {
            errors["nominal"] = "Nominal tidak boleh kurang dari nol"
            isValid = false
        } else if (isNominalRequired && (state.nominal.isBlank() || nominalValue == 0.0)) {
            errors["nominal"] = "Nominal harus berupa angka lebih dari 0"
            isValid = false
        } else {
            errors.remove("nominal")
        }

        _uiState.update { it.copy(validationErrors = errors) }
        return isValid
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
            val state = _uiState.value
            val ctx = state.boxContext
            val errors = mutableMapOf<String, String>()
            
            if (state.selectedRoom == null) errors["warehouse"] = "Gudang wajib dipilih"
            if (state.selectedShelf == null) errors["rack"] = "Nomor rak wajib dipilih"
            
            if (state.typedBox.isBlank()) {
                errors["box"] = "Nama Box wajib diisi"
            } else if (state.selectedShelf != null) {
                // Check duplicate box in the selected shelf
                if (storageLocationRepository.checkBoxExists(state.selectedShelf.id, state.typedBox)) {
                    errors["box"] = "Box dengan nomor ini sudah ada di rak tersebut"
                }
            }
            
            val yearInt = ctx.year.toIntOrNull()
            if (ctx.year.length != 4 || yearInt == null) {
                errors["year"] = "Tahun tidak valid (harus 4 digit)"
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
                val updatedBox = StagedBox(
                    sessionId = sessionId,
                    warehouse = state.selectedRoom!!.name,
                    rack = state.selectedShelf!!.name,
                    box = state.typedBox,
                    year = ctx.year
                )
                stagingRepository.saveStagedBox(updatedBox)
                _uiState.update { it.copy(
                    currentSessionId = sessionId,
                    boxContext = BoxContext(
                        warehouse = updatedBox.warehouse,
                        rack = updatedBox.rack,
                        box = updatedBox.box,
                        year = updatedBox.year
                    ),
                    isBoxContextSet = true,
                    validationErrors = emptyMap()
                ) }
                _navigationEvent.emit(sessionId)
            } else {
                _uiState.update { it.copy(validationErrors = errors) }
            }
        }
    }

    private fun addToStaging(forceSave: Boolean = false) {
        viewModelScope.launch {
            if (!validateInput()) return@launch

            val state = _uiState.value
            val sessionId = state.currentSessionId ?: return@launch
            
            // Check for potential duplicate if not forced
            if (!forceSave && state.copyType == DocCopyType.ORIGINAL) {
                val exists = archiveRepository.checkDocumentNumberAndTypeExists(
                    state.documentNumber, 
                    state.copyType.name
                )
                if (exists) {
                    _uiState.update { it.copy(showDuplicateWarning = true) }
                    return@launch
                }
            }

            val bundleId = if (state.isAutoBundleEnabled || state.docType == DocType.SPJ) {
                state.selectedBundleId ?: UUID.randomUUID().toString()
            } else null
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
                selectedBundleId = null, validationErrors = emptyMap(), error = null
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
            if (!validateInput()) return@launch

            val state = _uiState.value
            val archiveId = state.editingId ?: return@launch

            val errors = state.validationErrors.toMutableMap()
            if (state.subject.isBlank()) errors["subject"] = "Uraian dokumen wajib diisi"

            if (errors.isNotEmpty()) {
                _uiState.update { it.copy(validationErrors = errors) }
                return@launch
            }
            
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
                        successMessage = UiText.StringResource(R.string.msg_data_updated)
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
