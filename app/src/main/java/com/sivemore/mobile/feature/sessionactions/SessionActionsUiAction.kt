package com.sivemore.mobile.feature.sessionactions

sealed interface SessionActionsUiAction {
    data object PauseTapped : SessionActionsUiAction
    data object LogoutTapped : SessionActionsUiAction
    data object DismissDialogs : SessionActionsUiAction
    data object ConfirmPause : SessionActionsUiAction
    data object ConfirmLogout : SessionActionsUiAction
}

data class SessionActionsUiState(
    val isLoading: Boolean = true,
    val vehicleLabel: String = "",
    val showPauseDialog: Boolean = false,
    val showLogoutDialog: Boolean = false,
)

sealed interface SessionActionsEvent {
    data object BackToLookup : SessionActionsEvent
    data object SignedOut : SessionActionsEvent
}
