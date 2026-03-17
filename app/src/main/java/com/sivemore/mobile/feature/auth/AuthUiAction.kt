package com.sivemore.mobile.feature.auth

sealed interface AuthUiAction {
    data class EmailChanged(val value: String) : AuthUiAction
    data class PasswordChanged(val value: String) : AuthUiAction
    data object Submit : AuthUiAction
    data object ContinueAsGuest : AuthUiAction
}

