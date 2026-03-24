package com.sivemore.mobile

import com.sivemore.mobile.domain.model.VehicleSummary
import com.sivemore.mobile.domain.repository.VehicleRepository
import com.sivemore.mobile.feature.vehiclelookup.VehicleLookupEvent
import com.sivemore.mobile.feature.vehiclelookup.VehicleLookupUiAction
import com.sivemore.mobile.feature.vehiclelookup.VehicleLookupViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
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

    private class StubVehicleRepository(
        private val hasPendingVerification: Boolean = false,
    ) : VehicleRepository {
        override suspend fun loadVehicles(query: String): List<VehicleSummary> =
            listOf(sampleVehicle(hasPendingVerification = hasPendingVerification))
                .filter { query.isBlank() || it.plates.contains(query, ignoreCase = true) }

        override suspend fun loadVehicle(vehicleId: String): VehicleSummary? =
            sampleVehicle(id = vehicleId, hasPendingVerification = hasPendingVerification)
    }
}
