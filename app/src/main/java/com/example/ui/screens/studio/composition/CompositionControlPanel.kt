package com.example.ui.screens.studio.composition

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.BackgroundMedia
import com.example.model.CompositionConfig
import com.example.model.CompositionSourceType
import com.example.model.GameplayPreset
import com.example.model.LayerBorderAccent
import com.example.model.PersonLayerShape
import com.example.model.PersonPresetPosition
import com.example.model.Script
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
fun CompositionControlPanel(
    config: CompositionConfig,
    scripts: List<Script>,
    selectedScript: Script?,
    isRecording: Boolean,
    isAudioEnabled: Boolean,
    onConfigChange: (CompositionConfig) -> Unit,
    onScriptSelected: (Script) -> Unit,
    onPickCustomVideoUri: (Uri) -> Unit,
    onPickCustomImageUri: (Uri) -> Unit,
    onToggleAudio: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onResetLayout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Background", "Presenter Layer", "Teleprompter")

    // Video Picker Launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPickCustomVideoUri(uri)
        }
    }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPickCustomImageUri(uri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(CueCharcoal)
            .border(1.dp, CueBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(14.dp)
            .testTag("composition_control_panel")
    ) {
        // Tab Header
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = CueGreen,
            edgePadding = 0.dp,
            divider = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            tabTitles.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            color = if (isSelected) CueGreen else CueTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Content
        when (selectedTabIndex) {
            0 -> {
                // Background & Media Tab
                BackgroundMediaTab(
                    config = config,
                    onConfigChange = onConfigChange,
                    onLaunchVideoPicker = {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    onLaunchImagePicker = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }
            1 -> {
                // Presenter Layer Tab
                PresenterLayerTab(
                    config = config,
                    onConfigChange = onConfigChange,
                    onResetLayout = onResetLayout
                )
            }
            2 -> {
                // Teleprompter Tab
                TeleprompterTab(
                    config = config,
                    scripts = scripts,
                    selectedScript = selectedScript,
                    onConfigChange = onConfigChange,
                    onScriptSelected = onScriptSelected
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Primary Action Bar: Record Commentary / Reset Layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Audio toggle button
            IconButton(
                onClick = onToggleAudio,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isAudioEnabled) CueGreenContainer else CueSurface)
                    .border(1.dp, if (isAudioEnabled) CueGreen else CueBorder, CircleShape)
            ) {
                Icon(
                    imageVector = if (isAudioEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = "Toggle commentary mic",
                    tint = if (isAudioEnabled) CueGreen else CueRed,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Main Record Commentary Session Button
            Button(
                onClick = {
                    if (isRecording) onStopRecording() else onStartRecording()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) CueRed else CueGreen,
                    contentColor = if (isRecording) Color.White else CueBlack
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("composition_record_button")
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Videocam,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRecording) "STOP SESSION RECORDING" else "START COMMENTARY REC",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            // Reset layout quick button
            IconButton(
                onClick = onResetLayout,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(CueSurface)
                    .border(1.dp, CueBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Layout",
                    tint = CueTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun BackgroundMediaTab(
    config: CompositionConfig,
    onConfigChange: (CompositionConfig) -> Unit,
    onLaunchVideoPicker: () -> Unit,
    onLaunchImagePicker: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Gameplay Presets & Imported Media",
            style = MaterialTheme.typography.labelMedium,
            color = CueTextSecondary
        )

        // Gameplay preset cards
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(GameplayPreset.entries) { preset ->
                val isSelected = config.backgroundMedia.sourceType == CompositionSourceType.GAMEPLAY_PRESET &&
                        config.backgroundMedia.gameplayPreset == preset

                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) CueSurfaceElevated else CueSurface)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) preset.themeColor else CueBorder,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            onConfigChange(
                                config.copy(
                                    backgroundMedia = BackgroundMedia(
                                        sourceType = CompositionSourceType.GAMEPLAY_PRESET,
                                        gameplayPreset = preset,
                                        fileName = "${preset.name.lowercase()}.mp4",
                                        durationSeconds = 180
                                    )
                                )
                            )
                        }
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Gamepad,
                                contentDescription = null,
                                tint = preset.themeColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = preset.title.take(14),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = CueTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = preset.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = CueTextMuted
                        )
                    }
                }
            }
        }

        // Import Local Video or Image buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onLaunchVideoPicker,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.VideoFile, contentDescription = null, tint = CueGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Import Video", color = CueTextPrimary, fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onLaunchImagePicker,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = CueGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Import Image", color = CueTextPrimary, fontSize = 12.sp)
            }
        }

        // Active background status badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CueSurface)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Active: ${config.backgroundMedia.fileName}",
                style = MaterialTheme.typography.bodySmall,
                color = CueTextPrimary
            )
            Text(
                text = config.backgroundMedia.resolutionLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = CueGreen
            )
        }
    }
}

