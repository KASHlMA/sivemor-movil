package com.sivemore.mobile.feature.vehiclemenu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivemore.mobile.domain.repository.AuthRepository
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
class VehicleMenuViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(VehicleMenuUiState())
    val uiState: StateFlow<VehicleMenuUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<VehicleMenuEvent>()
    val events: SharedFlow<VehicleMenuEvent> = _events.asSharedFlow()

    fun onAction(action: VehicleMenuUiAction) {
        when (action) {
            VehicleMenuUiAction.OpenRegistration -> emitEvent(VehicleMenuEvent.OpenRegistration)
            VehicleMenuUiAction.OpenVisualization -> emitEvent(VehicleMenuEvent.OpenVisualization)
            VehicleMenuUiAction.OpenReports -> emitEvent(VehicleMenuEvent.OpenReports)
            VehicleMenuUiAction.SignOutTapped -> _uiState.update { it.copy(showSignOutDialog = true) }
            VehicleMenuUiAction.SignOutDismissed -> _uiState.update { it.copy(showSignOutDialog = false) }
            VehicleMenuUiAction.SignOutConfirmed -> signOut()
        }
    }

    private fun emitEvent(event: VehicleMenuEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningOut = true, showSignOutDialog = false) }
            runCatching { authRepository.signOut() }
            _uiState.update { it.copy(isSigningOut = false) }
            _events.emit(VehicleMenuEvent.SignedOut)
        }
    }
}
