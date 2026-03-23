package com.sivemore.mobile

import androidx.lifecycle.SavedStateHandle
import com.sivemore.mobile.data.repository.FakeVerificationRepository
import com.sivemore.mobile.data.repository.FakeVerificationStore
import com.sivemore.mobile.domain.model.EvidenceSource
import com.sivemore.mobile.domain.model.InspectionCategory
import com.sivemore.mobile.feature.verification.VerificationEvent
import com.sivemore.mobile.feature.verification.VerificationUiAction
import com.sivemore.mobile.feature.verification.VerificationViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VerificationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun buildViewModel(
        vehicleId: String = "veh-003",
        store: FakeVerificationStore = FakeVerificationStore(),
    ): VerificationViewModel = VerificationViewModel(
        savedStateHandle = SavedStateHandle(mapOf("vehicleId" to vehicleId)),
        verificationRepository = FakeVerificationRepository(store),
    )

    @Test
    fun categorySelectionUpdatesSession() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(VerificationUiAction.CategorySelected(InspectionCategory.Evidence))
        advanceUntilIdle()

        assertEquals(
            InspectionCategory.Evidence,
            viewModel.uiState.value.session?.selectedCategory,
        )
    }

    @Test
    fun optionAndNumericInputsUpdateSessionState() = runTest {
        val viewModel = buildViewModel(vehicleId = "veh-002")
        advanceUntilIdle()

        viewModel.onAction(VerificationUiAction.OptionToggled("tires_missing_lugs", "missing"))
        viewModel.onAction(VerificationUiAction.NumericChanged("tires_missing_lugs", "95"))
        advanceUntilIdle()

        val item = viewModel.uiState.value.session
            ?.categories
            ?.flatMap { it.sections }
            ?.flatMap { it.items }
            ?.first { it.id == "tires_missing_lugs" }

        assertTrue(item?.selectedOptionIds?.contains("missing") == true)
        assertEquals("95", item?.numericValue)
    }

    @Test
    fun evidenceCanBeAddedAndRemoved() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(VerificationUiAction.EvidenceSourceSelected(EvidenceSource.Camera))
        advanceUntilIdle()

        val evidenceId = viewModel.uiState.value.session?.evidence?.last()?.id
        assertTrue(viewModel.uiState.value.session?.evidence?.isNotEmpty() == true)

        viewModel.onAction(VerificationUiAction.RemoveEvidence(checkNotNull(evidenceId)))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.session?.evidence?.none { it.id == evidenceId } == true)
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
}
