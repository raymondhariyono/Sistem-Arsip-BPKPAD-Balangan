package com.example.arsipbpkpad.data.repository

import com.example.arsipbpkpad.data.local.dao.StagingArchiveDao
import com.example.arsipbpkpad.data.mapper.toDomain
import com.example.arsipbpkpad.data.mapper.toEntity
import com.example.arsipbpkpad.data.mapper.toStagingEntity
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.StagedBox
import com.example.arsipbpkpad.domain.repository.StagingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StagingRepositoryImpl @Inject constructor(
    private val stagingArchiveDao: StagingArchiveDao
) : StagingRepository {

    override fun getAllStagedBoxes(): Flow<List<StagedBox>> {
        return combine(
            stagingArchiveDao.getAllStagingBoxes(),
            stagingArchiveDao.getAllStagingArchives()
        ) { boxes, archives ->
            boxes.map { boxEntity ->
                val count = archives.count { it.boxSessionId == boxEntity.sessionId }
                boxEntity.toDomain(count)
            }
        }
    }

    override suspend fun saveStagedBox(box: StagedBox) {
        stagingArchiveDao.insertStagingBox(box.toEntity())
    }

    override suspend fun deleteStagedBox(sessionId: String) {
        stagingArchiveDao.deleteStagingBox(sessionId)
        stagingArchiveDao.clearStagingBySession(sessionId)
    }

    override suspend fun getStagedBoxById(sessionId: String): StagedBox? {
        return stagingArchiveDao.getStagingBoxById(sessionId)?.toDomain()
    }

    override fun getAllStagingArchives(): Flow<List<ArchiveDocument>> {
        return stagingArchiveDao.getAllStagingArchives().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getStagingArchivesBySession(sessionId: String): Flow<List<ArchiveDocument>> {
        return stagingArchiveDao.getStagingArchivesBySession(sessionId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertToStaging(archive: ArchiveDocument) {
        stagingArchiveDao.insertToStaging(archive.toStagingEntity())
    }

    override suspend fun deleteFromStaging(id: String) {
        stagingArchiveDao.deleteFromStaging(id)
    }

    override suspend fun clearStagingBySession(sessionId: String) {
        stagingArchiveDao.clearStagingBySession(sessionId)
    }

    override suspend fun clearAllStaging() {
        stagingArchiveDao.clearAllStaging()
    }
}
