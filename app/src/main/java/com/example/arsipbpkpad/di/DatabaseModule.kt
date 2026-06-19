package com.example.arsipbpkpad.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.arsipbpkpad.data.local.AppDatabase
import com.example.arsipbpkpad.data.local.dao.ArchiveDao
import com.example.arsipbpkpad.data.local.dao.StagingArchiveDao
import com.example.arsipbpkpad.data.local.entity.ArchiveEntity
import com.example.arsipbpkpad.domain.model.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        archiveDaoProvider: Provider<ArchiveDao>
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration()
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = archiveDaoProvider.get()
                    dao.insertArchives(getDummyArchives())
                }
            }
        }).build()
    }

    @Provides
    @Singleton
    fun provideArchiveDao(database: AppDatabase): ArchiveDao {
        return database.archiveDao()
    }

    @Provides
    @Singleton
    fun provideStagingArchiveDao(database: AppDatabase): StagingArchiveDao {
        return database.stagingArchiveDao()
    }

    @Provides
    @Singleton
    fun provideClassificationCodeDao(database: AppDatabase): com.example.arsipbpkpad.data.local.dao.ClassificationCodeDao {
        return database.classificationCodeDao()
    }

    private fun getDummyArchives(): List<ArchiveEntity> {
        return listOf(
            ArchiveEntity(
                id = "1",
                type = DocType.SP2D,
                copyType = DocCopyType.ORIGINAL,
                copyCount = 1,
                documentNumber = "SP2D-123",
                nominal = 50000000.0,
                description = "Pembangunan Gedung A",
                year = 2024,
                condition = DocCondition.GOOD,
                status = DocStatus.AVAILABLE,
                idStorageLocation = null,
                metadata = null,
                createdBy = null,
                verifiedBy = null,
                createdAt = "2024-01-15T00:00:00Z",
                updatedAt = "2024-01-15T00:00:00Z"
            ),
            ArchiveEntity(
                id = "2",
                type = DocType.SPM,
                copyType = DocCopyType.ORIGINAL,
                copyCount = 1,
                documentNumber = "SPM-456",
                nominal = 25000000.0,
                description = "Pengadaan ATK",
                year = 2024,
                condition = DocCondition.GOOD,
                status = DocStatus.AVAILABLE,
                idStorageLocation = null,
                metadata = null,
                createdBy = null,
                verifiedBy = null,
                createdAt = "2024-03-10T00:00:00Z",
                updatedAt = "2024-03-10T00:00:00Z"
            )
        )
    }
}
