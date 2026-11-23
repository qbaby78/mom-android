package com.example.mom.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_last_locations")
data class LastLocationEntity(
    @PrimaryKey
    val childId: Long,
    val latitude: Double,
    val longitude: Double,
    val measuredAt: Long,
)