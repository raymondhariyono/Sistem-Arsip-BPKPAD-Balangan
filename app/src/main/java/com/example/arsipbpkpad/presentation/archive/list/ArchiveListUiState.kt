package com.example.arsipbpkpad.presentation.archive.list

import com.example.arsipbpkpad.domain.model.ArchiveDocument

data class ArchiveListUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedFilter: String = "Semua",
    val selectedYears: Set<Int> = emptySet(),
    val isFilterConfirmed: Boolean = false,
    val availableYears: List<Int> = (2024 downTo 2018).toList()
)
