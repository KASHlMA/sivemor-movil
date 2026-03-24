package com.sivemore.mobile

import androidx.lifecycle.SavedStateHandle
import com.sivemore.mobile.domain.model.AuthCredentials
import com.sivemore.mobile.domain.model.AuthenticatedUser
import com.sivemore.mobile.domain.model.EvidenceUpload
import com.sivemore.mobile.domain.model.VerificationSession
import com.sivemore.mobile.domain.model.VehicleSummary
import com.sivemore.mobile.domain.repository.AuthRepository
import com.sivemore.mobile.domain.repository.VehicleRepository
import com.sivemore.mobile.domain.repository.VerificationRepository
import com.sivemore.mobile.feature.sessionactions.SessionActionsEvent
import com.sivemore.mobile.feature.sessionactions.SessionActionsUiAction
import com.sivemore.mobile.feature.sessionactions.SessionActionsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionActionsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun pausingReturnsToLookup() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val event = async { viewModel.events.first() }

        viewModel.onAction(SessionActionsUiAction.ConfirmPause)
        advanceUntilIdle()

        assertEquals(SessionActionsEvent.BackToLookup, event.await())
    }

    @Test
    fun signingOutEmitsSignedOut() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val event = async { viewModel.events.first() }

        viewModel.onAction(SessionActionsUiAction.ConfirmSignOut)
        advanceUntilIdle()

        assertEquals(SessionActionsEvent.SignedOut, event.await())
    }

    private fun buildViewModel(): SessionActionsViewModel = SessionActionsViewModel(
        savedStateHandle = SavedStateHandle(mapOf("vehicleId" to "1")),
        vehicleRepository = object : VehicleRepository {
            override suspend fun loadVehicles(query: String): List<VehicleSummary> = listOf(sampleVehicle())
            override suspend fun loadVehicle(vehicleId: String): VehicleSummary? = sampleVehicle(id = vehicleId)
        },
        verificationRepository = object : VerificationRepository {
            override suspend fun loadSession(orderUnitId: String): VerificationSession = sampleSession(orderUnitId)
            override suspend fun updateQuestionAnswer(orderUnitId: String, sectionId: String, questionId: String, optionId: String): VerificationSession = sampleSession(orderUnitId)
            override suspend fun updateQuestionComment(orderUnitId: String, sectionId: String, questionId: String, value: String): VerificationSession = sampleSession(orderUnitId)
            override suspend fun updateSectionNote(orderUnitId: String, sectionId: String, value: String): VerificationSession = sampleSession(orderUnitId)
            override suspend fun updateComments(orderUnitId: String, value: String): VerificationSession = sampleSession(orderUnitId)
            override suspend fun addEvidence(orderUnitId: String, sectionId: String, upload: EvidenceUpload): VerificationSession = sampleSession(orderUnitId)
            override suspend fun removeEvidence(orderUnitId: String, evidenceId: String): VerificationSession = sampleSession(orderUnitId)
            override suspend fun pauseSession(orderUnitId: String) = Unit
            override suspend fun completeSession(orderUnitId: String) = Unit
            override suspend fun abandonSession(orderUnitId: String) = Unit
        },
        authRepository = object : AuthRepository {
            override suspend fun signIn(credentials: AuthCredentials): Result<AuthenticatedUser> = Result.success(sampleUser)
            override suspend fun signOut() = Unit
            override fun hasActiveSession(): Boolean = true
            override fun currentUser(): AuthenticatedUser? = sampleUser
        },
    )
}
