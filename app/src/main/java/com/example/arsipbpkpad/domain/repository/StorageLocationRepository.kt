package com.example.arsipbpkpad.domain.repository

import com.example.arsipbpkpad.domain.model.Box
import com.example.arsipbpkpad.domain.model.BoxDetails
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.model.Room
import com.example.arsipbpkpad.domain.model.Shelf
import com.example.arsipbpkpad.utils.ResultState
import kotlinx.coroutines.flow.Flow

/**
 * Interface for storage location operations.
 */
interface StorageLocationRepository {
    suspend fun getOrCreateLocation(
        room: String,
        shelf: String,
        boxNumber: String,
        year: String
    ): DomainResult<String>

    fun getRooms(): Flow<ResultState<List<Room>>>
    fun getShelvesByRoom(roomId: String): Flow<ResultState<List<Shelf>>>
    fun getBoxesByShelf(shelfId: String): Flow<ResultState<List<Box>>>
    fun getAllBoxes(): Flow<ResultState<List<BoxDetails>>>

    suspend fun createRoom(name: String): Result<Room>
    suspend fun createShelf(roomId: String, name: String): Result<Shelf>
    suspend fun createBox(shelfId: String, name: String): Result<Box>
    
    suspend fun getRoomByName(name: String): Room?
    suspend fun getShelfByName(roomId: String, name: String): Shelf?
    suspend fun checkBoxExists(shelfId: String, name: String): Boolean
}
