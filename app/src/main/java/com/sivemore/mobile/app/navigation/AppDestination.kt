package com.sivemore.mobile.app.navigation

sealed class AppDestination(
    val route: String,
) {
    data object Auth : AppDestination(route = "auth")
    data object VehicleLookup : AppDestination(route = "vehicle_lookup")
    data object Verification : AppDestination(route = "verification/{vehicleId}") {
        fun createRoute(vehicleId: String): String = "verification/$vehicleId"
    }

    data object SessionActions : AppDestination(route = "session_actions/{vehicleId}") {
        fun createRoute(vehicleId: String): String = "session_actions/$vehicleId"
    }

    companion object {
        fun fromRoute(route: String?): AppDestination? = when (route) {
            Auth.route -> Auth
            VehicleLookup.route -> VehicleLookup
            Verification.route -> Verification
            SessionActions.route -> SessionActions
            else -> null
        }
    }
}
