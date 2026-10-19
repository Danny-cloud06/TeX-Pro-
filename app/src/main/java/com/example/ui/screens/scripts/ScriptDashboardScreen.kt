package com.example.ui.screens.scripts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
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
import com.example.ui.components.MilestoneBadge
import com.example.ui.theme.CueBlack
import com.example.ui.theme.CueBorder
import com.example.ui.theme.CueCharcoal
import com.example.ui.theme.CueGreen
import com.example.ui.theme.CueGreenContainer
import com.example.ui.theme.CueRed
import com.example.ui.theme.CueSurface
import com.example.ui.theme.CueSurfaceElevated
import com.example.ui.theme.CueTextMuted
import com.example.ui.theme.CueTextPrimary
import com.example.ui.theme.CueTextSecondary

@Composable
fun ScriptDashboardScreen(
    scripts: List<Script>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNewScript: () -> Unit,
    onEditScript: (Script) -> Unit,
    onDeleteScript: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var scriptToDelete by remember { mutableStateOf<Script?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "My Scripts",
                            style = MaterialTheme.typography.headlineLarge,
                            color = CueTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Create, organize, and edit your teleprompter scripts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CueTextSecondary
                        )
                    }
                }
            }

            // Top action bar with "New Script" primary button and count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onNewScript,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CueGreen,
                            contentColor = CueBlack
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("dashboard_new_script_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "New Script",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Text(
                        text = "${scripts.size} ${if (scripts.size == 1) "script" else "scripts"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = CueGreen
                    )
                }
            }

            // Search input field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = "Search scripts by title or content...",
                            color = CueTextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = CueTextSecondary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.testTag("clear_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = CueTextSecondary
                                )
                            }
                        }
                    },
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
                        .height(56.dp)
                        .testTag("script_search_field")
                )
            }

            // Scripts list or empty states
            if (scripts.isEmpty()) {
                item {
                    EmptyScriptsState(
                        isSearchActive = searchQuery.isNotBlank(),
                        searchQuery = searchQuery,
                        onClearSearch = { onSearchQueryChange("") },
                        onNewScript = onNewScript
                    )
                }
            } else {
                items(scripts, key = { it.id }) { script ->
                    ScriptDashboardCard(
                        script = script,
                        onEdit = { onEditScript(script) },
                        onDelete = { scriptToDelete = script }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(76.dp))
            }
        }

        // Floating Action Button for convenient thumb reach
        FloatingActionButton(
            onClick = onNewScript,
            containerColor = CueGreen,
            contentColor = CueBlack,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(56.dp)
                .testTag("fab_new_script_button")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Script",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Confirmation dialog before deleting script (Feature 5)
    scriptToDelete?.let { script ->
        AlertDialog(
            onDismissRequest = { scriptToDelete = null },
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
                    text = "Are you sure you want to delete \"${script.title}\"? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CueTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteScript(script.id)
                        scriptToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CueRed,
                        contentColor = CueTextPrimary
                    ),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { scriptToDelete = null },
                    modifier = Modifier.testTag("cancel_delete_button")
                ) {
                    Text("Cancel", color = CueTextSecondary)
                }
            },
            containerColor = CueSurface,
            modifier = Modifier.testTag("delete_script_dialog")
        )
    }
}

@Composable
fun ScriptDashboardCard(
    script: Script,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CueSurface)
            .border(1.dp, CueBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onEdit)
            .padding(16.dp)
            .testTag("script_card_${script.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = script.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = CueTextPrimary,
                modifier = Modifier.weight(1f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("edit_script_${script.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit script",
                        tint = CueGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_script_${script.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete script",
                        tint = CueTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = script.previewSnippet.ifEmpty { "Empty script content." },
            style = MaterialTheme.typography.bodyMedium,
            color = CueTextSecondary,
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TextFields,
                    contentDescription = null,
                    tint = CueGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${script.wordCount} words",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = CueGreen
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = CueTextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = script.formattedModifiedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = CueTextMuted
                )
            }
        }
    }
}

@Composable
fun EmptyScriptsState(
    isSearchActive: Boolean,
    searchQuery: String,
    onClearSearch: () -> Unit,
    onNewScript: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CueSurface)
            .border(1.dp, CueBorder, RoundedCornerShape(12.dp))
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSearchActive) Icons.Default.Search else Icons.Default.Description,
            contentDescription = null,
            tint = CueTextMuted,
            modifier = Modifier.size(44.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isSearchActive) "No Matching Scripts" else "No Scripts Yet",
            style = MaterialTheme.typography.titleMedium,
            color = CueTextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isSearchActive) {
                "No scripts match \"$searchQuery\". Try checking for typos or searching a different keyword."
            } else {
                "Your script library is empty. Tap \"New Script\" to start writing your first teleprompter script."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = CueTextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isSearchActive) {
            OutlinedButton(
                onClick = onClearSearch,
                modifier = Modifier.height(48.dp)
            ) {
                Text("Clear Search Filter", color = CueGreen)
            }
        } else {
            Button(
                onClick = onNewScript,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CueGreen,
                    contentColor = CueBlack
                ),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create First Script", fontWeight = FontWeight.Bold)
            }
        }
    }
}
