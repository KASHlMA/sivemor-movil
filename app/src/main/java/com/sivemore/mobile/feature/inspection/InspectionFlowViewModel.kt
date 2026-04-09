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
            direccionSection = InspectionSectionCatalog.direccionSection(),
            aireFrenosSection = InspectionSectionCatalog.aireFrenosSection(),
            motorEmisionesSection = InspectionSectionCatalog.motorEmisionesSection(),
            otrosSection = InspectionSectionCatalog.otrosSection(),
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
            is InspectionFlowAction.LucesOptionSelected -> updateSingleChoiceAnswer("luces", action.questionId, action.optionId)
            is InspectionFlowAction.LlantasOptionSelected -> updateSingleChoiceAnswer("llantas", action.questionId, action.optionId)
            is InspectionFlowAction.LlantasNumericValueChanged -> updateNumericAnswer(action.questionId, action.value, "llantas")
            is InspectionFlowAction.DireccionOptionSelected -> updateSingleChoiceAnswer("direccion", action.questionId, action.optionId)
            is InspectionFlowAction.DireccionNumericValueChanged -> updateNumericAnswer(action.questionId, action.value, "direccion")
            is InspectionFlowAction.AireFrenosOptionSelected -> updateSingleChoiceAnswer("aire_frenos", action.questionId, action.optionId)
            is InspectionFlowAction.AireFrenosNumericValueChanged -> updateNumericAnswer(action.questionId, action.value, "aire_frenos")
            is InspectionFlowAction.MotorEmisionesOptionSelected -> updateSingleChoiceAnswer("motor_emisiones", action.questionId, action.optionId)
            is InspectionFlowAction.OtrosOptionSelected -> updateSingleChoiceAnswer("otros", action.questionId, action.optionId)
            is InspectionFlowAction.BirloToggled -> updateBirloState(action.groupId, action.birloIndex, action.checked)
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
                    current.lucesSection.updateQuestion(questionId) { it.copy(selectedOptionId = optionId) }
                } else {
                    current.lucesSection
                },
                llantasSection = if (sectionId == "llantas") {
                    current.llantasSection.updateQuestion(questionId) { it.copy(selectedOptionId = optionId) }
                } else {
                    current.llantasSection
                },
                direccionSection = if (sectionId == "direccion") {
                    current.direccionSection.updateQuestion(questionId) { it.copy(selectedOptionId = optionId) }
                } else {
                    current.direccionSection
                },
                aireFrenosSection = if (sectionId == "aire_frenos") {
                    current.aireFrenosSection.updateQuestion(questionId) { it.copy(selectedOptionId = optionId) }
                } else {
                    current.aireFrenosSection
                },
                motorEmisionesSection = if (sectionId == "motor_emisiones") {
                    current.motorEmisionesSection.updateQuestion(questionId) { it.copy(selectedOptionId = optionId) }
                } else {
                    current.motorEmisionesSection
                },
                otrosSection = if (sectionId == "otros") {
                    current.otrosSection.updateQuestion(questionId) { it.copy(selectedOptionId = optionId) }
                } else {
                    current.otrosSection
                },
                errorMessage = null,
            )
        }
    }

    private fun updateNumericAnswer(
        questionId: String,
        value: String,
        sectionId: String,
    ) {
        val sanitizedValue = value.filter(Char::isDigit)
        _uiState.update { current ->
            current.copy(
                llantasSection = if (sectionId == "llantas") {
                    current.llantasSection.updateQuestion(questionId) { it.copy(numericValue = sanitizedValue) }
                } else {
                    current.llantasSection
                },
                direccionSection = if (sectionId == "direccion") {
                    current.direccionSection.updateQuestion(questionId) { it.copy(numericValue = sanitizedValue) }
                } else {
                    current.direccionSection
                },
                aireFrenosSection = if (sectionId == "aire_frenos") {
                    current.aireFrenosSection.updateQuestion(questionId) { it.copy(numericValue = sanitizedValue) }
                } else {
                    current.aireFrenosSection
                },
                errorMessage = null,
            )
        }
    }

    private fun updateBirloState(
        groupId: String,
        birloIndex: Int,
        checked: Boolean,
    ) {
        _uiState.update { current ->
            current.copy(
                llantasSection = current.llantasSection.updateBirlosGroup(groupId, birloIndex, checked),
                errorMessage = null,
            )
        }
    }

    private fun moveToPreviousSection() {
        _uiState.update { current ->
            if (current.currentSectionIndex == 0) current else current.copy(
                currentSectionIndex = current.currentSectionIndex - 1,
                errorMessage = null,
            )
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
        val currentSectionId = uiState.value.currentSection?.id ?: uiState.value.session?.sections?.lastOrNull()?.id
        if (currentSectionId == null) {
            _uiState.update { it.copy(errorMessage = "No hay una seccion activa para agregar evidencia.") }
            return
        }
        if (!uiState.value.canAddMorePhotos) {
            _uiState.update { it.copy(errorMessage = "Se alcanzo el numero maximo de fotos permitidas (3).") }
            return
        }
        viewModelScope.launch {
            val previousSession = uiState.value.session
            runCatching {
                verificationRepository.addEvidence(vehicleId, currentSectionId, upload)
            }.onSuccess { session ->
                _uiState.update {
                    it.copy(
                        session = preserveEvidencePreviewUris(
                            previous = previousSession,
                            updated = session,
                            newEvidenceSectionId = currentSectionId,
                            upload = upload,
                        ),
                        errorMessage = null,
                    )
                }
            }.onFailure { failure ->
                _uiState.update {
                    it.copy(errorMessage = failure.message ?: "No fue posible actualizar la inspeccion.")
                }
            }
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
                            session = preserveEvidencePreviewUris(
                                previous = it.session,
                                updated = session,
                            ),
                            errorMessage = null,
                        )
                    }
                }
                .onFailure { failure ->
                    _uiState.update { it.copy(errorMessage = failure.message ?: "No fue posible actualizar la inspeccion.") }
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
            runCatching { persistCommentIfNeeded() }
                .onSuccess { _uiState.update { it.copy(showCommentDialog = false, errorMessage = null) } }
                .onFailure { failure ->
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

private fun preserveEvidencePreviewUris(
    previous: VerificationSession?,
    updated: VerificationSession,
    newEvidenceSectionId: String? = null,
    upload: EvidenceUpload? = null,
): VerificationSession {
    val preservedUris = previous
        ?.sections
        .orEmpty()
        .flatMap { section -> section.evidence }
        .associate { evidence -> evidence.id to evidence.previewUri }

    val mergedSections = updated.sections.map { section ->
        section.copy(
            evidence = section.evidence.map { evidence ->
                evidence.copy(previewUri = preservedUris[evidence.id])
            },
        )
    }

    val withNewEvidenceUri = if (newEvidenceSectionId == null || upload?.fileName == null) {
        mergedSections
    } else {
        mergedSections.map { section ->
            if (section.id != newEvidenceSectionId) {
                section
            } else {
                section.copy(
                    evidence = applyPreviewUriToNewEvidence(
                        evidence = section.evidence,
                        fileName = upload.fileName,
                        previewUri = upload.uri,
                    ),
                )
            }
        }
    }

    return updated.copy(sections = withNewEvidenceUri)
}

private fun applyPreviewUriToNewEvidence(
    evidence: List<com.sivemore.mobile.domain.model.EvidenceItem>,
    fileName: String,
    previewUri: String,
): List<com.sivemore.mobile.domain.model.EvidenceItem> {
    var applied = false
    return evidence.map { item ->
        if (!applied && item.title == fileName && item.previewUri == null) {
            applied = true
            item.copy(previewUri = previewUri)
        } else {
            item
        }
    }
}

data class InspectionFlowUiState(
    val vehicleId: String,
    val isLoading: Boolean = true,
    val lucesSection: InspectionSectionUiState,
    val llantasSection: InspectionSectionUiState,
    val direccionSection: InspectionSectionUiState,
    val aireFrenosSection: InspectionSectionUiState,
    val motorEmisionesSection: InspectionSectionUiState,
    val otrosSection: InspectionSectionUiState,
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

    val allEvidence: List<com.sivemore.mobile.domain.model.EvidenceItem>
        get() = session?.sections.orEmpty().flatMap { it.evidence }

    val canGoBack: Boolean
        get() = currentSectionIndex > 0

    val isCurrentSectionComplete: Boolean
        get() = when (currentSectionIndex) {
            0 -> lucesSection.isComplete
            1 -> llantasSection.isComplete
            2 -> direccionSection.isComplete
            3 -> aireFrenosSection.isComplete
            4 -> motorEmisionesSection.isComplete
            5 -> otrosSection.isComplete
            else -> currentSection?.items.orEmpty().all { !it.required || it.selectedOptionId != null }
        }

    val isEntireVerificationComplete: Boolean
        get() {
            val remainingSectionsComplete = session?.sections
                ?.drop(6)
                .orEmpty()
                .all { section -> section.items.all { !it.required || it.selectedOptionId != null } }
            return lucesSection.isComplete &&
                llantasSection.isComplete &&
                direccionSection.isComplete &&
                aireFrenosSection.isComplete &&
                motorEmisionesSection.isComplete &&
                otrosSection.isComplete &&
                remainingSectionsComplete
        }
}

sealed interface InspectionFlowAction {
    data class EnterSection(val index: Int) : InspectionFlowAction
    data class LucesOptionSelected(val questionId: String, val optionId: String) : InspectionFlowAction
    data class LlantasOptionSelected(val questionId: String, val optionId: String) : InspectionFlowAction
    data class LlantasNumericValueChanged(val questionId: String, val value: String) : InspectionFlowAction
    data class DireccionOptionSelected(val questionId: String, val optionId: String) : InspectionFlowAction
    data class DireccionNumericValueChanged(val questionId: String, val value: String) : InspectionFlowAction
    data class AireFrenosOptionSelected(val questionId: String, val optionId: String) : InspectionFlowAction
    data class AireFrenosNumericValueChanged(val questionId: String, val value: String) : InspectionFlowAction
    data class MotorEmisionesOptionSelected(val questionId: String, val optionId: String) : InspectionFlowAction
    data class OtrosOptionSelected(val questionId: String, val optionId: String) : InspectionFlowAction
    data class BirloToggled(val groupId: String, val birloIndex: Int, val checked: Boolean) : InspectionFlowAction
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

private fun InspectionSectionUiState.updateBirlosGroup(
    groupId: String,
    birloIndex: Int,
    checked: Boolean,
): InspectionSectionUiState = copy(
    groups = groups.map { group ->
        if (group.id != groupId || group.birlosVisualState == null) {
            group
        } else {
            val birlosState = group.birlosVisualState.birlosState.toMutableList()
            val evaluated = group.birlosVisualState.evaluated.toMutableList()
            if (birloIndex in birlosState.indices) {
                birlosState[birloIndex] = checked
                evaluated[birloIndex] = true
            }
            group.copy(
                birlosVisualState = group.birlosVisualState.copy(
                    birlosState = birlosState,
                    evaluated = evaluated,
                ),
            )
        }
    },
)
