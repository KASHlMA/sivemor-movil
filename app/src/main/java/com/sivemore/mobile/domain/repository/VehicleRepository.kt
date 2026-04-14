package com.sivemore.mobile.domain.repository

import com.sivemore.mobile.domain.model.Vehicle
import com.sivemore.mobile.domain.model.VehicleSummary

interface VehicleRepository {
    suspend fun loadVehicles(query: String): List<VehicleSummary>
    suspend fun loadVehicle(vehicleId: String): VehicleSummary?
    suspend fun loadVehicleForEdit(vehicleId: String): Vehicle?
    suspend fun saveVehicle(vehicle: Vehicle): Vehicle
}
