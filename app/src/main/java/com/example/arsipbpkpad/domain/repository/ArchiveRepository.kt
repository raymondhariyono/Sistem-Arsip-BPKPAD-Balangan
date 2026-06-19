package com.example.arsipbpkpad.domain.repository

import androidx.paging.PagingData
import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import kotlinx.coroutines.flow.Flow

interface ArchiveRepository {
    fun getArchives(query: String? = null, years: List<Int> = emptyList()): Flow<PagingData<ArchiveDocument>>
    fun getArchivesList(query: String? = null, years: List<Int> = emptyList()): Flow<ResultState<List<ArchiveDocument>>>
    fun getArchiveDetail(id: String): Flow<ResultState<ArchiveDocument>>
    suspend fun checkDocumentNumberAndTypeExists(docNumber: String, copyType: String): Boolean
    suspend fun checkDocumentNumberExists(docNumber: String): Boolean
    suspend fun saveArchive(archive: ArchiveDocument): ResultState<Unit>
    suspend fun saveArchives(archives: List<ArchiveDocument>): ResultState<Unit>
    suspend fun deleteArchive(id: String): ResultState<Unit>
    suspend fun syncArchives(): ResultState<Unit>
    suspend fun syncPendingArchives(): ResultState<Unit>
    fun getArchivedYears(): Flow<List<Int>>
    fun getYearStats(): Flow<List<com.example.arsipbpkpad.domain.model.YearStats>>
    fun getArchivesByBundleId(bundleId: String): Flow<List<ArchiveDocument>>
    fun getTotalBudgetByYear(year: Int): Flow<ResultState<Double>>
    suspend fun uploadImage(id: String, imageByteArray: ByteArray): ResultState<String>
    
    // Master Data
    suspend fun syncClassificationCodes(): ResultState<Unit>
    fun observeClassificationCodes(): Flow<List<com.example.arsipbpkpad.domain.model.ClassificationCode>>
}
