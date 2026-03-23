package com.sivemore.mobile.data.repository

import com.sivemore.mobile.domain.model.VehicleSummary
import com.sivemore.mobile.domain.repository.VehicleRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeVehicleRepository @Inject constructor(
    private val store: FakeVerificationStore,
) : VehicleRepository {

    override suspend fun loadVehicles(query: String): List<VehicleSummary> = store.loadVehicles(query)

    override suspend fun loadVehicle(vehicleId: String): VehicleSummary? = store.loadVehicle(vehicleId)
}
