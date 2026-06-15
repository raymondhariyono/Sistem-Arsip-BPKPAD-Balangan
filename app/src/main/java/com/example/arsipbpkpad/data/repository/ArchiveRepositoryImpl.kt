package com.example.arsipbpkpad.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class ArchiveRepositoryImpl @Inject constructor(
    private val archiveDao: ArchiveDao,
    private val supabaseClient: SupabaseClient
) : ArchiveRepository {

    override fun getArchives(query: String?, years: List<Int>): Flow<PagingData<ArchiveDocument>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { archiveDao.getArchives(query, years, years.isEmpty()) }
        ).flow
            .map { pagingData ->
                pagingData.map { it.toDomain() }
            }
            .onEach {
                // Background sync
                syncArchives()
            }
    }

    override fun getArchivesList(query: String?, years: List<Int>): Flow<ResultState<List<ArchiveDocument>>> {
        return archiveDao.getArchivesList(query, years, years.isEmpty())
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

    override suspend fun checkDocumentNumberAndStatusExists(docNumber: String, copyStatus: String): Boolean {
        return archiveDao.existsByDocumentNumberAndStatus(docNumber, copyStatus)
    }

    override suspend fun saveArchive(archive: ArchiveDocument): ResultState<Unit> {
        return try {
            // 1. Save locally as DRAFT
            val entity = archive.toEntity(syncStatus = "DRAFT")
            archiveDao.insertArchive(entity)

            try {
                // 2. Push to Supabase
                val dto = archive.toDto()
                supabaseClient.postgrest["arsip_keuangan"].upsert(dto)

                // 3. Update local status to SYNCED
                archiveDao.insertArchive(entity.copy(syncStatus = "SYNCED"))
                ResultState.Success(Unit)
            } catch (e: Exception) {
                // Keep as DRAFT locally for later sync, but notify user
                ResultState.Error("Gagal mengirim ke server (tersimpan sebagai draft lokal): ${e.message}")
            }
        } catch (e: Exception) {
            ResultState.Error("Gagal menyimpan data: ${e.message}")
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

    override fun getTotalBudgetByYear(year: Int): Flow<ResultState<Double>> {
        return archiveDao.getTotalBudgetByYear(year)
            .map { total ->
                ResultState.Success(total ?: 0.0) as ResultState<Double>
            }
            .onStart {
                emit(ResultState.Loading)
            }
            .catch { e ->
                emit(ResultState.Error(e.message ?: "Failed to fetch total budget"))
            }
    }
}
