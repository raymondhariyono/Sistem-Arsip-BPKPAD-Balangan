package com.example.arsipbpkpad.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "staging_boxes")
data class StagingBoxEntity(
    @PrimaryKey
    val sessionId: String,
    val warehouse: String,
    val rack: String,
    val boxNumber: String,
    val year: String,
    val createdAt: Long = System.currentTimeMillis()
)
