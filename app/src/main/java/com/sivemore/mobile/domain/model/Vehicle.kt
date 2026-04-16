package com.sivemore.mobile.domain.model

data class Vehicle(
    val id: String,
    val numeroEconomico: String,
    val placas: String,
    val marca: String,
    val modelo: String,
    val tipoVehiculo: String,
    val vin: String,
    val verificationOrderId: String? = null,
)

data class VehicleClient(
    val id: String,
    val name: String,
    val regionId: String?,
)

data class VehicleRegion(
    val id: String,
    val name: String,
)

data class VehicleOrder(
    val id: String,
    val orderNumber: String,
    val clientCompanyId: String,
    val clientCompanyName: String,
    val vehiclePlate: String,
) {
    val displayName: String
        get() = "$orderNumber - $clientCompanyName - $vehiclePlate"
}
