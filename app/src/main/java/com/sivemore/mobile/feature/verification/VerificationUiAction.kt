package com.sivemore.mobile.feature.verification

import com.sivemore.mobile.domain.model.EvidenceUpload
import com.sivemore.mobile.domain.model.VerificationSession

sealed interface VerificationUiAction {
    data object Refresh : VerificationUiAction

    data class QuestionOptionSelected(
        val sectionId: String,
        val itemId: String,
        val optionId: String,
    ) : VerificationUiAction

    data class QuestionCommentChanged(
        val sectionId: String,
        val itemId: String,
        val value: String,
    ) : VerificationUiAction

    data class SectionNoteChanged(
        val sectionId: String,
        val value: String,
    ) : VerificationUiAction

    data object AddEvidenceRequested : VerificationUiAction
    data object EvidenceDialogDismissed : VerificationUiAction
    data class EvidencePicked(
        val sectionId: String,
        val upload: EvidenceUpload,
    ) : VerificationUiAction

    data class RemoveEvidence(val evidenceId: String) : VerificationUiAction
    data object AddCommentRequested : VerificationUiAction
    data class CommentDraftChanged(val value: String) : VerificationUiAction
    data object CommentDialogDismissed : VerificationUiAction
    data object CommentSaved : VerificationUiAction
    data object SubmitRequested : VerificationUiAction
    data object SubmitDismissed : VerificationUiAction
    data object SubmitConfirmed : VerificationUiAction
    data object SessionActionsRequested : VerificationUiAction
}

data class VerificationUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val session: VerificationSession? = null,
    val showEvidenceDialog: Boolean = false,
    val showCommentDialog: Boolean = false,
    val showSubmitDialog: Boolean = false,
    val commentDraft: String = "",
    val errorMessage: String? = null,
)

sealed interface VerificationEvent {
    data class OpenSessionActions(val orderUnitId: String) : VerificationEvent
    data object Completed : VerificationEvent
}