@Composable
private fun PresenterLayerTab(
    config: CompositionConfig,
    onConfigChange: (CompositionConfig) -> Unit,
    onResetLayout: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Quick Position Presets
        Text("Position Presets", style = MaterialTheme.typography.labelMedium, color = CueTextSecondary)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(PersonPresetPosition.entries) { pos ->
                val isSelected = config.personNormalizedX == pos.targetOffset.x && config.personNormalizedY == pos.targetOffset.y
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) CueGreenContainer else CueSurface)
                        .border(1.dp, if (isSelected) CueGreen else CueBorder, RoundedCornerShape(16.dp))
                        .clickable {
                            onConfigChange(
                                config.copy(
                                    personNormalizedX = pos.targetOffset.x,
                                    personNormalizedY = pos.targetOffset.y
                                )
                            )
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = pos.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) CueGreen else CueTextPrimary
                    )
                }
            }
        }

        // Size / Scale Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Layer Scale: ${(config.personScale * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = CueTextSecondary)
            Text("Drag on preview to move", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = CueGreen)
        }
        Slider(
            value = config.personScale,
            onValueChange = { onConfigChange(config.copy(personScale = it)) },
            valueRange = 0.20f..0.75f,
            colors = SliderDefaults.colors(thumbColor = CueGreen, activeTrackColor = CueGreen)
        )

        // Shape Switcher: AI Cutout, PiP Box, Circle Avatar
        Text("Frame Shape", style = MaterialTheme.typography.labelMedium, color = CueTextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            PersonLayerShape.entries.forEach { shape ->
                val isSelected = config.personShape == shape
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CueGreenContainer else CueSurface)
                        .border(1.dp, if (isSelected) CueGreen else CueBorder, RoundedCornerShape(8.dp))
                        .clickable { onConfigChange(config.copy(personShape = shape)) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = shape.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) CueGreen else CueTextPrimary
                    )
                }
            }
        }

        // Border Accent & Mirroring
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Accent:", style = MaterialTheme.typography.labelSmall, color = CueTextSecondary)
                LayerBorderAccent.entries.forEach { accent ->
                    val isSelected = config.borderAccent == accent
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (accent.color == Color.Transparent) Color.DarkGray else accent.color)
                            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) Color.White else CueBorder, CircleShape)
                            .clickable { onConfigChange(config.copy(borderAccent = accent)) }
                    )
                }
            }

            // Mirror toggle
            IconButton(
                onClick = { onConfigChange(config.copy(isPersonMirrored = !config.isPersonMirrored)) },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Flip,
                    contentDescription = "Mirror Camera",
                    tint = if (config.isPersonMirrored) CueGreen else CueTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Visibility toggle
            IconButton(
                onClick = { onConfigChange(config.copy(isPersonVisible = !config.isPersonVisible)) },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = if (config.isPersonVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Toggle Visibility",
                    tint = if (config.isPersonVisible) CueGreen else CueTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun TeleprompterTab(
    config: CompositionConfig,
    scripts: List<Script>,
    selectedScript: Script?,
    onConfigChange: (CompositionConfig) -> Unit,
    onScriptSelected: (Script) -> Unit
) {
    var isDropdownOpen by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Teleprompter Script Overlay", style = MaterialTheme.typography.labelMedium, color = CueTextSecondary)

        // Script Selector Dropdown
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CueSurface)
                    .border(1.dp, CueBorder, RoundedCornerShape(10.dp))
                    .clickable { isDropdownOpen = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = CueGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedScript?.title ?: "Select a Script",
                        color = CueTextPrimary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Text("Change", color = CueGreen, style = MaterialTheme.typography.labelSmall)
            }

            DropdownMenu(
                expanded = isDropdownOpen,
                onDismissRequest = { isDropdownOpen = false },
                modifier = Modifier.background(CueSurface)
            ) {
                scripts.forEach { script ->
                    DropdownMenuItem(
                        text = { Text(script.title, color = CueTextPrimary) },
                        onClick = {
                            onScriptSelected(script)
                            isDropdownOpen = false
                        }
                    )
                }
            }
        }

        // Prompter Opacity Slider
        Text("Overlay Opacity: ${(config.prompterOpacity * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = CueTextSecondary)
        Slider(
            value = config.prompterOpacity,
            onValueChange = { onConfigChange(config.copy(prompterOpacity = it)) },
            valueRange = 0.35f..0.95f,
            colors = SliderDefaults.colors(thumbColor = CueGreen, activeTrackColor = CueGreen)
        )

        // Prompter Scroll Speed Slider
        Text("Scroll Speed: ${config.prompterSpeedWpm} WPM", style = MaterialTheme.typography.labelSmall, color = CueTextSecondary)
        Slider(
            value = config.prompterSpeedWpm.toFloat(),
            onValueChange = { onConfigChange(config.copy(prompterSpeedWpm = it.toInt())) },
            valueRange = 60f..300f,
            colors = SliderDefaults.colors(thumbColor = CueGreen, activeTrackColor = CueGreen)
        )

        // Show/Hide Prompter Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Show Prompter on Canvas", style = MaterialTheme.typography.bodyMedium, color = CueTextPrimary)
            OutlinedButton(
                onClick = { onConfigChange(config.copy(isPrompterVisible = !config.isPrompterVisible)) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (config.isPrompterVisible) "Visible" else "Hidden", color = if (config.isPrompterVisible) CueGreen else CueTextSecondary)
            }
        }
    }
}
