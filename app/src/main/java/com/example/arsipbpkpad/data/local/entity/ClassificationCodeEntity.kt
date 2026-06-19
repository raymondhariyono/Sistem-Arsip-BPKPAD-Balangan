package com.example.arsipbpkpad.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classification_codes")
data class ClassificationCodeEntity(
    @PrimaryKey
    val code: String,
    val name: String,
    val parentCode: String?,
    val level: Int,
    val isActive: Boolean
)
