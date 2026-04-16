package com.sivemore.mobile.feature.verification

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sivemore.mobile.R
import com.sivemore.mobile.app.designsystem.ActionIconButton
import com.sivemore.mobile.app.designsystem.BrandedHeader
import com.sivemore.mobile.app.designsystem.BrandedLoadingScreen
import com.sivemore.mobile.app.designsystem.BrandGreenMuted
import com.sivemore.mobile.app.designsystem.InspectionChoiceRow
import com.sivemore.mobile.app.designsystem.SivemoreTheme
import com.sivemore.mobile.app.designsystem.VerificationCard
import com.sivemore.mobile.domain.model.EvidenceItem
import com.sivemore.mobile.domain.model.EvidenceUpload
import com.sivemore.mobile.domain.model.InspectionSection
import com.sivemore.mobile.domain.model.VerificationSession
import com.sivemore.mobile.feature.inspection.createPersistentCaptureUri
import com.sivemore.mobile.preview.PhonePreview
import kotlinx.coroutines.flow.collectLatest

@Composable
fun VerificationRoute(
    onBackToLookup: () -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VerificationViewModel = hiltViewModel(),
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
                VerificationUiAction.EvidencePicked(
                    upload = EvidenceUpload(
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
        viewModel.events.collectLatest { event ->
            when (event) {
                VerificationEvent.BackToLookup -> onBackToLookup()
                VerificationEvent.Completed -> onBackToLookup()
                VerificationEvent.SignedOut -> onSignedOut()
            }
        }
    }

    VerificationScreen(
        state = state,
        modifier = modifier,
        onAction = viewModel::onAction,
        onTakePhoto = {
            if (state.canAddMorePhotos && state.currentSection != null) {
                val captureUri = createPersistentCaptureUri(context)
                pendingCaptureUri = captureUri
                cameraLauncher.launch(captureUri)
            } else {
                viewModel.onAction(VerificationUiAction.PhotoLimitReached)
            }
        },
    )
}

@Composable
fun VerificationScreen(
    state: VerificationUiState,
    onAction: (VerificationUiAction) -> Unit,
    onTakePhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val session = state.session
    val currentSection = state.currentSection
    if (state.isLoading && session == null) {
        BrandedLoadingScreen(modifier = modifier)
        return
    }
    if (session == null || currentSection == null) {
        BrandedLoadingScreen(modifier = modifier)
        return
    }

    val commentFocusRequester = remember { FocusRequester() }

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onAction(VerificationUiAction.Refresh) },
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("verification_screen"),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            BrandedHeader(
                modifier = Modifier.systemBarsPadding(),
                showAction = true,
                onActionClick = { onAction(VerificationUiAction.LogoutRequested) },
            )
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
                    text = stringResource(R.string.verification_order_company, session.orderNumber, session.clientCompanyName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(
                        R.string.verification_section_progress,
                        state.currentSectionIndex + 1,
                        session.sections.size,
                        currentSection.title,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = state.errorMessage ?: stringResource(R.string.verification_summary, session.evidenceCount, session.updatedAtLabel),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.errorMessage == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item {
                        SectionContent(
                            section = currentSection,
                            onAction = onAction,
                        )
                    }
                    if (state.currentSectionIndex < session.sections.lastIndex) {
                        item {
                            Button(
                                onClick = { onAction(VerificationUiAction.NextSectionRequested) },
                                enabled = state.canGoNext,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("next_section_button"),
                            ) {
                                Text(stringResource(R.string.verification_next_section))
                            }
                        }
                    }
                }
            }

            VerificationGlobalActionsPanel(
                commentValue = state.commentDraft,
                onCommentValueChange = { onAction(VerificationUiAction.CommentDraftChanged(it)) },
                onFocusComment = { commentFocusRequester.requestFocus() },
                onTakePhoto = onTakePhoto,
                onPause = { onAction(VerificationUiAction.PauseRequested) },
                onFinish = { onAction(VerificationUiAction.SubmitRequested) },
                evidence = currentSection.evidence,
                isSavingComment = state.isSavingComment,
                onRemoveEvidence = { onAction(VerificationUiAction.RemoveEvidence(it)) },
                commentModifier = Modifier.focusRequester(commentFocusRequester),
            )
        }
    }

    if (state.showPauseDialog) {
        AlertDialog(
            onDismissRequest = { onAction(VerificationUiAction.PauseDismissed) },
            title = { Text(stringResource(R.string.session_actions_pause)) },
            text = { Text(stringResource(R.string.verification_pause_confirmation)) },
            confirmButton = {
                TextButton(onClick = { onAction(VerificationUiAction.PauseConfirmed) }) {
                    Text(stringResource(R.string.verification_pause_exit))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(VerificationUiAction.PauseDismissed) }) {
                    Text(stringResource(R.string.verification_pause_cancel))
                }
            },
        )
    }
}

