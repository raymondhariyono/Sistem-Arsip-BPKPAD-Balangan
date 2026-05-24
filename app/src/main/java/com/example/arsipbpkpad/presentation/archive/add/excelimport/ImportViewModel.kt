package com.example.arsipbpkpad.presentation.archive.add.excelimport

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// --- STATE & EVENTS ---
data class ImportUiState(
    val selectedFileName: String? = null,
    val isLoading: Boolean = false
)

sealed class ImportUiEvent {
    data object OnDownloadTemplateClick : ImportUiEvent()
    data object OnSelectFileClick : ImportUiEvent()
    data object OnProcessImportClick : ImportUiEvent()
}

@HiltViewModel
class ImportViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun onEvent(event: ImportUiEvent) {
        when (event) {
            is ImportUiEvent.OnDownloadTemplateClick -> { /* Logic */ }
            is ImportUiEvent.OnSelectFileClick -> { /* Logic */ }
            is ImportUiEvent.OnProcessImportClick -> { /* Logic */ }
        }
    }
}
