package com.sivemore.mobile

import androidx.lifecycle.SavedStateHandle
import com.sivemore.mobile.domain.model.AuthCredentials
import com.sivemore.mobile.domain.model.AuthenticatedUser
import com.sivemore.mobile.domain.model.EvidenceUpload
import com.sivemore.mobile.domain.model.InspectionFlowAnswerDraft
import com.sivemore.mobile.domain.model.VerificationSession
import com.sivemore.mobile.domain.repository.AuthRepository
import com.sivemore.mobile.domain.repository.VerificationRepository
import com.sivemore.mobile.feature.verification.VerificationEvent
import com.sivemore.mobile.feature.verification.VerificationUiAction
import com.sivemore.mobile.feature.verification.VerificationViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VerificationViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun buildViewModel(
        repository: StubVerificationRepository = StubVerificationRepository(),
        authRepository: StubAuthRepository = StubAuthRepository(),
    ): VerificationViewModel = VerificationViewModel(
        savedStateHandle = SavedStateHandle(mapOf("vehicleId" to "1")),
        verificationRepository = repository,
        authRepository = authRepository,
    )

    @Test
    fun nextSectionAdvancesWhenCurrentSectionIsComplete() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(VerificationUiAction.NextSectionRequested)

        assertEquals(1, viewModel.uiState.value.currentSectionIndex)
    }

    @Test
    fun commentDraftUpdatesLocallyAcrossSections() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(VerificationUiAction.CommentDraftChanged("Observacion general"))
        viewModel.onAction(VerificationUiAction.NextSectionRequested)

        assertEquals("Observacion general", viewModel.uiState.value.commentDraft)
        assertEquals(1, viewModel.uiState.value.currentSectionIndex)
    }

    @Test
    fun evidenceRemovalUpdatesCurrentSection() = runTest {
        val repository = StubVerificationRepository()
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        viewModel.onAction(VerificationUiAction.RemoveEvidence("e1"))
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.session?.sections?.first()?.evidence?.size)
    }

    @Test
    fun pauseConfirmationEmitsBackToLookup() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val event = async { viewModel.events.first() }

        viewModel.onAction(VerificationUiAction.PauseRequested)
        assertTrue(viewModel.uiState.value.showPauseDialog)

        viewModel.onAction(VerificationUiAction.PauseConfirmed)
        advanceUntilIdle()

        assertEquals(VerificationEvent.BackToLookup, event.await())
    }

    @Test
    fun finishFailsWhenAnySectionIsIncomplete() = runTest {
        val repository = StubVerificationRepository(session = sampleIncompleteSession())
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        viewModel.onAction(VerificationUiAction.SubmitRequested)
        advanceUntilIdle()

        assertEquals(
            "Debes completar todas las secciones antes de finalizar la verificacion.",
            viewModel.uiState.value.errorMessage,
        )
    }

    @Test
    fun finishSucceedsWhenAllSectionsAreComplete() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val event = async { viewModel.events.first() }

        viewModel.onAction(VerificationUiAction.SubmitRequested)
        advanceUntilIdle()

        assertEquals(VerificationEvent.Completed, event.await())
    }

    @Test
    fun finishSucceedsWhenAllSectionsAreCompleteWithoutEvidence() = runTest {
        val repository = StubVerificationRepository(
            session = sampleSession().copy(
                sections = sampleSession().sections.map { it.copy(evidence = emptyList()) },
                evidenceCount = 0,
            ),
        )
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()
        val event = async { viewModel.events.first() }

        viewModel.onAction(VerificationUiAction.SubmitRequested)
        advanceUntilIdle()

        assertEquals(VerificationEvent.Completed, event.await())
    }

    @Test
    fun logoutSignsOutAndEmitsSignedOut() = runTest {
        val authRepository = StubAuthRepository()
        val viewModel = buildViewModel(authRepository = authRepository)
        advanceUntilIdle()
        val event = async { viewModel.events.first() }

        viewModel.onAction(VerificationUiAction.LogoutRequested)
        advanceUntilIdle()

        assertTrue(authRepository.didSignOut)
        assertEquals(VerificationEvent.SignedOut, event.await())
    }

    @Test
    fun refreshKeepsSessionVisibleWhileReloading() = runTest {
        val refreshGate = CompletableDeferred<Unit>()
        val repository = StubVerificationRepository(refreshGate = refreshGate)
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        viewModel.onAction(VerificationUiAction.Refresh)
        runCurrent()

        assertNotNull(viewModel.uiState.value.session)
        assertTrue(viewModel.uiState.value.isRefreshing)
        assertFalse(viewModel.uiState.value.isLoading)

        refreshGate.complete(Unit)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    private class StubVerificationRepository(
        private val refreshGate: CompletableDeferred<Unit>? = null,
        private val failRefresh: Boolean = false,
        session: VerificationSession = sampleSession(),
    ) : VerificationRepository {
        private var sessionState = session
        private var loadCalls = 0

        override suspend fun loadSession(orderUnitId: String): VerificationSession {
            loadCalls += 1
            if (loadCalls > 1) {
                refreshGate?.await()
                if (failRefresh) error("refresh failed")
            }
            return sessionState
        }

        override suspend fun updateQuestionAnswer(orderUnitId: String, sectionId: String, questionId: String, optionId: String): VerificationSession = sessionState

        override suspend fun updateQuestionComment(orderUnitId: String, sectionId: String, questionId: String, value: String): VerificationSession = sessionState

        override suspend fun updateSectionNote(orderUnitId: String, sectionId: String, value: String): VerificationSession = sessionState

        override suspend fun updateComments(orderUnitId: String, value: String): VerificationSession {
            sessionState = sessionState.copy(comments = value)
            return sessionState
        }

        override suspend fun syncInspectionFlowDraft(orderUnitId: String, overallComment: String, answers: List<InspectionFlowAnswerDraft>): VerificationSession = sessionState

        override suspend fun addEvidence(orderUnitId: String, sectionId: String, upload: EvidenceUpload): VerificationSession = sessionState

        override suspend fun removeEvidence(orderUnitId: String, evidenceId: String): VerificationSession {
            sessionState = sessionState.copy(
                sections = sessionState.sections.map { section ->
                    section.copy(evidence = section.evidence.filterNot { it.id == evidenceId })
                },
                evidenceCount = sessionState.sections.sumOf { it.evidence.count { evidence -> evidence.id != evidenceId } },
            )
            return sessionState
        }

        override suspend fun pauseSession(orderUnitId: String) = Unit

        override suspend fun completeSession(orderUnitId: String) = Unit

        override suspend fun abandonSession(orderUnitId: String) = Unit
    }

    private class StubAuthRepository : AuthRepository {
        var didSignOut = false

        override suspend fun signIn(credentials: AuthCredentials): Result<AuthenticatedUser> =
            Result.success(sampleUser)

        override suspend fun probeBackend(): Result<String> = Result.success("OK")

        override suspend fun signOut() {
            didSignOut = true
        }

        override fun hasActiveSession(): Boolean = true

        override fun currentUser(): AuthenticatedUser? = sampleUser
    }
}
