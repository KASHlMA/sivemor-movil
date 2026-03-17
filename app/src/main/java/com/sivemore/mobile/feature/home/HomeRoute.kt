package com.sivemore.mobile.feature.home

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sivemore.mobile.R
import com.sivemore.mobile.app.designsystem.EmptyStateCard
import com.sivemore.mobile.app.designsystem.SectionHeader
import com.sivemore.mobile.app.designsystem.SivemoreCard
import com.sivemore.mobile.app.designsystem.SivemorePrimaryButton
import com.sivemore.mobile.app.designsystem.SivemoreTheme
import com.sivemore.mobile.app.designsystem.SivemoreThemeTokens
import com.sivemore.mobile.domain.model.HighlightMetric
import com.sivemore.mobile.domain.model.HomeOverview
import com.sivemore.mobile.domain.model.InsightCard
import com.sivemore.mobile.domain.model.QuickAction
import com.sivemore.mobile.preview.PhonePreview

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    HomeScreen(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onAction: (HomeUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = SivemoreThemeTokens.spacing

    if (state.isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("home_loading"),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val overview = state.overview
    if (overview == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(spacing.lg)
                .testTag("home_empty"),
        ) {
            EmptyStateCard(
                title = stringResource(R.string.home_empty_message),
                description = "Connect the mapped repository later to swap the fake dashboard for live data.",
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = spacing.lg)
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(spacing.lg),
    ) {
        item {
            HeroSection(overview = overview)
        }
        item {
            SectionHeader(
                title = "Today at a glance",
                subtitle = "A compact snapshot of the routines driving your momentum.",
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                overview.highlights.forEach { metric ->
                    MetricCard(
                        metric = metric,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        item {
            SectionHeader(
                title = "Quick actions",
                subtitle = "Use the highest-value moves first, then let the app recede.",
            )
        }
        items(overview.quickActions, key = { it.id }) { action ->
            ActionCard(action = action, onAction = onAction)
        }
        item {
            SectionHeader(
                title = "Signals",
                subtitle = "Mocked insights for the future backend seam.",
            )
        }
        items(overview.insights, key = { it.id }) { insight ->
            InsightItem(insight = insight)
        }
    }
}

@Composable
private fun HeroSection(overview: HomeOverview) {
    val spacing = SivemoreThemeTokens.spacing
    SivemoreCard(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer,
                    ),
                ),
                shape = MaterialTheme.shapes.medium,
            ),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            Text(
                text = overview.greeting,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = overview.headline,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = overview.summary,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MetricCard(
    metric: HighlightMetric,
    modifier: Modifier = Modifier,
) {
    val spacing = SivemoreThemeTokens.spacing
    SivemoreCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
            Text(
                text = metric.value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = metric.label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = metric.footnote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ActionCard(
    action: QuickAction,
    onAction: (HomeUiAction) -> Unit,
) {
    val spacing = SivemoreThemeTokens.spacing
    SivemoreCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            Text(
                text = action.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = action.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SivemorePrimaryButton(
                text = action.buttonLabel,
                onClick = { onAction(HomeUiAction.Refresh) },
            )
        }
    }
}

@Composable
private fun InsightItem(insight: InsightCard) {
    val spacing = SivemoreThemeTokens.spacing
    SivemoreCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
            Text(
                text = insight.eyebrow,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = insight.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = insight.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PhonePreview
@Composable
private fun HomeScreenPreview() {
    SivemoreTheme {
        HomeScreen(
            state = HomeUiState(
                isLoading = false,
                overview = com.sivemore.mobile.data.fixtures.FakeCatalog.overview,
            ),
            onAction = {},
        )
    }
}
