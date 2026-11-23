package com.example.mom.data.remote

import com.example.mom.domain.model.Interest
import kotlinx.coroutines.flow.Flow

interface InterestRemoteDataSource {
    fun observeInterests(childId: Long): Flow<List<Interest>>

    suspend fun getInterest(id: Long): Interest?

    suspend fun upsertInterest(interest: Interest): Interest

    suspend fun deleteInterest(id: Long)
}