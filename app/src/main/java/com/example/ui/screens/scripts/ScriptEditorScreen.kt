package com.example.ui.screens.scripts

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Script
import com.example.ui.theme.CueBlack
import com.example.ui.theme.CueBorder
import com.example.ui.theme.CueGreen
import com.example.ui.theme.CueGreenContainer
import com.example.ui.theme.CueRed
import com.example.ui.theme.CueSurface
import com.example.ui.theme.CueSurfaceElevated
import com.example.ui.theme.CueTextMuted
import com.example.ui.theme.CueTextPrimary
import com.example.ui.theme.CueTextSecondary

@Composable
fun ScriptEditorScreen(
    initialScript: Script?,
    titleDraft: String,
    contentDraft: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSave: (title: String, content: String) -> Unit,
    onCancel: () -> Unit,
    onDelete: ((String) -> Unit)? = null,
    validationError: String? = null,
    modifier: Modifier = Modifier
) {
    val isEditingExisting = initialScript != null

    // Check if there are unsaved changes compared to initial state
    val initialTitle = initialScript?.title ?: ""
    val initialContent = initialScript?.content ?: ""
    val hasUnsavedChanges = titleDraft != initialTitle || contentDraft != initialContent

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Intercept back button to protect unsaved work
    BackHandler(enabled = true) {
        if (hasUnsavedChanges) {
            showDiscardDialog = true
        } else {
            onCancel()
        }
    }

    // Dynamic word and character calculations
    val trimmedContent = contentDraft.trim()
    val wordCount = if (trimmedContent.isEmpty()) 0 else trimmedContent.split(Regex("\\s+")).filter { it.isNotBlank() }.size
    val charCount = contentDraft.length

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CueBlack)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header with Back, Title, and Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (hasUnsavedChanges) {
                            showDiscardDialog = true
                        } else {
                            onCancel()
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("editor_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Cancel and go back",
                        tint = CueGreen
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = if (isEditingExisting) "Edit Script" else "New Script",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = CueTextPrimary
                    )
                    Text(
                        text = if (isEditingExisting) "Update teleprompter draft" else "Draft video presentation",
                        style = MaterialTheme.typography.labelSmall,
                        color = CueTextSecondary
                    )
                }
            }

            if (isEditingExisting && onDelete != null) {
                IconButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.testTag("editor_delete_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete this script",
                        tint = CueRed
                    )
                }
            }
        }

        // Live Teleprompter Metric Chips (Word Count, Character Count, Estimated Pace)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricChip(
                label = "WORDS",
                value = "$wordCount",
                modifier = Modifier.weight(1f)
            )
            MetricChip(
                label = "CHARACTERS",
                value = "$charCount",
                modifier = Modifier.weight(1f)
            )
            val estDurationMinutes = if (wordCount == 0) 0 else (wordCount / 130).coerceAtLeast(0)
            val estDurationSeconds = if (wordCount == 0) 0 else ((wordCount % 130) * 60 / 130)
            MetricChip(
                label = "READ TIME",
                value = String.format("%d:%02d", estDurationMinutes, estDurationSeconds),
                modifier = Modifier.weight(1f)
            )
        }

        // Validation error display if user tried to save without title or content
        if (!validationError.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CueRed.copy(alpha = 0.15f))
                    .border(1.dp, CueRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = CueRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = validationError,
                        style = MaterialTheme.typography.bodySmall,
                        color = CueRed
                    )
                }
            }
        }

        // Title Input
        Column {
            Text(
                text = "SCRIPT TITLE",
                style = MaterialTheme.typography.labelSmall,
                color = CueGreen
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = titleDraft,
                onValueChange = onTitleChange,
                placeholder = { Text("e.g. YouTube Camera Gear Breakdown 2026", color = CueTextMuted) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CueGreen,
                    unfocusedBorderColor = CueBorder,
                    focusedContainerColor = CueSurface,
                    unfocusedContainerColor = CueSurface,
                    focusedTextColor = CueTextPrimary,
                    unfocusedTextColor = CueTextPrimary,
                    cursorColor = CueGreen
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("script_title_input")
            )
        }

        // Content Input (Large multi-line text area)
        Column {
            Text(
                text = "SCRIPT CONTENT",
                style = MaterialTheme.typography.labelSmall,
                color = CueGreen
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = contentDraft,
                onValueChange = onContentChange,
                placeholder = {
                    Text(
                        text = "Type or paste your presentation script here...\n\nBreak your thoughts into short paragraphs for optimal teleprompter cadence.",
                        color = CueTextMuted
                    )
                },
                minLines = 10,
                maxLines = 25,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CueGreen,
                    unfocusedBorderColor = CueBorder,
                    focusedContainerColor = CueSurface,
                    unfocusedContainerColor = CueSurface,
                    focusedTextColor = CueTextPrimary,
                    unfocusedTextColor = CueTextPrimary,
                    cursorColor = CueGreen
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("script_content_input")
            )
        }

        // Action Buttons: Save & Cancel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (hasUnsavedChanges) {
                        showDiscardDialog = true
                    } else {
                        onCancel()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("editor_cancel_button"),
                border = androidx.compose.foundation.BorderStroke(1.dp, CueBorder)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = CueTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.labelLarge,
                    color = CueTextSecondary
                )
            }

            Button(
                onClick = {
                    onSave(titleDraft, contentDraft)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CueGreen,
                    contentColor = CueBlack
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("editor_save_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isEditingExisting) "Save Changes" else "Save Script",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }

    // Unsaved Changes Protection Dialog (Feature 7)
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = CueGreen,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Discard Unsaved Changes?",
                    style = MaterialTheme.typography.titleLarge,
                    color = CueTextPrimary
                )
            },
            text = {
                Text(
                    text = "You have unsaved changes in your script draft. If you leave now, these modifications will be discarded.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CueTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDiscardDialog = false
                        onCancel()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CueRed,
                        contentColor = CueTextPrimary
                    ),
                    modifier = Modifier.testTag("confirm_discard_button")
                ) {
                    Text("Discard Changes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDiscardDialog = false },
                    modifier = Modifier.testTag("keep_editing_button")
                ) {
                    Text("Keep Editing", color = CueGreen)
                }
            },
            containerColor = CueSurface,
            modifier = Modifier.testTag("unsaved_changes_dialog")
        )
    }

    // Delete confirmation dialog when initiated from editor
    if (showDeleteConfirmDialog && initialScript != null && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = CueRed,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Delete Script?",
                    style = MaterialTheme.typography.titleLarge,
                    color = CueTextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${initialScript.title}\"? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CueTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete(initialScript.id)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CueRed,
                        contentColor = CueTextPrimary
                    ),
                    modifier = Modifier.testTag("confirm_delete_from_editor")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = CueTextSecondary)
                }
            },
            containerColor = CueSurface
        )
    }
}

@Composable
fun MetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CueSurface)
            .border(1.dp, CueBorder, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CueGreen
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = CueTextMuted
            )
        }
    }
}
