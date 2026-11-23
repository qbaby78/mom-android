package com.example.mom.domain.repository

import com.example.mom.domain.model.Interest
import kotlinx.coroutines.flow.Flow

interface InterestRepository {
    fun observeInterests(childId: Long): Flow<List<Interest>>

    suspend fun getInterest(id: Long): Interest?

    suspend fun upsertInterest(interest: Interest): Interest

    suspend fun deleteInterest(id: Long)
}