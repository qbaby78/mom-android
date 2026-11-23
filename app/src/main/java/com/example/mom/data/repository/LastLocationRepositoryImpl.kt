package com.example.mom.data.repository

import com.example.mom.data.local.LocationLocalDataSource
import com.example.mom.domain.model.GeoPoint
import com.example.mom.domain.model.LastLocation
import com.example.mom.domain.repository.LastLocationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LastLocationRepositoryImpl @Inject constructor(
    private val local: LocationLocalDataSource,
) : LastLocationRepository {

    override suspend fun saveLastLocation(
        childId: Long,
        point: GeoPoint,
        measuredAt: Long,
    ) = local.saveLastLocation(childId, point, measuredAt)

    override suspend fun getLastLocation(childId: Long): LastLocation? =
        local.getLastLocation(childId)
}