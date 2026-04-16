package com.sivemore.mobile.feature.vehicleregistration

import com.sivemore.mobile.domain.model.VehicleClient
import com.sivemore.mobile.domain.model.VehicleRegion

data class VehicleFormFieldState(
    val value: String = "",
    val errorMessage: String? = null,
)

data class VehicleRegistrationUiState(
    val vehicleId: String? = null,
    val placa: VehicleFormFieldState = VehicleFormFieldState(),
    val serie: VehicleFormFieldState = VehicleFormFieldState(),
    val tipo: VehicleFormFieldState = VehicleFormFieldState(),
    val cliente: VehicleFormFieldState = VehicleFormFieldState(),
    val cedis: VehicleFormFieldState = VehicleFormFieldState(),
    val marca: VehicleFormFieldState = VehicleFormFieldState(),
    val modelo: VehicleFormFieldState = VehicleFormFieldState(),
    val clients: List<VehicleClient> = emptyList(),
    val regions: List<VehicleRegion> = emptyList(),
    val showTipoMenu: Boolean = false,
    val showClienteMenu: Boolean = false,
    val showCedisMenu: Boolean = false,
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
            tipo.value.isNotBlank() &&
            cliente.value.isNotBlank() &&
            cedis.value.isNotBlank() &&
            marca.value.isNotBlank() &&
            modelo.value.isNotBlank()
}

sealed interface VehicleRegistrationUiAction {
    data class PlacaChanged(val value: String) : VehicleRegistrationUiAction
    data class SerieChanged(val value: String) : VehicleRegistrationUiAction
    data class TipoSelected(val value: String) : VehicleRegistrationUiAction
    data class ClienteSelected(val value: String) : VehicleRegistrationUiAction
    data class CedisSelected(val value: String) : VehicleRegistrationUiAction
    data class MarcaChanged(val value: String) : VehicleRegistrationUiAction
    data class ModeloChanged(val value: String) : VehicleRegistrationUiAction
    data object TipoMenuToggled : VehicleRegistrationUiAction
    data object TipoMenuDismissed : VehicleRegistrationUiAction
    data object ClienteMenuToggled : VehicleRegistrationUiAction
    data object ClienteMenuDismissed : VehicleRegistrationUiAction
    data object CedisMenuToggled : VehicleRegistrationUiAction
    data object CedisMenuDismissed : VehicleRegistrationUiAction
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
