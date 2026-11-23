package com.example.mom.data.remote.localserver.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mom.data.remote.localserver.entity.ServerActiveViolationEntity

@Dao
interface ServerActiveViolationDao {
    @Query("SELECT * FROM server_active_violations WHERE interestId = :interestId LIMIT 1")
    suspend fun findByInterestId(interestId: Long): ServerActiveViolationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ServerActiveViolationEntity)

    @Query("DELETE FROM server_active_violations WHERE interestId = :interestId")
    suspend fun deleteByInterestId(interestId: Long)

    @Query("DELETE FROM server_active_violations WHERE childId = :childId")
    suspend fun deleteByChildId(childId: Long)
}