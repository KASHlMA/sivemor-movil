package com.sivemore.mobile.feature.sessionactions

sealed interface SessionActionsUiAction {
    data object PauseTapped : SessionActionsUiAction
    data object AbandonTapped : SessionActionsUiAction
    data object SignOutTapped : SessionActionsUiAction
    data object DismissDialogs : SessionActionsUiAction
    data object ConfirmPause : SessionActionsUiAction
    data object ConfirmAbandon : SessionActionsUiAction
    data object ConfirmSignOut : SessionActionsUiAction
}

data class SessionActionsUiState(
    val isLoading: Boolean = true,
    val vehicleLabel: String = "",
    val showPauseDialog: Boolean = false,
    val showAbandonDialog: Boolean = false,
    val showSignOutDialog: Boolean = false,
)

sealed interface SessionActionsEvent {
    data object BackToLookup : SessionActionsEvent
    data object SignedOut : SessionActionsEvent
}
