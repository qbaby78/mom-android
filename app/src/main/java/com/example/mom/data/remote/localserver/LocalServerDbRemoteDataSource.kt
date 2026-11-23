package com.example.mom.data.remote.localserver

import androidx.room.withTransaction
import com.example.mom.data.mapper.toActiveEntity
import com.example.mom.data.mapper.toDomain
import com.example.mom.data.mapper.toServerEntity
import com.example.mom.data.remote.ChildRemoteDataSource
import com.example.mom.data.remote.InterestRemoteDataSource
import com.example.mom.data.remote.ViolationRemoteDataSource
import com.example.mom.data.remote.localserver.dao.ServerActiveViolationDao
import com.example.mom.data.remote.localserver.dao.ServerChildDao
import com.example.mom.data.remote.localserver.dao.ServerInterestDao
import com.example.mom.data.remote.localserver.dao.ServerViolationDao
import com.example.mom.data.remote.localserver.db.MomServerDatabase
import com.example.mom.domain.model.ActiveViolation
import com.example.mom.domain.model.Child
import com.example.mom.domain.model.Interest
import com.example.mom.domain.model.ViolationLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalServerDbRemoteDataSource @Inject constructor(
    private val database: MomServerDatabase,
    private val childDao: ServerChildDao,
    private val interestDao: ServerInterestDao,
    private val violationDao: ServerViolationDao,
    private val activeViolationDao: ServerActiveViolationDao,
) : ChildRemoteDataSource,
    InterestRemoteDataSource,
    ViolationRemoteDataSource {

    override fun observeChildren(): Flow<List<Child>> =
        childDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getChild(id: Long): Child? =
        childDao.getById(id)?.toDomain()

    override suspend fun upsertChild(child: Child): Child {
        val entity = child.toServerEntity()
        return if (child.id == 0L) {
            val newId = childDao.insert(entity.copy(id = 0))
            child.copy(id = newId)
        } else {
            childDao.update(entity)
            child
        }
    }

    override suspend fun deleteChild(id: Long) {
        database.withTransaction {
            activeViolationDao.deleteByChildId(id)
            childDao.deleteById(id)
        }
    }

    override fun observeInterests(childId: Long): Flow<List<Interest>> =
        interestDao.observeByChild(childId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getInterest(id: Long): Interest? =
        interestDao.getById(id)?.toDomain()

    override suspend fun upsertInterest(interest: Interest): Interest {
        val entity = interest.toServerEntity()
        return if (interest.id == 0L) {
            val newId = interestDao.insert(entity.copy(id = 0))
            interest.copy(id = newId)
        } else {
            interestDao.update(entity)
            interest
        }
    }

    override suspend fun deleteInterest(id: Long) {
        database.withTransaction {
            activeViolationDao.deleteByInterestId(id)
            interestDao.deleteById(id)
        }
    }

    override fun observeViolations(): Flow<List<ViolationLog>> =
        violationDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun findActive(interestId: Long): ActiveViolation? =
        activeViolationDao.findByInterestId(interestId)?.toDomain()

    override suspend fun recordViolation(log: ViolationLog): ViolationLog {
        val startedAt = log.occurredAt
        val entity = log.toServerEntity()
        return database.withTransaction {
            val newId = violationDao.insert(entity.copy(id = 0))
            activeViolationDao.upsert(log.toActiveEntity(startedAt))
            log.copy(id = newId)
        }
    }

    override suspend fun clearActive(interestId: Long) {
        activeViolationDao.deleteByInterestId(interestId)
    }
}