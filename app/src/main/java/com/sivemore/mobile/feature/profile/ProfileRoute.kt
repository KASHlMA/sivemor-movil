package com.sivemore.mobile.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sivemore.mobile.R
import com.sivemore.mobile.app.designsystem.EmptyStateCard
import com.sivemore.mobile.app.designsystem.Pill
import com.sivemore.mobile.app.designsystem.SectionHeader
import com.sivemore.mobile.app.designsystem.SivemoreCard
import com.sivemore.mobile.app.designsystem.SivemorePrimaryButton
import com.sivemore.mobile.app.designsystem.SivemoreSecondaryButton
import com.sivemore.mobile.app.designsystem.SivemoreTheme
import com.sivemore.mobile.app.designsystem.SivemoreThemeTokens
import com.sivemore.mobile.data.fixtures.FakeCatalog
import com.sivemore.mobile.domain.model.ProfileSetting
import com.sivemore.mobile.domain.model.ProfileSummary
import com.sivemore.mobile.preview.PhonePreview
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ProfileRoute(
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            if (event is ProfileEvent.SignedOut) {
                onSignedOut()
            }
        }
    }

    ProfileScreen(
        state = state,
        modifier = modifier,
        onAction = viewModel::onAction,
    )
}

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onAction: (ProfileUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = SivemoreThemeTokens.spacing

    if (state.isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val summary = state.summary
    if (summary == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(spacing.lg),
        ) {
            EmptyStateCard(
                title = stringResource(R.string.profile_empty_message),
                description = "This mocked screen is ready for the future Spring Boot contract.",
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = spacing.lg)
            .testTag("profile_screen"),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        item {
            ProfileHero(summary = summary)
        }
        item {
            SectionHeader(
                title = "Focus areas",
                subtitle = "The parts of your week that deserve the sharpest execution.",
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                summary.focusAreas.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        row.forEach { pill ->
                            Pill(text = pill.label)
                        }
                    }
                }
            }
        }
        item {
            SectionHeader(
                title = "Preferences",
                subtitle = "Static mock settings now, repository-backed settings later.",
            )
        }
        items(summary.settings, key = { it.title }) { setting ->
            SivemoreCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    Text(
                        text = setting.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = setting.value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                SivemorePrimaryButton(
                    text = stringResource(R.string.profile_edit_cta),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onAction(ProfileUiAction.Refresh) },
                )
                SivemoreSecondaryButton(
                    text = stringResource(R.string.profile_logout_cta),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_logout"),
                    onClick = { onAction(ProfileUiAction.SignOut) },
                )
            }
        }
    }
}

@Composable
private fun ProfileHero(summary: ProfileSummary) {
    val spacing = SivemoreThemeTokens.spacing
    SivemoreCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            Text(
                text = summary.displayName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = summary.role,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "${summary.city} • ${summary.email}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Profile completion ${summary.completion}%",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@PhonePreview
@Composable
private fun ProfileScreenPreview() {
    SivemoreTheme {
        ProfileScreen(
            state = ProfileUiState(
                isLoading = false,
                summary = FakeCatalog.profile,
            ),
            onAction = {},
        )
    }
}
