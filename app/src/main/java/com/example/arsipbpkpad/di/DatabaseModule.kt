package com.example.arsipbpkpad.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.arsipbpkpad.data.local.AppDatabase
import com.example.arsipbpkpad.data.local.dao.ArchiveDao
import com.example.arsipbpkpad.data.local.dao.StagingArchiveDao
import com.example.arsipbpkpad.data.local.entity.ArchiveEntity
import com.example.arsipbpkpad.domain.model.DocCopyStatus
import com.example.arsipbpkpad.domain.model.DocStatus
import com.example.arsipbpkpad.domain.model.DocType
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

    private fun getDummyArchives(): List<ArchiveEntity> {
        return listOf(
            ArchiveEntity(
                id = "1",
                type = DocType.SP2D,
                copyStatus = DocCopyStatus.ORIGINAL,
                documentNumber = "SP2D-123",
                nominal = 50000000.0,
                thirdParty = "PT. Pembangunan Jaya",
                year = 2024,
                dateIssued = "2024-01-15",
                status = DocStatus.AVAILABLE,
                idStorageLocation = null,
                imageUrl = null,
                metadata = null,
                createdBy = null,
                verifiedBy = null,
                createdAt = "2024-01-15T00:00:00Z",
                updatedAt = "2024-01-15T00:00:00Z"
            ),
            ArchiveEntity(
                id = "2",
                type = DocType.SPM,
                copyStatus = DocCopyStatus.ORIGINAL,
                documentNumber = "SPM-456",
                nominal = 25000000.0,
                thirdParty = "CV. Berkah Utama",
                year = 2024,
                dateIssued = "2024-03-10",
                status = DocStatus.AVAILABLE,
                idStorageLocation = null,
                imageUrl = null,
                metadata = null,
                createdBy = null,
                verifiedBy = null,
                createdAt = "2024-03-10T00:00:00Z",
                updatedAt = "2024-03-10T00:00:00Z"
            )
        )
    }
}
