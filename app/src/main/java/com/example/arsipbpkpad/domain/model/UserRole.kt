package com.example.arsipbpkpad.domain.model

enum class UserRole {
    SUPER_ADMIN,
    ARSIPARIS,
    KASSUBAG,
    UNKNOWN
}

fun UserRole.canMutateArchive(): Boolean =
    this == UserRole.ARSIPARIS || this == UserRole.SUPER_ADMIN

fun UserRole.canExport(): Boolean =
    this == UserRole.KASSUBAG || this == UserRole.ARSIPARIS || this == UserRole.SUPER_ADMIN

fun UserRole.canViewAnalytics(): Boolean =
    this == UserRole.KASSUBAG || this == UserRole.SUPER_ADMIN

fun UserRole.canManageStorage(): Boolean =
    this == UserRole.ARSIPARIS || this == UserRole.SUPER_ADMIN

fun UserRole.canManageStaging(): Boolean =
    this == UserRole.ARSIPARIS || this == UserRole.SUPER_ADMIN

fun UserRole.isReadOnly(): Boolean =
    this == UserRole.KASSUBAG
