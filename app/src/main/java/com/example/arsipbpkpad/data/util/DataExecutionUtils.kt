package com.example.arsipbpkpad.data.util

import com.example.arsipbpkpad.domain.model.DomainResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Utility to execute a database call safely and wrap the result in DomainResult.
 */
suspend fun <T> safeDbCall(
    dispatcher: CoroutineDispatcher,
    call: suspend () -> T
): DomainResult<T> {
    return withContext(dispatcher) {
        try {
            DomainResult.Success(call())
        } catch (e: Exception) {
            DomainResult.Error(e.message ?: "Database error", e)
        }
    }
}

/**
 * Utility to execute an API call safely and wrap the result in DomainResult.
 */
suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    call: suspend () -> T
): DomainResult<T> {
    return withContext(dispatcher) {
        try {
            DomainResult.Success(call())
        } catch (e: Exception) {
            // Map common network exceptions to friendly messages if needed
            DomainResult.Error(e.message ?: "Network error", e)
        }
    }
}
