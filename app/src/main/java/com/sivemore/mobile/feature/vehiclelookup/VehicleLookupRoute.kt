package com.sivemore.mobile.feature.vehiclelookup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sivemore.mobile.R
import com.sivemore.mobile.app.designsystem.BrandedHeader
import com.sivemore.mobile.app.designsystem.BrandedLoadingScreen
import com.sivemore.mobile.app.designsystem.ConfirmationDialog
import com.sivemore.mobile.app.designsystem.EmptyStateCard
import com.sivemore.mobile.app.designsystem.SearchField
import com.sivemore.mobile.app.designsystem.SivemoreTheme
import com.sivemore.mobile.app.designsystem.SivemoreThemeTokens
import com.sivemore.mobile.app.designsystem.VehicleResultCard
import com.sivemore.mobile.domain.model.VehicleStatus
import com.sivemore.mobile.preview.PhonePreview
import kotlinx.coroutines.flow.collectLatest

@Composable
fun VehicleLookupRoute(
    onOpenVerification: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VehicleLookupViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            if (event is VehicleLookupEvent.OpenVerification) {
                onOpenVerification(event.vehicleId)
            }
        }
    }

    VehicleLookupScreen(
        state = state,
        modifier = modifier,
        onAction = viewModel::onAction,
    )
}

@Composable
fun VehicleLookupScreen(
    state: VehicleLookupUiState,
    onAction: (VehicleLookupUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = SivemoreThemeTokens.spacing

    if (state.isLoading) {
        BrandedLoadingScreen(modifier = modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("vehicle_lookup_screen"),
    ) {
        BrandedHeader()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(spacing.md),
        ) {
            Text(
                text = "Órdenes asignadas",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = state.errorMessage ?: "Busca por placa, orden o cliente para abrir o retomar un borrador.",
                style = MaterialTheme.typography.bodyMedium,
                color = if (state.errorMessage == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                SearchField(
                    value = state.query,
                    onValueChange = { onAction(VehicleLookupUiAction.QueryChanged(it)) },
                    modifier = Modifier.weight(1f),
                    testTag = "vehicle_search",
                )
                Button(
                    onClick = { onAction(VehicleLookupUiAction.SearchSubmitted) },
                    modifier = Modifier.testTag("vehicle_search_button"),
                ) {
                    Text(stringResource(R.string.vehicle_search_action))
                }
            }
        }

        if (state.vehicles.isEmpty()) {
            EmptyStateCard(
                title = "Sin asignaciones",
                description = "No hay unidades asignadas para este técnico en este momento.",
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 23.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(state.vehicles, key = { it.id }) { vehicle ->
                    VehicleResultCard(
                        plates = vehicle.plates,
                        serialNumber = vehicle.serialNumber,
                        vehicleNumber = vehicle.vehicleNumber,
                        admissionDate = vehicle.admissionDate,
                        completedDate = vehicle.completedDate,
                        status = vehicle.status,
                        onClick = { onAction(VehicleLookupUiAction.VehicleTapped(vehicle.id)) },
                        modifier = Modifier.testTag("vehicle_card_${vehicle.id}"),
                    )
                }
            }
        }
    }

    val pendingVehicle = state.pendingVehicle
    if (pendingVehicle != null) {
        ConfirmationDialog(
            title = stringResource(R.string.pending_dialog_title),
            text = stringResource(R.string.pending_dialog_message),
            confirmLabel = stringResource(R.string.pending_dialog_continue),
            onConfirm = { onAction(VehicleLookupUiAction.PendingDialogConfirmed) },
            onDismiss = { onAction(VehicleLookupUiAction.PendingDialogDismissed) },
            modifier = Modifier.testTag("pending_dialog"),
        )
    }
}

@PhonePreview
@Composable
private fun VehicleLookupScreenPreview() {
    SivemoreTheme {
        VehicleLookupScreen(
            state = VehicleLookupUiState(
                isLoading = false,
                vehicles = listOf(
                    com.sivemore.mobile.domain.model.VehicleSummary(
                        id = "1",
                        plates = "MOR-123-A",
                        serialNumber = "ORD-2026-001",
                        vehicleNumber = "Transportes Morelos",
                        status = VehicleStatus.InProgress,
                        admissionDate = "23/03/2026 10:00",
                        completedDate = "Cuernavaca",
                        hasPendingVerification = true,
                        draftInspectionId = "99",
                    )
                ),
            ),
            onAction = {},
        )
    }
}
