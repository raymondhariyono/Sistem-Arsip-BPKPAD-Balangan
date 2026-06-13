package com.example.arsipbpkpad.presentation.archive.add.manual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.DocCopyStatus
import com.example.arsipbpkpad.domain.model.DocStatus
import com.example.arsipbpkpad.domain.model.DocType
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import com.example.arsipbpkpad.domain.repository.StagingRepository
import com.example.arsipbpkpad.domain.usecase.BulkInsertArchivesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    val boxContext: BoxContext = BoxContext(),
    val isBoxContextSet: Boolean = false,
    val stagedDocuments: List<ArchiveDocument> = emptyList(),
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
    val editingId: String? = null
)

sealed class RapidInputUiEvent {
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
    data object OnAddToBoxClick : RapidInputUiEvent()

    // Staging Actions
    data class OnDeleteStagedDoc(val id: String) : RapidInputUiEvent()
    data class OnEditStagedDoc(val doc: ArchiveDocument) : RapidInputUiEvent()
    
    // Bulk Actions
    data object OnConfirmUpload : RapidInputUiEvent()
    data object ResetState : RapidInputUiEvent()
}

@HiltViewModel
class RapidInputViewModel @Inject constructor(
    private val stagingRepository: StagingRepository,
    private val archiveRepository: ArchiveRepository,
    private val bulkInsertArchivesUseCase: BulkInsertArchivesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RapidInputUiState())
    val uiState: StateFlow<RapidInputUiState> = _uiState.asStateFlow()

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

    fun onEvent(event: RapidInputUiEvent) {
        when (event) {
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
            
            is RapidInputUiEvent.OnAddToBoxClick -> addToStaging()
            is RapidInputUiEvent.OnDeleteStagedDoc -> deleteFromStaging(event.id)
            is RapidInputUiEvent.OnEditStagedDoc -> startEditing(event.doc)
            is RapidInputUiEvent.OnConfirmUpload -> executeBulkUpload()
            is RapidInputUiEvent.ResetState -> _uiState.value = RapidInputUiState()
        }
    }

    private fun validateBoxContext() {
        val ctx = _uiState.value.boxContext
        val errors = mutableMapOf<String, String>()
        if (ctx.warehouse.isBlank()) errors["warehouse"] = "Wajib diisi"
        if (ctx.rack.isBlank()) errors["rack"] = "Wajib diisi"
        if (ctx.box.isBlank()) errors["box"] = "Wajib diisi"
        if (ctx.year.length != 4) errors["year"] = "Tahun tidak valid"
        
        if (errors.isEmpty()) {
            _uiState.update { it.copy(isBoxContextSet = true, validationErrors = emptyMap()) }
        } else {
            _uiState.update { it.copy(validationErrors = errors) }
        }
    }

    private fun addToStaging() {
        viewModelScope.launch {
            val state = _uiState.value
            val errors = mutableMapOf<String, String>()
            if (state.documentNumber.isBlank()) errors["docNumber"] = "Wajib diisi"
            if (state.subject.isBlank()) errors["subject"] = "Wajib diisi"
            
            if (errors.isNotEmpty()) {
                _uiState.update { it.copy(validationErrors = errors) }
                return@launch
            }

            // Duplicate check
            val exists = archiveRepository.checkDocumentNumberAndStatusExists(state.documentNumber, state.copyStatus)
            if (exists && state.editingId == null) {
                _uiState.update { it.copy(error = "Nomor dokumen dengan status tersebut sudah ada.") }
                return@launch
            }

            val docYear = state.boxContext.year.toIntOrNull() ?: 2026
            val doc = ArchiveDocument(
                id = state.editingId ?: UUID.randomUUID().toString(),
                type = DocType.valueOf(state.docType),
                copyStatus = DocCopyStatus.valueOf(state.copyStatus),
                documentNumber = state.documentNumber,
                nominal = state.nominal.toDoubleOrNull(),
                thirdParty = state.subject,
                year = docYear,
                dateIssued = "${docYear + 10}-12-31",
                status = DocStatus.UNVERIFIED,
                idStorageLocation = "${state.boxContext.warehouse}-${state.boxContext.rack}-${state.boxContext.box}",
                metadata = null,
                createdBy = "Admin",
                verifiedBy = null,
                createdAt = null,
                updatedAt = null
            )

            stagingRepository.insertToStaging(doc)
            
            // Reset form but keep box context
            _uiState.update { it.copy(
                documentNumber = "",
                subject = "",
                nominal = "",
                editingId = null,
                validationErrors = emptyMap(),
                error = null
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

    private fun executeBulkUpload() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val ctx = _uiState.value.boxContext
            val result = bulkInsertArchivesUseCase(ctx.warehouse, ctx.rack, ctx.box)
            
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
}
