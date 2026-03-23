package com.sivemore.mobile.feature.verification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sivemore.mobile.R
import com.sivemore.mobile.app.designsystem.BottomActionBar
import com.sivemore.mobile.app.designsystem.BrandedHeader
import com.sivemore.mobile.app.designsystem.BrandedLoadingScreen
import com.sivemore.mobile.app.designsystem.CategoryStrip
import com.sivemore.mobile.app.designsystem.ConfirmationDialog
import com.sivemore.mobile.app.designsystem.EvidenceTile
import com.sivemore.mobile.app.designsystem.InspectionChoiceRow
import com.sivemore.mobile.app.designsystem.SivemoreTheme
import com.sivemore.mobile.app.designsystem.SivemoreThemeTokens
import com.sivemore.mobile.app.designsystem.VerificationCard
import com.sivemore.mobile.domain.model.EvidenceSource
import com.sivemore.mobile.domain.model.InspectionItemInputMode
import com.sivemore.mobile.preview.PhonePreview
import kotlinx.coroutines.flow.collectLatest

@Composable
fun VerificationRoute(
    onOpenSessionActions: (String) -> Unit,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VerificationViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                VerificationEvent.Completed -> onCompleted()
                is VerificationEvent.OpenSessionActions -> onOpenSessionActions(event.vehicleId)
            }
        }
    }

    VerificationScreen(
        state = state,
        modifier = modifier,
        onAction = viewModel::onAction,
    )
}

@Composable
fun VerificationScreen(
    state: VerificationUiState,
    onAction: (VerificationUiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val session = state.session
    if (state.isLoading || session == null) {
        BrandedLoadingScreen(modifier = modifier)
        return
    }

    val spacing = SivemoreThemeTokens.spacing
    val selectedContent = session.categories.first { it.category == session.selectedCategory }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("verification_screen"),
    ) {
        BrandedHeader(showAction = true, onActionClick = {
            onAction(VerificationUiAction.SessionActionsRequested)
        })
        CategoryStrip(
            categories = session.categories.map { it.category },
            selectedCategory = session.selectedCategory,
            onSelected = { onAction(VerificationUiAction.CategorySelected(it)) },
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = session.selectedCategory.label,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(selectedContent.sections, key = { it.id }) { section ->
                VerificationCard {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    section.items.forEach { item ->
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (item.inputMode == InspectionItemInputMode.EvidenceTiles) {
                                Text(
                                    text = item.helperText.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (session.evidence.isEmpty()) {
                                    Text(
                                        text = "Aún no hay evidencia registrada.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                } else {
                                    session.evidence.forEach { evidence ->
                                        EvidenceTile(
                                            evidence = evidence,
                                            onRemove = {
                                                onAction(VerificationUiAction.RemoveEvidence(evidence.id))
                                            },
                                        )
                                    }
                                }
                            } else {
                                item.options.forEach { option ->
                                    InspectionChoiceRow(
                                        text = option.label,
                                        selected = option.id in item.selectedOptionIds,
                                        onClick = {
                                            onAction(
                                                VerificationUiAction.OptionToggled(
                                                    itemId = item.id,
                                                    optionId = option.id,
                                                ),
                                            )
                                        },
                                        modifier = Modifier.testTag("item_${item.id}_option_${option.id}"),
                                    )
                                }
                            }

                            if (item.inputMode == InspectionItemInputMode.CheckboxesWithNote) {
                                OutlinedTextField(
                                    value = item.noteValue,
                                    onValueChange = {
                                        onAction(VerificationUiAction.NoteChanged(item.id, it))
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(item.noteLabel ?: "Comentario") },
                                )
                            }

                            if (item.inputMode == InspectionItemInputMode.CheckboxesWithNumeric) {
                                OutlinedTextField(
                                    value = item.numericValue,
                                    onValueChange = {
                                        onAction(VerificationUiAction.NumericChanged(item.id, it))
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(item.numericLabel ?: "Valor") },
                                    suffix = {
                                        if (item.numericSuffix != null) {
                                            Text(item.numericSuffix)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        BottomActionBar(
            onAddEvidence = { onAction(VerificationUiAction.AddEvidenceRequested) },
            onAddComment = { onAction(VerificationUiAction.AddCommentRequested) },
            onSubmit = { onAction(VerificationUiAction.SubmitRequested) },
        )
    }

    if (state.showEvidenceDialog) {
        AlertDialog(
            onDismissRequest = { onAction(VerificationUiAction.EvidenceDialogDismissed) },
            title = { Text(stringResource(R.string.evidence_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    TextButton(
                        onClick = {
                                onAction(
                                    VerificationUiAction.EvidenceSourceSelected(
                                        EvidenceSource.Camera,
                                    ),
                                )
                            },
                        modifier = Modifier.testTag("source_camera"),
                    ) {
                        Text(stringResource(R.string.evidence_dialog_camera))
                    }
                    TextButton(
                        onClick = {
                                onAction(
                                    VerificationUiAction.EvidenceSourceSelected(
                                        EvidenceSource.Gallery,
                                    ),
                                )
                            },
                        modifier = Modifier.testTag("source_gallery"),
                    ) {
                        Text(stringResource(R.string.evidence_dialog_gallery))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { onAction(VerificationUiAction.EvidenceDialogDismissed) }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        )
    }

    if (state.showCommentDialog) {
        AlertDialog(
            onDismissRequest = { onAction(VerificationUiAction.CommentDialogDismissed) },
            title = { Text("Comentarios") },
            text = {
                OutlinedTextField(
                    value = state.commentDraft,
                    onValueChange = { onAction(VerificationUiAction.CommentDraftChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Agrega observaciones generales") },
                )
            },
            confirmButton = {
                TextButton(onClick = { onAction(VerificationUiAction.CommentSaved) }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(VerificationUiAction.CommentDialogDismissed) }) {
                    Text("Cancelar")
                }
            },
        )
    }

    if (state.showSubmitDialog) {
        ConfirmationDialog(
            title = "Finalizar verificación",
            text = "Se cerrará la sesión actual y el vehículo volverá a la bandeja principal.",
            confirmLabel = "Finalizar",
            onConfirm = { onAction(VerificationUiAction.SubmitConfirmed) },
            onDismiss = { onAction(VerificationUiAction.SubmitDismissed) },
        )
    }
}

@PhonePreview
@Composable
private fun VerificationScreenPreview() {
    SivemoreTheme {
        VerificationScreen(
            state = VerificationUiState(
                isLoading = false,
                session = com.sivemore.mobile.data.fixtures.FakeCatalog.createPendingSession("veh-003"),
            ),
            onAction = {},
        )
    }
}
