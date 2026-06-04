package com.example.arsipbpkpad.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.arsipbpkpad.data.local.entity.ArchiveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArchiveDao {
    @Query("SELECT * FROM archives WHERE :query IS NULL OR documentNumber LIKE '%' || :query || '%' OR thirdParty LIKE '%' || :query || '%'")
    fun getArchives(query: String?): Flow<List<ArchiveEntity>>

    @Query("SELECT * FROM archives WHERE id = :id")
    fun getArchiveById(id: String): Flow<ArchiveEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM archives WHERE documentNumber = :docNumber)")
    suspend fun existsByDocumentNumber(docNumber: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArchive(archive: ArchiveEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArchives(archives: List<ArchiveEntity>)

    @Query("DELETE FROM archives WHERE id = :id")
    suspend fun deleteArchiveById(id: String)

    @Query("DELETE FROM archives")
    suspend fun clearArchives()
}
