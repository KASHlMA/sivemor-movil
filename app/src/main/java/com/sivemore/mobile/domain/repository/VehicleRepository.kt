package com.sivemore.mobile.domain.repository

import com.sivemore.mobile.domain.model.Vehicle
import com.sivemore.mobile.domain.model.VehicleClient
import com.sivemore.mobile.domain.model.VehicleOrder
import com.sivemore.mobile.domain.model.VehicleRegion
import com.sivemore.mobile.domain.model.VehicleSummary

interface VehicleRepository {
    suspend fun loadClients(): List<VehicleClient>
    suspend fun loadRegions(): List<VehicleRegion>
    suspend fun loadOrders(): List<VehicleOrder>
    suspend fun loadVehicles(query: String): List<VehicleSummary>
    suspend fun loadVehicle(vehicleId: String): VehicleSummary?
    suspend fun loadVehicleForEdit(vehicleId: String): Vehicle?
    suspend fun saveVehicle(vehicle: Vehicle): Vehicle
}
