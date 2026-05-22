package com.example.arsipbpkpad.domain.repository

import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.utils.ResultState
import kotlinx.coroutines.flow.Flow

interface ArchiveRepository {
    fun getArchives(): Flow<ResultState<List<ArchiveDocument>>>
    suspend fun saveArchive(archive: ArchiveDocument): ResultState<Unit>
}