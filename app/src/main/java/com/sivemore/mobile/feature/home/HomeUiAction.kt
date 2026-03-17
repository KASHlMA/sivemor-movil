package com.sivemore.mobile.feature.home

import com.sivemore.mobile.domain.model.HomeOverview

sealed interface HomeUiAction {
    data object Refresh : HomeUiAction
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val overview: HomeOverview? = null,
)

