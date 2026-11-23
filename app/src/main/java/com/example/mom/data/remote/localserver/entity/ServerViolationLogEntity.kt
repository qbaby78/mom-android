package com.example.mom.data.remote.localserver.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "server_violation_logs",
    indices = [Index("childId"), Index("interestId")],
)
data class ServerViolationLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val childId: Long,
    val childName: String,
    val interestId: Long,
    val interestName: String,
    val occurredAt: Long,
    val measuredLatitude: Double,
    val measuredLongitude: Double,
    val distanceKm: Double,
)