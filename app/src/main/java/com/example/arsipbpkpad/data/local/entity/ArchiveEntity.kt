package com.example.arsipbpkpad.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.arsipbpkpad.domain.model.ArchiveMetadata
import com.example.arsipbpkpad.domain.model.DocCondition
import com.example.arsipbpkpad.domain.model.DocCopyType
import com.example.arsipbpkpad.domain.model.DocStatus
import com.example.arsipbpkpad.domain.model.DocType

@Entity(tableName = "archives")
data class ArchiveEntity(
    @PrimaryKey
    val id: String,
    val boxSessionId: String? = null,
    val type: DocType,
    val copyType: DocCopyType,
    val copyCount: Int,
    val documentNumber: String?,
    val classificationCode: String = "900.1.3.1",
    val description: String?,
    val nominal: Double?,
    val year: Int,
    val condition: DocCondition,
    val status: DocStatus,
    val metadata: ArchiveMetadata?,
    val idStorageLocation: String? = null,
    val bundleId: String? = null,
    val createdBy: String? = null,
    val verifiedBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val syncStatus: String = "SYNCED" // DRAFT, SYNCED, ERROR
)
