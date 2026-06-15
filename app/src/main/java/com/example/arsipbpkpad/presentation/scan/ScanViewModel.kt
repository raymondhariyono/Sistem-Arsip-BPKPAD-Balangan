package com.example.arsipbpkpad.presentation.scan

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.usecase.ExtractTextWithMlKitUseCase
import com.example.arsipbpkpad.domain.usecase.ParseMetadataWithAiUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val extractTextUseCase: ExtractTextWithMlKitUseCase,
    private val parseMetadataUseCase: ParseMetadataWithAiUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun onImageCaptured(uri: Uri) {
        _uiState.update { it.copy(isLoading = true, capturedImageUri = uri.toString(), errorMessage = null) }
        
        viewModelScope.launch {
            // Step 1: Extract Text
            val ocrResult = extractTextUseCase(uri)
            if (ocrResult is ResultState.Success) {
                // Step 2: Parse Metadata
                val aiResult = parseMetadataUseCase(ocrResult.data)
                if (aiResult is ResultState.Success) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        isSuccess = true,
                        parsedData = aiResult.data
                    ) }
                } else if (aiResult is ResultState.Error) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = aiResult.message) }
                }
            } else if (ocrResult is ResultState.Error) {
                _uiState.update { it.copy(isLoading = false, errorMessage = ocrResult.message) }
            }
        }
    }

    fun resetState() {
        _uiState.update { ScanUiState() }
    }
}
