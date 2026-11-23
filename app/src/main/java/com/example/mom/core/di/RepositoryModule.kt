package com.example.mom.core.di

import com.example.mom.data.repository.ChildRepositoryImpl
import com.example.mom.data.repository.InterestRepositoryImpl
import com.example.mom.data.repository.LastLocationRepositoryImpl
import com.example.mom.data.repository.ModeRepositoryImpl
import com.example.mom.data.repository.ViolationRepositoryImpl
import com.example.mom.domain.repository.ChildRepository
import com.example.mom.domain.repository.InterestRepository
import com.example.mom.domain.repository.LastLocationRepository
import com.example.mom.domain.repository.ModeRepository
import com.example.mom.domain.repository.ViolationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

  @Binds
  @Singleton
  abstract fun bindChildRepository(impl: ChildRepositoryImpl): ChildRepository

  @Binds
  @Singleton
  abstract fun bindInterestRepository(impl: InterestRepositoryImpl): InterestRepository

  @Binds
  @Singleton
  abstract fun bindViolationRepository(impl: ViolationRepositoryImpl): ViolationRepository

  @Binds
  @Singleton
  abstract fun bindModeRepository(impl: ModeRepositoryImpl): ModeRepository

  @Binds
  @Singleton
  abstract fun bindLastLocationRepository(impl: LastLocationRepositoryImpl): LastLocationRepository
}