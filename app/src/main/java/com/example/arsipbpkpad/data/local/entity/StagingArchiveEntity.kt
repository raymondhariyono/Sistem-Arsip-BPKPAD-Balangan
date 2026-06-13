package com.example.arsipbpkpad.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.arsipbpkpad.domain.model.ArchiveMetadata
import com.example.arsipbpkpad.domain.model.DocCopyStatus
import com.example.arsipbpkpad.domain.model.DocStatus
import com.example.arsipbpkpad.domain.model.DocType

@Entity(tableName = "staging_archives")
data class StagingArchiveEntity(
    @PrimaryKey
    val id: String,
    val boxSessionId: String,
    val type: DocType,
    val copyStatus: DocCopyStatus,
    val documentNumber: String,
    val nominal: Double?,
    val thirdParty: String?,
    val year: Int,
    val dateIssued: String?,
    val status: DocStatus,
    val idStorageLocation: String?,
    val metadata: ArchiveMetadata?,
    val createdBy: String?,
    val verifiedBy: String?
)
