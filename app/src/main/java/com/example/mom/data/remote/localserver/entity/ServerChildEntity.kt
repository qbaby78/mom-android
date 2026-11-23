package com.example.mom.data.remote.localserver.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "server_children")
data class ServerChildEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
)