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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        modifier = modifier,
        onAction = viewModel::onAction,
    )
}

@Composable
fun SessionActionsScreen(
    state: SessionActionsUiState,
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
        BrandedHeader()
        VerificationCard(
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "Acciones de sesión",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Unidad ${state.vehicleLabel}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = { onAction(SessionActionsUiAction.PauseTapped) },
                modifier = Modifier.testTag("pause_session_button"),
            ) {
                Text("Pausar inspección")
            }
            OutlinedButton(
                onClick = { onAction(SessionActionsUiAction.AbandonTapped) },
                modifier = Modifier.testTag("abandon_session_button"),
            ) {
                Text("Abandonar borrador")
            }
            OutlinedButton(
                onClick = { onAction(SessionActionsUiAction.SignOutTapped) },
                modifier = Modifier.testTag("signout_button"),
            ) {
                Text("Cerrar sesión")
            }
        }
    }

    if (state.showPauseDialog) {
        ConfirmationDialog(
            title = "Pausar inspección",
            text = "La evaluación permanecerá disponible para retomarla más tarde.",
            confirmLabel = "Pausar",
            onConfirm = { onAction(SessionActionsUiAction.ConfirmPause) },
            onDismiss = { onAction(SessionActionsUiAction.DismissDialogs) },
            modifier = Modifier.testTag("pause_dialog"),
        )
    }

    if (state.showAbandonDialog) {
        ConfirmationDialog(
            title = "Abandonar borrador",
            text = "Se archivará el borrador actual y deberás iniciar una nueva inspección si vuelves a entrar.",
            confirmLabel = "Abandonar",
            onConfirm = { onAction(SessionActionsUiAction.ConfirmAbandon) },
            onDismiss = { onAction(SessionActionsUiAction.DismissDialogs) },
            modifier = Modifier.testTag("abandon_dialog"),
        )
    }

    if (state.showSignOutDialog) {
        ConfirmationDialog(
            title = "Cerrar sesión",
            text = "Se cerrará tu sesión actual en el dispositivo.",
            confirmLabel = "Salir",
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
            onAction = {},
        )
    }
}
