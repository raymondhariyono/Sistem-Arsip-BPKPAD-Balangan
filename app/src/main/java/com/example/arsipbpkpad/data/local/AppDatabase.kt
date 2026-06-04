package com.example.arsipbpkpad.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.arsipbpkpad.data.local.converter.DatabaseConverters
import com.example.arsipbpkpad.data.local.dao.ArchiveDao
import com.example.arsipbpkpad.data.local.entity.ArchiveEntity

@Database(entities = [ArchiveEntity::class], version = 2, exportSchema = false)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun archiveDao(): ArchiveDao

    companion object {
        const val DATABASE_NAME = "bpkpad_arsip_db"
    }
}
