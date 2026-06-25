package com.example.arsipbpkpad.data.repository

import com.example.arsipbpkpad.data.local.dao.ArchiveDao
import com.example.arsipbpkpad.data.local.dao.ClassificationCodeDao
import com.example.arsipbpkpad.data.local.dao.ClassificationBudget
import com.example.arsipbpkpad.data.mapper.toDomain
import com.example.arsipbpkpad.data.mapper.toDto
import com.example.arsipbpkpad.data.mapper.toEntity
import com.example.arsipbpkpad.data.remote.dto.ArchiveDto
import com.example.arsipbpkpad.data.remote.dto.ClassificationCodeDto
import com.example.arsipbpkpad.data.util.safeApiCall
import com.example.arsipbpkpad.data.util.safeDbCall
import com.example.arsipbpkpad.domain.model.DomainConstants
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.ClassificationCode
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.model.YearStats
import com.example.arsipbpkpad.domain.model.AnalyticsData
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Named

/**
 * Implementation of ArchiveRepository.
 * Follows Stage 2 rules: strict separation, safe calls, and mapping.
 */
class ArchiveRepositoryImpl @Inject constructor(
    private val archiveDao: ArchiveDao,
    private val classificationCodeDao: ClassificationCodeDao,
    private val supabaseClient: SupabaseClient,
    private val activityLogRepository: com.example.arsipbpkpad.domain.repository.ActivityLogRepository,
    private val authRepository: com.example.arsipbpkpad.domain.repository.AuthRepository,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : ArchiveRepository {

    private val tableName = "archive_documents"
    private val classificationTableName = "archive_classifications"

    override fun getArchivesFlow(query: String?, years: List<Int>): Flow<List<ArchiveDocument>> {
        return archiveDao.getArchivesList(query, years, years.isEmpty())
            .map { entities -> entities.map { it.toDomain() } }
            .onEach {
                // Background sync trigger (could be moved to a WorkManager or separate trigger)
                syncPendingArchives()
            }
    }

    override fun getArchivesList(query: String?, years: List<Int>): Flow<DomainResult<List<ArchiveDocument>>> {
        return archiveDao.getArchivesList(query, years, years.isEmpty())
            .map { entities -> 
                DomainResult.Success(entities.map { it.toDomain() }) as DomainResult<List<ArchiveDocument>>
            }
    }

    override fun getArchiveDetail(id: String): Flow<DomainResult<ArchiveDocument>> {
        return archiveDao.getArchiveById(id).map { entity ->
            if (entity != null) DomainResult.Success(entity.toDomain())
            else DomainResult.Error(DomainConstants.ERROR_ARCHIVE_NOT_FOUND)
        }
    }

    override suspend fun checkDocumentNumberAndTypeExists(docNumber: String, copyType: String): Boolean {
        return archiveDao.existsByDocumentNumberAndType(docNumber, copyType)
    }

    override suspend fun checkDocumentNumberExists(docNumber: String): Boolean {
        return archiveDao.existsByDocumentNumber(docNumber)
    }

    override suspend fun saveArchive(archive: ArchiveDocument): DomainResult<Boolean> {
        return safeDbCall(ioDispatcher) {
            var isSynced = false
            val isUpdate = archiveDao.getArchiveByIdSync(archive.id) != null
            val entity = archive.toEntity(syncStatus = "DRAFT")
            archiveDao.insertArchive(entity)
            
            // Try to push to remote
            val apiResult = safeApiCall(ioDispatcher) {
                supabaseClient.postgrest[tableName].upsert(archive.toDto())
            }
            
            if (apiResult is DomainResult.Success) {
                isSynced = true
                archiveDao.insertArchive(entity.copy(syncStatus = "SYNCED"))
                
                // Log Activity
                val userId = authRepository.getCurrentUserId()
                val userEmail = authRepository.getCurrentUserEmail() ?: "Unknown User"
                activityLogRepository.logActivity(
                    com.example.arsipbpkpad.domain.model.ActivityLog(
                        actorId = userId,
                        action = if (isUpdate) "UPDATE" else "CREATE",
                        entityType = "ARCHIVE",
                        entityId = archive.id,
                        details = "User: $userEmail | Doc: ${archive.documentNumber}"
                    )
                )
            }
            // We return Success(Boolean) if local save worked, Boolean tells if remote sync worked
            isSynced
        }
    }

    override suspend fun saveArchives(archives: List<ArchiveDocument>): DomainResult<Boolean> {
        return safeDbCall(ioDispatcher) {
            val entities = archives.map { it.toEntity(syncStatus = "DRAFT") }
            archiveDao.insertArchives(entities)

            val apiResult = safeApiCall(ioDispatcher) {
                supabaseClient.postgrest[tableName].upsert(archives.map { it.toDto() })
            }

            var isSynced = false
            if (apiResult is DomainResult.Success) {
                isSynced = true
                val syncedEntities = entities.map { it.copy(syncStatus = "SYNCED") }
                archiveDao.insertArchives(syncedEntities)

                // Log Activity for each document
                val userId = authRepository.getCurrentUserId()
                val userEmail = authRepository.getCurrentUserEmail() ?: "Unknown User"
                archives.forEach { archive ->
                    activityLogRepository.logActivity(
                        com.example.arsipbpkpad.domain.model.ActivityLog(
                            actorId = userId,
                            action = "UPSERT",
                            entityType = "ARCHIVE",
                            entityId = archive.id,
                            details = "User: $userEmail | Bulk Doc: ${archive.documentNumber}"
                        )
                    )
                }
            }
            isSynced
        }
    }

    override suspend fun deleteArchive(id: String): DomainResult<Unit> {
        val dbResult = safeDbCall(ioDispatcher) { archiveDao.deleteArchiveById(id) }
        if (dbResult is DomainResult.Error) return dbResult

        val apiResult = safeApiCall(ioDispatcher) {
            supabaseClient.postgrest[tableName].delete {
                filter { eq("id", id) }
            }
            Unit
        }

        if (apiResult is DomainResult.Success) {
            val userId = authRepository.getCurrentUserId()
            val userEmail = authRepository.getCurrentUserEmail() ?: "Unknown User"
            activityLogRepository.logActivity(
                com.example.arsipbpkpad.domain.model.ActivityLog(
                    actorId = userId,
                    action = "DELETE",
                    entityType = "ARCHIVE",
                    entityId = id,
                    details = "User: $userEmail | Deleted ID: $id"
                )
            )
        }
        return apiResult
    }

    override suspend fun syncArchives(): DomainResult<Unit> {
        val apiResult = safeApiCall(ioDispatcher) {
            supabaseClient.postgrest[tableName].select().decodeList<ArchiveDto>()
        }

        return when (apiResult) {
            is DomainResult.Success -> {
                safeDbCall(ioDispatcher) {
                    val entities = apiResult.data.map { it.toDomain().toEntity() }
                    archiveDao.insertArchives(entities)
                }
            }
            is DomainResult.Error -> apiResult
        }
    }

    override suspend fun syncPendingArchives(): DomainResult<Unit> {
        val pending = archiveDao.getPendingArchives()
        if (pending.isEmpty()) return DomainResult.Success(Unit)

        val apiResult = safeApiCall(ioDispatcher) {
            supabaseClient.postgrest[tableName].upsert(pending.map { it.toDomain().toDto() })
        }

        return when (apiResult) {
            is DomainResult.Success -> {
                safeDbCall(ioDispatcher) {
                    archiveDao.insertArchives(pending.map { it.copy(syncStatus = "SYNCED") })
                }
            }
            is DomainResult.Error -> apiResult
        }
    }

    override fun getArchivedYears(): Flow<List<Int>> = archiveDao.getArchivedYears()

    override fun getYearStats(): Flow<List<YearStats>> {
        return archiveDao.getYearStats().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getArchivesByBundleId(bundleId: String): Flow<List<ArchiveDocument>> {
        return archiveDao.getArchivesByBundleId(bundleId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAnalyticsData(year: Int): Flow<DomainResult<AnalyticsData>> {
        return combine(
            archiveDao.getTotalBudgetByYear(year),
            archiveDao.getBudgetByClassification(year)
        ) { total, classificationBudgets ->
            DomainResult.Success(
                AnalyticsData(
                    totalBudget = total ?: 0.0,
                    budgetByClassification = classificationBudgets.associate { it.classificationCode to it.total }
                )
            )
        }
    }

    override fun getAnalyticsDataForRange(startYear: Int, endYear: Int): Flow<DomainResult<AnalyticsData>> {
        return combine(
            archiveDao.getTotalBudgetForRange(startYear, endYear),
            archiveDao.getBudgetByClassificationForRange(startYear, endYear)
        ) { total, classificationBudgets ->
            DomainResult.Success(
                AnalyticsData(
                    totalBudget = total ?: 0.0,
                    budgetByClassification = classificationBudgets.associate { it.classificationCode to it.total }
                )
            )
        }
    }

    override suspend fun uploadImage(id: String, imageByteArray: ByteArray): DomainResult<String> {
        return safeApiCall(ioDispatcher) {
            val fileName = "$id.jpg"
            val bucket = supabaseClient.storage["archive-covers"]
            bucket.upload(fileName, imageByteArray) { upsert = true }
            bucket.publicUrl(fileName)
        }
    }

    override suspend fun syncClassificationCodes(): DomainResult<Unit> {
        val count = classificationCodeDao.getCodesCount()
        if (count > 0) return DomainResult.Success(Unit)

        val apiResult = safeApiCall(ioDispatcher) {
            supabaseClient.postgrest[classificationTableName].select().decodeList<ClassificationCodeDto>()
        }

        return when (apiResult) {
            is DomainResult.Success -> {
                safeDbCall(ioDispatcher) {
                    classificationCodeDao.insertAll(apiResult.data.map { it.toEntity() })
                }
            }
            is DomainResult.Error -> apiResult
        }
    }

    override fun observeClassificationCodes(): Flow<List<ClassificationCode>> {
        return classificationCodeDao.getAllActiveCodes().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
