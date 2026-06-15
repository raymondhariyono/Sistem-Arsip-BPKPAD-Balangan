package com.example.arsipbpkpad.domain.repository

import androidx.paging.PagingData
import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import kotlinx.coroutines.flow.Flow

interface ArchiveRepository {
    fun getArchives(query: String? = null, years: List<Int> = emptyList()): Flow<PagingData<ArchiveDocument>>
    fun getArchivesList(query: String? = null, years: List<Int> = emptyList()): Flow<ResultState<List<ArchiveDocument>>>
    fun getArchiveDetail(id: String): Flow<ResultState<ArchiveDocument>>
    suspend fun checkDocumentNumberAndStatusExists(docNumber: String, copyStatus: String): Boolean
    suspend fun saveArchive(archive: ArchiveDocument): ResultState<Unit>
    suspend fun deleteArchive(id: String): ResultState<Unit>
    suspend fun syncArchives(): ResultState<Unit>
    fun getTotalBudgetByYear(year: Int): Flow<ResultState<Double>>
}
