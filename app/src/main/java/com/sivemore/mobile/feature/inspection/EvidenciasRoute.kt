package com.sivemore.mobile.feature.inspection

import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sivemore.mobile.R
import com.sivemore.mobile.app.designsystem.BrandedHeader
import com.sivemore.mobile.app.designsystem.BrandedLoadingScreen
import com.sivemore.mobile.app.designsystem.SivemoreTheme
import com.sivemore.mobile.domain.model.EvidenceItem
import com.sivemore.mobile.domain.model.EvidenceUpload
import com.sivemore.mobile.preview.PhonePreview
import kotlinx.coroutines.flow.collectLatest

@Composable
fun EvidenciasRoute(
    viewModel: InspectionFlowViewModel,
    onNavigateBack: () -> Unit,
    onBackToMenu: () -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

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
        viewModel.onAction(InspectionFlowAction.EnterSection(6))
        viewModel.events.collectLatest { event ->
            when (event) {
                InspectionFlowEvent.NavigateToNextSection -> Unit
                InspectionFlowEvent.BackToLookup -> onBackToMenu()
                InspectionFlowEvent.Completed -> showSuccessDialog = true
                InspectionFlowEvent.SignedOut -> onSignedOut()
            }
        }
    }

    EvidenciasScreen(
        state = state,
        onAction = viewModel::onAction,
        onBack = onNavigateBack,
        onTakePhoto = {
            if (state.allEvidence.size < 3) {
                val captureUri = createPersistentCaptureUri(context)
                pendingCaptureUri = captureUri
                cameraLauncher.launch(captureUri)
            } else {
                viewModel.onAction(InspectionFlowAction.PhotoLimitReached)
            }
        },
        modifier = modifier,
    )

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onBackToMenu()
            },
            title = {
                Text(stringResource(R.string.inspection_submit_success_title))
            },
            text = {
                Text(stringResource(R.string.inspection_submit_success_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onBackToMenu()
                    },
                ) {
                    Text(stringResource(R.string.inspection_submit_success_confirm))
                }
            },
        )
    }
}

@Composable
fun EvidenciasScreen(
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

    var selectedEvidence by remember(state.allEvidence) { mutableStateOf<EvidenceItem?>(null) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("evidencias_screen"),
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
                onFinish = { onAction(InspectionFlowAction.SubmitRequested) },
            )
        },
    ) { innerPadding ->
        EvidenciasContent(
            state = state,
            onAction = onAction,
            onBack = onBack,
            innerPadding = innerPadding,
            onEvidenceSelected = { selectedEvidence = it },
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

    if (selectedEvidence != null) {
        EvidencePreviewDialog(
            evidence = selectedEvidence!!,
            onDismiss = { selectedEvidence = null },
            onDelete = {
                onAction(InspectionFlowAction.RemoveEvidence(selectedEvidence!!.id))
                selectedEvidence = null
            },
        )
    }
}

@Composable
private fun EvidenciasContent(
    state: InspectionFlowUiState,
    onAction: (InspectionFlowAction) -> Unit,
    onBack: () -> Unit,
    innerPadding: PaddingValues,
    onEvidenceSelected: (EvidenceItem) -> Unit,
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
                text = "Evidencias",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        item {
            Text(
                text = state.errorMessage ?: "El registro de evidencias es opcional. Se permite un máximo de 3 fotografías por evaluación.",
                style = MaterialTheme.typography.bodyMedium,
                color = if (state.errorMessage == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
            )
        }
        if (state.allEvidence.isEmpty()) {
            item {
                Text(
                    text = "No hay evidencias registradas",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("evidencias_empty_message"),
                )
            }
        } else {
            items(state.allEvidence, key = { it.id }) { evidence ->
                EvidenceGalleryItem(
                    evidence = evidence,
                    onOpen = { onEvidenceSelected(evidence) },
                    onDelete = { onAction(InspectionFlowAction.RemoveEvidence(evidence.id)) },
                )
            }
        }
        item {
            Text(
                text = "Comentario de la evaluación",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        item {
            OutlinedTextField(
                value = state.commentDraft,
                onValueChange = { onAction(InspectionFlowAction.CommentDraftChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("evidencias_comment_input"),
                placeholder = { Text("Escriba su comentario") },
                minLines = 3,
                maxLines = 5,
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
                        .testTag("evidencias_back_button"),
                ) {
                    Text("Atrás")
                }
                Button(
                    onClick = { onAction(InspectionFlowAction.SubmitRequested) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("evidencias_submit_button"),
                ) {
                    Text("Enviar")
                }
            }
        }
    }
}

@Composable
private fun EvidenceGalleryItem(
    evidence: EvidenceItem,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE2E2E2), RoundedCornerShape(12.dp))
            .clickable(onClick = onOpen)
            .testTag("evidence_item_${evidence.id}")
            .padding(12.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            EvidenceImage(
                evidence = evidence,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.7f),
            )
            Text(
                text = evidence.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = evidence.addedAtLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDelete) {
                    Text(stringResource(R.string.evidence_remove))
                }
            }
        }
    }
}