@Composable
private fun SectionContent(
    section: InspectionSection,
    onAction: (VerificationUiAction) -> Unit,
) {
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
            onValueChange = { onAction(VerificationUiAction.SectionNoteChanged(section.id, it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.verification_section_note)) },
        )
        section.items.forEach { item ->
            VerificationCard {
                Text(
                    text = item.title + if (item.required) " *" else "",
                    style = MaterialTheme.typography.titleMedium,
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
                                ),
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
                            ),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.verification_comment)) },
                )
            }
        }
    }
}

@Composable
private fun VerificationGlobalActionsPanel(
    commentValue: String,
    onCommentValueChange: (String) -> Unit,
    onFocusComment: () -> Unit,
    onTakePhoto: () -> Unit,
    onPause: () -> Unit,
    onFinish: () -> Unit,
    evidence: List<EvidenceItem>,
    isSavingComment: Boolean,
    onRemoveEvidence: (String) -> Unit,
    commentModifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandGreenMuted)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = commentValue,
            onValueChange = onCommentValueChange,
            modifier = commentModifier
                .fillMaxWidth()
                .testTag("verification_comment_input"),
            enabled = !isSavingComment,
            label = { Text(stringResource(R.string.verification_comments_label)) },
            placeholder = { Text(stringResource(R.string.verification_comments_hint)) },
        )

        if (evidence.isEmpty()) {
            Text(
                text = stringResource(R.string.verification_photos_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.surface,
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(evidence, key = { it.id }) { item ->
                    CompactEvidenceItem(
                        evidence = item,
                        onRemove = { onRemoveEvidence(item.id) },
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ActionIconButton(
                iconRes = R.drawable.ic_bottom_evidence,
                label = stringResource(R.string.bottom_action_evidence),
                onClick = onTakePhoto,
                testTag = "action_take_photo",
            )
            ActionIconButton(
                iconRes = R.drawable.ic_bottom_comment,
                label = stringResource(R.string.bottom_action_comment),
                onClick = onFocusComment,
                testTag = "action_focus_comment",
            )
            ActionIconButton(
                iconRes = R.drawable.ic_bottom_pause,
                label = stringResource(R.string.bottom_action_pause),
                onClick = onPause,
                testTag = "action_pause_verification",
            )
            ActionIconButton(
                iconRes = R.drawable.ic_bottom_submit,
                label = stringResource(R.string.bottom_action_submit),
                onClick = onFinish,
                testTag = "action_submit_verification",
            )
        }
    }
}

@Composable
private fun CompactEvidenceItem(
    evidence: EvidenceItem,
    onRemove: () -> Unit,
) {
    VerificationCard(
        modifier = Modifier
            .height(96.dp)
            .testTag("evidence_${evidence.id}"),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
        )
        Text(
            text = evidence.title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        TextButton(
            onClick = onRemove,
        ) {
            Text(stringResource(R.string.evidence_remove))
        }
    }
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
                        InspectionSection(
                            id = "10",
                            title = "Luces",
                            description = "Validacion visual de luces.",
                            noteValue = "",
                            items = listOf(
                                com.sivemore.mobile.domain.model.InspectionItem(
                                    id = "100",
                                    title = "Luces bajas",
                                    required = true,
                                    options = listOf(
                                        com.sivemore.mobile.domain.model.InspectionOption("PASS", "Aprobadas"),
                                        com.sivemore.mobile.domain.model.InspectionOption("LEFT", "Izquierda fundida"),
                                    ),
                                    selectedOptionId = null,
                                    noteValue = "",
                                ),
                            ),
                            evidence = emptyList(),
                        ),
                    ),
                    comments = "",
                    updatedAtLabel = "23/03/2026 12:00",
                    evidenceCount = 0,
                ),
                commentDraft = "",
            ),
            onAction = {},
            onTakePhoto = {},
        )
    }
}
