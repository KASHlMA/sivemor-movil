package com.sivemore.mobile.data.repository

import com.sivemore.mobile.data.network.MobileApiService
import com.sivemore.mobile.data.network.CreateVehicleRequestDto
import com.sivemore.mobile.data.network.InspectionDraftDto
import com.sivemore.mobile.data.network.toDomain
import com.sivemore.mobile.domain.model.Vehicle
import com.sivemore.mobile.domain.model.VehicleStatus
import com.sivemore.mobile.domain.model.VehicleOrder
import com.sivemore.mobile.domain.model.VehicleSummary
import com.sivemore.mobile.domain.repository.VehicleRepository
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
        val remoteVehicles = mobileApiService.listOrders().map { order ->
            order.toSummary(
                draft = order.draftInspectionId?.let { draftId ->
                    runCatching { mobileApiService.getInspection(draftId) }.getOrNull()
                },
            )
        }
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
                .map { order ->
                    order.toSummary(
                        draft = order.draftInspectionId?.let { draftId ->
                            runCatching { mobileApiService.getInspection(draftId) }.getOrNull()
                        },
                    )
                }
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

private fun com.sivemore.mobile.data.network.AssignedOrderDto.toSummary(
    draft: InspectionDraftDto?,
): VehicleSummary {
    val isPaused = draft?.status == "PAUSED"
    return VehicleSummary(
        id = orderUnitId.toString(),
        editableVehicleId = vehicleUnitId.toString(),
        plates = vehiclePlate,
        serialNumber = orderNumber,
        vehicleNumber = clientCompanyName,
        status = if (isPaused) VehicleStatus.Paused else VehicleStatus.Assigned,
        admissionDate = scheduledAt.toDisplayDateTime(),
        completedDate = regionName,
        hasPendingVerification = isPaused,
        draftInspectionId = draftInspectionId?.toString(),
    )
}

private fun String.toDisplayDateTime(): String = runCatching {
    Instant.parse(this)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
}.getOrDefault(this)
