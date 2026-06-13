package com.example.arsipbpkpad.presentation.home

data class HomeUiState(
    val totalDocuments: String = "0",
    val expiredDocuments: String = "0",
    val sp2dCount: String = "0",
    val spmCount: String = "0",
    val sp3bCount: String = "0",
    val dsbCount: String = "0",
    val recentItems: List<RecentArchive> = emptyList(),
    val stagedItemsCount: Int = 0,
    val stagedBoxSummary: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class RecentArchive(
    val id: String,
    val title: String,
    val type: String,
    val isAvailable: Boolean
)
