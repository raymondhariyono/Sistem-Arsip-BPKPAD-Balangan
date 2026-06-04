package com.example.arsipbpkpad.domain.repository

import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import kotlinx.coroutines.flow.Flow

interface ArchiveRepository {
    fun getArchives(query: String? = null): Flow<ResultState<List<ArchiveDocument>>>
    fun getArchiveDetail(id: String): Flow<ResultState<ArchiveDocument>>
    suspend fun checkDocumentNumberExists(docNumber: String): Boolean
    suspend fun saveArchive(archive: ArchiveDocument): ResultState<Unit>
    suspend fun deleteArchive(id: String): ResultState<Unit>
    suspend fun syncArchives(): ResultState<Unit>
}
