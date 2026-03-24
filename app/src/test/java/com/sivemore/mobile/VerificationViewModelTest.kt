package com.sivemore.mobile

import androidx.lifecycle.SavedStateHandle
import com.sivemore.mobile.domain.model.EvidenceUpload
import com.sivemore.mobile.domain.model.VerificationSession
import com.sivemore.mobile.domain.repository.VerificationRepository
import com.sivemore.mobile.feature.verification.VerificationEvent
import com.sivemore.mobile.feature.verification.VerificationUiAction
import com.sivemore.mobile.feature.verification.VerificationViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VerificationViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun buildViewModel(repository: StubVerificationRepository = StubVerificationRepository()): VerificationViewModel =
        VerificationViewModel(
            savedStateHandle = SavedStateHandle(mapOf("vehicleId" to "1")),
            verificationRepository = repository,
        )

    @Test
    fun commentEditingUpdatesSession() = runTest {
        val repository = StubVerificationRepository()
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        viewModel.onAction(VerificationUiAction.CommentDraftChanged("Observación general"))
        viewModel.onAction(VerificationUiAction.CommentSaved)
        advanceUntilIdle()

        assertEquals("Observación general", viewModel.uiState.value.session?.comments)
    }

    @Test
    fun evidenceRemovalUpdatesSession() = runTest {
        val repository = StubVerificationRepository()
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()

        viewModel.onAction(VerificationUiAction.RemoveEvidence("e1"))
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.session?.sections?.first()?.evidence?.size)
    }

    @Test
    fun completingSessionEmitsCompletedEvent() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val event = async { viewModel.events.first() }

        viewModel.onAction(VerificationUiAction.SubmitConfirmed)
        advanceUntilIdle()

        assertEquals(VerificationEvent.Completed, event.await())
    }

    private class StubVerificationRepository : VerificationRepository {
        private var session = sampleSession()

        override suspend fun loadSession(orderUnitId: String): VerificationSession = session

        override suspend fun updateQuestionAnswer(orderUnitId: String, sectionId: String, questionId: String, optionId: String): VerificationSession = session

        override suspend fun updateQuestionComment(orderUnitId: String, sectionId: String, questionId: String, value: String): VerificationSession = session

        override suspend fun updateSectionNote(orderUnitId: String, sectionId: String, value: String): VerificationSession = session

        override suspend fun updateComments(orderUnitId: String, value: String): VerificationSession {
            session = session.copy(comments = value)
            return session
        }

        override suspend fun addEvidence(orderUnitId: String, sectionId: String, upload: EvidenceUpload): VerificationSession = session

        override suspend fun removeEvidence(orderUnitId: String, evidenceId: String): VerificationSession {
            session = session.copy(
                sections = session.sections.map { section ->
                    section.copy(evidence = section.evidence.filterNot { it.id == evidenceId })
                }
            )
            return session
        }

        override suspend fun pauseSession(orderUnitId: String) = Unit

        override suspend fun completeSession(orderUnitId: String) = Unit

        override suspend fun abandonSession(orderUnitId: String) = Unit
    }
}
