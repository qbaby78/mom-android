package com.example.mom.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mom.data.local.entity.LastLocationEntity

@Dao
interface LastLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LastLocationEntity)

    @Query("SELECT * FROM local_last_locations WHERE childId = :childId LIMIT 1")
    suspend fun getByChildId(childId: Long): LastLocationEntity?
}