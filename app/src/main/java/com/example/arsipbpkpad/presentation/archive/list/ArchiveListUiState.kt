package com.example.arsipbpkpad.presentation.archive.list

import com.example.arsipbpkpad.domain.model.ArchiveDocument

data class ArchiveListUiState(
    val isLoading: Boolean = false,
    val archives: List<ArchiveDocument> = emptyList(),
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedFilter: String = "Semua"
)
