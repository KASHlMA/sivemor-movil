package com.sivemore.mobile.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivemore.mobile.domain.model.AuthCredentials
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
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    fun onAction(action: AuthUiAction) {
        when (action) {
            is AuthUiAction.EmailChanged -> _uiState.update {
                it.copy(email = action.value, errorMessage = null)
            }

            is AuthUiAction.PasswordChanged -> _uiState.update {
                it.copy(password = action.value, errorMessage = null)
            }

            AuthUiAction.Submit -> signIn()
            AuthUiAction.ContinueAsGuest -> continueAsGuest()
        }
    }

    private fun signIn() {
        val state = uiState.value
        if (!state.isSubmitEnabled) {
            _uiState.update {
                it.copy(errorMessage = "Ingresa correo y contraseña para continuar.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.signIn(
                credentials = AuthCredentials(
                    email = state.email.trim(),
                    password = state.password,
                ),
            ).onSuccess {
                _uiState.update { current -> current.copy(isLoading = false) }
                _events.emit(AuthEvent.Authenticated)
            }.onFailure {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "No fue posible iniciar sesión.",
                    )
                }
            }
        }
    }

    private fun continueAsGuest() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.continueAsGuest()
            _uiState.update { it.copy(isLoading = false) }
            _events.emit(AuthEvent.Authenticated)
        }
    }
}
