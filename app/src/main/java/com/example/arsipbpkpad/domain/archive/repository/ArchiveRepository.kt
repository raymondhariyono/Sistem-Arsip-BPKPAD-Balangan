package com.example.arsipbpkpad.domain.archive.repository

import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.archive.model.ArchiveDocument
import kotlinx.coroutines.flow.Flow

interface ArchiveRepository {
    fun getArchives(): Flow<ResultState<List<ArchiveDocument>>>
    fun getArchiveDetail(id: String): Flow<ResultState<ArchiveDocument>>
    suspend fun saveArchive(archive: ArchiveDocument): ResultState<Unit>
}
