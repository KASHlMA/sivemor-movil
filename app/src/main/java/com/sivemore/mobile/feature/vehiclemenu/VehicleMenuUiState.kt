package com.sivemore.mobile.feature.vehiclemenu

data class VehicleMenuUiState(
    val isSigningOut: Boolean = false,
)

sealed interface VehicleMenuEvent {
    data object OpenVisualization : VehicleMenuEvent
    data object OpenRegistration : VehicleMenuEvent
    data object SignedOut : VehicleMenuEvent
}

sealed interface VehicleMenuUiAction {
    data object OpenVisualization : VehicleMenuUiAction
    data object OpenRegistration : VehicleMenuUiAction
    data object SignOut : VehicleMenuUiAction
}
