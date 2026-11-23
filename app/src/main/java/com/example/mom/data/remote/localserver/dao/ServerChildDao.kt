package com.example.mom.data.remote.localserver.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mom.data.remote.localserver.entity.ServerChildEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerChildDao {
    @Query("SELECT * FROM server_children ORDER BY id ASC")
    fun observeAll(): Flow<List<ServerChildEntity>>

    @Query("SELECT * FROM server_children WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ServerChildEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ServerChildEntity): Long

    @Update
    suspend fun update(entity: ServerChildEntity)

    @Delete
    suspend fun delete(entity: ServerChildEntity)

    @Query("DELETE FROM server_children WHERE id = :id")
    suspend fun deleteById(id: Long)
}