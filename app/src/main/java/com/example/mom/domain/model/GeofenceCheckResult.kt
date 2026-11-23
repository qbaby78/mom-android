package com.example.mom.domain.model

data class GeofenceCheckResult(
    val interestId: Long,
    val distanceKm: Double,
    val violated: Boolean,
)
