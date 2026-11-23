package com.example.mom.data.local

import com.example.mom.domain.model.GeoPoint
import com.example.mom.domain.model.LastLocation

interface LocationLocalDataSource {
    suspend fun saveLastLocation(
        childId: Long,
        point: GeoPoint,
        measuredAt: Long,
    )

    suspend fun getLastLocation(childId: Long): LastLocation?
}