package com.example.mom.data.remote.localserver.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "server_active_violations")
data class ServerActiveViolationEntity(
    @PrimaryKey
    val interestId: Long,
    val childId: Long,
    val startedAt: Long,
)