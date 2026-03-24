package com.sivemore.mobile.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sivemore.mobile.R
import com.sivemore.mobile.app.designsystem.BrandedLoadingScreen
import com.sivemore.mobile.app.designsystem.Ink
import com.sivemore.mobile.app.designsystem.LoginFooterDecoration
import com.sivemore.mobile.app.designsystem.MutedText
import com.sivemore.mobile.app.designsystem.SivemoreTheme
import com.sivemore.mobile.app.designsystem.SivemoreThemeTokens
import com.sivemore.mobile.app.designsystem.Surface
import com.sivemore.mobile.preview.PhonePreview
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AuthRoute(
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            if (event is AuthEvent.Authenticated) {
                onAuthenticated()
            }
        }
    }

    AuthScreen(
        state = state,
        modifier = modifier,
        onAction = viewModel::onAction,
    )
}

@Composable
fun AuthScreen(
    state: AuthUiState,
    onAction: (AuthUiAction) -> Unit,
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
            .background(Surface)
            .imePadding()
            .testTag("auth_screen"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 36.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            Image(
                painter = painterResource(R.drawable.figma_logo_login),
                contentDescription = null,
                modifier = Modifier
                    .width(210.dp)
                    .height(146.dp),
            )
            Text(
                text = "SIVEMOR",
                style = MaterialTheme.typography.displayLarge,
                color = Ink,
            )
            Text(
                text = stringResource(R.string.auth_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = Ink,
            )
            Text(
                text = stringResource(R.string.auth_title),
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )

            OutlinedTextField(
                value = state.username,
                onValueChange = { onAction(AuthUiAction.UsernameChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_username"),
                label = { Text(stringResource(R.string.auth_username_label)) },
                singleLine = true,
            )
            OutlinedTextField(
                value = state.password,
                onValueChange = { onAction(AuthUiAction.PasswordChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_password"),
                label = { Text(stringResource(R.string.auth_password_label)) },
                singleLine = true,
            )
            Text(
                text = state.errorMessage ?: stringResource(R.string.auth_helper),
                style = MaterialTheme.typography.bodyMedium,
                color = if (state.errorMessage != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    MutedText
                },
            )

            Button(
                onClick = { onAction(AuthUiAction.Submit) },
                modifier = Modifier
                    .width(179.dp)
                    .height(44.dp)
                    .testTag("auth_continue"),
                enabled = state.isSubmitEnabled,
            ) {
                Text(stringResource(R.string.auth_primary_cta))
            }
        }

        LoginFooterDecoration(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(144.dp),
        )
    }
}

@PhonePreview
@Composable
private fun AuthScreenPreview() {
    SivemoreTheme {
        AuthScreen(
            state = AuthUiState(username = "tecnico1", password = "secret"),
            onAction = {},
        )
    }
}
