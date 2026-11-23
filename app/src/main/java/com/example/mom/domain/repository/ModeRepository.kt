package com.example.mom.domain.repository

import com.example.mom.domain.model.AppMode
import kotlinx.coroutines.flow.Flow

interface ModeRepository {
    fun observeMode(): Flow<AppMode>

    suspend fun setMode(mode: AppMode)

    fun observeMonitoredChildId(): Flow<Long?>

    suspend fun setMonitoredChildId(childId: Long?)
}