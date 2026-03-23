package com.sivemore.mobile.domain.repository

import com.sivemore.mobile.domain.model.EvidenceSource
import com.sivemore.mobile.domain.model.InspectionCategory
import com.sivemore.mobile.domain.model.VerificationSession

interface VerificationRepository {
    suspend fun loadSession(vehicleId: String): VerificationSession
    suspend fun setActiveCategory(
        vehicleId: String,
        category: InspectionCategory,
    ): VerificationSession

    suspend fun toggleOption(
        vehicleId: String,
        itemId: String,
        optionId: String,
    ): VerificationSession

    suspend fun updateNote(
        vehicleId: String,
        itemId: String,
        value: String,
    ): VerificationSession

    suspend fun updateNumeric(
        vehicleId: String,
        itemId: String,
        value: String,
    ): VerificationSession

    suspend fun updateComments(
        vehicleId: String,
        value: String,
    ): VerificationSession

    suspend fun addEvidence(
        vehicleId: String,
        source: EvidenceSource,
    ): VerificationSession

    suspend fun removeEvidence(
        vehicleId: String,
        evidenceId: String,
    ): VerificationSession

    suspend fun pauseSession(vehicleId: String)
    suspend fun completeSession(vehicleId: String)
    suspend fun abandonSession(vehicleId: String)
}
