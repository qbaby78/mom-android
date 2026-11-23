package com.example.mom.domain.repository

import com.example.mom.domain.model.Child
import kotlinx.coroutines.flow.Flow

interface ChildRepository {
    fun observeChildren(): Flow<List<Child>>

    suspend fun getChild(id: Long): Child?

    suspend fun upsertChild(child: Child): Child

    suspend fun deleteChild(id: Long)
}