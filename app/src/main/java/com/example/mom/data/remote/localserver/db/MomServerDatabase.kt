package com.example.mom.data.remote.localserver.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mom.data.remote.localserver.dao.ServerActiveViolationDao
import com.example.mom.data.remote.localserver.dao.ServerChildDao
import com.example.mom.data.remote.localserver.dao.ServerInterestDao
import com.example.mom.data.remote.localserver.dao.ServerViolationDao
import com.example.mom.data.remote.localserver.entity.ServerActiveViolationEntity
import com.example.mom.data.remote.localserver.entity.ServerChildEntity
import com.example.mom.data.remote.localserver.entity.ServerInterestEntity
import com.example.mom.data.remote.localserver.entity.ServerViolationLogEntity

@Database(
    entities = [
        ServerChildEntity::class,
        ServerInterestEntity::class,
        ServerViolationLogEntity::class,
        ServerActiveViolationEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class MomServerDatabase : RoomDatabase() {
    abstract fun childDao(): ServerChildDao

    abstract fun interestDao(): ServerInterestDao

    abstract fun violationDao(): ServerViolationDao

    abstract fun activeViolationDao(): ServerActiveViolationDao
}