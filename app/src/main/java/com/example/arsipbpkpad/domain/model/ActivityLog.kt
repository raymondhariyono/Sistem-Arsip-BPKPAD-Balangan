package com.example.arsipbpkpad.domain.model

data class ActivityLog(
    val id: String = "",
    val actorId: String? = null,
    val action: String, // CREATE, UPDATE, DELETE
    val entityType: String, // ARCHIVE, LOCATION
    val entityId: String,
    val details: String?,
    val timestamp: String? = null
)
