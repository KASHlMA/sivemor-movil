package com.sivemore.mobile.feature.inspection

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sivemore.mobile.app.designsystem.BrandedHeader
import com.sivemore.mobile.app.designsystem.BrandedLoadingScreen
import com.sivemore.mobile.app.designsystem.SivemoreTheme
import com.sivemore.mobile.app.designsystem.VerificationCard
import com.sivemore.mobile.domain.model.EvidenceUpload
import com.sivemore.mobile.preview.PhonePreview
import java.io.File
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AireFrenosRoute(
    viewModel: InspectionFlowViewModel,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
    onBackToLookup: () -> Unit,
    onBackToMenu: () -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        val captureUri = pendingCaptureUri
        if (success && captureUri != null) {
            viewModel.onAction(
                InspectionFlowAction.EvidencePicked(
                    EvidenceUpload(
                        uri = captureUri.toString(),
                        fileName = "captured-${System.currentTimeMillis()}.jpg",
                        mimeType = "image/jpeg",
                    ),
                ),
            )
        }
        pendingCaptureUri = null
    }

    LaunchedEffect(viewModel) {
        viewModel.onAction(InspectionFlowAction.EnterSection(3))
        viewModel.events.collectLatest { event ->
            when (event) {
                InspectionFlowEvent.NavigateToNextSection -> onNavigateNext()
                InspectionFlowEvent.BackToLookup -> onBackToLookup()
                InspectionFlowEvent.BackToMenu -> onBackToMenu()
                InspectionFlowEvent.Completed -> onBackToLookup()
                InspectionFlowEvent.SignedOut -> onSignedOut()
            }
        }
    }

    AireFrenosScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onNavigateBack,
        onTakePhoto = {
            if (state.canAddMorePhotos) {
                val captureUri = createPersistentCaptureUri(context)
                pendingCaptureUri = captureUri
                cameraLauncher.launch(captureUri)
            } else {
                viewModel.onAction(InspectionFlowAction.PhotoLimitReached)
            }
        },
        modifier = modifier,
    )
}

@Composable
fun AireFrenosScreen(
    state: InspectionFlowUiState,
    onAction: (InspectionFlowAction) -> Unit,
    onBack: () -> Unit,
    onTakePhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isLoading) {
        BrandedLoadingScreen(modifier = modifier)
        return
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("aire_frenos_screen"),
        topBar = {
            BrandedHeader(
                modifier = Modifier.systemBarsPadding(),
                showAction = true,
                onActionClick = { onAction(InspectionFlowAction.LogoutRequested) },
            )
        },
        bottomBar = {
            InspectionGlobalActionsPanel(
                onAddComment = { onAction(InspectionFlowAction.CommentDialogOpened) },
                onTakePhoto = onTakePhoto,
                onPause = { onAction(InspectionFlowAction.PauseRequested) },
                onFinish = null,
            )
        },
    ) { innerPadding ->
        AireFrenosContent(
            state = state,
            onAction = onAction,
            onBack = onBack,
            innerPadding = innerPadding,
        )
    }

    if (state.showPauseDialog) {
        InspectionPauseDialog(
            onConfirm = { onAction(InspectionFlowAction.PauseConfirmed) },
            onDismiss = { onAction(InspectionFlowAction.PauseDismissed) },
        )
    }

    if (state.showCommentDialog) {
        InspectionCommentDialog(
            commentValue = state.commentDraft,
            onValueChange = { onAction(InspectionFlowAction.CommentDraftChanged(it)) },
            onDismiss = { onAction(InspectionFlowAction.CommentDialogDismissed) },
            onSave = { onAction(InspectionFlowAction.CommentSaved) },
            isSaving = state.isSavingComment,
        )
    }
}

