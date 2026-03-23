package com.sivemore.mobile.feature.verification

import com.sivemore.mobile.domain.model.EvidenceSource
import com.sivemore.mobile.domain.model.InspectionCategory
import com.sivemore.mobile.domain.model.VerificationSession

sealed interface VerificationUiAction {
    data class CategorySelected(val category: InspectionCategory) : VerificationUiAction
    data class OptionToggled(val itemId: String, val optionId: String) : VerificationUiAction
    data class NoteChanged(val itemId: String, val value: String) : VerificationUiAction
    data class NumericChanged(val itemId: String, val value: String) : VerificationUiAction
    data object AddEvidenceRequested : VerificationUiAction
    data class EvidenceSourceSelected(val source: EvidenceSource) : VerificationUiAction
    data object EvidenceDialogDismissed : VerificationUiAction
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
    val session: VerificationSession? = null,
    val showEvidenceDialog: Boolean = false,
    val showCommentDialog: Boolean = false,
    val showSubmitDialog: Boolean = false,
    val commentDraft: String = "",
)

sealed interface VerificationEvent {
    data class OpenSessionActions(val vehicleId: String) : VerificationEvent
    data object Completed : VerificationEvent
}
