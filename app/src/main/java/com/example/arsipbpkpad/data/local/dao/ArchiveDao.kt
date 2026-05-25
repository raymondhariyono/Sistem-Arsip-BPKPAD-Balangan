package com.example.arsipbpkpad.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.arsipbpkpad.data.local.entity.ArchiveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArchiveDao {
    @Query("SELECT * FROM archives")
    fun getArchives(): Flow<List<ArchiveEntity>>

    @Query("SELECT * FROM archives WHERE id = :id")
    fun getArchiveById(id: String): Flow<ArchiveEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArchive(archive: ArchiveEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArchives(archives: List<ArchiveEntity>)
}
