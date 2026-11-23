package com.example.mom.data.mapper

import com.example.mom.data.remote.localserver.entity.ServerInterestEntity
import com.example.mom.domain.model.Interest
import com.example.mom.domain.model.InterestType

fun ServerInterestEntity.toDomain(): Interest =
    Interest(
        id = id,
        childId = childId,
        name = name,
        type = InterestType.valueOf(type),
        latitude = latitude,
        longitude = longitude,
        radiusKm = radiusKm,
        intervalMinutes = intervalMinutes,
    )

fun Interest.toServerEntity(): ServerInterestEntity =
    ServerInterestEntity(
        id = id,
        childId = childId,
        name = name,
        type = type.name,
        latitude = latitude,
        longitude = longitude,
        radiusKm = radiusKm,
        intervalMinutes = intervalMinutes,
    )