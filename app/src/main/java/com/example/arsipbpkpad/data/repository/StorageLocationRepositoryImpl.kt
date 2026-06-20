package com.example.arsipbpkpad.data.repository

import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.data.remote.dto.StorageLocationDto
import com.example.arsipbpkpad.domain.repository.StorageLocationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject

class StorageLocationRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : StorageLocationRepository {

    override suspend fun getOrCreateLocation(
        room: String,
        shelf: String,
        boxNumber: String,
        year: String
    ): ResultState<String> {
        return try {
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
                return ResultState.Success(existing.id)
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

            ResultState.Success(inserted.id!!)
        } catch (e: Exception) {
            val userFriendlyError = com.example.arsipbpkpad.utils.handleNetworkError(e.message)
            ResultState.Error(userFriendlyError)
        }
    }
}
