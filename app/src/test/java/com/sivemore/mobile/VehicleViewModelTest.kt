package com.sivemore.mobile

import com.sivemore.mobile.domain.model.AuthCredentials
import com.sivemore.mobile.domain.model.AuthenticatedUser
import com.sivemore.mobile.domain.model.Vehicle
import com.sivemore.mobile.domain.model.VehicleSummary
import com.sivemore.mobile.domain.repository.AuthRepository
import com.sivemore.mobile.domain.repository.VehicleRepository
import com.sivemore.mobile.feature.vehicleregistration.VehicleRegistrationEvent
import com.sivemore.mobile.feature.vehicleregistration.VehicleRegistrationUiAction
import com.sivemore.mobile.feature.vehicleregistration.VehicleViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun saveVehicleEmitsEventWhenFormIsValid() = runTest {
        val repository = RecordingVehicleRepository()
        val viewModel = VehicleViewModel(
            vehicleRepository = repository,
            authRepository = RecordingAuthRepository(),
        )
        val event = async { viewModel.events.first() }

        viewModel.onAction(VehicleRegistrationUiAction.PlacaChanged("MOR-TQ8-452"))
        viewModel.onAction(VehicleRegistrationUiAction.SerieChanged("4S3BMMB68B3286050"))
        viewModel.onAction(VehicleRegistrationUiAction.CedisChanged("Morelos"))
        viewModel.onAction(VehicleRegistrationUiAction.NumeroClienteChanged("1500"))
        viewModel.onAction(VehicleRegistrationUiAction.SaveVehicle)
        advanceUntilIdle()

        assertNotNull(repository.savedVehicle)
        assertTrue(event.await() is VehicleRegistrationEvent.VehicleSaved)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun saveVehicleValidatesRequiredFields() = runTest {
        val viewModel = VehicleViewModel(
            vehicleRepository = RecordingVehicleRepository(),
            authRepository = RecordingAuthRepository(),
        )

        viewModel.onAction(VehicleRegistrationUiAction.SaveVehicle)
        advanceUntilIdle()

        assertEquals("Este campo es obligatorio", viewModel.uiState.value.placa.errorMessage)
        assertEquals("Este campo es obligatorio", viewModel.uiState.value.cedis.errorMessage)
        assertFalse(viewModel.uiState.value.isFormValid)
    }

    @Test
    fun backToMenuEmitsEventWithoutSigningOut() = runTest {
        val authRepository = RecordingAuthRepository()
        val viewModel = VehicleViewModel(
            vehicleRepository = RecordingVehicleRepository(),
            authRepository = authRepository,
        )
        val event = async { viewModel.events.first() }

        viewModel.onAction(VehicleRegistrationUiAction.BackToMenuSelected)
        advanceUntilIdle()

        assertEquals(VehicleRegistrationEvent.BackToMenu, event.await())
        assertFalse(authRepository.signOutCalled)
    }

    @Test
    fun signOutFlowShowsDialogAndSignsOutOnConfirm() = runTest {
        val authRepository = RecordingAuthRepository()
        val viewModel = VehicleViewModel(
            vehicleRepository = RecordingVehicleRepository(),
            authRepository = authRepository,
        )
        val event = async { viewModel.events.first() }

        viewModel.onAction(VehicleRegistrationUiAction.SignOutSelected)
        assertTrue(viewModel.uiState.value.showSignOutDialog)

        viewModel.onAction(VehicleRegistrationUiAction.SignOutConfirmed)
        advanceUntilIdle()

        assertTrue(authRepository.signOutCalled)
        assertEquals(VehicleRegistrationEvent.SignedOut, event.await())
        assertFalse(viewModel.uiState.value.showSignOutDialog)
    }

    private class RecordingVehicleRepository : VehicleRepository {
        var savedVehicle: Vehicle? = null

        override suspend fun loadVehicles(query: String): List<VehicleSummary> = emptyList()

        override suspend fun loadVehicle(vehicleId: String): VehicleSummary? = null

        override suspend fun saveVehicle(vehicle: Vehicle): Vehicle {
            savedVehicle = vehicle
            return vehicle
        }
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
