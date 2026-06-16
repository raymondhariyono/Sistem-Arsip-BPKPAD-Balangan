package com.example.arsipbpkpad.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.arsipbpkpad.data.local.converter.DatabaseConverters
import com.example.arsipbpkpad.data.local.dao.ArchiveDao
import com.example.arsipbpkpad.data.local.dao.StagingArchiveDao
import com.example.arsipbpkpad.data.local.entity.ArchiveEntity
import com.example.arsipbpkpad.data.local.entity.StagingArchiveEntity
import com.example.arsipbpkpad.data.local.entity.StagingBoxEntity

@Database(entities = [ArchiveEntity::class, StagingArchiveEntity::class, StagingBoxEntity::class], version = 12, exportSchema = false)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun archiveDao(): ArchiveDao
    abstract fun stagingArchiveDao(): StagingArchiveDao

    companion object {
        const val DATABASE_NAME = "bpkpad_arsip_db"
    }
}
