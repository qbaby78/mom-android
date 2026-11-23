package com.example.mom.data.repository

import com.example.mom.data.local.ModeLocalDataSource
import com.example.mom.domain.model.AppMode
import com.example.mom.domain.repository.ModeRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class ModeRepositoryImpl @Inject constructor(
    private val local: ModeLocalDataSource,
) : ModeRepository {

    override fun observeMode(): Flow<AppMode> = local.observeMode()

    override suspend fun setMode(mode: AppMode) = local.setMode(mode)

    override fun observeMonitoredChildId(): Flow<Long?> = local.observeMonitoredChildId()

    override suspend fun setMonitoredChildId(childId: Long?) =
        local.setMonitoredChildId(childId)
}