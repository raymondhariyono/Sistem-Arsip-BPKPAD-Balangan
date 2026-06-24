package com.example.arsipbpkpad.data.repository

import com.example.arsipbpkpad.data.remote.dto.ActivityLogDto
import com.example.arsipbpkpad.data.util.safeApiCall
import com.example.arsipbpkpad.domain.model.ActivityLog
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.ActivityLogRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Named

class ActivityLogRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : ActivityLogRepository {

    private val tableName = "activity_logs"

    override suspend fun logActivity(log: ActivityLog): DomainResult<Unit> {
        return safeApiCall(ioDispatcher) {
            val dto = ActivityLogDto(
                actorId = log.actorId,
                action = log.action,
                entityType = log.entityType,
                entityId = log.entityId,
                details = log.details
            )
            supabaseClient.postgrest[tableName].insert(dto)
            Unit
        }
    }
}
