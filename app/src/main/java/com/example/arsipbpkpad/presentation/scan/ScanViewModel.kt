package com.example.arsipbpkpad.presentation.scan

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.usecase.ExtractTextUseCase
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
    private val extractTextUseCase: ExtractTextUseCase,
    private val parseMetadataUseCase: ParseMetadataWithAiUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun onImageCaptured(uri: Uri) {
        _uiState.update { it.copy(isLoading = true, capturedImageUri = uri.toString(), errorMessage = null) }
        
        viewModelScope.launch {
            when (val ocrResult = extractTextUseCase(uri.toString())) {
                is DomainResult.Success -> {
                    when (val aiResult = parseMetadataUseCase(ocrResult.data)) {
                        is DomainResult.Success -> {
                            _uiState.update { it.copy(
                                isLoading = false,
                                isSuccess = true,
                                parsedData = aiResult.data
                            ) }
                        }
                        is DomainResult.Error -> {
                            _uiState.update { it.copy(isLoading = false, errorMessage = aiResult.message) }
                        }
                    }
                }
                is DomainResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = ocrResult.message) }
                }
            }
        }
    }

    fun resetState() {
        _uiState.update { ScanUiState() }
    }
}
