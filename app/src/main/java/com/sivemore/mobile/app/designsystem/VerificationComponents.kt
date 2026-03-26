package com.sivemore.mobile.app.designsystem

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sivemore.mobile.R
import com.sivemore.mobile.domain.model.EvidenceItem
import com.sivemore.mobile.domain.model.InspectionCategory
import com.sivemore.mobile.domain.model.VehicleStatus

@Composable
fun LoginFooterDecoration(
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val dark = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width * 0.82f, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path = dark, color = BrandGreenMuted)

        val mint = Path().apply {
            moveTo(size.width * 0.58f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width * 0.74f, size.height)
            lineTo(size.width * 0.14f, size.height)
            close()
        }
        drawPath(path = mint, color = BrandMint)
    }
}

@Composable
fun BrandedLoadingScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Surface),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = SivemoreThemeTokens.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SivemoreThemeTokens.spacing.lg),
        ) {
            Image(
                painter = painterResource(R.drawable.figma_logo_login),
                contentDescription = null,
                modifier = Modifier
                    .width(170.dp)
                    .height(118.dp),
            )
            Text(
                text = stringResource(R.string.brand_name),
                style = MaterialTheme.typography.headlineLarge,
                color = Ink,
            )
            Text(
                text = stringResource(R.string.brand_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = Ink,
                textAlign = TextAlign.Center,
            )
            CircularProgressIndicator(color = BrandGreen)
        }

        LoginFooterDecoration(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(136.dp),
        )
    }
}

@Composable
fun BrandedHeader(
    modifier: Modifier = Modifier,
    showAction: Boolean = false,
    onActionClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(114.dp)
            .background(BrandGreenMuted),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 17.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.figma_logo_header),
                    contentDescription = null,
                    modifier = Modifier
                        .width(70.dp)
                        .height(48.dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = stringResource(R.string.brand_name),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Snow,
                    )
                    Text(
                        text = stringResource(R.string.brand_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = Snow,
                    )
                }
            }

            if (showAction && onActionClick != null) {
                HeaderActionButton(onClick = onActionClick)
            }
        }
    }
}

@Composable
fun HeaderActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width = 40.dp, height = 38.dp)
            .background(color = BrandGreenDeep, shape = RoundedCornerShape(5.dp))
            .clickable(onClick = onClick)
            .testTag("header_action"),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_header_action),
            contentDescription = null,
            tint = Snow,
        )
    }
}

@Composable
fun VerificationCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(3.dp),
                spotColor = Color(0x33000000),
            ),
        color = Surface,
        shape = RoundedCornerShape(3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
fun VehicleStatusChip(
    status: VehicleStatus,
    modifier: Modifier = Modifier,
) {
    val color = when (status) {
        VehicleStatus.Assigned -> Approved
        VehicleStatus.InProgress -> Rejected
        VehicleStatus.Paused -> Rejected
    }
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
    }
}

@Composable
fun CategoryStrip(
    categories: List<InspectionCategory>,
    selectedCategory: InspectionCategory,
    onSelected: (InspectionCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        items(categories, key = { it.name }) { category ->
            val selected = category == selectedCategory
            Box(
                modifier = Modifier
                    .background(
                        color = if (selected) BrandGreen else Color(0xFFD9D9D9),
                        shape = RoundedCornerShape(if (selected) 2.dp else 0.dp),
                    )
                    .clickable { onSelected(category) }
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("category_${category.name}"),
            ) {
                Text(
                    text = category.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) Snow else Ink,
                )
            }
        }
    }
}

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.then(
            if (testTag != null) {
                Modifier.testTag(testTag)
            } else {
                Modifier
            },
        ),
        singleLine = true,
        shape = RoundedCornerShape(6.dp),
        label = {
            Text(
                text = stringResource(R.string.search_label),
                style = MaterialTheme.typography.bodySmall,
            )
        },
        placeholder = {
            Text(
                text = stringResource(R.string.search_placeholder),
                style = MaterialTheme.typography.bodySmall,
            )
        },
    )
}

@Composable
fun VehicleResultCard(
    plates: String,
    serialNumber: String,
    vehicleNumber: String,
    admissionDate: String,
    completedDate: String?,
    status: VehicleStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VerificationCard(
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.vehicle_card_plate, plates),
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                )
                Text(
                    text = stringResource(R.string.vehicle_card_order_number, serialNumber),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedText,
                )
                Text(
                    text = stringResource(R.string.vehicle_card_unit_number, vehicleNumber),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedText,
                )
            }
            VehicleStatusChip(status = status)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.vehicle_card_admission_date),
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedText,
                )
                Text(
                    text = admissionDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.vehicle_card_completed_date),
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedText,
                )
                Text(
                    text = completedDate ?: stringResource(R.string.vehicle_card_pending),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                )
            }
        }
    }
}

@Composable
fun InspectionChoiceRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .border(1.dp, color = Color(0xFFAFafAF), shape = RoundedCornerShape(2.dp))
                .background(if (selected) BrandGreen else Snow, RoundedCornerShape(2.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.labelMedium,
                    color = Snow,
                )
            }
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = Ink,
        )
    }
}

@Composable
fun ActionIconButton(
    @DrawableRes iconRes: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(43.dp)
                .background(BrandGreenDeep, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = Snow,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Snow,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun BottomActionBar(
    onAddEvidence: () -> Unit,
    onAddComment: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(BrandGreenMuted)
            .padding(horizontal = 28.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ActionIconButton(
                iconRes = R.drawable.ic_bottom_evidence,
                label = stringResource(R.string.bottom_action_evidence),
                onClick = onAddEvidence,
                testTag = "action_add_evidence",
            )
            ActionIconButton(
                iconRes = R.drawable.ic_bottom_comment,
                label = stringResource(R.string.bottom_action_comment),
                onClick = onAddComment,
                testTag = "action_add_comment",
            )
            ActionIconButton(
                iconRes = R.drawable.ic_bottom_submit,
                label = stringResource(R.string.bottom_action_submit),
                onClick = onSubmit,
                testTag = "action_submit_verification",
            )
        }
    }
}

@Composable
fun EvidenceTile(
    evidence: EvidenceItem,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VerificationCard(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .background(Color(evidence.accentColor), RoundedCornerShape(8.dp)),
        )
        Text(
            text = evidence.title,
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
        )
        Text(
            text = evidence.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MutedText,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = evidence.addedAtLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MutedText,
            )
            TextButton(onClick = onRemove) {
                Text(stringResource(R.string.evidence_remove))
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    text: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        containerColor = Surface,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )
        },
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = Ink,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = BrandGreenDeep)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel), color = MutedText)
            }
        },
    )
}
