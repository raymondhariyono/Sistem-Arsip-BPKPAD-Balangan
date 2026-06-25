package com.example.arsipbpkpad.domain.model

data class UserProfile(
    val id: String,
    val email: String,
    val fullName: String,
    val role: UserRole,
    val isActive: Boolean
)
