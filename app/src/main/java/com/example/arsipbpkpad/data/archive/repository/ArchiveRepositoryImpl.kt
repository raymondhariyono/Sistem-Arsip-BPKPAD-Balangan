package com.example.arsipbpkpad.data.archive.repository

import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.data.local.dao.ArchiveDao
import com.example.arsipbpkpad.data.mapper.toDomain
import com.example.arsipbpkpad.data.mapper.toEntity
import com.example.arsipbpkpad.domain.archive.model.ArchiveDocument
import com.example.arsipbpkpad.domain.archive.repository.ArchiveRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class ArchiveRepositoryImpl @Inject constructor(
    private val archiveDao: ArchiveDao
) : ArchiveRepository {
    override fun getArchives(): Flow<ResultState<List<ArchiveDocument>>> {
        return archiveDao.getArchives()
            .map { entities ->
                val documents = entities.map { it.toDomain() }
                ResultState.Success(documents) as ResultState<List<ArchiveDocument>>
            }
            .onStart {
                emit(ResultState.Loading)
            }
            .catch { e ->
                emit(ResultState.Error(e.message ?: "Unknown Error"))
            }
    }

    override suspend fun saveArchive(archive: ArchiveDocument): ResultState<Unit> {
        return try {
            archiveDao.insertArchive(archive.toEntity())
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Failed to save archive")
        }
    }

    override fun getArchiveDetail(id: String): Flow<ResultState<ArchiveDocument>> = flow {
        emit(ResultState.Loading)
        // Simulate detail fetching
        val mockDetail = ArchiveDocument(id, "SP2D-2023-11-0045", "Surat Perintah Pencairan Dana (Dinas Pendidikan)", "2023-11-12", "Dinas Pendidikan")
        emit(ResultState.Success(mockDetail))
    }
}
