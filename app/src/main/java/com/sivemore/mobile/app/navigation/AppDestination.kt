package com.sivemore.mobile.app.navigation

sealed class AppDestination(
    val route: String,
) {
    data object Auth : AppDestination(route = "auth")
    data object VehicleMenu : AppDestination(route = "vehicle_menu")
    data object VehicleLookup : AppDestination(route = "vehicle_lookup")
    data object VehicleRegistration : AppDestination(route = "vehicle_registration")
    data object VerificationFlow : AppDestination(route = "verification_flow/{vehicleId}") {
        fun createRoute(vehicleId: String): String = "verification_flow/$vehicleId"
    }
    data object Luces : AppDestination(route = "luces")
    data object Llantas : AppDestination(route = "llantas")
    data object Direccion : AppDestination(route = "direccion")
    data object AireFrenos : AppDestination(route = "aire_frenos")
    data object Motor : AppDestination(route = "motor")
    data object Otros : AppDestination(route = "otros")
    data object Evidencias : AppDestination(route = "evidencias")
    data object InspectionNextSection : AppDestination(route = "inspection_next_section")
    data object Verification : AppDestination(route = "verification/{vehicleId}") {
        fun createRoute(vehicleId: String): String = "verification/$vehicleId"
    }

    data object SessionActions : AppDestination(route = "session_actions/{vehicleId}") {
        fun createRoute(vehicleId: String): String = "session_actions/$vehicleId"
    }

    companion object {
        fun fromRoute(route: String?): AppDestination? = when (route) {
            Auth.route -> Auth
            VehicleMenu.route -> VehicleMenu
            VehicleLookup.route -> VehicleLookup
            VehicleRegistration.route -> VehicleRegistration
            VerificationFlow.route -> VerificationFlow
            Luces.route -> Luces
            Llantas.route -> Llantas
            Direccion.route -> Direccion
            AireFrenos.route -> AireFrenos
            Motor.route -> Motor
            Otros.route -> Otros
            Evidencias.route -> Evidencias
            InspectionNextSection.route -> InspectionNextSection
            Verification.route -> Verification
            SessionActions.route -> SessionActions
            else -> null
        }
    }
}
