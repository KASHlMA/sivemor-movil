package com.sivemore.mobile.feature.luces

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sivemore.mobile.R
import com.sivemore.mobile.app.designsystem.BrandedHeader
import com.sivemore.mobile.app.designsystem.SivemoreTheme
import com.sivemore.mobile.app.designsystem.VerificationCard
import com.sivemore.mobile.feature.inspection.InspectionQuestionItem
import com.sivemore.mobile.preview.PhonePreview
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LucesRoute(
    onNavigateNext: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LucesViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is LucesEvent.NavigateToNextSection -> onNavigateNext(event.vehicleId)
            }
        }
    }

    LucesScreen(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier,
    )
}

@Composable
fun LucesScreen(
    state: LucesUiState,
    onAction: (LucesUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("luces_screen"),
        topBar = {
            BrandedHeader(modifier = Modifier.systemBarsPadding())
        },
        bottomBar = {
            Button(
                onClick = { onAction(LucesUiAction.NextClicked) },
                enabled = state.isNextEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .testTag("luces_next_button"),
            ) {
                Text(stringResource(R.string.luces_next_button))
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = state.section.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            item {
                Text(
                    text = state.section.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(state.section.questions, key = { it.id }) { question ->
                LucesInspectionCard(
                    question = question,
                    onOptionSelected = { optionId ->
                        onAction(LucesUiAction.OptionSelected(question.id, optionId))
                    },
                )
            }
        }
    }
}

@Composable
private fun LucesInspectionCard(
    question: InspectionQuestionItem,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    VerificationCard(modifier = modifier.testTag("luces_card_${question.id}")) {
        Text(
            text = question.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            question.options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOptionSelected(option.id) }
                        .padding(vertical = 2.dp)
                        .testTag("luces_option_${question.id}_${option.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    RadioButton(
                        selected = question.selectedOptionId == option.id,
                        onClick = { onOptionSelected(option.id) },
                    )
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@PhonePreview
@Composable
private fun LucesScreenPreview() {
    SivemoreTheme {
        LucesScreen(
            state = LucesUiState(
                vehicleId = "1",
                section = com.sivemore.mobile.feature.inspection.InspectionSectionCatalog.lucesSection(),
            ),
            onAction = {},
        )
    }
}
