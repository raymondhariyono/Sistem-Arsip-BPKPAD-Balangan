package com.example.arsipbpkpad.data.repository

import com.example.arsipbpkpad.data.remote.dto.ActivityLogDto
import com.example.arsipbpkpad.data.util.safeApiCall
import com.example.arsipbpkpad.domain.model.ActivityLog
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.ActivityLogRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Named

class ActivityLogRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : ActivityLogRepository {

    private val tableName = "activity_logs"

    override suspend fun logActivity(log: ActivityLog): DomainResult<Unit> {
        android.util.Log.i("ActivityLogRepo", "INSERT_START: action=${log.action}, entityId=${log.entityId}, actorId=${log.actorId}")
        return safeApiCall(ioDispatcher) {
            val dto = ActivityLogDto(
                actorId = log.actorId,
                action = log.action,
                entityType = log.entityType,
                entityId = log.entityId,
                metadata = buildJsonObject {
                    put("details", log.details ?: "")
                }
            )
            supabaseClient.postgrest[tableName].insert(dto)
            android.util.Log.i("ActivityLogRepo", "INSERT_SUCCESS")
            Unit
        }
    }

    override fun getActivityLogsForEntity(
        entityId: String,
        entityType: String
    ): kotlinx.coroutines.flow.Flow<DomainResult<List<ActivityLog>>> = kotlinx.coroutines.flow.flow {
        android.util.Log.i("ActivityLogRepo", "FETCH_START: entityId=$entityId, type=$entityType")
        
        val result = safeApiCall(ioDispatcher) {
            val response = supabaseClient.postgrest[tableName]
                .select {
                    filter {
                        eq("entity_id", entityId)
                    }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
            
            // Log the raw JSON to see exactly what columns are coming back
            val rawJson = response.data
            android.util.Log.i("ActivityLogRepo", "RAW_RESPONSE: $rawJson")
            
            val decoded = response.decodeList<ActivityLogDto>()
            android.util.Log.i("ActivityLogRepo", "FETCH_SUCCESS_RAW: found ${decoded.size} total logs for ID")
            
            // Client-side filter with deep debugging
            val filtered = decoded.filter { dto ->
                val type = dto.entityType ?: "NULL"
                android.util.Log.i("ActivityLogRepo", "DEBUG_TYPE_FOUND: '$type' (ID matches: ${dto.entityId == entityId})")
                type.equals(entityType, ignoreCase = true)
            }
            
            android.util.Log.i("ActivityLogRepo", "FETCH_FILTERED: found ${filtered.size} logs after type filter ($entityType)")
            
            filtered.map { dto ->
                // Parse details from metadata JSON
                val detailsFromMetadata = when (val meta = dto.metadata) {
                    is JsonObject -> {
                        // Check if it's our manual log with "details" key
                        val manualDetails = meta["details"]?.jsonPrimitive?.contentOrNull
                        if (manualDetails != null) {
                            manualDetails
                        } else {
                            // It's likely a trigger-generated log with archive fields
                            val docNum = meta["document_number"]?.jsonPrimitive?.contentOrNull ?: "-"
                            val desc = meta["description"]?.jsonPrimitive?.contentOrNull ?: ""
                            val actorId = dto.actorId?.take(8) ?: "System"
                            "Actor: $actorId | Doc: $docNum | $desc"
                        }
                    }
                    is JsonPrimitive -> meta.content
                    else -> "No details available"
                }

                ActivityLog(
                    id = dto.id ?: "",
                    actorId = dto.actorId,
                    action = dto.action,
                    entityType = dto.entityType ?: "",
                    entityId = dto.entityId ?: "",
                    details = detailsFromMetadata,
                    timestamp = dto.createdAt
                )
            }
        }
        
        if (result is DomainResult.Error) {
            android.util.Log.e("ActivityLogRepo", "FETCH_ERROR: ${result.message}")
        }

        emit(result)
    }
}
