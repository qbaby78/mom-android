package com.example.mom.core.di

import android.content.Context
import androidx.room.Room
import com.example.mom.data.local.dao.LastLocationDao
import com.example.mom.data.local.db.MomLocalDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalDatabaseModule {

  @Provides
  @Singleton
  fun provideMomLocalDatabase(
      @ApplicationContext context: Context,
  ): MomLocalDatabase =
      Room.databaseBuilder(
              context,
              MomLocalDatabase::class.java,
              "mom_local.db",
          )
          .fallbackToDestructiveMigration(dropAllTables = true)
          .build()

  @Provides
  fun provideLastLocationDao(database: MomLocalDatabase): LastLocationDao =
      database.lastLocationDao()
}