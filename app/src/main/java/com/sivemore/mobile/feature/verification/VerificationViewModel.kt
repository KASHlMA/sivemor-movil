package com.sivemore.mobile.feature.verification

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class VerificationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val verificationRepository: VerificationRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val orderUnitId: String = checkNotNull(savedStateHandle["vehicleId"])

    private val _uiState = MutableStateFlow(VerificationUiState())
    val uiState: StateFlow<VerificationUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<VerificationEvent>()
    val events: SharedFlow<VerificationEvent> = _events.asSharedFlow()

    init {
        refresh(initialLoad = true)
    }

    fun onAction(action: VerificationUiAction) {
        when (action) {
            VerificationUiAction.Refresh -> refresh()
            is VerificationUiAction.QuestionOptionSelected -> mutate {
                verificationRepository.updateQuestionAnswer(orderUnitId, action.sectionId, action.itemId, action.optionId)
            }
            is VerificationUiAction.QuestionCommentChanged -> mutate {
                verificationRepository.updateQuestionComment(orderUnitId, action.sectionId, action.itemId, action.value)
            }
            is VerificationUiAction.SectionNoteChanged -> mutate {
                verificationRepository.updateSectionNote(orderUnitId, action.sectionId, action.value)
            }
            is VerificationUiAction.EvidencePicked -> addEvidence(action.upload)
            is VerificationUiAction.RemoveEvidence -> mutate {
                verificationRepository.removeEvidence(orderUnitId, action.evidenceId)
            }
            is VerificationUiAction.CommentDraftChanged -> _uiState.update {
                it.copy(commentDraft = action.value, errorMessage = null)
            }
            VerificationUiAction.SubmitRequested -> completeVerification()
            VerificationUiAction.PauseRequested -> _uiState.update { it.copy(showPauseDialog = true, errorMessage = null) }
            VerificationUiAction.PauseDismissed -> _uiState.update { it.copy(showPauseDialog = false) }
            VerificationUiAction.PauseConfirmed -> pauseVerification()
            VerificationUiAction.LogoutRequested -> signOut()
            VerificationUiAction.NextSectionRequested -> moveToNextSection()
            VerificationUiAction.PhotoLimitReached -> _uiState.update {
                it.copy(errorMessage = "Solo puedes agregar hasta 3 fotos en la seccion actual.")
            }
        }
    }

    private fun refresh(initialLoad: Boolean = false) {
        viewModelScope.launch {
            val hasVisibleContent = uiState.value.session != null
            _uiState.update {
                it.copy(
                    isLoading = initialLoad || !hasVisibleContent,
                    isRefreshing = !initialLoad && hasVisibleContent,
                    errorMessage = null,
                )
            }
            runCatching {
                verificationRepository.loadSession(orderUnitId)
            }.onSuccess { session ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        session = session,
                        commentDraft = session.comments,
                        currentSectionIndex = it.currentSectionIndex.coerceIn(0, session.sections.lastIndex.coerceAtLeast(0)),
                        errorMessage = null,
                    )
                }
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = failure.message ?: "No fue posible cargar la inspeccion.",
                    )
                }
            }
        }
    }

    private fun mutate(
        mutation: suspend () -> com.sivemore.mobile.domain.model.VerificationSession,
    ) {
        viewModelScope.launch {
            runCatching { mutation() }
                .onSuccess { session ->
                    _uiState.update {
                        it.copy(
                            session = session,
                            errorMessage = null,
                            currentSectionIndex = it.currentSectionIndex.coerceIn(0, session.sections.lastIndex.coerceAtLeast(0)),
                        )
                    }
                }
                .onFailure { failure ->
                    _uiState.update {
                        it.copy(errorMessage = failure.message ?: "No fue posible actualizar la inspeccion.")
                    }
                }
        }
    }

    private fun addEvidence(upload: com.sivemore.mobile.domain.model.EvidenceUpload) {
        val currentSectionId = uiState.value.currentSection?.id
        if (currentSectionId == null) return
        if (!uiState.value.canAddMorePhotos) {
            _uiState.update { it.copy(errorMessage = "Solo puedes agregar hasta 3 fotos en la seccion actual.") }
            return
        }
        mutate {
            verificationRepository.addEvidence(orderUnitId, currentSectionId, upload)
        }
    }

    private fun moveToNextSection() {
        _uiState.update { current ->
            if (!current.canGoNext) return@update current
            current.copy(
                currentSectionIndex = current.currentSectionIndex + 1,
                errorMessage = null,
            )
        }
    }

    private suspend fun persistCommentIfNeeded() {
        val state = uiState.value
        val session = state.session ?: return
        if (state.commentDraft == session.comments) return
        _uiState.update { it.copy(isSavingComment = true) }
        val updated = verificationRepository.updateComments(orderUnitId, state.commentDraft)
        _uiState.update {
            it.copy(
                session = updated,
                commentDraft = updated.comments,
                isSavingComment = false,
                currentSectionIndex = it.currentSectionIndex.coerceIn(0, updated.sections.lastIndex.coerceAtLeast(0)),
            )
        }
    }

    private fun pauseVerification() {
        viewModelScope.launch {
            runCatching {
                persistCommentIfNeeded()
                verificationRepository.pauseSession(orderUnitId)
            }.onSuccess {
                _uiState.update { it.copy(showPauseDialog = false) }
                _events.emit(VerificationEvent.BackToLookup)
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(
                        showPauseDialog = false,
                        isSavingComment = false,
                        errorMessage = failure.message ?: "No fue posible pausar la verificacion.",
                    )
                }
            }
        }
    }

    private fun completeVerification() {
        if (!uiState.value.isEntireVerificationComplete) {
            _uiState.update {
                it.copy(
                    errorMessage = if (it.totalEvidenceCount < 3) {
                        "Debes agregar al menos 3 evidencias antes de finalizar la verificacion."
                    } else {
                        "Debes completar todas las secciones antes de finalizar la verificacion."
                    }
                )
            }
            return
        }
        viewModelScope.launch {
            runCatching {
                persistCommentIfNeeded()
                verificationRepository.completeSession(orderUnitId)
            }.onSuccess {
                _uiState.update { it.copy(isSavingComment = false) }
                _events.emit(VerificationEvent.Completed)
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(
                        isSavingComment = false,
                        errorMessage = failure.message ?: "No fue posible enviar la inspeccion.",
                    )
                }
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _events.emit(VerificationEvent.SignedOut)
        }
    }
}
