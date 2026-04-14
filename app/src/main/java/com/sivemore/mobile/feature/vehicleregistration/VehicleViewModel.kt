package com.sivemore.mobile.feature.vehicleregistration

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivemore.mobile.domain.model.Vehicle
import com.sivemore.mobile.domain.repository.AuthRepository
import com.sivemore.mobile.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class VehicleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vehicleRepository: VehicleRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val vehicleId: String? = savedStateHandle["vehicleId"]
    private val _uiState = MutableStateFlow(
        VehicleRegistrationUiState(
            vehicleId = vehicleId,
            isLoading = !vehicleId.isNullOrBlank(),
        ),
    )
    val uiState: StateFlow<VehicleRegistrationUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<VehicleRegistrationEvent>()
    val events: SharedFlow<VehicleRegistrationEvent> = _events.asSharedFlow()

    init {
        if (!vehicleId.isNullOrBlank()) {
            loadVehicleForEdit(vehicleId)
        }
    }

    fun onAction(action: VehicleRegistrationUiAction) {
        when (action) {
            is VehicleRegistrationUiAction.PlacaChanged -> updatePlaca(action.value)
            is VehicleRegistrationUiAction.SerieChanged -> updateSerie(action.value)
            is VehicleRegistrationUiAction.CedisChanged -> updateCedis(action.value)
            is VehicleRegistrationUiAction.NumeroClienteChanged -> updateNumeroCliente(action.value)
            VehicleRegistrationUiAction.OptionsMenuToggled -> _uiState.update {
                it.copy(showOptionsMenu = !it.showOptionsMenu, globalErrorMessage = null)
            }
            VehicleRegistrationUiAction.OptionsMenuDismissed -> _uiState.update {
                it.copy(showOptionsMenu = false)
            }
            VehicleRegistrationUiAction.BackToMenuSelected -> backToMenu()
            VehicleRegistrationUiAction.SignOutSelected -> _uiState.update {
                it.copy(showOptionsMenu = false, showSignOutDialog = true)
            }
            VehicleRegistrationUiAction.SignOutDismissed -> _uiState.update {
                it.copy(showSignOutDialog = false)
            }
            VehicleRegistrationUiAction.SignOutConfirmed -> signOut()
            VehicleRegistrationUiAction.SaveVehicle -> saveVehicle()
        }
    }

    private fun updatePlaca(value: String) {
        _uiState.update {
            it.copy(
                placa = VehicleFormFieldState(
                    value = value,
                    errorMessage = validateRequired(value),
                ),
                globalErrorMessage = null,
            )
        }
    }

    private fun updateSerie(value: String) {
        _uiState.update {
            it.copy(
                serie = VehicleFormFieldState(
                    value = value,
                    errorMessage = validateRequired(value),
                ),
                globalErrorMessage = null,
            )
        }
    }

    private fun updateCedis(value: String) {
        _uiState.update {
            it.copy(
                cedis = VehicleFormFieldState(
                    value = value,
                    errorMessage = validateRequired(value),
                ),
                globalErrorMessage = null,
            )
        }
    }

    private fun updateNumeroCliente(value: String) {
        _uiState.update {
            it.copy(
                numeroCliente = VehicleFormFieldState(
                    value = value,
                    errorMessage = validateRequired(value),
                ),
                globalErrorMessage = null,
            )
        }
    }

    private fun saveVehicle() {
        val validatedState = uiState.value.validated()
        _uiState.value = validatedState
        if (!validatedState.isFormValid || validatedState.hasErrors()) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, globalErrorMessage = null) }
            runCatching {
                vehicleRepository.saveVehicle(
                    Vehicle(
                        id = validatedState.vehicleId ?: "local-${System.currentTimeMillis()}",
                        numeroEconomico = validatedState.numeroCliente.value,
                        placas = validatedState.placa.value,
                        marca = validatedState.cedis.value,
                        modelo = "",
                        tipoVehiculo = "",
                        vin = validatedState.serie.value,
                    ),
                )
            }.onSuccess { vehicle ->
                _uiState.update { it.copy(isSaving = false) }
                _events.emit(VehicleRegistrationEvent.VehicleSaved(vehicle.id))
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        globalErrorMessage = failure.message ?: "No fue posible guardar el vehiculo.",
                    )
                }
            }
        }
    }

    private fun loadVehicleForEdit(vehicleId: String) {
        viewModelScope.launch {
            runCatching {
                vehicleRepository.loadVehicleForEdit(vehicleId)
            }.onSuccess { vehicle ->
                if (vehicle == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            globalErrorMessage = "No fue posible cargar la informacion del vehiculo.",
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        vehicleId = vehicle.id,
                        placa = VehicleFormFieldState(value = vehicle.placas),
                        serie = VehicleFormFieldState(value = vehicle.vin),
                        cedis = VehicleFormFieldState(value = vehicle.marca),
                        numeroCliente = VehicleFormFieldState(value = vehicle.numeroEconomico),
                        isLoading = false,
                        globalErrorMessage = null,
                    )
                }
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        globalErrorMessage = failure.message ?: "No fue posible cargar la informacion del vehiculo.",
                    )
                }
            }
        }
    }

    private fun backToMenu() {
        viewModelScope.launch {
            _uiState.update { it.copy(showOptionsMenu = false) }
            _events.emit(VehicleRegistrationEvent.BackToMenu)
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(showSignOutDialog = false, showOptionsMenu = false) }
            authRepository.signOut()
            _events.emit(VehicleRegistrationEvent.SignedOut)
        }
    }
}

private fun VehicleRegistrationUiState.validated(): VehicleRegistrationUiState = copy(
    placa = placa.copy(errorMessage = validateRequired(placa.value)),
    serie = serie.copy(errorMessage = validateRequired(serie.value)),
    cedis = cedis.copy(errorMessage = validateRequired(cedis.value)),
    numeroCliente = numeroCliente.copy(errorMessage = validateRequired(numeroCliente.value)),
)

private fun VehicleRegistrationUiState.hasErrors(): Boolean = listOf(
    placa.errorMessage,
    serie.errorMessage,
    cedis.errorMessage,
    numeroCliente.errorMessage,
).any { it != null }

private fun validateRequired(value: String): String? =
    if (value.isBlank()) "Este campo es obligatorio" else null
