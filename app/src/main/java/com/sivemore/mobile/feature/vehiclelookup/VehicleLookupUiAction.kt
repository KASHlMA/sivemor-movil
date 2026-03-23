package com.sivemore.mobile.feature.vehiclelookup

import com.sivemore.mobile.domain.model.VehicleSummary

sealed interface VehicleLookupUiAction {
    data object Refresh : VehicleLookupUiAction
    data class QueryChanged(val value: String) : VehicleLookupUiAction
    data object SearchSubmitted : VehicleLookupUiAction
    data class VehicleTapped(val vehicleId: String) : VehicleLookupUiAction
    data object PendingDialogDismissed : VehicleLookupUiAction
    data object PendingDialogConfirmed : VehicleLookupUiAction
}

data class VehicleLookupUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val vehicles: List<VehicleSummary> = emptyList(),
    val pendingVehicle: VehicleSummary? = null,
)

sealed interface VehicleLookupEvent {
    data class OpenVerification(val vehicleId: String) : VehicleLookupEvent
}
