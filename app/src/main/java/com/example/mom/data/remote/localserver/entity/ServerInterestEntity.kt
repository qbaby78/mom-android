package com.example.mom.data.remote.localserver.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "server_interests",
    foreignKeys = [
        ForeignKey(
            entity = ServerChildEntity::class,
            parentColumns = ["id"],
            childColumns = ["childId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("childId")],
)
data class ServerInterestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val childId: Long,
    val name: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val radiusKm: Double,
    val intervalMinutes: Int,
)