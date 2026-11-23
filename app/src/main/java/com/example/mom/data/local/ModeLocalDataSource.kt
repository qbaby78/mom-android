package com.example.mom.data.local

import com.example.mom.domain.model.AppMode
import kotlinx.coroutines.flow.Flow

interface ModeLocalDataSource {
    fun observeMode(): Flow<AppMode>

    suspend fun setMode(mode: AppMode)

    fun observeMonitoredChildId(): Flow<Long?>

    suspend fun setMonitoredChildId(childId: Long?)

    suspend fun clearMonitoredChildIdIf(childId: Long)
}