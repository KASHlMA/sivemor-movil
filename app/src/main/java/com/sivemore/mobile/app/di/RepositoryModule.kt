package com.sivemore.mobile.app.di

import com.sivemore.mobile.data.repository.RealAuthRepository
import com.sivemore.mobile.data.repository.RealVehicleRepository
import com.sivemore.mobile.data.repository.RealVerificationRepository
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
        implementation: RealAuthRepository,
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(
        implementation: RealVehicleRepository,
    ): VehicleRepository

    @Binds
    @Singleton
    abstract fun bindVerificationRepository(
        implementation: RealVerificationRepository,
    ): VerificationRepository
}
