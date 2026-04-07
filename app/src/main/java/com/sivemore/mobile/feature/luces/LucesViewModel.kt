package com.sivemore.mobile.feature.luces

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivemore.mobile.feature.inspection.InspectionSectionCatalog
import com.sivemore.mobile.feature.inspection.InspectionSectionUiState
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
class LucesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val vehicleId: String = checkNotNull(savedStateHandle["vehicleId"])

    private val _uiState = MutableStateFlow(
        LucesUiState(
            vehicleId = vehicleId,
            section = InspectionSectionCatalog.lucesSection(),
        )
    )
    val uiState: StateFlow<LucesUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LucesEvent>()
    val events: SharedFlow<LucesEvent> = _events.asSharedFlow()

    fun onAction(action: LucesUiAction) {
        when (action) {
            is LucesUiAction.OptionSelected -> selectOption(action.questionId, action.optionId)
            LucesUiAction.NextClicked -> navigateNextIfComplete()
        }
    }

    private fun selectOption(questionId: String, optionId: String) {
        _uiState.update { current ->
            current.copy(
                section = current.section.copy(
                    questions = current.section.questions.map { question ->
                        if (question.id == questionId) {
                            question.copy(selectedOptionId = optionId)
                        } else {
                            question
                        }
                    }
                )
            )
        }
    }

    private fun navigateNextIfComplete() {
        if (!_uiState.value.isNextEnabled) return
        viewModelScope.launch {
            _events.emit(LucesEvent.NavigateToNextSection(vehicleId))
        }
    }
}

data class LucesUiState(
    val vehicleId: String,
    val section: InspectionSectionUiState,
) {
    val isNextEnabled: Boolean
        get() = section.isComplete
}

sealed interface LucesUiAction {
    data class OptionSelected(
        val questionId: String,
        val optionId: String,
    ) : LucesUiAction

    data object NextClicked : LucesUiAction
}

sealed interface LucesEvent {
    data class NavigateToNextSection(val vehicleId: String) : LucesEvent
}
