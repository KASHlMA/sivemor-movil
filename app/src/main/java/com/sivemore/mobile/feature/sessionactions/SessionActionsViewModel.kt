package com.sivemore.mobile.feature.sessionactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivemore.mobile.domain.repository.VehicleRepository
import com.sivemore.mobile.domain.repository.VerificationRepository
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
class SessionActionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vehicleRepository: VehicleRepository,
    private val verificationRepository: VerificationRepository,
) : ViewModel() {

    private val vehicleId: String = checkNotNull(savedStateHandle["vehicleId"])

    private val _uiState = MutableStateFlow(SessionActionsUiState())
    val uiState: StateFlow<SessionActionsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SessionActionsEvent>()
    val events: SharedFlow<SessionActionsEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val vehicle = vehicleRepository.loadVehicle(vehicleId)
            _uiState.value = SessionActionsUiState(
                isLoading = false,
                vehicleLabel = vehicle?.plates.orEmpty(),
            )
        }
    }

    fun onAction(action: SessionActionsUiAction) {
        when (action) {
            SessionActionsUiAction.PauseTapped -> _uiState.update { it.copy(showPauseDialog = true) }
            SessionActionsUiAction.LogoutTapped -> _uiState.update { it.copy(showLogoutDialog = true) }
            SessionActionsUiAction.DismissDialogs -> _uiState.update {
                it.copy(showPauseDialog = false, showLogoutDialog = false)
            }

            SessionActionsUiAction.ConfirmPause -> pauseSession()
            SessionActionsUiAction.ConfirmLogout -> logout()
        }
    }

    private fun pauseSession() {
        viewModelScope.launch {
            verificationRepository.pauseSession(vehicleId)
            _uiState.update { it.copy(showPauseDialog = false) }
            _events.emit(SessionActionsEvent.BackToLookup)
        }
    }

    private fun logout() {
        viewModelScope.launch {
            verificationRepository.abandonSession(vehicleId)
            _uiState.update { it.copy(showLogoutDialog = false) }
            _events.emit(SessionActionsEvent.SignedOut)
        }
    }
}
