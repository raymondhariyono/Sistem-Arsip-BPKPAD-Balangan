package com.example.arsipbpkpad.data.repository

import com.example.arsipbpkpad.domain.archive.model.ArchiveDocument
import com.example.arsipbpkpad.domain.archive.repository.ArchiveRepository
import com.example.arsipbpkpad.core.common.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ArchiveRepositoryImpl : ArchiveRepository {
    override fun getArchives(): Flow<ResultState<List<ArchiveDocument>>> = flow {
        emit(ResultState.Loading)
        // Implementation will fetch from API or Local DB
    }

    override suspend fun saveArchive(archive: ArchiveDocument): ResultState<Unit> {
        return ResultState.Success(Unit)
    }
}
