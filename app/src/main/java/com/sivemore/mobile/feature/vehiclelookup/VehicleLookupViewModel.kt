package com.sivemore.mobile.feature.vehiclelookup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class VehicleLookupViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehicleLookupUiState())
    val uiState: StateFlow<VehicleLookupUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<VehicleLookupEvent>()
    val events: SharedFlow<VehicleLookupEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun onAction(action: VehicleLookupUiAction) {
        when (action) {
            VehicleLookupUiAction.Refresh,
            VehicleLookupUiAction.SearchSubmitted -> refresh()

            VehicleLookupUiAction.PendingDialogDismissed -> _uiState.update {
                it.copy(pendingVehicle = null)
            }

            VehicleLookupUiAction.PendingDialogConfirmed -> continuePendingVehicle()
            is VehicleLookupUiAction.QueryChanged -> _uiState.update { it.copy(query = action.value) }
            is VehicleLookupUiAction.VehicleTapped -> onVehicleTapped(action.vehicleId)
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val vehicles = vehicleRepository.loadVehicles(uiState.value.query)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    vehicles = vehicles,
                )
            }
        }
    }

    private fun onVehicleTapped(vehicleId: String) {
        viewModelScope.launch {
            val vehicle = vehicleRepository.loadVehicle(vehicleId) ?: return@launch
            if (vehicle.hasPendingVerification) {
                _uiState.update { it.copy(pendingVehicle = vehicle) }
            } else {
                _events.emit(VehicleLookupEvent.OpenVerification(vehicleId))
            }
        }
    }

    private fun continuePendingVehicle() {
        val pendingVehicle = uiState.value.pendingVehicle ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(pendingVehicle = null) }
            _events.emit(VehicleLookupEvent.OpenVerification(pendingVehicle.id))
        }
    }
}
