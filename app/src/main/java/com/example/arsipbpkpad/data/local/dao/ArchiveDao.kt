package com.example.arsipbpkpad.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.arsipbpkpad.data.local.entity.ArchiveEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArchiveDao {
    @Query("""
        SELECT * FROM archives 
        WHERE (:isYearEmpty OR year IN (:years))
        AND (:query IS NULL OR :query = '' OR documentNumber LIKE '%' || :query || '%' OR thirdParty LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    fun getArchives(query: String?, years: List<Int>, isYearEmpty: Boolean): PagingSource<Int, ArchiveEntity>

    @Query("""
        SELECT * FROM archives 
        WHERE (:isYearEmpty OR year IN (:years))
        AND (:query IS NULL OR :query = '' OR documentNumber LIKE '%' || :query || '%' OR thirdParty LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    fun getArchivesList(query: String?, years: List<Int>, isYearEmpty: Boolean): Flow<List<ArchiveEntity>>

    @Query("SELECT SUM(nominal) FROM archives WHERE year = :year")
    fun getTotalBudgetByYear(year: Int): Flow<Double?>

    @Query("SELECT * FROM archives WHERE id = :id")
    fun getArchiveById(id: String): Flow<ArchiveEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM archives WHERE documentNumber = :docNumber AND copyStatus = :copyStatus)")
    suspend fun existsByDocumentNumberAndStatus(docNumber: String, copyStatus: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArchive(archive: ArchiveEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArchives(archives: List<ArchiveEntity>)

    @Query("DELETE FROM archives WHERE id = :id")
    suspend fun deleteArchiveById(id: String)

    @Query("DELETE FROM archives")
    suspend fun clearArchives()
}
