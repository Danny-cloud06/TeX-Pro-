package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.model.Script
import com.example.ui.screens.scripts.ScriptDashboardScreen
import com.example.ui.screens.scripts.ScriptEditorScreen

enum class ScriptsViewMode {
    DASHBOARD,
    EDITOR
}

@Composable
fun ScriptsScreen(
    scripts: List<Script>,
    searchQuery: String,
    viewMode: ScriptsViewMode,
    editingScript: Script?,
    titleDraft: String,
    contentDraft: String,
    validationError: String?,
    onSearchQueryChange: (String) -> Unit,
    onStartNewScript: () -> Unit,
    onOpenEditScript: (Script) -> Unit,
    onTitleDraftChange: (String) -> Unit,
    onContentDraftChange: (String) -> Unit,
    onSaveScript: (title: String, content: String) -> Unit,
    onCancelEditor: () -> Unit,
    onDeleteScript: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Crossfade(targetState = viewMode, label = "ScriptsViewModeCrossfade") { mode ->
            when (mode) {
                ScriptsViewMode.DASHBOARD -> {
                    ScriptDashboardScreen(
                        scripts = scripts,
                        searchQuery = searchQuery,
                        onSearchQueryChange = onSearchQueryChange,
                        onNewScript = onStartNewScript,
                        onEditScript = onOpenEditScript,
                        onDeleteScript = onDeleteScript
                    )
                }

                ScriptsViewMode.EDITOR -> {
                    ScriptEditorScreen(
                        initialScript = editingScript,
                        titleDraft = titleDraft,
                        contentDraft = contentDraft,
                        onTitleChange = onTitleDraftChange,
                        onContentChange = onContentDraftChange,
                        onSave = onSaveScript,
                        onCancel = onCancelEditor,
                        onDelete = onDeleteScript,
                        validationError = validationError
                    )
                }
            }
        }
    }
}
