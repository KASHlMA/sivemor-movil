package com.sivemore.mobile.app.di

import com.sivemore.mobile.data.repository.FakeAuthRepository
import com.sivemore.mobile.data.repository.FakeVehicleRepository
import com.sivemore.mobile.data.repository.FakeVerificationRepository
import com.sivemore.mobile.domain.repository.AuthRepository
import com.sivemore.mobile.domain.repository.VehicleRepository
import com.sivemore.mobile.domain.repository.VerificationRepository
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
    abstract fun bindVehicleRepository(
        implementation: FakeVehicleRepository,
    ): VehicleRepository

    @Binds
    @Singleton
    abstract fun bindVerificationRepository(
        implementation: FakeVerificationRepository,
    ): VerificationRepository
}
