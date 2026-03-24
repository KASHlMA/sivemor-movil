package com.sivemore.mobile.domain.repository

import com.sivemore.mobile.domain.model.EvidenceUpload
import com.sivemore.mobile.domain.model.VerificationSession

interface VerificationRepository {
    suspend fun loadSession(orderUnitId: String): VerificationSession
    suspend fun updateQuestionAnswer(
        orderUnitId: String,
        sectionId: String,
        questionId: String,
        optionId: String,
    ): VerificationSession

    suspend fun updateQuestionComment(
        orderUnitId: String,
        sectionId: String,
        questionId: String,
        value: String,
    ): VerificationSession

    suspend fun updateSectionNote(
        orderUnitId: String,
        sectionId: String,
        value: String,
    ): VerificationSession

    suspend fun updateComments(
        orderUnitId: String,
        value: String,
    ): VerificationSession

    suspend fun addEvidence(
        orderUnitId: String,
        sectionId: String,
        upload: EvidenceUpload,
    ): VerificationSession

    suspend fun removeEvidence(
        orderUnitId: String,
        evidenceId: String,
    ): VerificationSession

    suspend fun pauseSession(orderUnitId: String)
    suspend fun completeSession(orderUnitId: String)
    suspend fun abandonSession(orderUnitId: String)
}
