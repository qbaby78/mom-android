package com.example.mom.data.remote

import com.example.mom.domain.model.ActiveViolation
import com.example.mom.domain.model.ViolationLog
import kotlinx.coroutines.flow.Flow

interface ViolationRemoteDataSource {
    fun observeViolations(): Flow<List<ViolationLog>>

    suspend fun findActive(interestId: Long): ActiveViolation?

    suspend fun recordViolation(log: ViolationLog): ViolationLog

    suspend fun clearActive(interestId: Long)
}