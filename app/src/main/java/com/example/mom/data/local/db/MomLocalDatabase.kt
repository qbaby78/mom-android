package com.example.mom.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mom.data.local.dao.LastLocationDao
import com.example.mom.data.local.entity.LastLocationEntity

@Database(
    entities = [LastLocationEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class MomLocalDatabase : RoomDatabase() {
    abstract fun lastLocationDao(): LastLocationDao
}