package com.sivemore.mobile.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivemore.mobile.domain.model.CompletedReport
import com.sivemore.mobile.domain.model.ReportVerdict
import com.sivemore.mobile.domain.repository.AuthRepository
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
class ReportsViewModel @Inject constructor(
    private val verificationRepository: VerificationRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ReportsEvent>()
    val events: SharedFlow<ReportsEvent> = _events.asSharedFlow()

    init {
        load(initialLoad = true)
    }

    fun onAction(action: ReportsUiAction) {
        when (action) {
            ReportsUiAction.Refresh -> load()
            ReportsUiAction.NavigateBack -> viewModelScope.launch { _events.emit(ReportsEvent.NavigateBack) }
            ReportsUiAction.LogoutRequested -> signOut()
        }
    }

    private fun load(initialLoad: Boolean = false) {
        viewModelScope.launch {
            val hasContent = _uiState.value.reports.isNotEmpty()
            _uiState.update {
                it.copy(
                    isLoading = initialLoad || !hasContent,
                    isRefreshing = !initialLoad && hasContent,
                    errorMessage = null,
                )
            }
            runCatching {
                verificationRepository.loadCompletedReports()
            }.onSuccess { reports ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        reports = reports,
                    )
                }
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = failure.message ?: "No fue posible cargar los reportes.",
                    )
                }
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _events.emit(ReportsEvent.SignedOut)
        }
    }
}

data class ReportsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val reports: List<CompletedReport> = emptyList(),
    val errorMessage: String? = null,
)

sealed interface ReportsUiAction {
    data object Refresh : ReportsUiAction
    data object NavigateBack : ReportsUiAction
    data object LogoutRequested : ReportsUiAction
}

sealed interface ReportsEvent {
    data object NavigateBack : ReportsEvent
    data object SignedOut : ReportsEvent
}
