package com.sivemore.mobile.feature.verification

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sivemore.mobile.R
import com.sivemore.mobile.app.designsystem.BottomActionBar
import com.sivemore.mobile.app.designsystem.BrandedHeader
import com.sivemore.mobile.app.designsystem.BrandedLoadingScreen
import com.sivemore.mobile.app.designsystem.ConfirmationDialog
import com.sivemore.mobile.app.designsystem.EvidenceTile
import com.sivemore.mobile.app.designsystem.InspectionChoiceRow
import com.sivemore.mobile.app.designsystem.SivemoreTheme
import com.sivemore.mobile.app.designsystem.VerificationCard
import com.sivemore.mobile.domain.model.EvidenceUpload
import com.sivemore.mobile.domain.model.VerificationSession
import com.sivemore.mobile.preview.PhonePreview
import java.io.File
import kotlinx.coroutines.flow.collectLatest

@Composable
fun VerificationRoute(
    onOpenSessionActions: (String) -> Unit,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VerificationViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    var pendingSectionId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        val targetSection = pendingSectionId
        if (uri != null && targetSection != null) {
            viewModel.onAction(
                VerificationUiAction.EvidencePicked(
                    sectionId = targetSection,
                    upload = EvidenceUpload(
                        uri = uri.toString(),
                        fileName = resolveFileName(context, uri),
                        mimeType = context.contentResolver.getType(uri),
                    ),
                )
            )
        } else {
            viewModel.onAction(VerificationUiAction.EvidenceDialogDismissed)
        }
        pendingSectionId = null
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        val targetSection = pendingSectionId
        val captureUri = pendingCaptureUri
        if (success && targetSection != null && captureUri != null) {
            viewModel.onAction(
                VerificationUiAction.EvidencePicked(
                    sectionId = targetSection,
                    upload = EvidenceUpload(
                        uri = captureUri.toString(),
                        fileName = "captured-${System.currentTimeMillis()}.jpg",
                        mimeType = "image/jpeg",
                    ),
                )
            )
        } else {
            viewModel.onAction(VerificationUiAction.EvidenceDialogDismissed)
        }
        pendingSectionId = null
        pendingCaptureUri = null
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                VerificationEvent.Completed -> onCompleted()
                is VerificationEvent.OpenSessionActions -> onOpenSessionActions(event.orderUnitId)
            }
        }
    }

    VerificationScreen(
        state = state,
        modifier = modifier,
        onAction = viewModel::onAction,
        onPickFromGallery = { sectionId ->
            pendingSectionId = sectionId
            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onCaptureWithCamera = { sectionId ->
            pendingSectionId = sectionId
            val captureUri = createCaptureUri(context)
            pendingCaptureUri = captureUri
            cameraLauncher.launch(captureUri)
        },
    )
}

