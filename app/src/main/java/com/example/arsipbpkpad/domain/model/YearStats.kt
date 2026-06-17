package com.example.arsipbpkpad.domain.model

data class YearStats(
    val year: Int,
    val count: Int,
    val lastUpdated: String? = null
)
