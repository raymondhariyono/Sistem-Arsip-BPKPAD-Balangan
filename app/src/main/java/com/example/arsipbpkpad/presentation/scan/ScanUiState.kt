package com.example.arsipbpkpad.presentation.scan

import com.example.arsipbpkpad.domain.usecase.ParsedMetadata

data class ScanUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val parsedData: ParsedMetadata? = null,
    val capturedImageUri: String? = null
)
