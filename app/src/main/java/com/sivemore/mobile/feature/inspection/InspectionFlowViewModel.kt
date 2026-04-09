package com.sivemore.mobile.feature.inspection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sivemore.mobile.domain.model.EvidenceUpload
import com.sivemore.mobile.domain.model.InspectionSection
import com.sivemore.mobile.domain.model.VerificationSession
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
class InspectionFlowViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val verificationRepository: VerificationRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val vehicleId: String = checkNotNull(savedStateHandle["vehicleId"])

    private val _uiState = MutableStateFlow(
        InspectionFlowUiState(
            vehicleId = vehicleId,
            lucesSection = InspectionSectionCatalog.lucesSection(),
            llantasSection = InspectionSectionCatalog.llantasSection(),
        ),
    )
    val uiState: StateFlow<InspectionFlowUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<InspectionFlowEvent>()
    val events: SharedFlow<InspectionFlowEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun onAction(action: InspectionFlowAction) {
        when (action) {
            is InspectionFlowAction.EnterSection -> _uiState.update {
                it.copy(currentSectionIndex = action.index, errorMessage = null)
            }
            is InspectionFlowAction.LucesOptionSelected -> updateSingleChoiceAnswer(
                sectionId = "luces",
                questionId = action.questionId,
                optionId = action.optionId,
            )
            is InspectionFlowAction.LlantasOptionSelected -> updateSingleChoiceAnswer(
                sectionId = "llantas",
                questionId = action.questionId,
                optionId = action.optionId,
            )
            is InspectionFlowAction.LlantasNumericValueChanged -> updateNumericAnswer(
                questionId = action.questionId,
                value = action.value,
            )
            InspectionFlowAction.PreviousClicked -> moveToPreviousSection()
            InspectionFlowAction.NextClicked -> navigateNextIfComplete()
            InspectionFlowAction.CommentDialogOpened -> _uiState.update { it.copy(showCommentDialog = true, errorMessage = null) }
            InspectionFlowAction.CommentDialogDismissed -> _uiState.update { it.copy(showCommentDialog = false) }
            is InspectionFlowAction.CommentDraftChanged -> _uiState.update { it.copy(commentDraft = action.value, errorMessage = null) }
            InspectionFlowAction.CommentSaved -> saveComment()
            is InspectionFlowAction.EvidencePicked -> addEvidence(action.upload)
            is InspectionFlowAction.RemoveEvidence -> mutate {
                verificationRepository.removeEvidence(vehicleId, action.evidenceId)
            }
            InspectionFlowAction.PauseRequested -> _uiState.update { it.copy(showPauseDialog = true, errorMessage = null) }
            InspectionFlowAction.PauseDismissed -> _uiState.update { it.copy(showPauseDialog = false) }
            InspectionFlowAction.PauseConfirmed -> pauseVerification()
            InspectionFlowAction.SubmitRequested -> completeVerification()
            InspectionFlowAction.LogoutRequested -> signOut()
            InspectionFlowAction.PhotoLimitReached -> _uiState.update {
                it.copy(errorMessage = "Se alcanzo el numero maximo de fotos permitidas (3).")
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            runCatching {
                verificationRepository.loadSession(vehicleId)
            }.onSuccess { session ->
                _uiState.update {
                    it.copy(
                        session = session,
                        commentDraft = session.comments,
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = failure.message ?: "No fue posible cargar la inspeccion.",
                    )
                }
            }
        }
    }

    private fun updateSingleChoiceAnswer(
        sectionId: String,
        questionId: String,
        optionId: String,
    ) {
        _uiState.update { current ->
            current.copy(
                lucesSection = if (sectionId == "luces") {
                    current.lucesSection.updateQuestion(questionId) { question ->
                        question.copy(selectedOptionId = optionId)
                    }
                } else {
                    current.lucesSection
                },
                llantasSection = if (sectionId == "llantas") {
                    current.llantasSection.updateQuestion(questionId) { question ->
                        question.copy(selectedOptionId = optionId)
                    }
                } else {
                    current.llantasSection
                },
                errorMessage = null,
            )
        }
    }

    private fun updateNumericAnswer(
        questionId: String,
        value: String,
    ) {
        val sanitizedValue = value.filter(Char::isDigit)
        _uiState.update { current ->
            current.copy(
                llantasSection = current.llantasSection.updateQuestion(questionId) { question ->
                    question.copy(numericValue = sanitizedValue)
                },
                errorMessage = null,
            )
        }
    }

    private fun moveToPreviousSection() {
        _uiState.update { current ->
            if (current.currentSectionIndex == 0) {
                current
            } else {
                current.copy(
                    currentSectionIndex = current.currentSectionIndex - 1,
                    errorMessage = null,
                )
            }
        }
    }

    private fun navigateNextIfComplete() {
        if (!uiState.value.isCurrentSectionComplete) return
        viewModelScope.launch {
            _uiState.update { it.copy(currentSectionIndex = it.currentSectionIndex + 1, errorMessage = null) }
            _events.emit(InspectionFlowEvent.NavigateToNextSection)
        }
    }

    private fun addEvidence(upload: EvidenceUpload) {
        val currentSectionId = uiState.value.currentSection?.id
        if (currentSectionId == null) {
            _uiState.update { it.copy(errorMessage = "No hay una seccion activa para agregar evidencia.") }
            return
        }
        if (!uiState.value.canAddMorePhotos) {
            _uiState.update { it.copy(errorMessage = "Se alcanzo el numero maximo de fotos permitidas (3).") }
            return
        }
        mutate {
            verificationRepository.addEvidence(vehicleId, currentSectionId, upload)
        }
    }

    private fun mutate(
        mutation: suspend () -> VerificationSession,
    ) {
        viewModelScope.launch {
            runCatching { mutation() }
                .onSuccess { session ->
                    _uiState.update {
                        it.copy(
                            session = session,
                            errorMessage = null,
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

    private suspend fun persistCommentIfNeeded() {
        val state = uiState.value
        val session = state.session ?: return
        if (session.comments == state.commentDraft) return
        _uiState.update { it.copy(isSavingComment = true) }
        val updated = verificationRepository.updateComments(vehicleId, state.commentDraft)
        _uiState.update {
            it.copy(
                session = updated,
                commentDraft = updated.comments,
                isSavingComment = false,
            )
        }
    }

    private fun saveComment() {
        viewModelScope.launch {
            runCatching {
                persistCommentIfNeeded()
            }.onSuccess {
                _uiState.update { it.copy(showCommentDialog = false, errorMessage = null) }
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(
                        showCommentDialog = true,
                        isSavingComment = false,
                        errorMessage = failure.message ?: "No fue posible guardar el comentario.",
                    )
                }
            }
        }
    }

    private fun pauseVerification() {
        viewModelScope.launch {
            runCatching {
                persistCommentIfNeeded()
                verificationRepository.pauseSession(vehicleId)
            }.onSuccess {
                _uiState.update { it.copy(showPauseDialog = false, isSavingComment = false) }
                _events.emit(InspectionFlowEvent.BackToLookup)
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
                it.copy(errorMessage = "Debes completar todas las secciones antes de finalizar la verificacion.")
            }
            return
        }
        viewModelScope.launch {
            runCatching {
                persistCommentIfNeeded()
                verificationRepository.completeSession(vehicleId)
            }.onSuccess {
                _uiState.update { it.copy(isSavingComment = false) }
                _events.emit(InspectionFlowEvent.Completed)
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
            _events.emit(InspectionFlowEvent.SignedOut)
        }
    }
}

data class InspectionFlowUiState(
    val vehicleId: String,
    val isLoading: Boolean = true,
    val lucesSection: InspectionSectionUiState,
    val llantasSection: InspectionSectionUiState,
    val session: VerificationSession? = null,
    val currentSectionIndex: Int = 0,
    val commentDraft: String = "",
    val isSavingComment: Boolean = false,
    val showCommentDialog: Boolean = false,
    val showPauseDialog: Boolean = false,
    val errorMessage: String? = null,
) {
    val currentSection: InspectionSection?
        get() = session?.sections?.getOrNull(currentSectionIndex)

    val currentEvidenceCount: Int
        get() = currentSection?.evidence?.size ?: 0

    val canAddMorePhotos: Boolean
        get() = currentEvidenceCount < 3

    val canGoBack: Boolean
        get() = currentSectionIndex > 0

    val isCurrentSectionComplete: Boolean
        get() = when (currentSectionIndex) {
            0 -> lucesSection.isComplete
            1 -> llantasSection.isComplete
            else -> currentSection?.items.orEmpty().all { !it.required || it.selectedOptionId != null }
        }

    val isEntireVerificationComplete: Boolean
        get() {
            val remainingSectionsComplete = session?.sections
                ?.drop(2)
                .orEmpty()
                .all { section -> section.items.all { !it.required || it.selectedOptionId != null } }
            return lucesSection.isComplete && llantasSection.isComplete && remainingSectionsComplete
        }
}

sealed interface InspectionFlowAction {
    data class EnterSection(val index: Int) : InspectionFlowAction
    data class LucesOptionSelected(val questionId: String, val optionId: String) : InspectionFlowAction
    data class LlantasOptionSelected(val questionId: String, val optionId: String) : InspectionFlowAction
    data class LlantasNumericValueChanged(val questionId: String, val value: String) : InspectionFlowAction
    data object PreviousClicked : InspectionFlowAction
    data object CommentDialogOpened : InspectionFlowAction
    data object CommentDialogDismissed : InspectionFlowAction
    data class CommentDraftChanged(val value: String) : InspectionFlowAction
    data object CommentSaved : InspectionFlowAction
    data class EvidencePicked(val upload: EvidenceUpload) : InspectionFlowAction
    data class RemoveEvidence(val evidenceId: String) : InspectionFlowAction
    data object NextClicked : InspectionFlowAction
    data object PauseRequested : InspectionFlowAction
    data object PauseDismissed : InspectionFlowAction
    data object PauseConfirmed : InspectionFlowAction
    data object SubmitRequested : InspectionFlowAction
    data object LogoutRequested : InspectionFlowAction
    data object PhotoLimitReached : InspectionFlowAction
}

sealed interface InspectionFlowEvent {
    data object NavigateToNextSection : InspectionFlowEvent
    data object BackToLookup : InspectionFlowEvent
    data object Completed : InspectionFlowEvent
    data object SignedOut : InspectionFlowEvent
}

private fun InspectionSectionUiState.updateQuestion(
    questionId: String,
    transform: (InspectionQuestionItem) -> InspectionQuestionItem,
): InspectionSectionUiState = if (groups.isNotEmpty()) {
    copy(
        groups = groups.map { group ->
            group.copy(
                questions = group.questions.map { question ->
                    if (question.id == questionId) transform(question) else question
                },
            )
        },
    )
} else {
    copy(
        questions = questions.map { question ->
            if (question.id == questionId) transform(question) else question
        },
    )
}
