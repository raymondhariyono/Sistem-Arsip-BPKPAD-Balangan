package com.example.arsipbpkpad.presentation.analytics

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val isFilterConfirmed: Boolean = false,
    val totalBudget: Double = 0.0,
    val selectedYear: Int? = 2024,
    val availableYears: List<Int> = listOf(2024, 2023, 2022, 2021, 2020),
    val errorMessage: String? = null
)
