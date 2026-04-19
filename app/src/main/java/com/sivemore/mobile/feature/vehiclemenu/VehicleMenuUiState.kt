package com.sivemore.mobile.feature.vehiclemenu

data class VehicleMenuUiState(
    val isSigningOut: Boolean = false,
    val showSignOutDialog: Boolean = false,
)

sealed interface VehicleMenuEvent {
    data object OpenVisualization : VehicleMenuEvent
    data object OpenRegistration : VehicleMenuEvent
    data object OpenReports : VehicleMenuEvent
    data object SignedOut : VehicleMenuEvent
}

sealed interface VehicleMenuUiAction {
    data object OpenVisualization : VehicleMenuUiAction
    data object OpenRegistration : VehicleMenuUiAction
    data object OpenReports : VehicleMenuUiAction
    data object SignOutTapped : VehicleMenuUiAction
    data object SignOutDismissed : VehicleMenuUiAction
    data object SignOutConfirmed : VehicleMenuUiAction
}
