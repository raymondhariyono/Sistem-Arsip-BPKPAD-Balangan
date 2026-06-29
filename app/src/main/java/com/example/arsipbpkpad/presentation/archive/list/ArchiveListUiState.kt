package com.example.arsipbpkpad.presentation.archive.list

data class ArchiveListUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedFilter: String = "Semua",
    val selectedYears: Set<Int> = emptySet(),
    val isFilterConfirmed: Boolean = false,
    val availableYears: List<Int> = emptyList(),
    val yearStats: List<com.example.arsipbpkpad.domain.model.YearStats> = emptyList(),
    val excelOperationMessage: String? = null,
    
    // Multi-select delete states
    val isSelectionMode: Boolean = false,
    val selectedArchiveIds: Set<String> = emptySet(),
    val showDeleteConfirmDialog: Boolean = false,
    val successMessage: String? = null,
    val isDeleting: Boolean = false
)
