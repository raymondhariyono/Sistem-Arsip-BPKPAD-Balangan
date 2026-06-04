package com.example.arsipbpkpad.data.local.converter

import androidx.room.TypeConverter
import com.example.arsipbpkpad.domain.model.ArchiveMetadata
import com.example.arsipbpkpad.domain.model.DocStatus
import com.example.arsipbpkpad.domain.model.DocType
import kotlinx.serialization.json.Json

class DatabaseConverters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromDocType(value: DocType): String = value.name

    @TypeConverter
    fun toDocType(value: String): DocType = DocType.valueOf(value)

    @TypeConverter
    fun fromDocStatus(value: DocStatus): String = value.name

    @TypeConverter
    fun toDocStatus(value: String): DocStatus = DocStatus.valueOf(value)

    @TypeConverter
    fun fromMetadata(value: ArchiveMetadata?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toMetadata(value: String?): ArchiveMetadata? {
        return value?.let { json.decodeFromString<ArchiveMetadata>(it) }
    }
}
