package com.example.arsipbpkpad.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.arsipbpkpad.data.local.AppDatabase
import com.example.arsipbpkpad.data.local.dao.ArchiveDao
import com.example.arsipbpkpad.data.local.entity.ArchiveEntity
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
        ).addCallback(object : RoomDatabase.Callback() {
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

    private fun getDummyArchives(): List<ArchiveEntity> {
        return listOf(
            ArchiveEntity(
                id = "1",
                title = "Surat Keputusan Bupati No. 123",
                description = "SK Penetapan Lokasi Pembangunan Gedung Baru BPKPAD Balangan.",
                date = "2024-01-15",
                category = "SK Bupati",
                imageUrl = "https://picsum.photos/200"
            ),
            ArchiveEntity(
                id = "2",
                title = "Laporan Keuangan Tahunan 2023",
                description = "Laporan realisasi anggaran dan neraca keuangan daerah tahun 2023.",
                date = "2024-03-10",
                category = "Laporan Keuangan",
                imageUrl = "https://picsum.photos/201"
            ),
            ArchiveEntity(
                id = "3",
                title = "Sertifikat Aset Daerah Blok A",
                description = "Dokumen kepemilikan tanah aset daerah di wilayah Balangan.",
                date = "2023-11-20",
                category = "Sertifikat",
                imageUrl = "https://picsum.photos/202"
            ),
            ArchiveEntity(
                id = "4",
                title = "Nota Dinas Kepegawaian",
                description = "Nota dinas terkait mutasi pegawai internal BPKPAD.",
                date = "2024-02-05",
                category = "Kepegawaian",
                imageUrl = "https://picsum.photos/203"
            ),
            ArchiveEntity(
                id = "5",
                title = "Peraturan Daerah No. 45 Tahun 2023",
                description = "Perda tentang Pengelolaan Barang Milik Daerah.",
                date = "2023-12-28",
                category = "Perda",
                imageUrl = "https://picsum.photos/204"
            )
        )
    }
}
