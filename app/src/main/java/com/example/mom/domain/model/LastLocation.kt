package com.example.mom.domain.model

data class LastLocation(
    val childId: Long,
    val latitude: Double,
    val longitude: Double,
    val measuredAt: Long,
)