package com.sivemore.mobile.app.navigation

sealed class AppDestination(
    val route: String,
) {
    data object Auth : AppDestination(route = "auth")
    data object VehicleMenu : AppDestination(route = "vehicle_menu")
    data object VehicleLookup : AppDestination(route = "vehicle_lookup")
    data object VehicleRegistration : AppDestination(route = "vehicle_registration")
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
            Verification.route -> Verification
            SessionActions.route -> SessionActions
            else -> null
        }
    }
}
