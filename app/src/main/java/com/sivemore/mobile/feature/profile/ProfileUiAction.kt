package com.sivemore.mobile.feature.profile

import com.sivemore.mobile.domain.model.ProfileSummary

sealed interface ProfileUiAction {
    data object Refresh : ProfileUiAction
    data object SignOut : ProfileUiAction
}

data class ProfileUiState(
    val isLoading: Boolean = true,
    val summary: ProfileSummary? = null,
)

sealed interface ProfileEvent {
    data object SignedOut : ProfileEvent
}

