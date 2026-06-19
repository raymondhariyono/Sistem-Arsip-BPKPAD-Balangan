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
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class ArchiveRepositoryImpl @Inject constructor(
    private val archiveDao: ArchiveDao,
    private val classificationCodeDao: com.example.arsipbpkpad.data.local.dao.ClassificationCodeDao,
    private val supabaseClient: SupabaseClient
) : ArchiveRepository {

    private val tableName = "archive_documents"
    private val classificationTableName = "archive_classifications"

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
                syncPendingArchives()
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
                syncPendingArchives()
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

    override suspend fun checkDocumentNumberAndTypeExists(docNumber: String, copyType: String): Boolean {
        return archiveDao.existsByDocumentNumberAndType(docNumber, copyType)
    }

    override suspend fun checkDocumentNumberExists(docNumber: String): Boolean {
        return archiveDao.existsByDocumentNumber(docNumber)
    }

    override suspend fun saveArchive(archive: ArchiveDocument): ResultState<Unit> {
        return try {
            // 1. Save locally as DRAFT (or keep existing if update)
            val entity = archive.toEntity(syncStatus = "DRAFT")
            archiveDao.insertArchive(entity)

            try {
                // 2. Push to Supabase
                val dto = archive.toDto()
                supabaseClient.postgrest[tableName].upsert(dto)

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

    override suspend fun saveArchives(archives: List<ArchiveDocument>): ResultState<Unit> {
        return try {
            // 1. Save all locally as DRAFT
            val entities = archives.map { it.toEntity(syncStatus = "DRAFT") }
            archiveDao.insertArchives(entities)

            try {
                // 2. Push to Supabase
                val dtos = archives.map { it.toDto() }
                supabaseClient.postgrest[tableName].upsert(dtos)

                // 3. Update local status to SYNCED
                val syncedEntities = entities.map { it.copy(syncStatus = "SYNCED") }
                archiveDao.insertArchives(syncedEntities)
                ResultState.Success(Unit)
            } catch (e: Exception) {
                ResultState.Error("Gagal mengirim data ke server (tersimpan lokal sebagai draft): ${e.message}")
            }
        } catch (e: Exception) {
            ResultState.Error("Gagal menyimpan data bulk: ${e.message}")
        }
    }

    override suspend fun deleteArchive(id: String): ResultState<Unit> {
        return try {
            // 1. Delete locally
            archiveDao.deleteArchiveById(id)

            // 2. Delete from Supabase
            supabaseClient.postgrest[tableName].delete {
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
            val response = supabaseClient.postgrest[tableName]
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

    override suspend fun syncPendingArchives(): ResultState<Unit> {
        return try {
            val pendingEntities = archiveDao.getPendingArchives()
            if (pendingEntities.isEmpty()) return ResultState.Success(Unit)

            val dtos = pendingEntities.map { it.toDomain().toDto() }
            supabaseClient.postgrest[tableName].upsert(dtos)

            val syncedEntities = pendingEntities.map { it.copy(syncStatus = "SYNCED") }
            archiveDao.insertArchives(syncedEntities)

            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error("Sync pending failed: ${e.message}")
        }
    }

    override fun getArchivedYears(): Flow<List<Int>> {
        return archiveDao.getArchivedYears()
    }

    override fun getYearStats(): Flow<List<com.example.arsipbpkpad.domain.model.YearStats>> {
        return archiveDao.getYearStats().map { entities ->
            entities.map { 
                com.example.arsipbpkpad.domain.model.YearStats(
                    year = it.year,
                    count = it.count,
                    lastUpdated = it.lastUpdated
                )
            }
        }
    }

    override fun getArchivesByBundleId(bundleId: String): Flow<List<ArchiveDocument>> {
        return archiveDao.getArchivesByBundleId(bundleId).map { entities ->
            entities.map { it.toDomain() }
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

    override suspend fun uploadImage(id: String, imageByteArray: ByteArray): ResultState<String> {
        return try {
            val fileName = "$id.jpg"
            val bucket = supabaseClient.storage["archive-covers"]
            bucket.upload(fileName, imageByteArray) {
                upsert = true
            }
            val publicUrl = bucket.publicUrl(fileName)
            ResultState.Success(publicUrl)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Failed to upload image")
        }
    }

    override suspend fun syncClassificationCodes(): ResultState<Unit> {
        return try {
            val count = classificationCodeDao.getCodesCount()
            if (count > 0) {
                return ResultState.Success(Unit)
            }

            val dtos = supabaseClient.postgrest[classificationTableName]
                .select()
                .decodeList<com.example.arsipbpkpad.data.remote.dto.ClassificationCodeDto>()
            
            val entities = dtos.map { 
                com.example.arsipbpkpad.data.local.entity.ClassificationCodeEntity(
                    code = it.code,
                    name = it.name,
                    parentCode = it.parentCode,
                    level = it.level,
                    isActive = it.isActive
                )
            }
            
            classificationCodeDao.insertAll(entities)
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error("Sync classification codes failed: ${e.message}")
        }
    }

    override fun observeClassificationCodes(): Flow<List<com.example.arsipbpkpad.domain.model.ClassificationCode>> {
        return classificationCodeDao.getAllActiveCodes().map { entities ->
            entities.map { 
                com.example.arsipbpkpad.domain.model.ClassificationCode(
                    code = it.code,
                    name = it.name,
                    parentCode = it.parentCode,
                    level = it.level,
                    isActive = it.isActive
                )
            }
        }
    }
}
