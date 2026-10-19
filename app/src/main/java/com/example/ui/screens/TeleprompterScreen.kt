package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.PrompterSettings
import com.example.model.Script
import com.example.ui.screens.teleprompter.PrompterControlsHud
import com.example.ui.screens.teleprompter.PrompterReadingViewport
import com.example.ui.screens.teleprompter.PrompterScriptSelector
import com.example.ui.theme.CueBlack
import com.example.ui.theme.CueBorder
import com.example.ui.theme.CueCharcoal
import com.example.ui.theme.CueGreen
import kotlinx.coroutines.launch

@Composable
fun TeleprompterScreen(
    scripts: List<Script>,
    selectedScriptId: String?,
    settings: PrompterSettings,
    onSelectScript: (Script) -> Unit,
    onDeselectScript: () -> Unit,
    onSettingsChange: (PrompterSettings) -> Unit,
    onRestoreDefaults: () -> Unit,
    onCreateScriptRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Locate currently selected script from the live list to ensure latest edits are shown
    val activeScript = remember(scripts, selectedScriptId) {
        if (selectedScriptId != null) {
            scripts.find { it.id == selectedScriptId }
        } else null
    }

    var isPlaying by remember { mutableStateOf(false) }
    var progressPercent by remember { mutableFloatStateOf(0f) }
    var currentWordOffset by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Back handling: if viewing a script in teleprompter, back returns to script selector
    BackHandler(enabled = activeScript != null) {
        isPlaying = false
        onDeselectScript()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CueBlack)
    ) {
        if (activeScript == null) {
            // Feature 1: Script Selection Screen
            PrompterScriptSelector(
                scripts = scripts,
                onSelectScript = { script ->
                    isPlaying = false
                    coroutineScope.launch { scrollState.scrollTo(0) }
                    onSelectScript(script)
                },
                onCreateScriptRequested = onCreateScriptRequested
            )
        } else {
            // Feature 2-11: Live Teleprompter Engine
            Box(modifier = Modifier.fillMaxSize()) {
                PrompterReadingViewport(
                    script = activeScript,
                    settings = settings,
                    isPlaying = isPlaying,
                    onPlayStateChange = { playing -> isPlaying = playing },
                    onProgressUpdate = { percent, wordOffset ->
                        progressPercent = percent
                        currentWordOffset = wordOffset
                    },
                    scrollState = scrollState,
                    modifier = Modifier.fillMaxSize()
                )

                // Bottom docked controls HUD
                PrompterControlsHud(
                    isPlaying = isPlaying,
                    settings = settings,
                    progressPercent = progressPercent,
                    totalWords = activeScript.wordCount,
                    currentWordOffset = currentWordOffset,
                    onPlayPauseToggle = {
                        isPlaying = !isPlaying
                    },
                    onResetToBeginning = {
                        isPlaying = false
                        coroutineScope.launch { scrollState.scrollTo(0) }
                    },
                    onRestartPlayback = {
                        isPlaying = false
                        coroutineScope.launch {
                            scrollState.scrollTo(0)
                            isPlaying = true
                        }
                    },
                    onSpeedChange = { newWpm ->
                        onSettingsChange(settings.copy(scrollSpeedWpm = newWpm))
                    },
                    onSettingsChange = onSettingsChange,
                    onRestoreDefaults = onRestoreDefaults,
                    onToggleDistractionFree = {
                        onSettingsChange(settings.copy(isDistractionFree = !settings.isDistractionFree))
                    },
                    onChangeScript = {
                        isPlaying = false
                        onDeselectScript()
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
