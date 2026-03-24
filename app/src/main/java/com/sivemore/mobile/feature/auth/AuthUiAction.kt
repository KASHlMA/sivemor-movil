package com.sivemore.mobile.feature.auth

sealed interface AuthUiAction {
    data class UsernameChanged(val value: String) : AuthUiAction
    data class PasswordChanged(val value: String) : AuthUiAction
    data object Submit : AuthUiAction
}
