package com.example.arsipbpkpad.domain.model

/**
 * A pure domain-layer result wrapper to avoid dependencies on framework or core modules.
 */
sealed class DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : DomainResult<Nothing>()
}
