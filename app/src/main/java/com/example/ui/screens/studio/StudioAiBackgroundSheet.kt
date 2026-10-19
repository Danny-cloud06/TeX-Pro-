package com.example.ui.screens.studio

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiBackgroundSettings
import com.example.model.BackgroundMode
import com.example.model.BlurIntensity
import com.example.model.SolidColorPresets
import com.example.model.VirtualStudioPreset
import com.example.ui.theme.CueBlack
import com.example.ui.theme.CueBorder
import com.example.ui.theme.CueCharcoal
import com.example.ui.theme.CueGreen
import com.example.ui.theme.CueSurface
import com.example.ui.theme.CueSurfaceElevated
import com.example.ui.theme.CueTextMuted
import com.example.ui.theme.CueTextPrimary
import com.example.ui.theme.CueTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioAiBackgroundSheet(
    settings: AiBackgroundSettings,
    onSettingsChange: (AiBackgroundSettings) -> Unit,
    onPickImageUri: (Uri) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    // Zero-permission Android Photo Picker for Custom Backdrop
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onPickImageUri(uri)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CueSurface,
        dragHandle = null,
        modifier = modifier.testTag("studio_ai_background_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CueGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CueGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI Background Studio",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CueTextPrimary
                        )
                        Text(
                            text = "On-device person segmentation & backdrops",
                            style = MaterialTheme.typography.labelSmall,
                            color = CueTextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Main AI Master Switch
                    Switch(
                        checked = settings.isAiEnabled,
                        onCheckedChange = { enabled ->
                            onSettingsChange(
                                settings.copy(
                                    isAiEnabled = enabled,
                                    mode = if (enabled && settings.mode == BackgroundMode.ORIGINAL) {
                                        BackgroundMode.GREEN_SCREEN
                                    } else {
                                        settings.mode
                                    }
                                )
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CueBlack,
                            checkedTrackColor = CueGreen,
                            uncheckedThumbColor = CueTextMuted,
                            uncheckedTrackColor = CueCharcoal
                        ),
                        modifier = Modifier.testTag("ai_master_switch")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = CueTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mode Selector Cards
            Text(
                text = "BACKGROUND MODES",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold),
                color = CueTextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val modes = listOf(
                    Triple(BackgroundMode.ORIGINAL, "Original", Icons.Default.Videocam),
                    Triple(BackgroundMode.GREEN_SCREEN, "Green Screen", Icons.Default.ColorLens),
                    Triple(BackgroundMode.BLUR, "Depth Blur", Icons.Default.BlurOn),
                    Triple(BackgroundMode.VIRTUAL_STUDIO, "Virtual Set", Icons.Default.ViewQuilt),
                    Triple(BackgroundMode.SOLID_COLOR, "Solid Color", Icons.Default.Palette),
                    Triple(BackgroundMode.CUSTOM_IMAGE, "Custom Photo", Icons.Default.AddPhotoAlternate)
                )

                modes.forEach { (mode, label, icon) ->
                    val isSelected = settings.isAiEnabled && settings.mode == mode ||
                            (!settings.isAiEnabled && mode == BackgroundMode.ORIGINAL)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) CueGreen.copy(alpha = 0.15f) else CueCharcoal)
                            .border(
                                1.5.dp,
                                if (isSelected) CueGreen else CueBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onSettingsChange(
                                    settings.copy(
                                        mode = mode,
                                        isAiEnabled = mode != BackgroundMode.ORIGINAL
                                    )
                                )
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("mode_${mode.name.lowercase()}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) CueGreen else CueTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) CueGreen else CueTextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Contextual Controls based on active mode
            AnimatedVisibility(
                visible = settings.isAiEnabled && settings.mode != BackgroundMode.ORIGINAL,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    when (settings.mode) {
                        BackgroundMode.BLUR -> {
                            Text(
                                text = "BLUR INTENSITY",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold),
                                color = CueTextMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BlurIntensity.values().forEach { intensity ->
                                    val isSelected = settings.blurIntensity == intensity
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onSettingsChange(settings.copy(blurIntensity = intensity)) },
                                        label = { Text(intensity.label) },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CueGreen.copy(alpha = 0.2f),
                                            selectedLabelColor = CueGreen,
                                            containerColor = CueCharcoal,
                                            labelColor = CueTextPrimary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = isSelected,
                                            borderColor = CueBorder,
                                            selectedBorderColor = CueGreen
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        BackgroundMode.GREEN_SCREEN -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CueCharcoal)
                                    .border(1.dp, CueBorder, RoundedCornerShape(12.dp))
                                    .padding(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF00FF00))
                                            .border(1.dp, Color.White, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Broadcast Standard Chroma Key (#00FF00)",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = CueTextPrimary
                                        )
                                        Text(
                                            text = "Optimized for 1-click keying in CapCut, Premiere, and DaVinci.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CueTextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        BackgroundMode.SOLID_COLOR -> {
                            Text(
                                text = "STUDIO COLOR PALETTE",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold),
                                color = CueTextMuted
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) {
                                items(SolidColorPresets.options) { option ->
                                    val isSelected = settings.solidColor == option.color
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable { onSettingsChange(settings.copy(solidColor = option.color)) }
                                            .padding(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(option.color)
                                                .border(
                                                    if (isSelected) 3.dp else 1.dp,
                                                    if (isSelected) CueGreen else CueBorder,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = if (option.color == Color(0xFFF8FAFC) || option.color == Color(0xFF00FF00)) CueBlack else Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = option.name,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = if (isSelected) CueGreen else CueTextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        BackgroundMode.VIRTUAL_STUDIO -> {
                            Text(
                                text = "SELECT VIRTUAL SET",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold),
                                color = CueTextMuted
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(VirtualStudioPreset.values()) { preset ->
                                    val isSelected = settings.virtualStudio == preset
                                    Box(
                                        modifier = Modifier
                                            .width(160.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(CueCharcoal)
                                            .border(
                                                1.5.dp,
                                                if (isSelected) CueGreen else CueBorder,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { onSettingsChange(settings.copy(virtualStudio = preset)) }
                                            .padding(12.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .clip(CircleShape)
                                                        .background(preset.accentColor)
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = CueGreen,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = preset.title,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = CueTextPrimary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = preset.subtitle,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = CueTextSecondary,
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        BackgroundMode.CUSTOM_IMAGE -> {
                            Column {
                                Text(
                                    text = "DEVICE PHOTO BACKDROP",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold),
                                    color = CueTextMuted
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CueSurfaceElevated,
                                        contentColor = CueGreen
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, CueBorder, RoundedCornerShape(12.dp))
                                        .testTag("pick_photo_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (settings.customImageBitmap != null) "Change Selected Photo" else "Pick Photo From Gallery",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        else -> {}
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Edge Confidence Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EDGE SEGMENTATION SENSITIVITY",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold),
                            color = CueTextMuted
                        )
                        Text(
                            text = "${(settings.edgeConfidenceThreshold * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CueGreen
                        )
                    }

                    Slider(
                        value = settings.edgeConfidenceThreshold,
                        onValueChange = { onSettingsChange(settings.copy(edgeConfidenceThreshold = it)) },
                        valueRange = 0.25f..0.85f,
                        colors = SliderDefaults.colors(
                            thumbColor = CueGreen,
                            activeTrackColor = CueGreen,
                            inactiveTrackColor = CueCharcoal
                        ),
                        modifier = Modifier.testTag("edge_confidence_slider")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Reset Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        onSettingsChange(
                            AiBackgroundSettings(
                                mode = BackgroundMode.ORIGINAL,
                                isAiEnabled = false
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CueCharcoal,
                        contentColor = CueTextSecondary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset to Normal Camera", style = MaterialTheme.typography.labelSmall)
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CueGreen,
                        contentColor = CueBlack
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Done", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
