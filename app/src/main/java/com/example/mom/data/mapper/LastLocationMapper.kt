package com.example.mom.data.mapper

import com.example.mom.data.local.entity.LastLocationEntity
import com.example.mom.domain.model.GeoPoint
import com.example.mom.domain.model.LastLocation

fun LastLocationEntity.toDomain(): LastLocation =
    LastLocation(
        childId = childId,
        latitude = latitude,
        longitude = longitude,
        measuredAt = measuredAt,
    )

fun toLastLocationEntity(
    childId: Long,
    point: GeoPoint,
    measuredAt: Long,
): LastLocationEntity =
    LastLocationEntity(
        childId = childId,
        latitude = point.latitude,
        longitude = point.longitude,
        measuredAt = measuredAt,
    )