package com.sivemore.mobile.feature.inspection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sivemore.mobile.R
import com.sivemore.mobile.app.designsystem.ActionIconButton
import com.sivemore.mobile.app.designsystem.BrandGreenMuted

@Composable
fun InspectionGlobalActionsPanel(
    onAddComment: () -> Unit,
    onTakePhoto: () -> Unit,
    onPause: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(BrandGreenMuted)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
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
                onClick = onAddComment,
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
fun InspectionPauseDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.bottom_action_pause)) },
        text = { Text(stringResource(R.string.verification_pause_confirmation)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.verification_pause_exit))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.verification_pause_cancel))
            }
        },
    )
}

@Composable
fun InspectionCommentDialog(
    commentValue: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.inspection_comment_dialog_title)) },
        text = {
            OutlinedTextField(
                value = commentValue,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                label = { Text(stringResource(R.string.verification_comments_label)) },
                placeholder = { Text(stringResource(R.string.verification_comments_hint)) },
            )
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = !isSaving) {
                Text(stringResource(R.string.inspection_comment_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(stringResource(R.string.inspection_comment_cancel))
            }
        },
    )
}
