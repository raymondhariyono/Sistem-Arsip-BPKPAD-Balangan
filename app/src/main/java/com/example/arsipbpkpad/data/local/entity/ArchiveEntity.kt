package com.example.arsipbpkpad.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "archives")
data class ArchiveEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val category: String,
    val imageUrl: String?
)