@Composable
fun VerificationScreen(
    state: VerificationUiState,
    onAction: (VerificationUiAction) -> Unit,
    onPickFromGallery: (String) -> Unit,
    onCaptureWithCamera: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val session = state.session
    if (state.isLoading || session == null) {
        BrandedLoadingScreen(modifier = modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("verification_screen"),
    ) {
        BrandedHeader(showAction = true, onActionClick = {
            onAction(VerificationUiAction.SessionActionsRequested)
        })
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = session.vehiclePlate,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "${session.orderNumber} · ${session.clientCompanyName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = state.errorMessage ?: "Evidencias: ${session.evidenceCount} · Actualizado ${session.updatedAtLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = if (state.errorMessage == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(session.sections, key = { it.id }) { section ->
                VerificationCard {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (!section.description.isNullOrBlank()) {
                        Text(
                            text = section.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    OutlinedTextField(
                        value = section.noteValue,
                        onValueChange = {
                            onAction(VerificationUiAction.SectionNoteChanged(section.id, it))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nota de sección") },
                    )

                    section.items.forEach { item ->
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = item.title + if (item.required) " *" else "",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            item.options.forEach { option ->
                                InspectionChoiceRow(
                                    text = option.label,
                                    selected = option.id == item.selectedOptionId,
                                    onClick = {
                                        onAction(
                                            VerificationUiAction.QuestionOptionSelected(
                                                sectionId = section.id,
                                                itemId = item.id,
                                                optionId = option.id,
                                            )
                                        )
                                    },
                                    modifier = Modifier.testTag("item_${item.id}_option_${option.id}"),
                                )
                            }
                            OutlinedTextField(
                                value = item.noteValue,
                                onValueChange = {
                                    onAction(
                                        VerificationUiAction.QuestionCommentChanged(
                                            sectionId = section.id,
                                            itemId = item.id,
                                            value = it,
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Comentario") },
                            )
                        }
                    }

                    if (section.evidence.isEmpty()) {
                        Text(
                            text = "Aún no hay evidencia registrada para esta sección.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        section.evidence.forEach { evidence ->
                            EvidenceTile(
                                evidence = evidence,
                                onRemove = {
                                    onAction(VerificationUiAction.RemoveEvidence(evidence.id))
                                },
                            )
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
        var selectedSectionId by rememberSaveable(session.id) {
            mutableStateOf(session.sections.firstOrNull()?.id.orEmpty())
        }

        AlertDialog(
            onDismissRequest = { onAction(VerificationUiAction.EvidenceDialogDismissed) },
            title = { Text(stringResource(R.string.evidence_dialog_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Selecciona la sección destino")
                    session.sections.forEach { section ->
                        TextButton(onClick = { selectedSectionId = section.id }) {
                            Text(
                                text = if (selectedSectionId == section.id) "• ${section.title}" else section.title,
                            )
                        }
                    }
                    TextButton(
                        onClick = { onCaptureWithCamera(selectedSectionId) },
                        enabled = selectedSectionId.isNotBlank(),
                        modifier = Modifier.testTag("source_camera"),
                    ) {
                        Text(stringResource(R.string.evidence_dialog_camera))
                    }
                    TextButton(
                        onClick = { onPickFromGallery(selectedSectionId) },
                        enabled = selectedSectionId.isNotBlank(),
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
            title = { Text("Comentarios generales") },
            text = {
                OutlinedTextField(
                    value = state.commentDraft,
                    onValueChange = { onAction(VerificationUiAction.CommentDraftChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Observaciones") },
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
            title = "Finalizar inspección",
            text = "Se enviará el borrador actual al backend y desaparecerá de tus asignaciones activas.",
            confirmLabel = "Finalizar",
            onConfirm = { onAction(VerificationUiAction.SubmitConfirmed) },
            onDismiss = { onAction(VerificationUiAction.SubmitDismissed) },
        )
    }
}

private fun createCaptureUri(context: Context): Uri {
    val imageDir = File(context.cacheDir, "images").apply { mkdirs() }
    val imageFile = File.createTempFile("capture_", ".jpg", imageDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile,
    )
}

private fun resolveFileName(context: Context, uri: Uri): String? {
    val projection = arrayOf(android.provider.OpenableColumns.DISPLAY_NAME)
    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) {
            return cursor.getString(index)
        }
    }
    return null
}

@PhonePreview
@Composable
private fun VerificationScreenPreview() {
    SivemoreTheme {
        VerificationScreen(
            state = VerificationUiState(
                isLoading = false,
                session = VerificationSession(
                    id = "1",
                    orderUnitId = "1",
                    orderNumber = "ORD-2026-001",
                    vehiclePlate = "MOR-123-A",
                    clientCompanyName = "Transportes Morelos",
                    status = com.sivemore.mobile.domain.model.VerificationSessionStatus.InProgress,
                    sections = listOf(
                        com.sivemore.mobile.domain.model.InspectionSection(
                            id = "10",
                            title = "Luces",
                            description = "Validación visual de luces.",
                            noteValue = "",
                            items = listOf(
                                com.sivemore.mobile.domain.model.InspectionItem(
                                    id = "100",
                                    title = "Luces frontales",
                                    required = true,
                                    options = listOf(
                                        com.sivemore.mobile.domain.model.InspectionOption("PASS", "Cumple"),
                                        com.sivemore.mobile.domain.model.InspectionOption("FAIL", "No cumple"),
                                        com.sivemore.mobile.domain.model.InspectionOption("NA", "No aplica"),
                                    ),
                                    selectedOptionId = "PASS",
                                    noteValue = "Sin observaciones",
                                )
                            ),
                            evidence = emptyList(),
                        )
                    ),
                    comments = "",
                    updatedAtLabel = "23/03/2026 12:00",
                    evidenceCount = 0,
                ),
            ),
            onAction = {},
            onPickFromGallery = {},
            onCaptureWithCamera = {},
        )
    }
}
