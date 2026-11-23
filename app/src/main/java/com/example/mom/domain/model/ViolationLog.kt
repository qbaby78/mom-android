package com.example.mom.domain.model

data class ViolationLog(
    val id: Long,
    val childId: Long,
    val childName: String,
    val interestId: Long,
    val interestName: String,
    val occurredAt: Long,
    val measuredLatitude: Double,
    val measuredLongitude: Double,
    val distanceKm: Double,
)
