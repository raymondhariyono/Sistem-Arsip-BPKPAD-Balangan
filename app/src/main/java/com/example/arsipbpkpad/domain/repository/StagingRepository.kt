package com.example.arsipbpkpad.domain.repository

import com.example.arsipbpkpad.domain.model.ArchiveDocument
import kotlinx.coroutines.flow.Flow

interface StagingRepository {
    fun getAllStagingArchives(): Flow<List<ArchiveDocument>>
    suspend fun insertToStaging(archive: ArchiveDocument)
    suspend fun deleteFromStaging(id: String)
    suspend fun clearStaging()
}
