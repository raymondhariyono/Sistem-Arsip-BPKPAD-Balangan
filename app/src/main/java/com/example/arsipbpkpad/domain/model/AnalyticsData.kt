package com.example.arsipbpkpad.domain.model

data class AnalyticsData(
    val totalBudget: Double,
    val budgetByClassification: Map<String, Double> = emptyMap()
)
