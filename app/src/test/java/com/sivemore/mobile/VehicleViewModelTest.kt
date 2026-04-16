package com.sivemore.mobile

import androidx.lifecycle.SavedStateHandle
import com.sivemore.mobile.domain.model.AuthCredentials
import com.sivemore.mobile.domain.model.AuthenticatedUser
import com.sivemore.mobile.domain.model.Vehicle
import com.sivemore.mobile.domain.model.VehicleClient
import com.sivemore.mobile.domain.model.VehicleRegion
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
            savedStateHandle = SavedStateHandle(),
            vehicleRepository = repository,
            authRepository = RecordingAuthRepository(),
        )
        val event = async { viewModel.events.first() }

        viewModel.onAction(VehicleRegistrationUiAction.PlacaChanged("MOR-TQ8-452"))
        viewModel.onAction(VehicleRegistrationUiAction.SerieChanged("4S3BMMB68B3286050"))
        viewModel.onAction(VehicleRegistrationUiAction.TipoSelected("N2"))
        viewModel.onAction(VehicleRegistrationUiAction.ClienteSelected("1500"))
        viewModel.onAction(VehicleRegistrationUiAction.CedisSelected("20"))
        viewModel.onAction(VehicleRegistrationUiAction.MarcaChanged("Nissan"))
        viewModel.onAction(VehicleRegistrationUiAction.ModeloChanged("NP300"))
        viewModel.onAction(VehicleRegistrationUiAction.SaveVehicle)
        advanceUntilIdle()

        assertNotNull(repository.savedVehicle)
        assertTrue(event.await() is VehicleRegistrationEvent.VehicleSaved)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun saveVehicleValidatesRequiredFields() = runTest {
        val viewModel = VehicleViewModel(
            savedStateHandle = SavedStateHandle(),
            vehicleRepository = RecordingVehicleRepository(),
            authRepository = RecordingAuthRepository(),
        )

        viewModel.onAction(VehicleRegistrationUiAction.SaveVehicle)
        advanceUntilIdle()

        assertEquals("Este campo es obligatorio", viewModel.uiState.value.placa.errorMessage)
        assertEquals("Este campo es obligatorio", viewModel.uiState.value.cliente.errorMessage)
        assertFalse(viewModel.uiState.value.isFormValid)
    }

    @Test
    fun backToMenuEmitsEventWithoutSigningOut() = runTest {
        val authRepository = RecordingAuthRepository()
        val viewModel = VehicleViewModel(
            savedStateHandle = SavedStateHandle(),
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
            savedStateHandle = SavedStateHandle(),
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

    @Test
    fun existingVehicleLoadsIntoFormForEditing() = runTest {
        val existingVehicle = Vehicle(
            id = "vehicle-1",
            numeroEconomico = "1500",
            placas = "MOR-TQ8-452",
            marca = "Nissan",
            modelo = "NP300",
            tipoVehiculo = "N2",
            vin = "4S3BMMB68B3286050",
        )
        val repository = RecordingVehicleRepository(existingVehicle = existingVehicle)
        val viewModel = VehicleViewModel(
            savedStateHandle = SavedStateHandle(mapOf("vehicleId" to "vehicle-1")),
            vehicleRepository = repository,
            authRepository = RecordingAuthRepository(),
        )

        advanceUntilIdle()

        assertEquals("MOR-TQ8-452", viewModel.uiState.value.placa.value)
        assertEquals("20", viewModel.uiState.value.cedis.value)
        assertEquals("1500", viewModel.uiState.value.cliente.value)
        assertEquals("Nissan", viewModel.uiState.value.marca.value)
        assertEquals("NP300", viewModel.uiState.value.modelo.value)
        assertTrue(viewModel.uiState.value.isEditing)
    }

    private class RecordingVehicleRepository(
        private val existingVehicle: Vehicle? = null,
    ) : VehicleRepository {
        var savedVehicle: Vehicle? = null

        override suspend fun loadClients(): List<VehicleClient> =
            listOf(VehicleClient(id = "1500", name = "Transportes Morelos", regionId = "20"))

        override suspend fun loadRegions(): List<VehicleRegion> =
            listOf(VehicleRegion(id = "20", name = "Region Sur"))

        override suspend fun loadVehicles(query: String): List<VehicleSummary> = emptyList()

        override suspend fun loadVehicle(vehicleId: String): VehicleSummary? = null

        override suspend fun loadVehicleForEdit(vehicleId: String): Vehicle? = existingVehicle

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
