package com.sivemore.mobile.feature.verification

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivemore.mobile.domain.model.EvidenceSource
import com.sivemore.mobile.domain.model.InspectionCategory
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
class VerificationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val verificationRepository: VerificationRepository,
) : ViewModel() {

    private val vehicleId: String = checkNotNull(savedStateHandle["vehicleId"])

    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<VerificationEvent>()
    val events: SharedFlow<VerificationEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun onAction(action: VerificationUiAction) {
        when (action) {
            is VerificationUiAction.CategorySelected -> setCategory(action.category)
            is VerificationUiAction.OptionToggled -> updateOption(action.itemId, action.optionId)
            is VerificationUiAction.NoteChanged -> updateNote(action.itemId, action.value)
            is VerificationUiAction.NumericChanged -> updateNumeric(action.itemId, action.value)
            is VerificationUiAction.EvidenceSourceSelected -> addEvidence(action.source)
            is VerificationUiAction.RemoveEvidence -> removeEvidence(action.evidenceId)
            is VerificationUiAction.CommentDraftChanged -> _uiState.update { it.copy(commentDraft = action.value) }
            VerificationUiAction.AddEvidenceRequested -> _uiState.update { it.copy(showEvidenceDialog = true) }
            VerificationUiAction.EvidenceDialogDismissed -> _uiState.update { it.copy(showEvidenceDialog = false) }
            VerificationUiAction.AddCommentRequested -> _uiState.update {
                it.copy(
                    showCommentDialog = true,
                    commentDraft = it.session?.comments.orEmpty(),
                )
            }

            VerificationUiAction.CommentDialogDismissed -> _uiState.update { it.copy(showCommentDialog = false) }
            VerificationUiAction.CommentSaved -> saveComments()
            VerificationUiAction.SubmitRequested -> _uiState.update { it.copy(showSubmitDialog = true) }
            VerificationUiAction.SubmitDismissed -> _uiState.update { it.copy(showSubmitDialog = false) }
            VerificationUiAction.SubmitConfirmed -> completeVerification()
            VerificationUiAction.SessionActionsRequested -> openSessionActions()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            val session = verificationRepository.loadSession(vehicleId)
            _uiState.value = VerificationUiState(
                isLoading = false,
                session = session,
                commentDraft = session.comments,
            )
        }
    }

    private fun setCategory(category: InspectionCategory) {
        viewModelScope.launch {
            val session = verificationRepository.setActiveCategory(vehicleId, category)
            _uiState.update { it.copy(session = session) }
        }
    }

    private fun updateOption(itemId: String, optionId: String) {
        viewModelScope.launch {
            val session = verificationRepository.toggleOption(vehicleId, itemId, optionId)
            _uiState.update { it.copy(session = session) }
        }
    }

    private fun updateNote(itemId: String, value: String) {
        viewModelScope.launch {
            val session = verificationRepository.updateNote(vehicleId, itemId, value)
            _uiState.update { it.copy(session = session) }
        }
    }

    private fun updateNumeric(itemId: String, value: String) {
        viewModelScope.launch {
            val session = verificationRepository.updateNumeric(vehicleId, itemId, value)
            _uiState.update { it.copy(session = session) }
        }
    }

    private fun addEvidence(source: EvidenceSource) {
        viewModelScope.launch {
            val session = verificationRepository.addEvidence(vehicleId, source)
            _uiState.update {
                it.copy(
                    session = session,
                    showEvidenceDialog = false,
                )
            }
        }
    }

    private fun removeEvidence(evidenceId: String) {
        viewModelScope.launch {
            val session = verificationRepository.removeEvidence(vehicleId, evidenceId)
            _uiState.update { it.copy(session = session) }
        }
    }

    private fun saveComments() {
        viewModelScope.launch {
            val session = verificationRepository.updateComments(vehicleId, uiState.value.commentDraft)
            _uiState.update {
                it.copy(
                    session = session,
                    showCommentDialog = false,
                )
            }
        }
    }

    private fun completeVerification() {
        viewModelScope.launch {
            verificationRepository.completeSession(vehicleId)
            _uiState.update { it.copy(showSubmitDialog = false) }
            _events.emit(VerificationEvent.Completed)
        }
    }

    private fun openSessionActions() {
        viewModelScope.launch {
            _events.emit(VerificationEvent.OpenSessionActions(vehicleId))
        }
    }
}
