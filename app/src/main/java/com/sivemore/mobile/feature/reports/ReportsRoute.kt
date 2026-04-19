package com.sivemore.mobile.feature.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sivemore.mobile.R
import com.sivemore.mobile.app.designsystem.BrandedHeader
import com.sivemore.mobile.app.designsystem.BrandedLoadingScreen
import com.sivemore.mobile.app.designsystem.EmptyStateCard
import com.sivemore.mobile.app.designsystem.SivemoreTheme
import com.sivemore.mobile.domain.model.CompletedReport
import com.sivemore.mobile.domain.model.ReportVerdict
import com.sivemore.mobile.preview.PhonePreview
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ReportsRoute(
    onNavigateBack: () -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                ReportsEvent.NavigateBack -> onNavigateBack()
                ReportsEvent.SignedOut -> onSignedOut()
            }
        }
    }

    ReportsScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@Composable
fun ReportsScreen(
    state: ReportsUiState,
    onNavigateBack: () -> Unit,
    onAction: (ReportsUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isLoading) {
        BrandedLoadingScreen(modifier = modifier)
        return
    }

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onAction(ReportsUiAction.Refresh) },
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("reports_screen"),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            BrandedHeader(
                modifier = Modifier.systemBarsPadding(),
                showBackButton = true,
                onBackClick = onNavigateBack,
                showAction = true,
                onActionClick = { onAction(ReportsUiAction.LogoutRequested) },
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.reports_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = state.errorMessage ?: stringResource(R.string.reports_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (state.errorMessage == null) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.error,
                )
            }

            if (state.reports.isEmpty()) {
                EmptyStateCard(
                    title = stringResource(R.string.reports_empty_title),
                    description = stringResource(R.string.reports_empty_description),
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.reports, key = { it.id }) { report ->
                        ReportCard(
                            report = report,
                            modifier = Modifier.testTag("report_card_${report.id}"),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportCard(
    report: CompletedReport,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = report.vehiclePlate,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                VerdictChip(verdict = report.verdict)
            }
            Text(
                text = report.orderNumber,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = report.clientCompanyName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.reports_submitted_at, report.submittedAtLabel),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (report.comments.isNotBlank()) {
                Text(
                    text = report.comments,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun VerdictChip(
    verdict: ReportVerdict,
    modifier: Modifier = Modifier,
) {
    val (bgColor, textColor) = when (verdict) {
        ReportVerdict.Approved -> Color(0xFFDFF3E3) to Color(0xFF155724)
        ReportVerdict.Rejected -> Color(0xFFFADBDD) to Color(0xFF8A1C24)
        ReportVerdict.Pending -> Color(0xFFE8ECEF) to Color(0xFF39434A)
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = bgColor,
    ) {
        Text(
            text = verdict.label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@PhonePreview
@Composable
private fun ReportsScreenPreview() {
    SivemoreTheme {
        ReportsScreen(
            state = ReportsUiState(
                isLoading = false,
                reports = listOf(
                    CompletedReport(
                        id = "1",
                        orderNumber = "ORD-2026-001",
                        vehiclePlate = "MOR-123-A",
                        clientCompanyName = "Transportes Morelos",
                        submittedAtLabel = "15/03/2026 09:30",
                        verdict = ReportVerdict.Approved,
                        comments = "Sin observaciones.",
                    ),
                    CompletedReport(
                        id = "2",
                        orderNumber = "ORD-2026-002",
                        vehiclePlate = "MOR-456-B",
                        clientCompanyName = "Logistica Central",
                        submittedAtLabel = "16/03/2026 14:00",
                        verdict = ReportVerdict.Rejected,
                        comments = "Falla en frenos traseros.",
                    ),
                ),
            ),
            onNavigateBack = {},
            onAction = {},
        )
    }
}
