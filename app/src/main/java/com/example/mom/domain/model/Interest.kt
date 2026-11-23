package com.example.mom.domain.model

data class Interest(
    val id: Long,
    val childId: Long,
    val name: String,
    val type: InterestType,
    val latitude: Double,
    val longitude: Double,
    val radiusKm: Double,
    val intervalMinutes: Int,
)
