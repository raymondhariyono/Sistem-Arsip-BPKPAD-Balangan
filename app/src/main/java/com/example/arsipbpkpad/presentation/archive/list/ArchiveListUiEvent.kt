package com.example.arsipbpkpad.presentation.archive.list

sealed class ArchiveListUiEvent {
    data object Refresh : ArchiveListUiEvent()
    data class OnArchiveClick(val archiveId: String) : ArchiveListUiEvent()
    data class OnSearchQueryChange(val query: String) : ArchiveListUiEvent()
    data class OnFilterChange(val type: String) : ArchiveListUiEvent()
}
