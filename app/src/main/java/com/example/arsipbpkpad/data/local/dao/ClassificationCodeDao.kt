package com.example.arsipbpkpad.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.arsipbpkpad.data.local.entity.ClassificationCodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassificationCodeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(codes: List<ClassificationCodeEntity>)

    @Query("SELECT * FROM classification_codes WHERE isActive = 1 ORDER BY code ASC")
    fun getAllActiveCodes(): Flow<List<ClassificationCodeEntity>>

    @Query("SELECT COUNT(*) FROM classification_codes")
    suspend fun getCodesCount(): Int
}
