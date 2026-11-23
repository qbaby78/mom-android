package com.example.mom.data.repository

import com.example.mom.data.local.ModeLocalDataSource
import com.example.mom.data.remote.ChildRemoteDataSource
import com.example.mom.domain.model.Child
import com.example.mom.domain.repository.ChildRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class ChildRepositoryImpl @Inject constructor(
    private val remote: ChildRemoteDataSource,
    private val modeLocalDataSource: ModeLocalDataSource,
) : ChildRepository {

    override fun observeChildren(): Flow<List<Child>> = remote.observeChildren()

    override suspend fun getChild(id: Long): Child? = remote.getChild(id)

    override suspend fun upsertChild(child: Child): Child = remote.upsertChild(child)

    override suspend fun deleteChild(id: Long) {
        remote.deleteChild(id)
        modeLocalDataSource.clearMonitoredChildIdIf(id)
    }
}