package com.sivemore.mobile.data.repository

import com.sivemore.mobile.data.network.MobileApiService
import com.sivemore.mobile.data.network.CreateVehicleRequestDto
import com.sivemore.mobile.data.network.toDomain
import com.sivemore.mobile.domain.model.Vehicle
import com.sivemore.mobile.domain.model.VehicleOrder
import com.sivemore.mobile.domain.model.VehicleSummary
import com.sivemore.mobile.domain.repository.VehicleRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealVehicleRepository @Inject constructor(
    private val mobileApiService: MobileApiService,
    private val registrationStore: VehicleRegistrationStore,
) : VehicleRepository {
    override suspend fun loadClients() = mobileApiService.listClients().map { it.toDomain() }

    override suspend fun loadRegions() = mobileApiService.listRegions().map { it.toDomain() }

    override suspend fun loadOrders(): List<VehicleOrder> = emptyList()

    override suspend fun loadVehicles(query: String): List<VehicleSummary> {
        val normalizedQuery = query.trim()
        val remoteVehicles = mobileApiService.listOrders().map { it.toDomain() }
        val localVehicles = registrationStore.loadVehicles()
        val vehicles = (localVehicles + remoteVehicles).distinctBy { it.id }
        if (normalizedQuery.isBlank()) return vehicles
        return vehicles.filter { vehicle ->
            listOf(vehicle.plates, vehicle.serialNumber, vehicle.vehicleNumber)
                .any { it.contains(normalizedQuery, ignoreCase = true) }
        }
    }

    override suspend fun loadVehicle(vehicleId: String): VehicleSummary? =
        registrationStore.loadVehicle(vehicleId)
            ?: mobileApiService.listOrders()
                .map { it.toDomain() }
                .firstOrNull { it.id == vehicleId }

    override suspend fun loadVehicleForEdit(vehicleId: String): Vehicle? =
        registrationStore.loadEditableVehicle(vehicleId)
            ?: mobileApiService.getVehicle(vehicleId.toLong()).toDomain()

    override suspend fun saveVehicle(vehicle: Vehicle): Vehicle {
        val request = CreateVehicleRequestDto(
            clientCompanyId = vehicle.numeroEconomico.trim().toLongOrNull()
                ?: error("El numero de cliente debe ser numerico."),
            plate = vehicle.placas,
            vin = vehicle.vin,
            category = vehicle.tipoVehiculo.ifBlank { "N2" },
            brand = vehicle.marca.ifBlank { "Sin marca" },
            model = vehicle.modelo.ifBlank { "Sin modelo" },
        )

        val remoteVehicleId = vehicle.id.trim().toLongOrNull()
        return if (remoteVehicleId != null) {
            mobileApiService.updateVehicle(remoteVehicleId, request).toDomain()
        } else {
            mobileApiService.createVehicle(request).toDomain()
        }
    }
}
