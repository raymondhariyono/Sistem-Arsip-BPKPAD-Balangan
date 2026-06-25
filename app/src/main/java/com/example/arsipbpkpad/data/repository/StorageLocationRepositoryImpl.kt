package com.example.arsipbpkpad.data.repository

import com.example.arsipbpkpad.data.remote.dto.BoxDto
import com.example.arsipbpkpad.data.remote.dto.RoomDto
import com.example.arsipbpkpad.data.remote.dto.ShelfDto
import com.example.arsipbpkpad.data.util.safeApiCall
import com.example.arsipbpkpad.domain.model.Box
import com.example.arsipbpkpad.domain.model.BoxDetails
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.model.Room
import com.example.arsipbpkpad.domain.model.Shelf
import com.example.arsipbpkpad.domain.repository.StorageLocationRepository
import com.example.arsipbpkpad.utils.ResultState
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Named

@Serializable
data class BoxWithDetailsDto(
    val id: String,
    val name: String,
    val shelf_id: String,
    val shelves: ShelfWithRoomDto,
    val archive_documents: List<ArchiveCountDto> = emptyList()
)

@Serializable
data class ArchiveCountDto(
    val count: Int = 0
)

@Serializable
data class ShelfWithRoomDto(
    val id: String,
    val name: String,
    val room_id: String,
    val rooms: RoomDto
)

/**
 * Implementation of StorageLocationRepository using Supabase.
 */
class StorageLocationRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val activityLogRepository: com.example.arsipbpkpad.domain.repository.ActivityLogRepository,
    private val authRepository: com.example.arsipbpkpad.domain.repository.AuthRepository,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : StorageLocationRepository {

    override suspend fun getOrCreateLocation(
        room: String,
        shelf: String,
        boxNumber: String,
        year: String
    ): DomainResult<String> {
        return safeApiCall(ioDispatcher) {
            // 1. Resolve Room
            val roomObj = getRoomByName(room) ?: createRoom(room).getOrThrow()
            
            // 2. Resolve Shelf
            val shelfObj = getShelfByName(roomObj.id, shelf) ?: createShelf(roomObj.id, shelf).getOrThrow()
            
            // 3. Resolve Box
            val existingBox = supabaseClient.postgrest["boxes"]
                .select {
                    filter {
                        eq("shelf_id", shelfObj.id)
                        eq("name", boxNumber)
                    }
                }
                .decodeSingleOrNull<BoxDto>()
            
            if (existingBox != null) {
                return@safeApiCall existingBox.id!!
            }
            
            val newBox = createBox(shelfObj.id, boxNumber).getOrThrow()
            newBox.id
        }
    }

    override fun getRooms(): Flow<ResultState<List<Room>>> = flow {
        emit(ResultState.Loading)
        try {
            val dtos = supabaseClient.postgrest["rooms"].select().decodeList<RoomDto>()
            emit(ResultState.Success(dtos.map { Room(it.id!!, it.name) }))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(ioDispatcher)

    override fun getShelvesByRoom(roomId: String): Flow<ResultState<List<Shelf>>> = flow {
        emit(ResultState.Loading)
        try {
            val dtos = supabaseClient.postgrest["shelves"]
                .select { filter { eq("room_id", roomId) } }
                .decodeList<ShelfDto>()
            emit(ResultState.Success(dtos.map { Shelf(it.id!!, it.roomId, it.name) }))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(ioDispatcher)

    override fun getBoxesByShelf(shelfId: String): Flow<ResultState<List<Box>>> = flow {
        emit(ResultState.Loading)
        try {
            val dtos = supabaseClient.postgrest["boxes"]
                .select { filter { eq("shelf_id", shelfId) } }
                .decodeList<BoxDto>()
            emit(ResultState.Success(dtos.map { Box(it.id!!, it.shelfId, it.name) }))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(ioDispatcher)

    override fun getAllBoxes(): Flow<ResultState<List<BoxDetails>>> = flow {
        emit(ResultState.Loading)
        try {
            val dtos = supabaseClient.postgrest["boxes"]
                .select(Columns.raw("*, shelves(*, rooms(*)), archive_documents(count)"))
                .decodeList<BoxWithDetailsDto>()
            emit(ResultState.Success(dtos.map { 
                BoxDetails(
                    id = it.id,
                    name = it.name,
                    shelfId = it.shelf_id,
                    shelfName = it.shelves.name,
                    roomId = it.shelves.room_id,
                    roomName = it.shelves.rooms.name,
                    itemCount = it.archive_documents.firstOrNull()?.count ?: 0
                )
            }))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(ioDispatcher)

    override suspend fun createRoom(name: String): Result<Room> = runCatching {
        val dto = RoomDto(
            name = name,
            createdBy = authRepository.getCurrentUserId()
        )
        val inserted = supabaseClient.postgrest["rooms"]
            .insert(dto) { select() }
            .decodeSingle<RoomDto>()
        
        val userId = authRepository.getCurrentUserId()
        val userEmail = authRepository.getCurrentUserEmail() ?: "Unknown User"
        activityLogRepository.logActivity(
            com.example.arsipbpkpad.domain.model.ActivityLog(
                actorId = userId,
                action = "CREATE",
                entityType = "LOCATION_ROOM",
                entityId = inserted.id!!,
                details = "User: $userEmail | Created Room: $name"
            )
        )
        Room(inserted.id!!, inserted.name)
    }

    override suspend fun updateRoom(id: String, name: String): Result<Unit> = runCatching {
        supabaseClient.postgrest["rooms"].update({
            "name" to name
        }) {
            filter { eq("id", id) }
        }
        
        val userId = authRepository.getCurrentUserId()
        val userEmail = authRepository.getCurrentUserEmail() ?: "Unknown User"
        activityLogRepository.logActivity(
            com.example.arsipbpkpad.domain.model.ActivityLog(
                actorId = userId,
                action = "UPDATE",
                entityType = "LOCATION_ROOM",
                entityId = id,
                details = "User: $userEmail | Updated Room Name to: $name"
            )
        )
    }

    override suspend fun deleteRoom(id: String): Result<Unit> = runCatching {
        supabaseClient.postgrest["rooms"].delete {
            filter { eq("id", id) }
        }
        
        val userId = authRepository.getCurrentUserId()
        val userEmail = authRepository.getCurrentUserEmail() ?: "Unknown User"
        activityLogRepository.logActivity(
            com.example.arsipbpkpad.domain.model.ActivityLog(
                actorId = userId,
                action = "DELETE",
                entityType = "LOCATION_ROOM",
                entityId = id,
                details = "User: $userEmail | Deleted Room ID: $id"
            )
        )
    }

    override suspend fun createShelf(roomId: String, name: String): Result<Shelf> = runCatching {
        val dto = ShelfDto(
            roomId = roomId,
            name = name,
            createdBy = authRepository.getCurrentUserId()
        )
        val inserted = supabaseClient.postgrest["shelves"]
            .insert(dto) { select() }
            .decodeSingle<ShelfDto>()
        
        val userId = authRepository.getCurrentUserId()
        val userEmail = authRepository.getCurrentUserEmail() ?: "Unknown User"
        activityLogRepository.logActivity(
            com.example.arsipbpkpad.domain.model.ActivityLog(
                actorId = userId,
                action = "CREATE",
                entityType = "LOCATION_SHELF",
                entityId = inserted.id!!,
                details = "User: $userEmail | Created Shelf: $name in Room ID: $roomId"
            )
        )
        Shelf(inserted.id!!, inserted.roomId, inserted.name)
    }

    override suspend fun updateShelf(id: String, name: String): Result<Unit> = runCatching {
        supabaseClient.postgrest["shelves"].update({
            "name" to name
        }) {
            filter { eq("id", id) }
        }
        
        val userId = authRepository.getCurrentUserId()
        val userEmail = authRepository.getCurrentUserEmail() ?: "Unknown User"
        activityLogRepository.logActivity(
            com.example.arsipbpkpad.domain.model.ActivityLog(
                actorId = userId,
                action = "UPDATE",
                entityType = "LOCATION_SHELF",
                entityId = id,
                details = "User: $userEmail | Updated Shelf Name to: $name"
            )
        )
    }

    override suspend fun deleteShelf(id: String): Result<Unit> = runCatching {
        supabaseClient.postgrest["shelves"].delete {
            filter { eq("id", id) }
        }
        
        val userId = authRepository.getCurrentUserId()
        val userEmail = authRepository.getCurrentUserEmail() ?: "Unknown User"
        activityLogRepository.logActivity(
            com.example.arsipbpkpad.domain.model.ActivityLog(
                actorId = userId,
                action = "DELETE",
                entityType = "LOCATION_SHELF",
                entityId = id,
                details = "User: $userEmail | Deleted Shelf ID: $id"
            )
        )
    }

    override suspend fun createBox(shelfId: String, name: String): Result<Box> = runCatching {
        val dto = BoxDto(
            shelfId = shelfId,
            name = name,
            createdBy = authRepository.getCurrentUserId()
        )
        val inserted = supabaseClient.postgrest["boxes"]
            .insert(dto) { select() }
            .decodeSingle<BoxDto>()
        
        val userId = authRepository.getCurrentUserId()
        val userEmail = authRepository.getCurrentUserEmail() ?: "Unknown User"
        activityLogRepository.logActivity(
            com.example.arsipbpkpad.domain.model.ActivityLog(
                actorId = userId,
                action = "CREATE",
                entityType = "LOCATION_BOX",
                entityId = inserted.id!!,
                details = "User: $userEmail | Created Box: $name in Shelf ID: $shelfId"
            )
        )
        Box(inserted.id!!, inserted.shelfId, inserted.name)
    }

    override suspend fun updateBox(id: String, name: String): Result<Unit> = runCatching {
        supabaseClient.postgrest["boxes"].update({
            "name" to name
        }) {
            filter { eq("id", id) }
        }
        
        val userId = authRepository.getCurrentUserId()
        val userEmail = authRepository.getCurrentUserEmail() ?: "Unknown User"
        activityLogRepository.logActivity(
            com.example.arsipbpkpad.domain.model.ActivityLog(
                actorId = userId,
                action = "UPDATE",
                entityType = "LOCATION_BOX",
                entityId = id,
                details = "User: $userEmail | Updated Box Name to: $name"
            )
        )
    }

    override suspend fun deleteBox(id: String): Result<Unit> = runCatching {
        supabaseClient.postgrest["boxes"].delete {
            filter { eq("id", id) }
        }
        
        val userId = authRepository.getCurrentUserId()
        val userEmail = authRepository.getCurrentUserEmail() ?: "Unknown User"
        activityLogRepository.logActivity(
            com.example.arsipbpkpad.domain.model.ActivityLog(
                actorId = userId,
                action = "DELETE",
                entityType = "LOCATION_BOX",
                entityId = id,
                details = "User: $userEmail | Deleted Box ID: $id"
            )
        )
    }

    override suspend fun getRoomByName(name: String): Room? {
        return try {
            val dto = supabaseClient.postgrest["rooms"]
                .select { filter { eq("name", name) } }
                .decodeSingleOrNull<RoomDto>()
            dto?.let { Room(it.id!!, it.name) }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getShelfByName(roomId: String, name: String): Shelf? {
        return try {
            val dto = supabaseClient.postgrest["shelves"]
                .select { 
                    filter { 
                        eq("room_id", roomId)
                        eq("name", name)
                    } 
                }
                .decodeSingleOrNull<ShelfDto>()
            dto?.let { Shelf(it.id!!, it.roomId, it.name) }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun checkBoxExists(shelfId: String, name: String): Boolean {
        return try {
            val dto = supabaseClient.postgrest["boxes"]
                .select { 
                    filter { 
                        eq("shelf_id", shelfId)
                        eq("name", name)
                    } 
                }
                .decodeSingleOrNull<BoxDto>()
            dto != null
        } catch (e: Exception) {
            false
        }
    }
}
