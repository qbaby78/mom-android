package com.example.mom.data.remote.localserver.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mom.data.remote.localserver.entity.ServerInterestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerInterestDao {
    @Query("SELECT * FROM server_interests WHERE childId = :childId ORDER BY id ASC")
    fun observeByChild(childId: Long): Flow<List<ServerInterestEntity>>

    @Query("SELECT * FROM server_interests WHERE childId = :childId ORDER BY id ASC")
    suspend fun listByChild(childId: Long): List<ServerInterestEntity>

    @Query("SELECT * FROM server_interests WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ServerInterestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ServerInterestEntity): Long

    @Update
    suspend fun update(entity: ServerInterestEntity)

    @Delete
    suspend fun delete(entity: ServerInterestEntity)

    @Query("DELETE FROM server_interests WHERE id = :id")
    suspend fun deleteById(id: Long)
}