package com.example.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NavSection
import com.example.model.PrompterSettings
import com.example.model.Script
import com.example.ui.screens.ScriptsScreen
import com.example.ui.screens.ScriptsViewMode
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.screens.TeleprompterScreen
import com.example.ui.theme.CueBlack
import com.example.ui.theme.CueBorder
import com.example.ui.theme.CueCharcoal
import com.example.ui.theme.CueGreen
import com.example.ui.theme.CueTextMuted
import com.example.ui.theme.CueTextPrimary
import com.example.ui.theme.CueTextSecondary

/**
 * Custom dark cinematic navigation bar for CueGreen Teleprompter.
 * Features high-contrast emerald cues, charcoal surfaces, and accessible touch targets.
 */
@Composable
fun CueGreenNavigationBar(
    currentSection: NavSection,
    onSectionSelected: (NavSection) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = CueCharcoal,
        contentColor = CueTextPrimary,
        tonalElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = CueBorder,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        NavSection.entries.forEach { section ->
            val isSelected = currentSection == section
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSectionSelected(section) },
                icon = {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isSelected) section.selectedIcon else section.unselectedIcon,
                            contentDescription = section.title,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(CueGreen)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                        }
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CueBlack,
                    selectedTextColor = CueGreen,
                    indicatorColor = CueGreen,
                    unselectedIconColor = CueTextSecondary,
                    unselectedTextColor = CueTextMuted
                ),
                modifier = Modifier.testTag(section.testTag)
            )
        }
    }
}

/**
 * State-based navigation host that renders screens conditionally based on [currentSection] state.
 * Manages smooth crossfade transitions while strictly preserving the dark cinematic atmosphere.
 */
@Composable
fun CueGreenStateNavHost(
    currentSection: NavSection,
    scripts: List<Script>,
    searchQuery: String,
    scriptsViewMode: ScriptsViewMode,
    editingScript: Script?,
    titleDraft: String,
    contentDraft: String,
    scriptValidationError: String?,
    selectedPrompterScriptId: String?,
    prompterSettings: PrompterSettings,
    onSearchQueryChange: (String) -> Unit,
    onStartNewScript: () -> Unit,
    onOpenEditScript: (Script) -> Unit,
    onTitleDraftChange: (String) -> Unit,
    onContentDraftChange: (String) -> Unit,
    onSaveScript: (title: String, content: String) -> Unit,
    onCancelEditor: () -> Unit,
    onDeleteScript: (String) -> Unit,
    onSelectPrompterScript: (Script) -> Unit,
    onDeselectPrompterScript: () -> Unit,
    onPrompterSettingsChange: (PrompterSettings) -> Unit,
    onRestorePrompterDefaults: () -> Unit,
    onCreateScriptFromPrompter: () -> Unit,
    onActionNotice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CueBlack)
    ) {
        AnimatedContent(
            targetState = currentSection,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            label = "StateBasedNavigation"
        ) { section ->
            when (section) {
                NavSection.SCRIPTS -> {
                    ScriptsScreen(
                        scripts = scripts,
                        searchQuery = searchQuery,
                        viewMode = scriptsViewMode,
                        editingScript = editingScript,
                        titleDraft = titleDraft,
                        contentDraft = contentDraft,
                        validationError = scriptValidationError,
                        onSearchQueryChange = onSearchQueryChange,
                        onStartNewScript = onStartNewScript,
                        onOpenEditScript = onOpenEditScript,
                        onTitleDraftChange = onTitleDraftChange,
                        onContentDraftChange = onContentDraftChange,
                        onSaveScript = onSaveScript,
                        onCancelEditor = onCancelEditor,
                        onDeleteScript = onDeleteScript
                    )
                }

                NavSection.TELEPROMPTER -> {
                    TeleprompterScreen(
                        scripts = scripts,
                        selectedScriptId = selectedPrompterScriptId,
                        settings = prompterSettings,
                        onSelectScript = onSelectPrompterScript,
                        onDeselectScript = onDeselectPrompterScript,
                        onSettingsChange = onPrompterSettingsChange,
                        onRestoreDefaults = onRestorePrompterDefaults,
                        onCreateScriptRequested = onCreateScriptFromPrompter
                    )
                }

                NavSection.STUDIO -> {
                    StudioScreen(
                        scripts = scripts,
                        prompterSettings = prompterSettings,
                        onActionNotice = onActionNotice
                    )
                }

                NavSection.SETTINGS -> {
                    SettingsScreen(
                        onActionNotice = onActionNotice
                    )
                }
            }
        }
    }
}
