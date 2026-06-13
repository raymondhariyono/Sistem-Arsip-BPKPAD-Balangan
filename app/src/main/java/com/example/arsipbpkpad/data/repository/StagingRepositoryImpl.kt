package com.example.arsipbpkpad.data.repository

import com.example.arsipbpkpad.data.local.dao.StagingArchiveDao
import com.example.arsipbpkpad.data.mapper.toDomain
import com.example.arsipbpkpad.data.mapper.toStagingEntity
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.repository.StagingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StagingRepositoryImpl @Inject constructor(
    private val stagingArchiveDao: StagingArchiveDao
) : StagingRepository {

    override fun getAllStagingArchives(): Flow<List<ArchiveDocument>> {
        return stagingArchiveDao.getAllStagingArchives().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertToStaging(archive: ArchiveDocument) {
        stagingArchiveDao.insertToStaging(archive.toStagingEntity())
    }

    override suspend fun deleteFromStaging(id: String) {
        stagingArchiveDao.deleteFromStaging(id)
    }

    override suspend fun clearStaging() {
        stagingArchiveDao.clearStaging()
    }
}
