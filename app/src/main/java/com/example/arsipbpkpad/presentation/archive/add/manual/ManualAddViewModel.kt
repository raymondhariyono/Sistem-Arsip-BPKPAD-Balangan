package com.example.arsipbpkpad.presentation.archive.add.manual

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
    val boxNo: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val validationErrors: Map<String, String> = emptyMap()
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
    data object ResetState : ManualAddUiEvent()
}

@HiltViewModel
class ManualAddViewModel @Inject constructor(
    private val saveArchiveUseCase: SaveArchiveUseCase,
    private val archiveRepository: ArchiveRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManualAddUiState())
    val uiState: StateFlow<ManualAddUiState> = _uiState.asStateFlow()

    private val _saveResult = MutableStateFlow<ResultState<Unit>>(ResultState.Idle)
    val saveResult: StateFlow<ResultState<Unit>> = _saveResult.asStateFlow()

    fun onEvent(event: ManualAddUiEvent) {
        when (event) {
            is ManualAddUiEvent.OnDocTypeChange -> _uiState.update { it.copy(docType = event.value, validationErrors = it.validationErrors - "docType") }
            is ManualAddUiEvent.OnDocNameChange -> _uiState.update { it.copy(docName = event.value, validationErrors = it.validationErrors - "docName") }
            is ManualAddUiEvent.OnDocNumberChange -> _uiState.update { it.copy(docNumber = event.value, validationErrors = it.validationErrors - "docNumber") }
            is ManualAddUiEvent.OnDepartmentChange -> _uiState.update { it.copy(department = event.value, validationErrors = it.validationErrors - "department") }
            is ManualAddUiEvent.OnYearChange -> _uiState.update { it.copy(year = event.value.filter { it.isDigit() }.take(4), validationErrors = it.validationErrors - "year") }
            is ManualAddUiEvent.OnValidityChange -> {
                val formattedDate = formatAsDate(event.value)
                _uiState.update { it.copy(validity = formattedDate, validationErrors = it.validationErrors - "validity") }
            }
            is ManualAddUiEvent.OnSubjectChange -> _uiState.update { it.copy(subject = event.value, validationErrors = it.validationErrors - "subject") }
            is ManualAddUiEvent.OnWarehouseChange -> _uiState.update { it.copy(warehouse = event.value, validationErrors = it.validationErrors - "warehouse") }
            is ManualAddUiEvent.OnRackNoChange -> _uiState.update { it.copy(rackNo = event.value, validationErrors = it.validationErrors - "rackNo") }
            is ManualAddUiEvent.OnBoxNoChange -> _uiState.update { it.copy(boxNo = event.value, validationErrors = it.validationErrors - "boxNo") }
            is ManualAddUiEvent.OnSaveClick -> saveArchive()
            is ManualAddUiEvent.ResetState -> {
                _uiState.value = ManualAddUiState()
                _saveResult.value = ResultState.Idle
            }
        }
    }

    private fun formatAsDate(input: String): String {
        // Remove non-numeric characters
        val digitsOnly = input.filter { it.isDigit() }
        val sb = StringBuilder()
        
        for (i in digitsOnly.indices) {
            sb.append(digitsOnly[i])
            if ((i == 1 || i == 3) && i != digitsOnly.lastIndex) {
                sb.append("-")
            }
        }
        
        // Limit to DD-MM-YYYY (10 characters)
        return sb.toString().take(10)
    }

    private fun validateFields(state: ManualAddUiState): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (state.docType.isBlank()) errors["docType"] = "Tipe dokumen wajib dipilih"
        if (state.docName.isBlank()) errors["docName"] = "Nama dokumen wajib diisi"
        if (state.docNumber.isBlank()) errors["docNumber"] = "Nomor dokumen wajib diisi"
        if (state.department.isBlank()) errors["department"] = "Dinas/Departemen wajib dipilih"
        if (state.year.isBlank()) {
            errors["year"] = "Tahun wajib diisi"
        } else if (state.year.length != 4) {
            errors["year"] = "Tahun harus 4 digit"
        }
        if (state.validity.isBlank()) {
            errors["validity"] = "Masa berlaku wajib diisi"
        } else if (state.validity.length < 10) {
            errors["validity"] = "Format tanggal tidak lengkap (DD-MM-YYYY)"
        }
        if (state.subject.isBlank()) errors["subject"] = "Perihal wajib diisi"
        if (state.warehouse.isBlank()) errors["warehouse"] = "Gudang wajib dipilih"
        if (state.rackNo.isBlank()) errors["rackNo"] = "Nomor rak wajib diisi"
        if (state.boxNo.isBlank()) errors["boxNo"] = "Nomor box wajib diisi"
        
        return errors
    }

    private fun saveArchive() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val validationErrors = validateFields(currentState)
            
            if (validationErrors.isNotEmpty()) {
                _uiState.update { it.copy(validationErrors = validationErrors, error = "Silakan lengkapi formulir dengan benar") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            // Check for duplicate document number
            val exists = archiveRepository.checkDocumentNumberExists(currentState.docNumber)
            if (exists) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        validationErrors = it.validationErrors + ("docNumber" to "Nomor dokumen sudah terdaftar"),
                        error = "Terjadi kesalahan pada input"
                    ) 
                }
                return@launch
            }

            val archive = ArchiveDocument(
                id = UUID.randomUUID().toString(),
                type = try { DocType.valueOf(currentState.docType) } catch(e: Exception) { DocType.SP2D },
                documentNumber = currentState.docNumber,
                nominal = null,
                thirdParty = currentState.subject,
                year = currentState.year.toIntOrNull() ?: 2024,
                dateIssued = currentState.validity,
                status = DocStatus.AVAILABLE,
                idStorageLocation = null,
                metadata = null,
                createdBy = null,
                verifiedBy = null,
                createdAt = null,
                updatedAt = null
            )
            
            _saveResult.value = ResultState.Loading
            
            val result = saveArchiveUseCase(archive)
            
            when (result) {
                is ResultState.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    _saveResult.value = ResultState.Success(Unit)
                }
                is ResultState.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                    _saveResult.value = result
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _saveResult.value = ResultState.Idle
                }
            }
        }
    }
}
