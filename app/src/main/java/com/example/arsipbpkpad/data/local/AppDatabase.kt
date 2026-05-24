package com.example.arsipbpkpad.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.arsipbpkpad.data.local.dao.ArchiveDao
import com.example.arsipbpkpad.data.local.entity.ArchiveEntity

@Database(entities = [ArchiveEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun archiveDao(): ArchiveDao

    companion object {
        const val DATABASE_NAME = "bpkpad_arsip_db"
    }
}
