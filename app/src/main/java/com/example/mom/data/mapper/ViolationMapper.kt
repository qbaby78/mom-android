package com.example.mom.data.mapper

import com.example.mom.data.remote.localserver.entity.ServerActiveViolationEntity
import com.example.mom.data.remote.localserver.entity.ServerViolationLogEntity
import com.example.mom.domain.model.ActiveViolation
import com.example.mom.domain.model.ViolationLog

fun ServerViolationLogEntity.toDomain(): ViolationLog =
    ViolationLog(
        id = id,
        childId = childId,
        childName = childName,
        interestId = interestId,
        interestName = interestName,
        occurredAt = occurredAt,
        measuredLatitude = measuredLatitude,
        measuredLongitude = measuredLongitude,
        distanceKm = distanceKm,
    )

fun ViolationLog.toServerEntity(): ServerViolationLogEntity =
    ServerViolationLogEntity(
        id = id,
        childId = childId,
        childName = childName,
        interestId = interestId,
        interestName = interestName,
        occurredAt = occurredAt,
        measuredLatitude = measuredLatitude,
        measuredLongitude = measuredLongitude,
        distanceKm = distanceKm,
    )

fun ServerActiveViolationEntity.toDomain(): ActiveViolation =
    ActiveViolation(
        interestId = interestId,
        childId = childId,
        startedAt = startedAt,
    )

fun ViolationLog.toActiveEntity(startedAt: Long): ServerActiveViolationEntity =
    ServerActiveViolationEntity(
        interestId = interestId,
        childId = childId,
        startedAt = startedAt,
    )