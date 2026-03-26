package com.sivemore.mobile

import com.sivemore.mobile.domain.model.AuthCredentials
import com.sivemore.mobile.domain.model.AuthenticatedUser
import com.sivemore.mobile.domain.repository.AuthRepository
import com.sivemore.mobile.feature.vehiclemenu.VehicleMenuEvent
import com.sivemore.mobile.feature.vehiclemenu.VehicleMenuUiAction
import com.sivemore.mobile.feature.vehiclemenu.VehicleMenuViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleMenuViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun visualizationActionEmitsVisualizationEvent() = runTest {
        val viewModel = VehicleMenuViewModel(authRepository = RecordingAuthRepository())
        val event = async { viewModel.events.first() }

        viewModel.onAction(VehicleMenuUiAction.OpenVisualization)
        advanceUntilIdle()

        assertEquals(VehicleMenuEvent.OpenVisualization, event.await())
    }

    @Test
    fun registrationActionEmitsRegistrationEvent() = runTest {
        val viewModel = VehicleMenuViewModel(authRepository = RecordingAuthRepository())
        val event = async { viewModel.events.first() }

        viewModel.onAction(VehicleMenuUiAction.OpenRegistration)
        advanceUntilIdle()

        assertEquals(VehicleMenuEvent.OpenRegistration, event.await())
    }

    @Test
    fun signOutCallsRepositoryAndEmitsSignedOut() = runTest {
        val repository = RecordingAuthRepository()
        val viewModel = VehicleMenuViewModel(authRepository = repository)
        val event = async { viewModel.events.first() }

        viewModel.onAction(VehicleMenuUiAction.SignOut)
        advanceUntilIdle()

        assertTrue(repository.signOutCalled)
        assertEquals(VehicleMenuEvent.SignedOut, event.await())
        assertFalse(viewModel.uiState.value.isSigningOut)
    }

    private class RecordingAuthRepository : AuthRepository {
        var signOutCalled: Boolean = false

        override suspend fun signIn(credentials: AuthCredentials): Result<AuthenticatedUser> =
            Result.success(sampleUser)

        override suspend fun probeBackend(): Result<String> = Result.success("Health OK")

        override suspend fun signOut() {
            signOutCalled = true
        }

        override fun hasActiveSession(): Boolean = true

        override fun currentUser(): AuthenticatedUser? = sampleUser
    }
}
