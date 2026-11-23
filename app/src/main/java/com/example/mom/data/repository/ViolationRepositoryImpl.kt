package com.example.mom.data.repository

import com.example.mom.data.remote.ViolationRemoteDataSource
import com.example.mom.domain.model.ViolationLog
import com.example.mom.domain.repository.ViolationRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class ViolationRepositoryImpl @Inject constructor(
    private val remote: ViolationRemoteDataSource,
) : ViolationRepository {

    override fun observeViolations(): Flow<List<ViolationLog>> = remote.observeViolations()

    override suspend fun recordIfEntered(log: ViolationLog) {
        if (remote.findActive(log.interestId) != null) {
            return
        }
        remote.recordViolation(log)
    }

    override suspend fun markInside(interestId: Long) {
        if (remote.findActive(interestId) != null) {
            remote.clearActive(interestId)
        }
    }
}