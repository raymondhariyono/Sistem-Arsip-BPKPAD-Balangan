package com.example.arsipbpkpad.domain.repository

import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.model.UserRole
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUserRole: StateFlow<UserRole>
    val isUserLoggedIn: StateFlow<Boolean>
    
    suspend fun login(email: String, password: String, rememberMe: Boolean): DomainResult<Unit>
    suspend fun logout(): DomainResult<Unit>
    suspend fun checkSession(): Boolean
    fun getCurrentUserId(): String?
    fun getCurrentUserEmail(): String?
}