@Composable
private fun EvidenceImage(
    evidence: EvidenceItem,
    modifier: Modifier = Modifier,
) {
    if (evidence.previewUri.isNullOrBlank()) {
        Box(
            modifier = modifier
                .background(Color(0xFFD9D9D9), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Sin vista previa",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                adjustViewBounds = true
                clipToOutline = true
            }
        },
        update = { imageView ->
            runCatching {
                imageView.setImageURI(Uri.parse(evidence.previewUri))
            }.onFailure {
                imageView.setImageDrawable(null)
            }
        },
    )
}

@Composable
private fun EvidencePreviewDialog(
    evidence: EvidenceItem,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF262626), RoundedCornerShape(16.dp))
                .padding(20.dp),
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                userScrollEnabled = false,
            ) {
                item {
                    EvidenceImage(
                        evidence = evidence,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.5f),
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cerrar")
                        }
                        TextButton(onClick = onDelete) {
                            Text(stringResource(R.string.evidence_remove))
                        }
                    }
                }
            }
        }
    }
}

@PhonePreview
@Composable
private fun EvidenciasScreenPreview() {
    SivemoreTheme {
        EvidenciasScreen(
            state = InspectionFlowUiState(
                vehicleId = "1",
                isLoading = false,
                lucesSection = InspectionSectionCatalog.lucesSection(),
                llantasSection = InspectionSectionCatalog.llantasSection(),
                direccionSection = InspectionSectionCatalog.direccionSection(),
                aireFrenosSection = InspectionSectionCatalog.aireFrenosSection(),
                motorEmisionesSection = InspectionSectionCatalog.motorEmisionesSection(),
                otrosSection = InspectionSectionCatalog.otrosSection(),
                session = com.sivemore.mobile.domain.model.VerificationSession(
                    id = "1",
                    orderUnitId = "1",
                    orderNumber = "ORD-1",
                    vehiclePlate = "ABC123",
                    clientCompanyName = "SIVEMOR",
                    status = com.sivemore.mobile.domain.model.VerificationSessionStatus.InProgress,
                    sections = listOf(
                        com.sivemore.mobile.domain.model.InspectionSection(
                            id = "section_1",
                            title = "Luces",
                            description = null,
                            noteValue = "",
                            items = emptyList(),
                            evidence = listOf(
                                EvidenceItem(
                                    id = "evidence_1",
                                    title = "captured-1.jpg",
                                    subtitle = "image/jpeg",
                                    addedAtLabel = "09/04/2026 10:00",
                                    accentColor = 0xFFD7EAD8,
                                ),
                            ),
                        ),
                    ),
                    comments = "Revisión final",
                    updatedAtLabel = "09/04/2026 10:00",
                    evidenceCount = 1,
                ),
                commentDraft = "Revisión final",
            ),
            onAction = {},
            onBack = {},
            onTakePhoto = {},
        )
    }
}
