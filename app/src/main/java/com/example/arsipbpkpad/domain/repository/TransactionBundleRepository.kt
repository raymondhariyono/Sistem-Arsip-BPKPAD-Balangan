package com.example.arsipbpkpad.domain.repository

import com.example.arsipbpkpad.domain.model.DomainResult

/**
 * Interface for transaction bundle operations.
 */
interface TransactionBundleRepository {
    suspend fun createBundle(
        description: String?,
        documentType: String,
        year: Int
    ): DomainResult<String>
}
