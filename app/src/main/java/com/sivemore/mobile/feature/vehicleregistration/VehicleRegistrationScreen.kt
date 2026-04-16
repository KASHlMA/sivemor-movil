package com.sivemore.mobile.feature.vehicleregistration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sivemore.mobile.R
import com.sivemore.mobile.app.designsystem.BrandedHeader
import com.sivemore.mobile.app.designsystem.BrandedLoadingScreen
import com.sivemore.mobile.app.designsystem.ConfirmationDialog
import com.sivemore.mobile.app.designsystem.LoginFooterDecoration
import com.sivemore.mobile.app.designsystem.SivemorePrimaryButton
import com.sivemore.mobile.app.designsystem.SivemoreTheme
import com.sivemore.mobile.app.designsystem.SivemoreThemeTokens
import com.sivemore.mobile.preview.PhonePreview
import kotlinx.coroutines.flow.collectLatest

@Composable
fun VehicleRegistrationRoute(
    onBackToMenu: () -> Unit,
    onVehicleSaved: (String) -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VehicleViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                VehicleRegistrationEvent.BackToMenu -> onBackToMenu()
                VehicleRegistrationEvent.SignedOut -> onSignedOut()
                is VehicleRegistrationEvent.VehicleSaved -> onVehicleSaved(event.vehicleId)
            }
        }
    }

    VehicleRegistrationScreen(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@Composable
fun VehicleRegistrationScreen(
    state: VehicleRegistrationUiState,
    onAction: (VehicleRegistrationUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = SivemoreThemeTokens.spacing

    if (state.isLoading) {
        BrandedLoadingScreen(modifier = modifier)
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("vehicle_registration_screen"),
    ) {
        BrandedHeader(
            modifier = Modifier.align(Alignment.TopCenter),
            showAction = true,
            onActionClick = { onAction(VehicleRegistrationUiAction.OptionsMenuToggled) },
        )

        DropdownMenu(
            expanded = state.showOptionsMenu,
            onDismissRequest = { onAction(VehicleRegistrationUiAction.OptionsMenuDismissed) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 86.dp, end = 20.dp)
                .testTag("vehicle_registration_options_menu"),
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.registration_menu_back)) },
                onClick = { onAction(VehicleRegistrationUiAction.BackToMenuSelected) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.registration_menu_sign_out)) },
                onClick = { onAction(VehicleRegistrationUiAction.SignOutSelected) },
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(start = 32.dp, top = 126.dp, end = 32.dp, bottom = 176.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                Text(
                    text = stringResource(
                        if (state.isEditing) {
                            R.string.registration_edit_title
                        } else {
                            R.string.registration_title
                        },
                    ),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag("vehicle_registration_title"),
                )
                Text(
                    text = stringResource(R.string.registration_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                VehicleTextField(
                    value = state.serie.value,
                    onValueChange = { onAction(VehicleRegistrationUiAction.SerieChanged(it)) },
                    label = stringResource(R.string.registration_serie),
                    errorMessage = state.serie.errorMessage,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    testTag = "serie_input",
                )
                VehicleTextField(
                    value = state.placa.value,
                    onValueChange = { onAction(VehicleRegistrationUiAction.PlacaChanged(it)) },
                    label = stringResource(R.string.registration_placa),
                    errorMessage = state.placa.errorMessage,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    testTag = "placa_input",
                )
                VehicleDropdownField(
                    label = stringResource(R.string.registration_tipo),
                    value = state.tipo.value,
                    displayValue = state.tipo.value,
                    options = listOf("N2" to "N2", "N3" to "N3"),
                    expanded = state.showTipoMenu,
                    errorMessage = state.tipo.errorMessage,
                    onToggle = { onAction(VehicleRegistrationUiAction.TipoMenuToggled) },
                    onDismiss = { onAction(VehicleRegistrationUiAction.TipoMenuDismissed) },
                    onSelected = { onAction(VehicleRegistrationUiAction.TipoSelected(it)) },
                    testTag = "tipo_input",
                )
                VehicleDropdownField(
                    label = stringResource(R.string.registration_cliente),
                    value = state.cliente.value,
                    displayValue = state.clients.firstOrNull { it.id == state.cliente.value }?.name.orEmpty(),
                    options = state.clients.map { it.id to it.name },
                    expanded = state.showClienteMenu,
                    errorMessage = state.cliente.errorMessage,
                    onToggle = { onAction(VehicleRegistrationUiAction.ClienteMenuToggled) },
                    onDismiss = { onAction(VehicleRegistrationUiAction.ClienteMenuDismissed) },
                    onSelected = { onAction(VehicleRegistrationUiAction.ClienteSelected(it)) },
                    testTag = "cliente_input",
                )
                VehicleDropdownField(
                    label = stringResource(R.string.registration_cedis),
                    value = state.cedis.value,
                    displayValue = state.regions.firstOrNull { it.id == state.cedis.value }?.name.orEmpty(),
                    options = state.regions.map { it.id to it.name },
                    expanded = state.showCedisMenu,
                    errorMessage = state.cedis.errorMessage,
                    onToggle = { onAction(VehicleRegistrationUiAction.CedisMenuToggled) },
                    onDismiss = { onAction(VehicleRegistrationUiAction.CedisMenuDismissed) },
                    onSelected = { onAction(VehicleRegistrationUiAction.CedisSelected(it)) },
                    testTag = "cedis_input",
                )
                VehicleDropdownField(
                    label = stringResource(R.string.registration_order),
                    value = state.orden.value,
                    displayValue = state.orders.firstOrNull { it.id == state.orden.value }?.displayName.orEmpty(),
                    options = state.orders
                        .filter { it.clientCompanyId == state.cliente.value }
                        .map { it.id to it.displayName },
                    expanded = state.showOrdenMenu,
                    errorMessage = state.orden.errorMessage,
                    onToggle = { onAction(VehicleRegistrationUiAction.OrdenMenuToggled) },
                    onDismiss = { onAction(VehicleRegistrationUiAction.OrdenMenuDismissed) },
                    onSelected = { onAction(VehicleRegistrationUiAction.OrdenSelected(it)) },
                    testTag = "orden_input",
                )
                VehicleTextField(
                    value = state.marca.value,
                    onValueChange = { onAction(VehicleRegistrationUiAction.MarcaChanged(it)) },
                    label = stringResource(R.string.registration_marca),
                    errorMessage = state.marca.errorMessage,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    testTag = "marca_input",
                )
                VehicleTextField(
                    value = state.modelo.value,
                    onValueChange = { onAction(VehicleRegistrationUiAction.ModeloChanged(it)) },
                    label = stringResource(R.string.registration_modelo),
                    errorMessage = state.modelo.errorMessage,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    testTag = "modelo_input",
                )

                state.globalErrorMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                SivemorePrimaryButton(
                    text = stringResource(
                        if (state.isEditing) {
                            R.string.registration_update
                        } else {
                            R.string.registration_save
                        },
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("save_vehicle_button"),
                    enabled = state.isFormValid && !state.isSaving,
                    onClick = { onAction(VehicleRegistrationUiAction.SaveVehicle) },
                )
            }
        }

        LoginFooterDecoration(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(144.dp),
        )

        if (state.showSignOutDialog) {
            ConfirmationDialog(
                title = stringResource(R.string.registration_menu_sign_out),
                text = stringResource(R.string.registration_sign_out_message),
                confirmLabel = stringResource(R.string.registration_sign_out_confirm),
                onConfirm = { onAction(VehicleRegistrationUiAction.SignOutConfirmed) },
                onDismiss = { onAction(VehicleRegistrationUiAction.SignOutDismissed) },
                modifier = Modifier.testTag("vehicle_registration_sign_out_dialog"),
            )
        }
    }
}

@Composable
private fun VehicleDropdownField(
    label: String,
    value: String,
    displayValue: String,
    options: List<Pair<String, String>>,
    expanded: Boolean,
    errorMessage: String?,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit,
    testTag: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onToggle,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag),
            ) {
                Text(
                    text = displayValue.ifBlank { stringResource(R.string.registration_select_placeholder) },
                    color = if (value.isBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            ) {
                options.forEach { (optionValue, optionLabel) ->
                    DropdownMenuItem(
                        text = { Text(optionLabel) },
                        onClick = { onSelected(optionValue) },
                    )
                }
            }
        }
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun VehicleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    errorMessage: String?,
    keyboardOptions: KeyboardOptions,
    testTag: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        singleLine = true,
        isError = errorMessage != null,
        label = { Text(label) },
        supportingText = {
            if (errorMessage != null) {
                Text(errorMessage)
            }
        },
        keyboardOptions = keyboardOptions,
    )
}

@PhonePreview
@Composable
private fun VehicleRegistrationScreenPreview() {
    SivemoreTheme {
        VehicleRegistrationScreen(
            state = VehicleRegistrationUiState(),
            onAction = {},
        )
    }
}
