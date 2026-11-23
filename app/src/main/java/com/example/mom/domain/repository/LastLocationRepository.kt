package com.example.mom.domain.repository

import com.example.mom.domain.model.GeoPoint
import com.example.mom.domain.model.LastLocation

interface LastLocationRepository {
    suspend fun saveLastLocation(
        childId: Long,
        point: GeoPoint,
        measuredAt: Long,
    )

    suspend fun getLastLocation(childId: Long): LastLocation?
}