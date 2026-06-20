package com.example.arsipbpkpad.domain.repository

import com.example.arsipbpkpad.domain.model.DomainResult

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
}
