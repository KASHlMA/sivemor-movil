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
        loadCatalogs()
        if (!vehicleId.isNullOrBlank()) {
            loadVehicleForEdit(vehicleId)
        }
    }

    fun onAction(action: VehicleRegistrationUiAction) {
        when (action) {
            is VehicleRegistrationUiAction.PlacaChanged -> updatePlaca(action.value)
            is VehicleRegistrationUiAction.SerieChanged -> updateSerie(action.value)
            is VehicleRegistrationUiAction.TipoSelected -> updateTipo(action.value)
            is VehicleRegistrationUiAction.ClienteSelected -> updateCliente(action.value)
            is VehicleRegistrationUiAction.CedisSelected -> updateCedis(action.value)
            is VehicleRegistrationUiAction.OrdenSelected -> updateOrden(action.value)
            is VehicleRegistrationUiAction.MarcaChanged -> updateMarca(action.value)
            is VehicleRegistrationUiAction.ModeloChanged -> updateModelo(action.value)
            VehicleRegistrationUiAction.TipoMenuToggled -> _uiState.update {
                it.copy(showTipoMenu = !it.showTipoMenu, globalErrorMessage = null)
            }
            VehicleRegistrationUiAction.TipoMenuDismissed -> _uiState.update {
                it.copy(showTipoMenu = false)
            }
            VehicleRegistrationUiAction.ClienteMenuToggled -> _uiState.update {
                it.copy(showClienteMenu = !it.showClienteMenu, globalErrorMessage = null)
            }
            VehicleRegistrationUiAction.ClienteMenuDismissed -> _uiState.update {
                it.copy(showClienteMenu = false)
            }
            VehicleRegistrationUiAction.CedisMenuToggled -> _uiState.update {
                it.copy(showCedisMenu = !it.showCedisMenu, globalErrorMessage = null)
            }
            VehicleRegistrationUiAction.CedisMenuDismissed -> _uiState.update {
                it.copy(showCedisMenu = false)
            }
            VehicleRegistrationUiAction.OrdenMenuToggled -> _uiState.update {
                it.copy(showOrdenMenu = !it.showOrdenMenu, globalErrorMessage = null)
            }
            VehicleRegistrationUiAction.OrdenMenuDismissed -> _uiState.update {
                it.copy(showOrdenMenu = false)
            }
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

    private fun loadCatalogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, globalErrorMessage = null) }
            runCatching {
                Triple(
                    vehicleRepository.loadClients(),
                    vehicleRepository.loadRegions(),
                    vehicleRepository.loadOrders(),
                )
            }.onSuccess { (clients, regions, orders) ->
                _uiState.update {
                    it.copy(
                        clients = clients,
                        regions = regions,
                        orders = orders,
                        isLoading = false,
                        globalErrorMessage = null,
                    )
                }
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        globalErrorMessage = failure.message ?: "No fue posible cargar los catalogos.",
                    )
                }
            }
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

    private fun updateTipo(value: String) {
        _uiState.update {
            it.copy(
                tipo = VehicleFormFieldState(
                    value = value,
                    errorMessage = validateRequired(value),
                ),
                showTipoMenu = false,
                globalErrorMessage = null,
            )
        }
    }

    private fun updateCliente(value: String) {
        _uiState.update {
            val selectedClient = it.clients.firstOrNull { client -> client.id == value }
            it.copy(
                cliente = VehicleFormFieldState(
                    value = value,
                    errorMessage = validateRequired(value),
                ),
                cedis = if (!selectedClient?.regionId.isNullOrBlank()) {
                    VehicleFormFieldState(value = selectedClient?.regionId.orEmpty())
                } else {
                    it.cedis
                },
                orden = VehicleFormFieldState(),
                showClienteMenu = false,
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
                showCedisMenu = false,
                globalErrorMessage = null,
            )
        }
    }

    private fun updateOrden(value: String) {
        _uiState.update {
            val selectedOrder = it.orders.firstOrNull { order -> order.id == value }
            it.copy(
                orden = VehicleFormFieldState(
                    value = value,
                    errorMessage = validateRequired(value),
                ),
                cliente = selectedOrder?.let { order ->
                    VehicleFormFieldState(value = order.clientCompanyId, errorMessage = null)
                } ?: it.cliente,
                showOrdenMenu = false,
                globalErrorMessage = null,
            )
        }
    }

    private fun updateMarca(value: String) {
        _uiState.update {
            it.copy(
                marca = VehicleFormFieldState(
                    value = value,
                    errorMessage = validateRequired(value),
                ),
                globalErrorMessage = null,
            )
        }
    }

    private fun updateModelo(value: String) {
        _uiState.update {
            it.copy(
                modelo = VehicleFormFieldState(
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
                        numeroEconomico = validatedState.cliente.value,
                        placas = validatedState.placa.value,
                        marca = validatedState.marca.value,
                        modelo = validatedState.modelo.value,
                        tipoVehiculo = validatedState.tipo.value,
                        vin = validatedState.serie.value,
                        verificationOrderId = validatedState.orden.value,
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
                        tipo = VehicleFormFieldState(value = vehicle.tipoVehiculo),
                        cliente = VehicleFormFieldState(value = vehicle.numeroEconomico),
                        orden = VehicleFormFieldState(value = vehicle.verificationOrderId.orEmpty()),
                        cedis = VehicleFormFieldState(
                            value = it.clients.firstOrNull { client -> client.id == vehicle.numeroEconomico }
                                ?.regionId
                                .orEmpty(),
                        ),
                        marca = VehicleFormFieldState(value = vehicle.marca),
                        modelo = VehicleFormFieldState(value = vehicle.modelo),
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
    tipo = tipo.copy(errorMessage = validateRequired(tipo.value)),
    cliente = cliente.copy(errorMessage = validateRequired(cliente.value)),
    cedis = cedis.copy(errorMessage = validateRequired(cedis.value)),
    orden = orden.copy(errorMessage = null),
    marca = marca.copy(errorMessage = validateRequired(marca.value)),
    modelo = modelo.copy(errorMessage = validateRequired(modelo.value)),
)

private fun VehicleRegistrationUiState.hasErrors(): Boolean = listOf(
    placa.errorMessage,
    serie.errorMessage,
    tipo.errorMessage,
    cliente.errorMessage,
    cedis.errorMessage,
    marca.errorMessage,
    modelo.errorMessage,
).any { it != null }

private fun validateRequired(value: String): String? =
    if (value.isBlank()) "Este campo es obligatorio" else null
