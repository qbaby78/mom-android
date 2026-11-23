package com.example.mom.data.repository

import com.example.mom.data.remote.InterestRemoteDataSource
import com.example.mom.domain.model.Interest
import com.example.mom.domain.repository.InterestRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class InterestRepositoryImpl @Inject constructor(
    private val remote: InterestRemoteDataSource,
) : InterestRepository {

    override fun observeInterests(childId: Long): Flow<List<Interest>> =
        remote.observeInterests(childId)

    override suspend fun getInterest(id: Long): Interest? = remote.getInterest(id)

    override suspend fun upsertInterest(interest: Interest): Interest =
        remote.upsertInterest(interest)

    override suspend fun deleteInterest(id: Long) = remote.deleteInterest(id)
}