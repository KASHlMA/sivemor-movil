package com.sivemore.mobile.feature.vehiclemenu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sivemore.mobile.R
import com.sivemore.mobile.app.designsystem.BrandedHeader
import com.sivemore.mobile.app.designsystem.ConfirmationDialog
import com.sivemore.mobile.app.designsystem.LoginFooterDecoration
import com.sivemore.mobile.app.designsystem.SivemorePrimaryButton
import com.sivemore.mobile.app.designsystem.SivemoreTheme
import com.sivemore.mobile.app.designsystem.SivemoreThemeTokens
import com.sivemore.mobile.preview.PhonePreview
import kotlinx.coroutines.flow.collectLatest

@Composable
fun VehicleMenuRoute(
    onOpenVehicleVisualization: () -> Unit,
    onOpenVehicleRegistration: () -> Unit,
    onOpenReports: () -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VehicleMenuViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                VehicleMenuEvent.OpenRegistration -> onOpenVehicleRegistration()
                VehicleMenuEvent.OpenVisualization -> onOpenVehicleVisualization()
                VehicleMenuEvent.OpenReports -> onOpenReports()
                VehicleMenuEvent.SignedOut -> onSignedOut()
            }
        }
    }

    VehicleMenuScreen(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@Composable
fun VehicleMenuScreen(
    state: VehicleMenuUiState,
    onAction: (VehicleMenuUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = SivemoreThemeTokens.spacing

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("vehicle_menu_screen"),
    ) {
        BrandedHeader(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .systemBarsPadding()
                .zIndex(1f),
            showAction = true,
            onActionClick = { onAction(VehicleMenuUiAction.SignOutTapped) },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 32.dp, top = 138.dp, end = 32.dp, bottom = 176.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 320.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.xl),
            ) {
                Text(
                    text = stringResource(R.string.vehicle_menu_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag("vehicle_menu_title"),
                )
                SivemorePrimaryButton(
                    text = stringResource(R.string.vehicle_menu_visualization),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("vehicle_menu_visualization"),
                    enabled = !state.isSigningOut,
                    onClick = { onAction(VehicleMenuUiAction.OpenVisualization) },
                )
                SivemorePrimaryButton(
                    text = stringResource(R.string.vehicle_menu_registration),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("vehicle_menu_registration"),
                    enabled = !state.isSigningOut,
                    onClick = { onAction(VehicleMenuUiAction.OpenRegistration) },
                )
                SivemorePrimaryButton(
                    text = stringResource(R.string.vehicle_menu_reports),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("vehicle_menu_reports"),
                    enabled = !state.isSigningOut,
                    onClick = { onAction(VehicleMenuUiAction.OpenReports) },
                )
            }
        }

        LoginFooterDecoration(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(144.dp),
        )
    }

    if (state.showSignOutDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.menu_sign_out_title),
            text = stringResource(R.string.menu_sign_out_message),
            confirmLabel = stringResource(R.string.menu_sign_out_confirm),
            onConfirm = { onAction(VehicleMenuUiAction.SignOutConfirmed) },
            onDismiss = { onAction(VehicleMenuUiAction.SignOutDismissed) },
        )
    }
}

@PhonePreview
@Composable
private fun VehicleMenuScreenPreview() {
    SivemoreTheme {
        VehicleMenuScreen(
            state = VehicleMenuUiState(),
            onAction = {},
        )
    }
}
