package com.example.arsipbpkpad.domain.repository

import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.ClassificationCode
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.model.YearStats
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for archive operations.
 * Uses DomainResult and avoids paging dependencies in the interface where possible
 * (PagingData is typically presentation/data layer, but some architectures keep it in domain.
 * Here we lean towards purity).
 */
interface ArchiveRepository {
    // Note: PagingData is removed from the domain interface to maintain purity.
    // If paging is strictly required in domain, we'd need a domain abstraction.
    // For now, we use Flow<List<ArchiveDocument>> or specialized results.
    fun getArchivesFlow(query: String? = null, years: List<Int> = emptyList()): Flow<List<ArchiveDocument>>
    
    fun getArchivesList(query: String? = null, years: List<Int> = emptyList()): Flow<DomainResult<List<ArchiveDocument>>>
    fun getArchiveDetail(id: String): Flow<DomainResult<ArchiveDocument>>
    
    suspend fun checkDocumentNumberAndTypeExists(docNumber: String, copyType: String): Boolean
    suspend fun checkDocumentNumberExists(docNumber: String): Boolean
    
    suspend fun saveArchive(archive: ArchiveDocument): DomainResult<Unit>
    suspend fun saveArchives(archives: List<ArchiveDocument>): DomainResult<Unit>
    suspend fun deleteArchive(id: String): DomainResult<Unit>
    
    suspend fun syncArchives(): DomainResult<Unit>
    suspend fun syncPendingArchives(): DomainResult<Unit>
    
    fun getArchivedYears(): Flow<List<Int>>
    fun getYearStats(): Flow<List<YearStats>>
    fun getArchivesByBundleId(bundleId: String): Flow<List<ArchiveDocument>>
    
    fun getTotalBudgetByYear(year: Int): Flow<DomainResult<Double>>
    suspend fun uploadImage(id: String, imageByteArray: ByteArray): DomainResult<String>
    
    // Master Data
    suspend fun syncClassificationCodes(): DomainResult<Unit>
    fun observeClassificationCodes(): Flow<List<ClassificationCode>>
}
