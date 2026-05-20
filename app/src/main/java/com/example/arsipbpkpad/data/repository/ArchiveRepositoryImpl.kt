package com.example.arsipbpkpad.data.repository

import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import com.example.arsipbpkpad.utils.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ArchiveRepositoryImpl @Inject constructor() : ArchiveRepository {
    override fun getArchives(): Flow<ResultState<List<ArchiveDocument>>> = flow {
        emit(ResultState.Loading)
        // Implementation will fetch from API or Local DB
    }

    override suspend fun saveArchive(archive: ArchiveDocument): ResultState<Unit> {
        return ResultState.Success(Unit)
    }
}