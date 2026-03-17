package com.sivemore.mobile.app.di

import com.sivemore.mobile.data.repository.FakeAuthRepository
import com.sivemore.mobile.data.repository.FakeHomeRepository
import com.sivemore.mobile.data.repository.FakeProfileRepository
import com.sivemore.mobile.domain.repository.AuthRepository
import com.sivemore.mobile.domain.repository.HomeRepository
import com.sivemore.mobile.domain.repository.ProfileRepository
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
    abstract fun bindAuthRepository(
        implementation: FakeAuthRepository,
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindHomeRepository(
        implementation: FakeHomeRepository,
    ): HomeRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        implementation: FakeProfileRepository,
    ): ProfileRepository
}
