package com.example.arsipbpkpad.data.local.dao

import androidx.room.*
import com.example.arsipbpkpad.data.local.entity.StagingArchiveEntity
import com.example.arsipbpkpad.data.local.entity.StagingBoxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StagingArchiveDao {
    // --- Box Management ---
    @Query("SELECT * FROM staging_boxes ORDER BY createdAt DESC")
    fun getAllStagingBoxes(): Flow<List<StagingBoxEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStagingBox(box: StagingBoxEntity)

    @Query("DELETE FROM staging_boxes WHERE sessionId = :sessionId")
    suspend fun deleteStagingBox(sessionId: String)

    @Query("SELECT * FROM staging_boxes WHERE sessionId = :sessionId")
    suspend fun getStagingBoxById(sessionId: String): StagingBoxEntity?

    // --- Archive Management ---
    @Query("SELECT * FROM staging_archives")
    fun getAllStagingArchives(): Flow<List<StagingArchiveEntity>>

    @Query("SELECT * FROM staging_archives WHERE boxSessionId = :sessionId")
    fun getStagingArchivesBySession(sessionId: String): Flow<List<StagingArchiveEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToStaging(archive: StagingArchiveEntity)

    @Query("DELETE FROM staging_archives WHERE id = :id")
    suspend fun deleteFromStaging(id: String)

    @Query("DELETE FROM staging_archives WHERE boxSessionId = :sessionId")
    suspend fun clearStagingBySession(sessionId: String)

    @Query("DELETE FROM staging_archives")
    suspend fun clearAllStaging()
}
