package com.example.arsipbpkpad.presentation.archive.list

sealed class ArchiveListUiEvent {
    data object Refresh : ArchiveListUiEvent()
    data class OnArchiveClick(val archiveId: String) : ArchiveListUiEvent()
    data class OnSearchQueryChange(val query: String) : ArchiveListUiEvent()
    data class OnFilterChange(val type: String) : ArchiveListUiEvent()
    data class OnYearToggle(val year: Int) : ArchiveListUiEvent()
    data object OnSelectAllYears : ArchiveListUiEvent()
    data object OnConfirmFilter : ArchiveListUiEvent()
    data object OnResetFilter : ArchiveListUiEvent()
    data class ImportExcel(val inputStream: java.io.InputStream) : ArchiveListUiEvent()
    data class ExportExcel(val outputStream: java.io.OutputStream) : ArchiveListUiEvent()
}
