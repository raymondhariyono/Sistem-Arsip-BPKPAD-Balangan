package com.example.arsipbpkpad.presentation.archive.list

data class ArchiveListUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedFilter: String = "Semua",
    val selectedYears: Set<Int> = emptySet(),
    val isFilterConfirmed: Boolean = false,
    val availableYears: List<Int> = (2024 downTo 2018).toList(),
    val yearStats: List<com.example.arsipbpkpad.domain.model.YearStats> = emptyList()
)
