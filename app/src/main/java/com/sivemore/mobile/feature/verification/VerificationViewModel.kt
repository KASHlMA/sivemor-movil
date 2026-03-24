package com.sivemore.mobile.feature.verification

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val orderUnitId: String = checkNotNull(savedStateHandle["vehicleId"])

    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<VerificationEvent>()
    val events: SharedFlow<VerificationEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun onAction(action: VerificationUiAction) {
        when (action) {
            is VerificationUiAction.QuestionOptionSelected -> mutate {
                verificationRepository.updateQuestionAnswer(orderUnitId, action.sectionId, action.itemId, action.optionId)
            }
            is VerificationUiAction.QuestionCommentChanged -> mutate {
                verificationRepository.updateQuestionComment(orderUnitId, action.sectionId, action.itemId, action.value)
            }
            is VerificationUiAction.SectionNoteChanged -> mutate {
                verificationRepository.updateSectionNote(orderUnitId, action.sectionId, action.value)
            }
            VerificationUiAction.AddEvidenceRequested -> _uiState.update { it.copy(showEvidenceDialog = true) }
            VerificationUiAction.EvidenceDialogDismissed -> _uiState.update { it.copy(showEvidenceDialog = false) }
            is VerificationUiAction.EvidencePicked -> mutate(
                closeEvidenceDialog = true,
            ) {
                verificationRepository.addEvidence(orderUnitId, action.sectionId, action.upload)
            }
            is VerificationUiAction.RemoveEvidence -> mutate {
                verificationRepository.removeEvidence(orderUnitId, action.evidenceId)
            }
            VerificationUiAction.AddCommentRequested -> _uiState.update {
                it.copy(showCommentDialog = true, commentDraft = it.session?.comments.orEmpty())
            }
            is VerificationUiAction.CommentDraftChanged -> _uiState.update { it.copy(commentDraft = action.value) }
            VerificationUiAction.CommentDialogDismissed -> _uiState.update { it.copy(showCommentDialog = false) }
            VerificationUiAction.CommentSaved -> mutate(closeCommentDialog = true) {
                verificationRepository.updateComments(orderUnitId, uiState.value.commentDraft)
            }
            VerificationUiAction.SubmitRequested -> _uiState.update { it.copy(showSubmitDialog = true) }
            VerificationUiAction.SubmitDismissed -> _uiState.update { it.copy(showSubmitDialog = false) }
            VerificationUiAction.SubmitConfirmed -> completeVerification()
            VerificationUiAction.SessionActionsRequested -> openSessionActions()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                verificationRepository.loadSession(orderUnitId)
            }.onSuccess { session ->
                _uiState.value = VerificationUiState(
                    isLoading = false,
                    session = session,
                    commentDraft = session.comments,
                )
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = failure.message ?: "No fue posible cargar la inspección.",
                    )
                }
            }
        }
    }

    private fun mutate(
        closeEvidenceDialog: Boolean = false,
        closeCommentDialog: Boolean = false,
        mutation: suspend () -> com.sivemore.mobile.domain.model.VerificationSession,
    ) {
        viewModelScope.launch {
            runCatching { mutation() }
                .onSuccess { session ->
                    _uiState.update {
                        it.copy(
                            session = session,
                            errorMessage = null,
                            showEvidenceDialog = if (closeEvidenceDialog) false else it.showEvidenceDialog,
                            showCommentDialog = if (closeCommentDialog) false else it.showCommentDialog,
                        )
                    }
                }
                .onFailure { failure ->
                    _uiState.update {
                        it.copy(errorMessage = failure.message ?: "No fue posible actualizar la inspección.")
                    }
                }
        }
    }

    private fun completeVerification() {
        viewModelScope.launch {
            runCatching {
                verificationRepository.completeSession(orderUnitId)
            }.onSuccess {
                _uiState.update { it.copy(showSubmitDialog = false) }
                _events.emit(VerificationEvent.Completed)
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(
                        showSubmitDialog = false,
                        errorMessage = failure.message ?: "No fue posible enviar la inspección.",
                    )
                }
            }
        }
    }

    private fun openSessionActions() {
        viewModelScope.launch {
            _events.emit(VerificationEvent.OpenSessionActions(orderUnitId))
        }
    }
}
