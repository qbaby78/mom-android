package com.example.mom.data.local

import com.example.mom.data.local.dao.LastLocationDao
import com.example.mom.data.mapper.toDomain
import com.example.mom.data.mapper.toLastLocationEntity
import com.example.mom.domain.model.GeoPoint
import com.example.mom.domain.model.LastLocation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationLocalDataSourceImpl @Inject constructor(
    private val lastLocationDao: LastLocationDao,
) : LocationLocalDataSource {

    override suspend fun saveLastLocation(
        childId: Long,
        point: GeoPoint,
        measuredAt: Long,
    ) {
        lastLocationDao.upsert(toLastLocationEntity(childId, point, measuredAt))
    }

    override suspend fun getLastLocation(childId: Long): LastLocation? =
        lastLocationDao.getByChildId(childId)?.toDomain()
}