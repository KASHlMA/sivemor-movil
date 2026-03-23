package com.sivemore.mobile

import com.sivemore.mobile.data.repository.FakeVerificationStore
import com.sivemore.mobile.data.repository.FakeVehicleRepository
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
        val viewModel = VehicleLookupViewModel(
            vehicleRepository = FakeVehicleRepository(FakeVerificationStore()),
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.vehicles.isNotEmpty())
    }

    @Test
    fun searchFiltersVehiclesByPlate() = runTest {
        val viewModel = VehicleLookupViewModel(
            vehicleRepository = FakeVehicleRepository(FakeVerificationStore()),
        )
        advanceUntilIdle()

        viewModel.onAction(VehicleLookupUiAction.QueryChanged("MOR"))
        viewModel.onAction(VehicleLookupUiAction.SearchSubmitted)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.vehicles.size)
        assertEquals("MOR-TQ8-452", viewModel.uiState.value.vehicles.first().plates)
    }

    @Test
    fun pendingVehicleSelectionShowsDialogAndContinuesFlow() = runTest {
        val viewModel = VehicleLookupViewModel(
            vehicleRepository = FakeVehicleRepository(FakeVerificationStore()),
        )
        advanceUntilIdle()
        val event = async { viewModel.events.first() }

        viewModel.onAction(VehicleLookupUiAction.VehicleTapped("veh-003"))
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.pendingVehicle)

        viewModel.onAction(VehicleLookupUiAction.PendingDialogConfirmed)
        advanceUntilIdle()

        assertEquals(
            VehicleLookupEvent.OpenVerification("veh-003"),
            event.await(),
        )
    }
}
