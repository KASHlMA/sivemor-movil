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

    data class EvidencePicked(
        val upload: EvidenceUpload,
    ) : VerificationUiAction
    data class RemoveEvidence(val evidenceId: String) : VerificationUiAction
    data class CommentDraftChanged(val value: String) : VerificationUiAction
    data object SubmitRequested : VerificationUiAction
    data object PauseRequested : VerificationUiAction
    data object PauseDismissed : VerificationUiAction
    data object PauseConfirmed : VerificationUiAction
    data object LogoutRequested : VerificationUiAction
    data object NextSectionRequested : VerificationUiAction
    data object PhotoLimitReached : VerificationUiAction
}

data class VerificationUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val session: VerificationSession? = null,
    val currentSectionIndex: Int = 0,
    val isSavingComment: Boolean = false,
    val showPauseDialog: Boolean = false,
    val commentDraft: String = "",
    val errorMessage: String? = null,
) {
    val totalEvidenceCount: Int = session?.sections.orEmpty().sumOf { it.evidence.size }
    val currentSection = session?.sections?.getOrNull(currentSectionIndex)
    val canGoNext: Boolean = currentSectionIndex < (session?.sections?.lastIndex ?: -1) &&
        currentSection?.items.orEmpty().all { !it.required || it.selectedOptionId != null }
    val canAddMorePhotos: Boolean = currentSection?.evidence?.size?.let { it < 3 } ?: false
    val isEntireVerificationComplete: Boolean = session?.sections.orEmpty().all { section ->
        section.items.all { !it.required || it.selectedOptionId != null }
    }
}

sealed interface VerificationEvent {
    data object Completed : VerificationEvent
    data object BackToLookup : VerificationEvent
    data object BackToMenu : VerificationEvent
    data object SignedOut : VerificationEvent
}
