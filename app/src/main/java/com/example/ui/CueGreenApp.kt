package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.NavSection
import com.example.ui.navigation.CueGreenNavigationBar
import com.example.ui.navigation.CueGreenStateNavHost
import com.example.ui.screens.ScriptsViewMode
import com.example.ui.theme.CueBlack
import com.example.ui.theme.CueGreen
import com.example.ui.theme.CueGreenContainer
import com.example.ui.theme.CueTextPrimary
import com.example.ui.theme.CueTextSecondary
import com.example.viewmodel.CueGreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CueGreenApp(
    viewModel: CueGreenViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredScripts by viewModel.filteredScripts.collectAsState()
    val allScripts by viewModel.allScripts.collectAsState()
    val prompterSettings by viewModel.prompterSettings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val isEditorActive = uiState.currentSection == NavSection.SCRIPTS && uiState.scriptsViewMode == ScriptsViewMode.EDITOR
    val isPrompterReadingActive = uiState.currentSection == NavSection.TELEPROMPTER && uiState.selectedPrompterScriptId != null
    val isDistractionFreeActive = isPrompterReadingActive && prompterSettings.isDistractionFree

    // Intercept back button when not on root
    BackHandler(enabled = !isEditorActive && uiState.canNavigateBack) {
        viewModel.navigateBack()
    }

    // Display feedback notices
    LaunchedEffect(uiState.infoNotice) {
        val notice = uiState.infoNotice
        if (notice != null) {
            snackbarHostState.showSnackbar(notice)
            viewModel.clearNotice()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (!isDistractionFreeActive) {
                TopAppBar(
                    navigationIcon = {
                        if (uiState.canNavigateBack && !isEditorActive) {
                            IconButton(
                                onClick = { viewModel.navigateBack() },
                                modifier = Modifier.testTag("nav_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Navigate Back",
                                    tint = CueGreen
                                )
                            }
                        }
                    },
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(CueGreen)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "TeX",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        letterSpacing = 1.sp
                                    ),
                                    color = CueTextPrimary
                                )
                            }
                            Text(
                                text = when {
                                    isEditorActive -> "Script Editor • ${if (uiState.editingScript != null) "Editing" else "New Script"}"
                                    isPrompterReadingActive -> "Teleprompter Viewport"
                                    uiState.currentSection == NavSection.STUDIO -> "Camera Teleprompter Studio"
                                    else -> uiState.currentSection.title
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = CueTextSecondary
                            )
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CueGreenContainer)
                                .border(1.dp, CueGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "PRO",
                                style = MaterialTheme.typography.labelSmall,
                                color = CueGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CueBlack,
                        titleContentColor = CueTextPrimary
                    )
                )
            }
        },
        bottomBar = {
            if (!isEditorActive && !isPrompterReadingActive && !isDistractionFreeActive) {
                CueGreenNavigationBar(
                    currentSection = uiState.currentSection,
                    onSectionSelected = { section -> viewModel.selectSection(section) },
                    modifier = Modifier.testTag("cuegreen_navigation_bar")
                )
            }
        },
        containerColor = CueBlack
    ) { innerPadding ->
        CueGreenStateNavHost(
            currentSection = uiState.currentSection,
            scripts = if (uiState.currentSection == NavSection.TELEPROMPTER || uiState.currentSection == NavSection.STUDIO) allScripts else filteredScripts,
            searchQuery = uiState.searchQuery,
            scriptsViewMode = uiState.scriptsViewMode,
            editingScript = uiState.editingScript,
            titleDraft = uiState.draftTitle,
            contentDraft = uiState.draftContent,
            scriptValidationError = uiState.scriptValidationError,
            selectedPrompterScriptId = uiState.selectedPrompterScriptId,
            prompterSettings = prompterSettings,
            onSearchQueryChange = { viewModel.setSearchQuery(it) },
            onStartNewScript = { viewModel.startNewScript() },
            onOpenEditScript = { viewModel.openEditScript(it) },
            onTitleDraftChange = { viewModel.updateDraftTitle(it) },
            onContentDraftChange = { viewModel.updateDraftContent(it) },
            onSaveScript = { title, content -> viewModel.saveCurrentScript(title, content) },
            onCancelEditor = { viewModel.cancelEditor() },
            onDeleteScript = { viewModel.deleteScript(it) },
            onSelectPrompterScript = { viewModel.selectPrompterScript(it) },
            onDeselectPrompterScript = { viewModel.deselectPrompterScript() },
            onPrompterSettingsChange = { viewModel.updatePrompterSettings(it) },
            onRestorePrompterDefaults = { viewModel.restorePrompterDefaults() },
            onCreateScriptFromPrompter = { viewModel.requestCreateScriptFromPrompter() },
            onActionNotice = { message -> viewModel.showNotice(message) },
            modifier = Modifier.padding(innerPadding)
        )
    }
}
