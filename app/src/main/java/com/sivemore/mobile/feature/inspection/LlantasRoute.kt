package com.sivemore.mobile.feature.inspection

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sivemore.mobile.app.designsystem.BrandedHeader
import com.sivemore.mobile.app.designsystem.BrandedLoadingScreen
import com.sivemore.mobile.app.designsystem.Ink
import com.sivemore.mobile.app.designsystem.SivemoreTheme
import com.sivemore.mobile.app.designsystem.VerificationCard
import com.sivemore.mobile.domain.model.EvidenceUpload
import com.sivemore.mobile.preview.PhonePreview
import java.io.File
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LlantasRoute(
    viewModel: InspectionFlowViewModel,
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
    onBackToLookup: () -> Unit,
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
        viewModel.onAction(InspectionFlowAction.EnterSection(1))
        viewModel.events.collectLatest { event ->
            when (event) {
                InspectionFlowEvent.NavigateToNextSection -> onNavigateNext()
                InspectionFlowEvent.BackToLookup -> onBackToLookup()
                InspectionFlowEvent.Completed -> onBackToLookup()
                InspectionFlowEvent.SignedOut -> onSignedOut()
            }
        }
    }

    LlantasScreen(
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
fun LlantasScreen(
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
            .testTag("llantas_screen"),
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
        LlantasContent(
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
private fun LlantasContent(
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
                text = state.llantasSection.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        item {
            Text(
                text = state.errorMessage ?: state.llantasSection.description,
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
        state.llantasSection.groups.forEach { group ->
            item {
                Text(
                    text = group.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (group.illustrationType != null) {
                item {
                    VerificationCard(
                        modifier = Modifier.testTag("llantas_illustration_${group.id}"),
                    ) {
                        InspectionIllustration(
                            type = group.illustrationType,
                            birlosState = group.birlosVisualState,
                            onBirloToggled = { index, checked ->
                                onAction(InspectionFlowAction.BirloToggled(group.id, index, checked))
                            },
                        )
                    }
                }
            }
            items(group.questions, key = { it.id }) { question ->
                LlantasInspectionCard(
                    question = question,
                    onOptionSelected = { optionId ->
                        onAction(InspectionFlowAction.LlantasOptionSelected(question.id, optionId))
                    },
                    onNumericValueChanged = { value ->
                        onAction(InspectionFlowAction.LlantasNumericValueChanged(question.id, value))
                    },
                )
            }
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
                        .testTag("llantas_back_button"),
                ) {
                    Text("Atras")
                }
                Button(
                    onClick = { onAction(InspectionFlowAction.NextClicked) },
                    enabled = state.isCurrentSectionComplete,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("llantas_next_button"),
                ) {
                    Text("Siguiente")
                }
            }
        }
    }
}

@Composable
private fun LlantasInspectionCard(
    question: InspectionQuestionItem,
    onOptionSelected: (String) -> Unit,
    onNumericValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    VerificationCard(modifier = modifier.testTag("llantas_card_${question.id}")) {
        Text(
            text = question.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        when (question.kind) {
            InspectionQuestionKind.SingleChoice -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    question.options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOptionSelected(option.id) }
                                .padding(vertical = 2.dp)
                                .testTag("llantas_option_${question.id}_${option.id}"),
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
                        .testTag("llantas_numeric_${question.id}"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text(question.placeholder) },
                )
            }
        }
    }
}

@Composable
private fun InspectionIllustration(
    type: InspectionIllustrationType,
    birlosState: BirlosVisualState? = null,
    onBirloToggled: (Int, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    when (type) {
        InspectionIllustrationType.Birlos -> BirlosIllustration(
            modifier = modifier,
            birlosState = birlosState,
            onBirloToggled = onBirloToggled,
        )
        InspectionIllustrationType.Tuercas -> TuercasIllustration(modifier = modifier)
    }
}

@Composable
private fun BirlosIllustration(
    birlosState: BirlosVisualState?,
    onBirloToggled: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(180.dp)) {
                drawCircle(color = Color(0xFF1E1E1E))
                drawCircle(color = Color(0xFF5E5E5E), radius = size.minDimension * 0.35f)
                drawCircle(color = Color(0xFFD8D8D8), radius = size.minDimension * 0.14f)
                repeat(6) { index ->
                    val angle = (index * 60f) * (Math.PI / 180f)
                    val x = center.x + kotlin.math.cos(angle).toFloat() * size.minDimension * 0.22f
                    val y = center.y + kotlin.math.sin(angle).toFloat() * size.minDimension * 0.22f
                    drawCircle(color = Color.White, radius = size.minDimension * 0.03f, center = Offset(x, y))
                }
            }
        }
        if (birlosState != null) {
            Text(
                text = "Verificacion de birlos",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(birlosState.count) { index ->
                    BirloIndicatorRow(
                        index = index,
                        checked = birlosState.birlosState.getOrElse(index) { false },
                        evaluated = birlosState.evaluated.getOrElse(index) { false },
                        onCheckedChange = { checked -> onBirloToggled(index, checked) },
                    )
                }
            }
        }
    }
}

@Composable
private fun BirloIndicatorRow(
    index: Int,
    checked: Boolean,
    evaluated: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
            )
            .padding(vertical = 2.dp)
            .testTag("birlo_indicator_${index + 1}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .width(22.dp)
                .height(6.dp)
                .background(
                    color = when {
                        !evaluated -> Color(0xFFBDBDBD)
                        checked -> Color(0xFF1FA463)
                        else -> Color(0xFFD14B4B)
                    },
                ),
        )
        Text(
            text = "Birlo ${index + 1}",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun TuercasIllustration(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
    ) {
        Canvas(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 180.dp, height = 200.dp),
        ) {
            drawRect(
                color = Color(0xFFB0BEC5),
                topLeft = Offset(size.width * 0.28f, size.height * 0.15f),
                size = Size(size.width * 0.44f, size.height * 0.62f),
            )
            drawRect(
                color = Color(0xFF0288D1),
                topLeft = Offset(size.width * 0.33f, size.height * 0.02f),
                size = Size(size.width * 0.34f, size.height * 0.22f),
            )
            drawRect(
                color = Color(0xFF263238),
                topLeft = Offset(size.width * 0.34f, size.height * 0.16f),
                size = Size(size.width * 0.32f, size.height * 0.12f),
                style = Stroke(width = 3f),
            )
        }
        Text(
            text = "Lado izquierdo",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Ink,
        )
        Text(
            text = "Lado derecho",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Ink,
        )
    }
}


@PhonePreview
@Composable
private fun LlantasScreenPreview() {
    SivemoreTheme {
        LlantasScreen(
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
