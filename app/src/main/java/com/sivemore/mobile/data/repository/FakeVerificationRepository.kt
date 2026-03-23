package com.sivemore.mobile.data.repository

import com.sivemore.mobile.domain.model.EvidenceSource
import com.sivemore.mobile.domain.model.InspectionCategory
import com.sivemore.mobile.domain.model.VerificationSession
import com.sivemore.mobile.domain.repository.VerificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeVerificationRepository @Inject constructor(
    private val store: FakeVerificationStore,
) : VerificationRepository {

    override suspend fun loadSession(vehicleId: String): VerificationSession = store.loadSession(vehicleId)

    override suspend fun setActiveCategory(
        vehicleId: String,
        category: InspectionCategory,
    ): VerificationSession = store.setActiveCategory(vehicleId, category)

    override suspend fun toggleOption(
        vehicleId: String,
        itemId: String,
        optionId: String,
    ): VerificationSession = store.toggleOption(vehicleId, itemId, optionId)

    override suspend fun updateNote(
        vehicleId: String,
        itemId: String,
        value: String,
    ): VerificationSession = store.updateNote(vehicleId, itemId, value)

    override suspend fun updateNumeric(
        vehicleId: String,
        itemId: String,
        value: String,
    ): VerificationSession = store.updateNumeric(vehicleId, itemId, value)

    override suspend fun updateComments(
        vehicleId: String,
        value: String,
    ): VerificationSession = store.updateComments(vehicleId, value)

    override suspend fun addEvidence(
        vehicleId: String,
        source: EvidenceSource,
    ): VerificationSession = store.addEvidence(vehicleId, source)

    override suspend fun removeEvidence(
        vehicleId: String,
        evidenceId: String,
    ): VerificationSession = store.removeEvidence(vehicleId, evidenceId)

    override suspend fun pauseSession(vehicleId: String) {
        store.pauseSession(vehicleId)
    }

    override suspend fun completeSession(vehicleId: String) {
        store.completeSession(vehicleId)
    }

    override suspend fun abandonSession(vehicleId: String) {
        store.abandonSession(vehicleId)
    }
}
