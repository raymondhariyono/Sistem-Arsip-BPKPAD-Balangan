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
    val shelves: ShelfWithRoomDto
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
                .select(Columns.raw("*, shelves(*, rooms(*))"))
                .decodeList<BoxWithDetailsDto>()
            emit(ResultState.Success(dtos.map { 
                BoxDetails(
                    id = it.id,
                    name = it.name,
                    shelfId = it.shelf_id,
                    shelfName = it.shelves.name,
                    roomId = it.shelves.room_id,
                    roomName = it.shelves.rooms.name
                )
            }))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(ioDispatcher)

    override suspend fun createRoom(name: String): Result<Room> = runCatching {
        val dto = RoomDto(name = name)
        val inserted = supabaseClient.postgrest["rooms"]
            .insert(dto) { select() }
            .decodeSingle<RoomDto>()
        Room(inserted.id!!, inserted.name)
    }

    override suspend fun createShelf(roomId: String, name: String): Result<Shelf> = runCatching {
        val dto = ShelfDto(roomId = roomId, name = name)
        val inserted = supabaseClient.postgrest["shelves"]
            .insert(dto) { select() }
            .decodeSingle<ShelfDto>()
        Shelf(inserted.id!!, inserted.roomId, inserted.name)
    }

    override suspend fun createBox(shelfId: String, name: String): Result<Box> = runCatching {
        val dto = BoxDto(shelfId = shelfId, name = name)
        val inserted = supabaseClient.postgrest["boxes"]
            .insert(dto) { select() }
            .decodeSingle<BoxDto>()
        Box(inserted.id!!, inserted.shelfId, inserted.name)
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
