package com.example.mom.domain.repository

import com.example.mom.domain.model.ViolationLog
import kotlinx.coroutines.flow.Flow

interface ViolationRepository {
    fun observeViolations(): Flow<List<ViolationLog>>

    suspend fun recordIfEntered(log: ViolationLog)

    suspend fun markInside(interestId: Long)
}