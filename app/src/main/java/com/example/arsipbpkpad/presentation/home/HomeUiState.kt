package com.example.arsipbpkpad.presentation.home

import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.StagedBox

data class HomeUiState(
    val totalDocuments: String = "0",
    val expiredDocuments: String = "0",
    val sp2dCount: String = "0",
    val spmCount: String = "0",
    val spjCount: String = "0",
    val otherTypeCount: String = "0",
    val recentItems: List<ArchiveDocument> = emptyList(),
    val activeStagingBoxes: List<StagedBox> = emptyList(),
    val availableYears: List<Int> = emptyList(),
    val yearStats: List<com.example.arsipbpkpad.domain.model.YearStats> = emptyList(),
    val showYearSelection: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
