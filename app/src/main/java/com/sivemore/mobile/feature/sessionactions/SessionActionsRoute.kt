package com.sivemore.mobile.feature.sessionactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.sivemore.mobile.app.designsystem.SivemoreTheme
import com.sivemore.mobile.app.designsystem.VerificationCard
import com.sivemore.mobile.preview.PhonePreview
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SessionActionsRoute(
    onBackToLookup: () -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SessionActionsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                SessionActionsEvent.BackToLookup -> onBackToLookup()
                SessionActionsEvent.SignedOut -> onSignedOut()
            }
        }
    }

    SessionActionsScreen(
        state = state,
        onBackToLookup = onBackToLookup,
        modifier = modifier,
        onAction = viewModel::onAction,
    )
}

@Composable
fun SessionActionsScreen(
    state: SessionActionsUiState,
    onBackToLookup: () -> Unit,
    onAction: (SessionActionsUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isLoading) {
        BrandedLoadingScreen(modifier = modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("session_actions_screen"),
    ) {
        BrandedHeader(
            showBackButton = true,
            onBackClick = onBackToLookup,
        )
        VerificationCard(
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = stringResource(R.string.session_actions_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.session_actions_unit, state.vehicleLabel),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = { onAction(SessionActionsUiAction.PauseTapped) },
                modifier = Modifier.testTag("pause_session_button"),
            ) {
                Text(stringResource(R.string.session_actions_pause))
            }
            OutlinedButton(
                onClick = { onAction(SessionActionsUiAction.AbandonTapped) },
                modifier = Modifier.testTag("abandon_session_button"),
            ) {
                Text(stringResource(R.string.session_actions_abandon))
            }
            OutlinedButton(
                onClick = { onAction(SessionActionsUiAction.SignOutTapped) },
                modifier = Modifier.testTag("signout_button"),
            ) {
                Text(stringResource(R.string.session_actions_sign_out))
            }
        }
    }

    if (state.showPauseDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.session_actions_pause),
            text = stringResource(R.string.session_actions_pause_message),
            confirmLabel = stringResource(R.string.session_actions_pause_confirm),
            onConfirm = { onAction(SessionActionsUiAction.ConfirmPause) },
            onDismiss = { onAction(SessionActionsUiAction.DismissDialogs) },
            modifier = Modifier.testTag("pause_dialog"),
        )
    }

    if (state.showAbandonDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.session_actions_abandon),
            text = stringResource(R.string.session_actions_abandon_message),
            confirmLabel = stringResource(R.string.session_actions_abandon_confirm),
            onConfirm = { onAction(SessionActionsUiAction.ConfirmAbandon) },
            onDismiss = { onAction(SessionActionsUiAction.DismissDialogs) },
            modifier = Modifier.testTag("abandon_dialog"),
        )
    }

    if (state.showSignOutDialog) {
        ConfirmationDialog(
            title = stringResource(R.string.session_actions_sign_out),
            text = stringResource(R.string.session_actions_sign_out_message),
            confirmLabel = stringResource(R.string.session_actions_sign_out_confirm),
            onConfirm = { onAction(SessionActionsUiAction.ConfirmSignOut) },
            onDismiss = { onAction(SessionActionsUiAction.DismissDialogs) },
            modifier = Modifier.testTag("signout_dialog"),
        )
    }
}

@PhonePreview
@Composable
private fun SessionActionsPreview() {
    SivemoreTheme {
        SessionActionsScreen(
            state = SessionActionsUiState(
                isLoading = false,
                vehicleLabel = "VUH-TQ8-453",
            ),
            onBackToLookup = {},
            onAction = {},
        )
    }
}
