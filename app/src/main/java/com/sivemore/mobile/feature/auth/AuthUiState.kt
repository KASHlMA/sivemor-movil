package com.sivemore.mobile.feature.auth

data class AuthUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val isSubmitEnabled: Boolean
        get() = username.isNotBlank() && password.isNotBlank() && !isLoading
}

sealed interface AuthEvent {
    data object Authenticated : AuthEvent
}
