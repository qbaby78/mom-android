package com.example.mom.core.di

import com.example.mom.data.local.LocationLocalDataSource
import com.example.mom.data.local.LocationLocalDataSourceImpl
import com.example.mom.data.local.ModeLocalDataSource
import com.example.mom.data.local.ModeLocalDataSourceImpl
import com.example.mom.data.remote.ChildRemoteDataSource
import com.example.mom.data.remote.InterestRemoteDataSource
import com.example.mom.data.remote.ViolationRemoteDataSource
import com.example.mom.data.remote.localserver.LocalServerDbRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

  @Binds
  @Singleton
  abstract fun bindChildRemoteDataSource(
      impl: LocalServerDbRemoteDataSource,
  ): ChildRemoteDataSource

  @Binds
  @Singleton
  abstract fun bindInterestRemoteDataSource(
      impl: LocalServerDbRemoteDataSource,
  ): InterestRemoteDataSource

  @Binds
  @Singleton
  abstract fun bindViolationRemoteDataSource(
      impl: LocalServerDbRemoteDataSource,
  ): ViolationRemoteDataSource

  @Binds
  @Singleton
  abstract fun bindLocationLocalDataSource(
      impl: LocationLocalDataSourceImpl,
  ): LocationLocalDataSource

  @Binds
  @Singleton
  abstract fun bindModeLocalDataSource(impl: ModeLocalDataSourceImpl): ModeLocalDataSource
}