package com.sivemore.mobile.feature.vehicleregistration

data class VehicleFormFieldState(
    val value: String = "",
    val errorMessage: String? = null,
)

data class VehicleRegistrationUiState(
    val vehicleId: String? = null,
    val placa: VehicleFormFieldState = VehicleFormFieldState(),
    val serie: VehicleFormFieldState = VehicleFormFieldState(),
    val cedis: VehicleFormFieldState = VehicleFormFieldState(),
    val numeroCliente: VehicleFormFieldState = VehicleFormFieldState(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val showOptionsMenu: Boolean = false,
    val showSignOutDialog: Boolean = false,
    val globalErrorMessage: String? = null,
) {
    val isEditing: Boolean
        get() = !vehicleId.isNullOrBlank()

    val isFormValid: Boolean
        get() = placa.value.isNotBlank() &&
            serie.value.isNotBlank() &&
            cedis.value.isNotBlank() &&
            numeroCliente.value.isNotBlank()
}

sealed interface VehicleRegistrationUiAction {
    data class PlacaChanged(val value: String) : VehicleRegistrationUiAction
    data class SerieChanged(val value: String) : VehicleRegistrationUiAction
    data class CedisChanged(val value: String) : VehicleRegistrationUiAction
    data class NumeroClienteChanged(val value: String) : VehicleRegistrationUiAction
    data object OptionsMenuToggled : VehicleRegistrationUiAction
    data object OptionsMenuDismissed : VehicleRegistrationUiAction
    data object BackToMenuSelected : VehicleRegistrationUiAction
    data object SignOutSelected : VehicleRegistrationUiAction
    data object SignOutDismissed : VehicleRegistrationUiAction
    data object SignOutConfirmed : VehicleRegistrationUiAction
    data object SaveVehicle : VehicleRegistrationUiAction
}

sealed interface VehicleRegistrationEvent {
    data class VehicleSaved(val vehicleId: String) : VehicleRegistrationEvent
    data object BackToMenu : VehicleRegistrationEvent
    data object SignedOut : VehicleRegistrationEvent
}
