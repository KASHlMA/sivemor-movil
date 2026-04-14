package com.sivemore.mobile

import com.sivemore.mobile.domain.model.Vehicle
import com.sivemore.mobile.domain.model.VehicleSummary
import com.sivemore.mobile.domain.repository.VehicleRepository
import com.sivemore.mobile.feature.vehiclelookup.VehicleLookupEvent
import com.sivemore.mobile.feature.vehiclelookup.VehicleLookupUiAction
import com.sivemore.mobile.feature.vehiclelookup.VehicleLookupViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleLookupViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun refreshLoadsVehicles() = runTest {
        val viewModel = VehicleLookupViewModel(vehicleRepository = StubVehicleRepository())
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.vehicles.isNotEmpty())
    }

    @Test
    fun searchFiltersVehiclesByPlate() = runTest {
        val viewModel = VehicleLookupViewModel(vehicleRepository = StubVehicleRepository())
        advanceUntilIdle()

        viewModel.onAction(VehicleLookupUiAction.QueryChanged("MOR"))
        viewModel.onAction(VehicleLookupUiAction.SearchSubmitted)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.vehicles.size)
        assertEquals("MOR-123-A", viewModel.uiState.value.vehicles.first().plates)
    }

    @Test
    fun pendingVehicleSelectionShowsDialogAndContinuesFlow() = runTest {
        val repository = StubVehicleRepository(hasPendingVerification = true)
        val viewModel = VehicleLookupViewModel(vehicleRepository = repository)
        advanceUntilIdle()
        val event = async { viewModel.events.first() }

        viewModel.onAction(VehicleLookupUiAction.VehicleTapped("1"))
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.pendingVehicle)

        viewModel.onAction(VehicleLookupUiAction.PendingDialogConfirmed)
        advanceUntilIdle()

        assertEquals(VehicleLookupEvent.OpenVerification("1"), event.await())
    }

    @Test
    fun refreshKeepsVehiclesVisibleWhileReloading() = runTest {
        val refreshGate = CompletableDeferred<Unit>()
        val repository = StubVehicleRepository(refreshGate = refreshGate)
        val viewModel = VehicleLookupViewModel(vehicleRepository = repository)
        advanceUntilIdle()

        viewModel.onAction(VehicleLookupUiAction.Refresh)
        runCurrent()

        assertTrue(viewModel.uiState.value.vehicles.isNotEmpty())
        assertTrue(viewModel.uiState.value.isRefreshing)
        assertTrue(!viewModel.uiState.value.isLoading)

        refreshGate.complete(Unit)
        advanceUntilIdle()

        assertTrue(!viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun refreshFailureKeepsPreviousVehiclesAndShowsError() = runTest {
        val repository = StubVehicleRepository(failRefresh = true)
        val viewModel = VehicleLookupViewModel(vehicleRepository = repository)
        advanceUntilIdle()

        viewModel.onAction(VehicleLookupUiAction.Refresh)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.vehicles.isNotEmpty())
        assertEquals("refresh failed", viewModel.uiState.value.errorMessage)
        assertTrue(!viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun editVehicleSelectionEmitsEditEvent() = runTest {
        val viewModel = VehicleLookupViewModel(vehicleRepository = StubVehicleRepository())
        advanceUntilIdle()
        val event = async { viewModel.events.first() }

        viewModel.onAction(VehicleLookupUiAction.EditVehicleTapped("1"))
        advanceUntilIdle()

        assertEquals(VehicleLookupEvent.OpenVehicleEdit("1"), event.await())
    }

    private class StubVehicleRepository(
        private val hasPendingVerification: Boolean = false,
        private val refreshGate: CompletableDeferred<Unit>? = null,
        private val failRefresh: Boolean = false,
    ) : VehicleRepository {
        private var loadCalls = 0

        override suspend fun loadVehicles(query: String): List<VehicleSummary> {
            loadCalls += 1
            if (loadCalls > 1) {
                refreshGate?.await()
                if (failRefresh) error("refresh failed")
            }
            return listOf(sampleVehicle(hasPendingVerification = hasPendingVerification))
                .filter { query.isBlank() || it.plates.contains(query, ignoreCase = true) }
        }

        override suspend fun loadVehicle(vehicleId: String): VehicleSummary? =
            sampleVehicle(id = vehicleId, hasPendingVerification = hasPendingVerification)

        override suspend fun loadVehicleForEdit(vehicleId: String): Vehicle? = null

        override suspend fun saveVehicle(vehicle: Vehicle): Vehicle = vehicle
    }
}
