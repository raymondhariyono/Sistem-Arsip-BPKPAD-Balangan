package com.example.arsipbpkpad.data.repository

import com.example.arsipbpkpad.data.remote.dto.StorageLocationDto
import com.example.arsipbpkpad.data.util.safeApiCall
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.StorageLocationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Named

/**
 * Implementation of StorageLocationRepository using Supabase.
 */
class StorageLocationRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : StorageLocationRepository {

    override suspend fun getOrCreateLocation(
        room: String,
        shelf: String,
        boxNumber: String,
        year: String
    ): DomainResult<String> {
        return safeApiCall(ioDispatcher) {
            // 1. Check if exists
            val existing = supabaseClient.postgrest["storage_locations"]
                .select(columns = Columns.list("id")) {
                    filter {
                        eq("room", room)
                        eq("shelf", shelf)
                        eq("box_number", boxNumber)
                    }
                }
                .decodeSingleOrNull<StorageLocationDto>()

            if (existing?.id != null) {
                return@safeApiCall existing.id
            }

            // 2. Create if not exists
            val newLocation = StorageLocationDto(
                room = room,
                shelf = shelf,
                boxNumber = boxNumber,
                description = "Box $boxNumber - Rak $shelf ($year)"
            )

            val inserted = supabaseClient.postgrest["storage_locations"]
                .insert(newLocation) {
                    select()
                }
                .decodeSingle<StorageLocationDto>()

            inserted.id!!
        }
    }
}
