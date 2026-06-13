package com.example.arsipbpkpad.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.arsipbpkpad.data.local.entity.StagingArchiveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StagingArchiveDao {
    @Query("SELECT * FROM staging_archives")
    fun getAllStagingArchives(): Flow<List<StagingArchiveEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToStaging(archive: StagingArchiveEntity)

    @Query("DELETE FROM staging_archives WHERE id = :id")
    suspend fun deleteFromStaging(id: String)

    @Query("DELETE FROM staging_archives")
    suspend fun clearStaging()
}
