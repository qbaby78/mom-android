package com.example.mom.core.di

import android.content.Context
import androidx.room.Room
import com.example.mom.data.remote.localserver.dao.ServerActiveViolationDao
import com.example.mom.data.remote.localserver.dao.ServerChildDao
import com.example.mom.data.remote.localserver.dao.ServerInterestDao
import com.example.mom.data.remote.localserver.dao.ServerViolationDao
import com.example.mom.data.remote.localserver.db.MomServerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServerDatabaseModule {

  @Provides
  @Singleton
  fun provideMomServerDatabase(
      @ApplicationContext context: Context,
  ): MomServerDatabase =
      Room.databaseBuilder(
              context,
              MomServerDatabase::class.java,
              "mom_server.db",
          )
          .fallbackToDestructiveMigration(dropAllTables = true)
          .build()

  @Provides
  fun provideServerChildDao(database: MomServerDatabase): ServerChildDao = database.childDao()

  @Provides
  fun provideServerInterestDao(database: MomServerDatabase): ServerInterestDao =
      database.interestDao()

  @Provides
  fun provideServerViolationDao(database: MomServerDatabase): ServerViolationDao =
      database.violationDao()

  @Provides
  fun provideServerActiveViolationDao(database: MomServerDatabase): ServerActiveViolationDao =
      database.activeViolationDao()
}