package com.sivemore.mobile

import androidx.lifecycle.SavedStateHandle
import com.sivemore.mobile.data.repository.FakeVerificationRepository
import com.sivemore.mobile.data.repository.FakeVerificationStore
import com.sivemore.mobile.data.repository.FakeVehicleRepository
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

    private fun buildViewModel(
        store: FakeVerificationStore = FakeVerificationStore(),
    ): SessionActionsViewModel = SessionActionsViewModel(
        savedStateHandle = SavedStateHandle(mapOf("vehicleId" to "veh-003")),
        vehicleRepository = FakeVehicleRepository(store),
        verificationRepository = FakeVerificationRepository(store),
    )

    @Test
    fun confirmPauseEmitsBackToLookup() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val event = async { viewModel.events.first() }

        viewModel.onAction(SessionActionsUiAction.ConfirmPause)
        advanceUntilIdle()

        assertEquals(SessionActionsEvent.BackToLookup, event.await())
    }

    @Test
    fun confirmLogoutEmitsSignedOut() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val event = async { viewModel.events.first() }

        viewModel.onAction(SessionActionsUiAction.ConfirmLogout)
        advanceUntilIdle()

        assertEquals(SessionActionsEvent.SignedOut, event.await())
    }
}
