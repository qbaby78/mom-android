package com.example.mom.data.remote.localserver.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mom.data.remote.localserver.entity.ServerViolationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerViolationDao {
    @Query("SELECT * FROM server_violation_logs ORDER BY occurredAt DESC")
    fun observeAll(): Flow<List<ServerViolationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ServerViolationLogEntity): Long
}