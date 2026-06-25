package com.example.arsipbpkpad.data.mapper

import com.example.arsipbpkpad.data.remote.dto.UserProfileDto
import com.example.arsipbpkpad.domain.model.UserProfile
import com.example.arsipbpkpad.domain.model.UserRole

fun UserProfileDto.toDomain(): UserProfile {
    return UserProfile(
        id = id,
        email = email,
        fullName = fullName,
        role = try {
            UserRole.valueOf(role)
        } catch (e: Exception) {
            UserRole.UNKNOWN
        },
        isActive = isActive
    )
}
