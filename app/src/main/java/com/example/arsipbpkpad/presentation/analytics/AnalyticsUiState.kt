package com.example.arsipbpkpad.presentation.analytics

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val isFilterConfirmed: Boolean = false,
    val totalBudget: Double = 0.0,
    val budgetByClassification: Map<String, Double> = emptyMap(),
    val past10YearsBudgets: Map<String, Double> = emptyMap(),
    val selectedYear: Int? = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
    val availableYears: List<Int> = emptyList(),
    val errorMessage: String? = null
)
