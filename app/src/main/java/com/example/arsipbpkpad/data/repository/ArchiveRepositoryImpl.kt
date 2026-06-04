package com.example.arsipbpkpad.data.repository

import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.data.local.dao.ArchiveDao
import com.example.arsipbpkpad.data.mapper.toDomain
import com.example.arsipbpkpad.data.mapper.toDto
import com.example.arsipbpkpad.data.mapper.toEntity
import com.example.arsipbpkpad.data.remote.dto.ArchiveDto
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class ArchiveRepositoryImpl @Inject constructor(
    private val archiveDao: ArchiveDao,
    private val supabaseClient: SupabaseClient
) : ArchiveRepository {

    override fun getArchives(query: String?): Flow<ResultState<List<ArchiveDocument>>> {
        return archiveDao.getArchives(query)
            .map { entities ->
                val documents = entities.map { it.toDomain() }
                ResultState.Success(documents) as ResultState<List<ArchiveDocument>>
            }
            .onStart {
                emit(ResultState.Loading)
                // Trigger background sync
                syncArchives()
            }
            .catch { e ->
                emit(ResultState.Error(e.message ?: "Unknown Error"))
            }
    }

    override fun getArchiveDetail(id: String): Flow<ResultState<ArchiveDocument>> {
        return archiveDao.getArchiveById(id)
            .map { entity ->
                if (entity != null) {
                    ResultState.Success(entity.toDomain())
                } else {
                    ResultState.Error("Archive not found")
                }
            }
            .onStart {
                emit(ResultState.Loading)
            }
            .catch { e ->
                emit(ResultState.Error(e.message ?: "Failed to fetch detail"))
            }
    }

    override suspend fun checkDocumentNumberExists(docNumber: String): Boolean {
        return archiveDao.existsByDocumentNumber(docNumber)
    }

    override suspend fun saveArchive(archive: ArchiveDocument): ResultState<Unit> {
        return try {
            // 1. Save locally as DRAFT
            val entity = archive.toEntity(syncStatus = "DRAFT")
            archiveDao.insertArchive(entity)

            // 2. Push to Supabase
            val dto = archive.toDto()
            supabaseClient.postgrest["arsip_keuangan"].upsert(dto)

            // 3. Update local status to SYNCED
            archiveDao.insertArchive(entity.copy(syncStatus = "SYNCED"))

            ResultState.Success(Unit)
        } catch (e: Exception) {
            // Keep as DRAFT locally for later sync
            ResultState.Error(e.message ?: "Failed to save archive to remote")
        }
    }

    override suspend fun deleteArchive(id: String): ResultState<Unit> {
        return try {
            // 1. Delete locally
            archiveDao.deleteArchiveById(id)

            // 2. Delete from Supabase
            supabaseClient.postgrest["arsip_keuangan"].delete {
                filter {
                    eq("id", id)
                }
            }

            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Failed to delete archive")
        }
    }

    override suspend fun syncArchives(): ResultState<Unit> {
        return try {
            val response = supabaseClient.postgrest["arsip_keuangan"]
                .select()
                .decodeList<ArchiveDto>()
            
            val entities = response.map { it.toDomain().toEntity() }
            
            // For simple offline-first, we can clear and insert or just insertAll with REPLACE strategy
            archiveDao.insertArchives(entities)
            
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Sync failed")
        }
    }
}