@Composable
private fun AireFrenosContent(
    state: InspectionFlowUiState,
    onAction: (InspectionFlowAction) -> Unit,
    onBack: () -> Unit,
    innerPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = state.aireFrenosSection.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        item {
            Text(
                text = state.errorMessage ?: state.aireFrenosSection.description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (state.errorMessage == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
            )
        }
        if (!state.commentDraft.isBlank()) {
            item {
                Text(
                    text = "Comentario actual: ${state.commentDraft}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(state.aireFrenosSection.questions, key = { it.id }) { question ->
            AireFrenosInspectionCard(
                question = question,
                onOptionSelected = { optionId ->
                    onAction(InspectionFlowAction.AireFrenosOptionSelected(question.id, optionId))
                },
                onNumericValueChanged = { value ->
                    onAction(InspectionFlowAction.AireFrenosNumericValueChanged(question.id, value))
                },
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = {
                        onAction(InspectionFlowAction.PreviousClicked)
                        onBack()
                    },
                    enabled = state.canGoBack,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("aire_frenos_back_button"),
                ) {
                    Text("Atrás")
                }
                Button(
                    onClick = { onAction(InspectionFlowAction.NextClicked) },
                    enabled = state.isCurrentSectionComplete,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("aire_frenos_next_button"),
                ) {
                    Text("Siguiente")
                }
            }
        }
    }
}

@Composable
private fun AireFrenosInspectionCard(
    question: InspectionQuestionItem,
    onOptionSelected: (String) -> Unit,
    onNumericValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    VerificationCard(modifier = modifier.testTag("aire_frenos_card_${question.id}")) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = question.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            RuleStatusChip(status = question.aireFrenosRuleStatus())
        }
        when (question.kind) {
            InspectionQuestionKind.SingleChoice -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    question.options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOptionSelected(option.id) }
                                .padding(vertical = 2.dp)
                                .testTag("aire_frenos_option_${question.id}_${option.id}"),
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

            InspectionQuestionKind.NumericInput -> {
                if (question.helperText != null) {
                    Text(
                        text = question.helperText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                OutlinedTextField(
                    value = question.numericValue,
                    onValueChange = onNumericValueChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("aire_frenos_numeric_${question.id}"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text(question.placeholder) },
                )
            }
        }
    }
}

@Composable
private fun RuleStatusChip(
    status: RuleStatus,
    modifier: Modifier = Modifier,
) {
    val colors = when (status.tone) {
        RuleStatusTone.Success -> AssistChipDefaults.assistChipColors(
            containerColor = androidx.compose.ui.graphics.Color(0xFFDFF3E3),
            labelColor = androidx.compose.ui.graphics.Color(0xFF155724),
        )
        RuleStatusTone.Danger -> AssistChipDefaults.assistChipColors(
            containerColor = androidx.compose.ui.graphics.Color(0xFFFADBDD),
            labelColor = androidx.compose.ui.graphics.Color(0xFF8A1C24),
        )
        RuleStatusTone.Neutral -> AssistChipDefaults.assistChipColors(
            containerColor = androidx.compose.ui.graphics.Color(0xFFE8ECEF),
            labelColor = androidx.compose.ui.graphics.Color(0xFF39434A),
        )
    }
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(status.label) },
        colors = colors,
        modifier = modifier,
    )
}

private fun InspectionQuestionItem.aireFrenosRuleStatus(): RuleStatus {
    if (kind != InspectionQuestionKind.NumericInput || numericValue.isBlank()) {
        return RuleStatus("Pendiente", RuleStatusTone.Neutral)
    }
    val numeric = numericValue.replace(",", ".").toDoubleOrNull()
        ?: return RuleStatus("Pendiente", RuleStatusTone.Neutral)
    return when (id) {
        "aire_frenos_tiempo_carga_psi" ->
            if (numeric in 70.0..120.0) RuleStatus("Cumple rango PSI", RuleStatusTone.Success) else RuleStatus("Fuera de rango PSI", RuleStatusTone.Danger)
        "aire_frenos_tiempo_carga_tiempo" ->
            if (numeric < 120.0) RuleStatus("Cumple tiempo", RuleStatusTone.Success) else RuleStatus("Excede tiempo", RuleStatusTone.Danger)
        else -> RuleStatus("Capturado", RuleStatusTone.Neutral)
    }
}


@PhonePreview
@Composable
private fun AireFrenosScreenPreview() {
    SivemoreTheme {
        AireFrenosScreen(
            state = InspectionFlowUiState(
                vehicleId = "1",
                isLoading = false,
                lucesSection = InspectionSectionCatalog.lucesSection(),
                llantasSection = InspectionSectionCatalog.llantasSection(),
                direccionSection = InspectionSectionCatalog.direccionSection(),
                aireFrenosSection = InspectionSectionCatalog.aireFrenosSection(),
                motorEmisionesSection = InspectionSectionCatalog.motorEmisionesSection(),
                otrosSection = InspectionSectionCatalog.otrosSection(),
            ),
            onAction = {},
            onBack = {},
            onTakePhoto = {},
        )
    }
}
