package com.sivemore.mobile.data.repository

import com.sivemore.mobile.data.network.MobileApiService
import com.sivemore.mobile.data.network.toDomain
import com.sivemore.mobile.domain.model.VehicleSummary
import com.sivemore.mobile.domain.repository.VehicleRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealVehicleRepository @Inject constructor(
    private val mobileApiService: MobileApiService,
) : VehicleRepository {
    override suspend fun loadVehicles(query: String): List<VehicleSummary> {
        val normalizedQuery = query.trim()
        val vehicles = mobileApiService.listOrders().map { it.toDomain() }
        if (normalizedQuery.isBlank()) return vehicles
        return vehicles.filter { vehicle ->
            listOf(vehicle.plates, vehicle.serialNumber, vehicle.vehicleNumber)
                .any { it.contains(normalizedQuery, ignoreCase = true) }
        }
    }

    override suspend fun loadVehicle(vehicleId: String): VehicleSummary? =
        mobileApiService.listOrders()
            .map { it.toDomain() }
            .firstOrNull { it.id == vehicleId }
}
