package com.example.ui.screens.teleprompter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PrompterSettings
import com.example.ui.theme.CueBlack
import com.example.ui.theme.CueBorder
import com.example.ui.theme.CueCharcoal
import com.example.ui.theme.CueGreen
import com.example.ui.theme.CueGreenContainer
import com.example.ui.theme.CueSurface
import com.example.ui.theme.CueSurfaceElevated
import com.example.ui.theme.CueTextMuted
import com.example.ui.theme.CueTextPrimary
import com.example.ui.theme.CueTextSecondary

@Composable
fun PrompterControlsHud(
    isPlaying: Boolean,
    settings: PrompterSettings,
    progressPercent: Float,
    totalWords: Int,
    currentWordOffset: Int,
    onPlayPauseToggle: () -> Unit,
    onResetToBeginning: () -> Unit,
    onRestartPlayback: () -> Unit,
    onSpeedChange: (Int) -> Unit,
    onSettingsChange: (PrompterSettings) -> Unit,
    onRestoreDefaults: () -> Unit,
    onToggleDistractionFree: () -> Unit,
    onChangeScript: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSettingsPanelOpen by remember { mutableStateOf(false) }

    // Calculate estimated remaining reading time
    val remainingWords = (totalWords - currentWordOffset).coerceAtLeast(0)
    val remainingSeconds = if (settings.scrollSpeedWpm > 0) {
        (remainingWords * 60 / settings.scrollSpeedWpm)
    } else 0
    val remMin = remainingSeconds / 60
    val remSec = remainingSeconds % 60

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(CueCharcoal)
            .border(1.dp, CueBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        // Linear Progress Bar across top of controls
        LinearProgressIndicator(
            progress = { progressPercent },
            color = CueGreen,
            trackColor = CueBorder,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        )

        // Reading Metrics Header (Progress % & Estimated Remaining Time)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${(progressPercent * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = CueGreen
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• $currentWordOffset / $totalWords words",
                    style = MaterialTheme.typography.labelSmall,
                    color = CueTextSecondary
                )
            }

            Text(
                text = "~${remMin}m ${remSec}s remaining (est.)",
                style = MaterialTheme.typography.labelSmall,
                color = CueTextMuted
            )
        }

        // Primary Playback Controls Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Change Script button
            IconButton(
                onClick = onChangeScript,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("prompter_change_script_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Select another script",
                    tint = CueTextSecondary
                )
            }

            // Reset (to top and stop)
            IconButton(
                onClick = onResetToBeginning,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("prompter_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = "Reset to beginning",
                    tint = CueTextSecondary
                )
            }

            // Central PLAY / PAUSE Button (Large, accessible)
            Button(
                onClick = onPlayPauseToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CueGreen,
                    contentColor = CueBlack
                ),
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .testTag("prompter_play_pause_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(30.dp)
                )
            }

            // Restart (resets and starts)
            IconButton(
                onClick = onRestartPlayback,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("prompter_restart_button")
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Restart playback",
                    tint = CueTextSecondary
                )
            }

            // Customization Toggle
            IconButton(
                onClick = { isSettingsPanelOpen = !isSettingsPanelOpen },
                modifier = Modifier
                    .size(44.dp)
                    .testTag("prompter_settings_toggle_button")
            ) {
                Icon(
                    imageVector = if (isSettingsPanelOpen) Icons.Default.KeyboardArrowDown else Icons.Default.Tune,
                    contentDescription = "Reading settings",
                    tint = if (isSettingsPanelOpen) CueGreen else CueTextSecondary
                )
            }
        }

        // Live Speed Slider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = CueGreen,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${settings.scrollSpeedWpm} WPM",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = CueTextPrimary,
                modifier = Modifier.width(68.dp)
            )
            Slider(
                value = settings.scrollSpeedWpm.toFloat(),
                onValueChange = { onSpeedChange(it.toInt()) },
                valueRange = 60f..300f,
                steps = 23,
                colors = SliderDefaults.colors(
                    thumbColor = CueGreen,
                    activeTrackColor = CueGreen,
                    inactiveTrackColor = CueSurfaceElevated
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("prompter_speed_slider")
            )
        }

        // Collapsible Advanced Customization Drawer
        AnimatedVisibility(
            visible = isSettingsPanelOpen,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CueSurface)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "TELEPROMPTER CUSTOMIZATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = CueGreen
                )

                // 1. Text Size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = null,
                            tint = CueTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Text Size: ${settings.fontSizeSp} sp",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CueTextPrimary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                onSettingsChange(settings.copy(fontSizeSp = (settings.fontSizeSp - 2).coerceAtLeast(18)))
                            },
                            modifier = Modifier.size(36.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text("-", color = CueTextPrimary)
                        }
                        OutlinedButton(
                            onClick = {
                                onSettingsChange(settings.copy(fontSizeSp = (settings.fontSizeSp + 2).coerceAtMost(56)))
                            },
                            modifier = Modifier.size(36.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text("+", color = CueTextPrimary)
                        }
                        TextButton(
                            onClick = { onSettingsChange(settings.copy(fontSizeSp = 28)) }
                        ) {
                            Text("Reset", color = CueGreen, fontSize = 12.sp)
                        }
                    }
                }

                // 2. Line Spacing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatLineSpacing,
                            contentDescription = null,
                            tint = CueTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Line Height: ${String.format("%.1f", settings.lineHeightMultiplier)}x",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CueTextPrimary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                onSettingsChange(settings.copy(lineHeightMultiplier = (settings.lineHeightMultiplier - 0.1f).coerceAtLeast(1.1f)))
                            },
                            modifier = Modifier.size(36.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text("-", color = CueTextPrimary)
                        }
                        OutlinedButton(
                            onClick = {
                                onSettingsChange(settings.copy(lineHeightMultiplier = (settings.lineHeightMultiplier + 0.1f).coerceAtMost(2.4f)))
                            },
                            modifier = Modifier.size(36.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text("+", color = CueTextPrimary)
                        }
                        TextButton(
                            onClick = { onSettingsChange(settings.copy(lineHeightMultiplier = 1.5f)) }
                        ) {
                            Text("Reset", color = CueGreen, fontSize = 12.sp)
                        }
                    }
                }

                // 3. Toggles: Bold & Mirror Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onSettingsChange(settings.copy(isBold = !settings.isBold)) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (settings.isBold) CueGreenContainer else CueCharcoal
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (settings.isBold) CueGreen else CueBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("prompter_toggle_bold_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatBold,
                            contentDescription = null,
                            tint = if (settings.isBold) CueGreen else CueTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (settings.isBold) "Bold: ON" else "Bold: OFF",
                            color = if (settings.isBold) CueGreen else CueTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { onSettingsChange(settings.copy(isMirrored = !settings.isMirrored)) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (settings.isMirrored) CueGreenContainer else CueCharcoal
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (settings.isMirrored) CueGreen else CueBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("prompter_toggle_mirror_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flip,
                            contentDescription = null,
                            tint = if (settings.isMirrored) CueGreen else CueTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (settings.isMirrored) "Mirror: ON" else "Mirror: OFF",
                            color = if (settings.isMirrored) CueGreen else CueTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // 4. Reading Guide Position Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reading Line Position: ${(settings.guidePositionPercent * 100).toInt()}% from top",
                            style = MaterialTheme.typography.bodySmall,
                            color = CueTextPrimary
                        )
                        TextButton(
                            onClick = { onSettingsChange(settings.copy(guidePositionPercent = 0.35f)) }
                        ) {
                            Text("Reset", color = CueGreen, fontSize = 11.sp)
                        }
                    }
                    Slider(
                        value = settings.guidePositionPercent,
                        onValueChange = { onSettingsChange(settings.copy(guidePositionPercent = it)) },
                        valueRange = 0.20f..0.70f,
                        colors = SliderDefaults.colors(
                            thumbColor = CueGreen,
                            activeTrackColor = CueGreen,
                            inactiveTrackColor = CueBorder
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("prompter_guide_slider")
                    )
                }

                // 5. Countdown Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = CueTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Countdown",
                            style = MaterialTheme.typography.bodySmall,
                            color = CueTextPrimary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(0 to "Off", 3 to "3s", 5 to "5s", 10 to "10s").forEach { (sec, label) ->
                            val isSelected = settings.countdownSeconds == sec
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) CueGreen else CueCharcoal)
                                    .border(1.dp, if (isSelected) CueGreen else CueBorder, RoundedCornerShape(6.dp))
                                    .clickable { onSettingsChange(settings.copy(countdownSeconds = sec)) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isSelected) CueBlack else CueTextSecondary
                                )
                            }
                        }
                    }
                }

                // 6. Distraction-Free Toggle & Restore Defaults
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onToggleDistractionFree,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CueBorder),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(
                            imageVector = if (settings.isDistractionFree) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = null,
                            tint = CueGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (settings.isDistractionFree) "Show Bars" else "Fullscreen HUD",
                            color = CueTextPrimary,
                            fontSize = 11.sp
                        )
                    }

                    TextButton(
                        onClick = onRestoreDefaults,
                        modifier = Modifier.testTag("prompter_restore_defaults_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = CueTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restore Defaults", color = CueTextMuted, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
