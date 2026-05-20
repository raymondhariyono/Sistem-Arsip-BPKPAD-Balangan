package com.example.arsipbpkpad.utils

sealed class ResultState<out T> {
    data object Idle : ResultState<Nothing>()
    data object Loading : ResultState<Nothing>()
    data class Success<T>(val data: T) : ResultState<T>()
    data class Error(val message: String, val cause: Throwable? = null) : ResultState<Nothing>()
}